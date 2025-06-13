#!/usr/bin/env bash
set -e
clear

# ------------------------------------------------
# 0. 切換到腳本所在路徑 (確保能找到 pyproject.toml)
# ------------------------------------------------
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)"
cd "$SCRIPT_DIR"


#------------------------------------------------
# 1. 尋找並決定要使用的 Python 3.10+ 直譯器
#------------------------------------------------
echo "INFO: 開始尋找可用的 Python 3.10+ 直譯器..."
PYTHON_TO_USE="" # 將被設定為最終選定的 Python 路徑
PYTHON_VERSION_DISPLAY="" # 用於顯示最終選定 Python 的版本字串

# 優先1：檢查 .python-version 和 pyenv
if [ -f ".python-version" ] && command -v pyenv >/dev/null; then
    echo "INFO: 偵測到 .python-version 檔案且 pyenv 已安裝。"
    V_FROM_FILE=$(cat .python-version | tr -d '[:space:]')
    echo "INFO: .python-version 指定版本為: $V_FROM_FILE"

    if echo "$V_FROM_FILE" | grep -Eq "^3\.1[0-9]"; then # 驗證 .python-version 內容是否為 3.10+
        PYENV_PYTHON_PATH_CANDIDATE="$(pyenv root)/versions/$V_FROM_FILE/bin/python"
        if [ -x "$PYENV_PYTHON_PATH_CANDIDATE" ]; then
            # 驗證這個 pyenv 提供的 python 是否真的存在且版本匹配
            PYENV_PYTHON_VERSION_OUTPUT=$("$PYENV_PYTHON_PATH_CANDIDATE" -V 2>&1)
            if echo "$PYENV_PYTHON_VERSION_OUTPUT" | grep -q -E "^Python ${V_FROM_FILE//\./\\\.}(\s|$)"; then # 精確匹配版本號
                echo "INFO: 找到由 pyenv 管理且與 .python-version ($V_FROM_FILE) 匹配的 Python: $PYENV_PYTHON_PATH_CANDIDATE"
                PYTHON_TO_USE="$PYENV_PYTHON_PATH_CANDIDATE"
                PYTHON_VERSION_DISPLAY="$PYENV_PYTHON_VERSION_OUTPUT"
            else
                echo "警告: pyenv 提供的 Python ($PYENV_PYTHON_PATH_CANDIDATE) 版本 ($PYENV_PYTHON_VERSION_OUTPUT) 與 .python-version ($V_FROM_FILE) 聲稱的不完全一致。將嘗試後備方案。"
            fi
        else
            echo "警告: .python-version 指定的 Python 版本 ($V_FROM_FILE) 似乎未透過 pyenv 正確安裝或其執行檔不存在於預期路徑。"
            echo "請嘗試執行: pyenv install $V_FROM_FILE"
        fi
    else
        echo "錯誤: .python-version 檔案中的版本 ($V_FROM_FILE) 不符合專案最低要求的 Python 3.10+。請更新 .python-version。"
        exit 1
    fi
fi

# 優先2：如果 pyenv 未能提供合適的 Python，檢查系統預設的 python3
if [ -z "$PYTHON_TO_USE" ]; then
    echo "INFO: 未能透過 pyenv 和 .python-version 確定 Python 版本，或 .python-version 不存在。嘗試系統預設 'python3'..."
    SYSTEM_PYTHON3_PATH=$(command -v python3)
    if [ -n "$SYSTEM_PYTHON3_PATH" ] && [ -x "$SYSTEM_PYTHON3_PATH" ]; then
        SYSTEM_PYTHON3_VERSION_OUTPUT=$("$SYSTEM_PYTHON3_PATH" -V 2>&1)
        if echo "$SYSTEM_PYTHON3_VERSION_OUTPUT" | grep -Eq "3\.1[0-9]"; then
            echo "INFO: 找到系統預設 python3 ($SYSTEM_PYTHON3_PATH) 版本 ($SYSTEM_PYTHON3_VERSION_OUTPUT) 符合 >=3.10 要求。"
            PYTHON_TO_USE="$SYSTEM_PYTHON3_PATH"
            PYTHON_VERSION_DISPLAY="$SYSTEM_PYTHON3_VERSION_OUTPUT"
        else
            echo "INFO: 系統預設 python3 ($SYSTEM_PYTHON3_PATH) 版本 ($SYSTEM_PYTHON3_VERSION_OUTPUT) 不符合 >=3.10 要求。"
        fi
    else
        echo "INFO: 系統中找不到 'python3' 命令或無法執行。"
    fi
fi

# 最終決定與退出條件
if [ -z "$PYTHON_TO_USE" ]; then
    echo "--------------------------------------------------------------------------------"
    echo "錯誤：腳本未能找到一個有效的 Python 3.10+ 直譯器來執行本專案。"
    echo "請確保您已透過以下任一方式設定好 Python 3.10+ 環境："
    echo "  1. (推薦) 使用 pyenv：在專案根目錄下建立 .python-version 檔案 (內容例如 '3.10.13')，"
    echo "     並執行 'pyenv install \$(cat .python-version)' (如果尚未安裝) 以及 'pyenv local \$(cat .python-version)'。"
    echo "  2. Conda 環境：啟動一個已安裝 Python 3.10+ 的 Conda 環境 (例如 'conda activate my_env_with_py310')。"
    echo "  3. 全域/系統 Python：確保您 PATH 中的 'python3' 命令指向一個 Python 3.10+ 版本。"
    echo "完成上述任一設定後，請重新執行本腳本。"
    echo "--------------------------------------------------------------------------------"
    exit 1
fi

echo "================================================================"
echo "INFO: 最終選定使用的 Python 直譯器:"
echo "路徑: $PYTHON_TO_USE"
echo "版本: $PYTHON_VERSION_DISPLAY"
echo "================================================================"
# 將 PYTHON_PATH 設為最終選定的 Python 路徑，供後續腳本使用
export PYTHON_PATH="$PYTHON_TO_USE"


#------------------------------------------------
# 1.1. 檢查 pyenv 和 .python-version 設定 (此部分現在主要用於提供額外資訊和建議)
#------------------------------------------------
if [ -f ".python-version" ]; then
    PROJECT_PYTHON_VERSION_FROM_FILE=$(cat .python-version | tr -d '[:space:]')
    if ! echo "$PROJECT_PYTHON_VERSION_FROM_FILE" | grep -Eq "^3\.1[0-9]"; then
        echo "警告: .python-version 檔案中指定的 Python 版本 ($PROJECT_PYTHON_VERSION_FROM_FILE) 不符合專案要求的 >=3.10。但腳本已選定 $PYTHON_PATH ($PYTHON_VERSION_DISPLAY) 執行。"
    fi
    if command -v pyenv >/dev/null; then
        CURRENT_PYENV_VERSION=$(pyenv version-name | tr -d '[:space:]')
        if [ "$CURRENT_PYENV_VERSION" != "$PROJECT_PYTHON_VERSION_FROM_FILE" ]; then
            echo "警告：pyenv 當前啟用的版本 ($CURRENT_PYENV_VERSION) 與 .python-version ($PROJECT_PYTHON_VERSION_FROM_FILE) 不一致。"
            echo "      腳本目前使用的是 $PYTHON_PATH ($PYTHON_VERSION_DISPLAY)。"
            echo "      建議執行 'pyenv local $PROJECT_PYTHON_VERSION_FROM_FILE' 或檢查您的 pyenv 設定。"
        elif "$PYTHON_PATH" != "$(pyenv root)/versions/$PROJECT_PYTHON_VERSION_FROM_FILE/bin/python" && "$PYTHON_PATH" != "$(pyenv which python)" ; then
             echo "警告：雖然 pyenv 版本與 .python-version 一致，但腳本選用的 Python ($PYTHON_PATH) 並非此 pyenv 版本。"
             echo "       這可能表示您的 PATH 或 pyenv shell 設定有問題。腳本將繼續使用 $PYTHON_PATH。"
        else
             echo "INFO: pyenv 設定與 .python-version ($PROJECT_PYTHON_VERSION_FROM_FILE) 一致，且腳本將使用此 Python。"
        fi
    elif [ -f ".python-version" ]; then # .python-version 存在但 pyenv 未安裝
        echo "警告：偵測到 .python-version 檔案，但系統未安裝 pyenv。建議安裝 pyenv 以獲得最佳體驗。"
    fi
elif command -v pyenv >/dev/null; then # pyenv 已安裝但沒有 .python-version 檔案
    echo "INFO: pyenv 已安裝，但專案中未找到 .python-version 檔案。建議建立此檔案以鎖定專案 Python 版本。"
fi


#------------------------------------------------
# 2. 安裝 Poetry（如尚未安裝）
#------------------------------------------------
if ! command -v poetry >/dev/null; then
    echo "INFO: Poetry 未安裝，正在嘗試安裝..."
    if command -v pipx >/dev/null; then
        echo "INFO: 使用 pipx 安裝 Poetry"
        pipx install poetry
    else
        echo "INFO: pipx 不存在，使用官方安裝腳本安裝 Poetry"
        curl -sSL https://install.python-poetry.org | "$PYTHON_PATH" -
        if [[ ":$PATH:" != *":$HOME/.local/bin:"* ]]; then
             export PATH="$HOME/.local/bin:$PATH" # poetry 預設安裝路徑
        fi
        if ! command -v poetry >/dev/null; then
            echo "錯誤: Poetry 安裝後仍然找不到。請確保 Poetry 的安裝路徑 (通常是 \$HOME/.local/bin) 在您的 PATH 中，並重新執行腳本。"
            exit 1
        fi
    fi
    echo "INFO: Poetry 安裝完成。"
fi
echo "INFO: Poetry 已安裝。"

#------------------------------------------------
# 2.1. 強制 Poetry 使用選定的 Python 直譯器並驗證
#------------------------------------------------
echo "INFO: 正在設定 Poetry 使用選定的 Python: $PYTHON_PATH"

if poetry env use "$PYTHON_PATH"; then
    echo "INFO: 'poetry env use $PYTHON_PATH' 命令執行成功。"
else
    echo "警告: 'poetry env use $PYTHON_PATH' 命令執行完畢，可能帶有警告或非零退出碼。將繼續驗證環境。"
fi

echo "INFO: 驗證 Poetry 當前啟動的虛擬環境 Python 版本..."
ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE=""
ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE=$(poetry run python -c "import sys; print(sys.executable)" 2>/dev/null || echo "failed_to_get_poetry_env_python")

if [[ -n "$ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE" && "$ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE" != "failed_to_get_poetry_env_python" && -x "$ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE" ]]; then
    POETRY_ENV_PYTHON_VERSION_OUTPUT=$("$ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE" -V 2>&1)
    if echo "$POETRY_ENV_PYTHON_VERSION_OUTPUT" | grep -Eq "3\.1[0-9]"; then 
        echo "INFO: Poetry 虛擬環境中的 Python ($POETRY_ENV_PYTHON_VERSION_OUTPUT @ $ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE) 版本符合 >=3.10 要求。"
    else
        echo "錯誤: Poetry 設定後，其虛擬環境中的 Python 版本 ($POETRY_ENV_PYTHON_VERSION_OUTPUT @ $ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE) 仍不符合 >=3.10 要求。"
        echo "      使用的 PYTHON_PATH 是 '$PYTHON_PATH' ($($PYTHON_PATH -V 2>&1))。"
        echo "      這可能表示 'poetry env use' 未能正確建立或啟動預期的環境，或者 Poetry 內部選用了其他 Python。"
        echo "      請嘗試手動移除可能存在的舊 Poetry 虛擬環境 (例如 'poetry env remove macrolab-autoflow-XYZ-pyX.X') 然後重新執行本腳本。"
        exit 1
    fi
else
    echo "錯誤: 無法獲取或執行 Poetry 虛擬環境中的 Python 直譯器路徑。"
    echo "      嘗試執行的路徑是: '$ACTIVE_POETRY_ENV_PYTHON_EXECUTABLE'"
    echo "      請檢查 Poetry 環境是否已成功建立。您可以嘗試執行 'poetry shell' 看看是否能進入虛擬環境。"
    exit 1
fi


#------------------------------------------------
# 3. 若無完整的 Poetry 設定，建立或修正 pyproject.toml 及 lock
#------------------------------------------------
FINAL_PYTHON_MAJOR_MINOR=$("$PYTHON_PATH" -V 2>&1 | sed -n 's/Python \([0-9]*\.[0-9]*\).*/\1/p')
if [ -n "$FINAL_PYTHON_MAJOR_MINOR" ]; then
    PROJECT_POETRY_PYTHON_CONSTRAINT="^$FINAL_PYTHON_MAJOR_MINOR"
    echo "INFO: 根據選定的 Python ($PYTHON_PATH)，設定 Poetry 的 Python 約束為 $PROJECT_POETRY_PYTHON_CONSTRAINT"
else
    echo "警告: 無法從選定的 Python ($PYTHON_PATH) 中解析主次版本號。將使用預設約束 ^3.10。"
    PROJECT_POETRY_PYTHON_CONSTRAINT="^3.10"
fi


if [ ! -f "pyproject.toml" ] || ! grep -q -E '^\s*\[tool\.poetry\]' pyproject.toml; then
    echo "INFO: pyproject.toml 不存在或缺少 [tool.poetry] 區塊。正在初始化..."
    poetry init --no-interaction \
      --name "macrolab_autoflow" \
      --python "$PROJECT_POETRY_PYTHON_CONSTRAINT" \
      --version "0.1.0" \
      --description "MacroLab AutoFlow Podcast 工具" \
      --author "jproacademic@gmail.com" \
      --dependency "feedparser" \
      --dependency "newspaper3k" \
      --dependency "beautifulsoup4" \
      --dependency "requests" \
      --dependency "python-dotenv" \
      --dependency "notion-client"
    echo "INFO: Poetry 專案初始化完成。"
else
    echo "INFO: pyproject.toml 已存在並包含 [tool.poetry] 區塊。"
    EXPECTED_PYPROJECT_PYTHON_LINE_REGEX="python\\s*=\\s*\"${PROJECT_POETRY_PYTHON_CONSTRAINT//^/\\\\^}\""

    if ! grep -q -E "$EXPECTED_PYPROJECT_PYTHON_LINE_REGEX" pyproject.toml; then
        echo "INFO: pyproject.toml 中的 Python 約束與期望 ($PROJECT_POETRY_PYTHON_CONSTRAINT) 不符或未正確配置。"
        SED_SAFE_NEW_CONSTRAINT=$(echo "$PROJECT_POETRY_PYTHON_CONSTRAINT" | sed -e 's/[\/&]/\\&/g')
        if grep -q -E '^\s*python\s*=\s*".*"' pyproject.toml; then
            echo "INFO: 找到現有的 python 約束行，嘗試更新..."
            if sed -i.bak -E "s/^(\s*python\s*=\s*)\".*\"/\1\"$SED_SAFE_NEW_CONSTRAINT\"/" pyproject.toml; then
                if grep -q -E "$EXPECTED_PYPROJECT_PYTHON_LINE_REGEX" pyproject.toml; then
                    echo "INFO: pyproject.toml 中的 'python' 版本約束已成功更新為 '$PROJECT_POETRY_PYTHON_CONSTRAINT'."
                    rm -f pyproject.toml.bak 
                else
                    echo "警告: 更新 pyproject.toml 中的 'python' 版本約束後，驗證失敗。原檔案已備份為 pyproject.toml.bak"
                    echo "      請手動檢查 pyproject.toml 並修正 'python' 約束為 '$PROJECT_POETRY_PYTHON_CONSTRAINT'。"
                fi
            else
                echo "錯誤: 使用 sed 更新 pyproject.toml 失敗。原檔案已備份為 pyproject.toml.bak"
                echo "      請手動更新 pyproject.toml 中的 'python' 版本約束為 '$PROJECT_POETRY_PYTHON_CONSTRAINT'。"
            fi
        else
            echo "警告: 在 pyproject.toml 中未找到可更新的 'python = \"...\"' 行。"
            echo "      如果您的 pyproject.toml 是由 Poetry 管理，通常 'python' 約束會在 [tool.poetry.dependencies] 下。"
            echo "      由於無法自動定位並插入，請手動添加或修正 'python = \"$PROJECT_POETRY_PYTHON_CONSTRAINT\"' 到 pyproject.toml 的適當位置。"
            echo "      腳本將繼續使用選定的 Python ($PYTHON_PATH) 建立環境，但 Poetry 解析依賴時可能受 pyproject.toml 中不正確或缺失的約束影響。"
        fi
    else
        echo "INFO: pyproject.toml 中的 Python 約束 ($PROJECT_POETRY_PYTHON_CONSTRAINT) 與選定的 Python 版本一致。"
    fi
fi

#------------------------------------------------
# 3.1 若無 dev-dependencies 或特定 dev-dependencies 不存在，統一加入
#------------------------------------------------
POETRY_VERSION_MAJOR_MINOR=$(poetry --version | sed -n 's/Poetry (version) \([0-9]*\.[0-9]*\).*/\1/p' | head -n1)
USE_GROUP_DEV=false
if [[ -n "$POETRY_VERSION_MAJOR_MINOR" ]]; then
    if (( $(echo "$POETRY_VERSION_MAJOR_MINOR >= 1.2" | bc -l 2>/dev/null || echo 0) )); then
        USE_GROUP_DEV=true
    fi
fi

DEV_DEPS_SECTION_FOUND=false
if [ -f "pyproject.toml" ]; then
    if $USE_GROUP_DEV && grep -q -E '^\s*\[tool\.poetry\.group\.dev\.dependencies\]' pyproject.toml; then
        DEV_DEPS_SECTION_FOUND=true
    elif ! $USE_GROUP_DEV && grep -q -E '^\s*\[tool\.poetry\.dev-dependencies\]' pyproject.toml; then
        DEV_DEPS_SECTION_FOUND=true
    fi
fi

declare -a dev_dependencies=("pytest" "pytest-cov" "flake8")
NEEDS_POETRY_ADD_DEV=false
MISSING_DEV_DEPS_LIST=()

for dep in "${dev_dependencies[@]}"; do
    DEP_FOUND=false
    if [ "$DEV_DEPS_SECTION_FOUND" = true ] && [ -f "pyproject.toml" ]; then
        if grep -q -E "^\s*${dep}\s*=" pyproject.toml; then 
            DEP_FOUND=true
        fi
    fi
    if [ "$DEP_FOUND" = false ]; then
        NEEDS_POETRY_ADD_DEV=true
        MISSING_DEV_DEPS_LIST+=("$dep")
    fi
done

if [ "$NEEDS_POETRY_ADD_DEV" = true ] && [ ${#MISSING_DEV_DEPS_LIST[@]} -gt 0 ]; then 
    echo "INFO: 正在加入缺少的開發依賴: ${MISSING_DEV_DEPS_LIST[*]}"
    if $USE_GROUP_DEV; then
        poetry add --group dev "${MISSING_DEV_DEPS_LIST[@]}"
    else
        poetry add --dev "${MISSING_DEV_DEPS_LIST[@]}"
    fi
    echo "INFO: 開發依賴加入完成。"
else
    echo "INFO: 所有必要的開發依賴 (pytest, pytest-cov, flake8) 已存在於 pyproject.toml 或無需加入。"
fi

# ------------------------------------------------
# 3.2 確保 main-dependencies 中有 bs4 與 requests
# ------------------------------------------------
if ! poetry show beautifulsoup4 > /dev/null 2>&1; then
    echo "INFO: 偵測到缺少 beautifulsoup4，正在加入依賴…"
    poetry add beautifulsoup4
else
    echo "INFO: beautifulsoup4 已是專案依賴。"
fi
if ! poetry show requests > /dev/null 2>&1; then
    echo "INFO: 偵測到缺少 requests，正在加入依賴…"
    poetry add requests
else
    echo "INFO: requests 已是專案依賴。"
fi


# ------------------------------------------------
# 3.3 檢查其他常用模組（lxml）是否可 import，若不可就安裝
# ------------------------------------------------
echo "INFO: 自動檢查是否缺少解析器 lxml…"
if ! poetry run python - <<'PYCODE'
try:
    import lxml
except ModuleNotFoundError:
    exit(1) 
else:
    exit(0) 
PYCODE
then
    echo "INFO: 偵測到缺少 lxml，正在加入依賴…"
    poetry add lxml
else
    echo "INFO: lxml 已安裝。"
fi


#------------------------------------------------
# 4. 安裝相依並啟動虛擬環境 （包含 dev-dependencies）
#------------------------------------------------
echo "INFO: 正在安裝/更新專案依賴 (包含開發依賴)..." 
if $USE_GROUP_DEV; then
    poetry install --with dev 
else
    poetry install 
fi
echo "INFO: 依賴安裝/更新完成。" 

#------------------------------------------------
# 函式：執行互動式單元測試
#------------------------------------------------
run_interactive_tests() {
    echo "INFO: 正在搜尋位於 tests/ 目錄下的測試檔案 (test_*.py)..."
    declare -a test_files
    while IFS= read -r line; do
        test_files+=("$line")
    done < <(find tests -name "test_*.py" -type f 2>/dev/null | sort)

    if [ ${#test_files[@]} -eq 0 ]; then
        echo "警告: 在 tests/ 目錄下未找到任何 'test_*.py' 檔案。跳過單元測試步驟。"
    else
        echo "------------------------------------------------"
        echo "INFO: 🧪 可執行的單元測試："
        for i in "${!test_files[@]}"; do
            printf "%3d) %s\n" $((i+1)) "${test_files[$i]}"
        done
        echo " all) 執行所有測試檔案"
        echo "------------------------------------------------"

        local PYTEST_COMMON_ARGS="--maxfail=1 --disable-warnings -q --cov=macrolab_autoflow" 
        local test_choice

        while true; do
            read -p "請輸入測試代號 (1-${#test_files[@]})，或輸入 'all'，或直接按 Enter 跳過測試： " test_choice

            if [[ -z "$test_choice" ]]; then
                echo "INFO: 跳過單元測試。"
                break
            elif [[ "$test_choice" == "all" ]]; then
                echo "INFO: 🧪 執行所有單元測試（pytest）…"
                if poetry run pytest $PYTEST_COMMON_ARGS; then
                    echo "INFO: ✅ 所有測試通過，覆蓋率檢查 OK"
                else
                    echo "錯誤: 測試執行中發生錯誤或有測試未通過 (執行所有測試)。"
                fi
                break
            elif [[ "$test_choice" =~ ^[0-9]+$ ]] && [ "$test_choice" -ge 1 ] && [ "$test_choice" -le ${#test_files[@]} ]; then
                local SELECTED_TEST_FILE="${test_files[$((test_choice-1))]}"
                echo "INFO: 🧪 執行單元測試: $SELECTED_TEST_FILE （pytest）…"
                if poetry run pytest "$SELECTED_TEST_FILE" $PYTEST_COMMON_ARGS; then
                     echo "INFO: ✅ 測試 $SELECTED_TEST_FILE 通過。"
                else
                    echo "錯誤: 測試 $SELECTED_TEST_FILE 執行中發生錯誤或未通過。"
                fi
                break 
            else
                echo "錯誤：無效的輸入 '$test_choice'。請重新輸入。"
            fi
        done
    fi
}

#------------------------------------------------
# MODIFIED FUNCTION: 函式：運行主要 Python 程式
#------------------------------------------------
run_main_program() {
    echo "------------------------------------------------"
    local SCRIPT_NAME=""
    local RUN_MAIN_SCRIPT_FLAG=false # 預設為不執行，除非有效選擇
    declare -a main_py_files # 宣告一個陣列來儲存 .py 檔案列表
    local script_choice
    local i # 迴圈計數器

    main_py_files=() 
    while IFS= read -r found_file; do
        if [[ -n "$found_file" ]]; then 
            main_py_files+=("$found_file")
        fi
    done < <(find src -name "*.py" -type f | sort) 

    if [ ${#main_py_files[@]} -eq 0 ]; then
        echo "INFO: 在 'src' 目錄及其子目錄下未找到任何 '.py' 檔案。跳過運行主要程式步驟。"
    else
        echo "INFO: 🐍 可執行的主要 Python 程式 (位於 'src' 目錄及其子目錄)："
        for i in "${!main_py_files[@]}"; do
            printf "%3d) %s\n" $((i+1)) "${main_py_files[$i]}"
        done
        echo "------------------------------------------------"

        while true; do
            read -p "請輸入程式代號 (1-${#main_py_files[@]})，或直接按 Enter 跳過： " script_choice

            if [[ -z "$script_choice" ]]; then
                echo "INFO: 跳過運行主要程式。"
                break 
            elif [[ "$script_choice" =~ ^[0-9]+$ ]] && \
                 [ "$script_choice" -ge 1 ] && \
                 [ "$script_choice" -le ${#main_py_files[@]} ]; then
                SCRIPT_NAME="${main_py_files[$((script_choice-1))]}" 
                RUN_MAIN_SCRIPT_FLAG=true
                echo "INFO: 您選擇了執行: $SCRIPT_NAME"
                break 
            else
                echo "錯誤：無效的輸入 '$script_choice'。請重新輸入。"
            fi
        done
    fi

    if [ "$RUN_MAIN_SCRIPT_FLAG" = true ]; then
        local STATUS=0 # <<< --- 主要修正點：在此處初始化 STATUS 為 0 (成功狀態)
        local MISSING
        # 執行迴圈，用於處理模組缺失並重試
        while true; do
            echo "INFO: 執行：poetry run python -u \"$SCRIPT_NAME\""
            rm -f error.log 
            if poetry run python -u "$SCRIPT_NAME" 2>error.log; then
                echo "INFO: 腳本 $SCRIPT_NAME 執行成功。"
                rm -f error.log 
                STATUS=0 # <<< --- 確認成功時 STATUS 為 0
                break # 執行成功，跳出執行迴圈
            else
                STATUS=$? # Python 腳本失敗，捕獲其狀態
                echo "警告: 腳本 $SCRIPT_NAME 執行時發生錯誤 (退出狀態: $STATUS)。"
                if [ -s error.log ]; then # error.log 非空

                    MISSING_MODULE=$(sed -n "s/^.*ModuleNotFoundError: No module named '\([^']*\)'.*$/\1/p" error.log | head -n1)
                    
                    # 嘗試從 ImportError 中提取名稱（這可能不總是直接對應套件名）
                    MISSING_IMPORT_NAME=$(sed -n "s/^.*ImportError: cannot import name '\([^']*\)'.*$/\1/p" error.log | head -n1)

                    if [ -n "$MISSING_MODULE" ]; then
                        MISSING="$MISSING_MODULE"
                    elif [ -n "$MISSING_IMPORT_NAME" ]; then
                        MISSING="$MISSING_IMPORT_NAME"
                        # 這裡可以選擇性地加入一個 logger.warning，因為導入名稱不一定等於套件名
                        echo "警告: 偵測到 ImportError 名稱 '$MISSING'。嘗試將其作為套件安裝，但這可能不完全正確。"
                    else
                        MISSING=""
                    fi

                    if [ -n "$MISSING" ]; then # 是 ModuleNotFoundError
                        echo "INFO: 偵測缺少模組相關錯誤，可能是：'$MISSING'。正在嘗試安裝..."
                        if poetry add "$MISSING"; then
                            echo "INFO: 已嘗試安裝 $MISSING，將重新執行腳本 $SCRIPT_NAME..."
                            rm -f error.log
                            sleep 1 
                            # STATUS 將在下一次迭代如果再次失敗時被更新
                            continue # 回到執行迴圈的開始，重試執行
                        else
                            echo "錯誤: poetry add \"$MISSING\" 失敗。請檢查模組名稱或手動安裝。"
                            cat error.log 
                            rm -f error.log
                            # STATUS 已經是 poetry run 的失敗狀態，或者我們可以設定一個 poetry add 失敗的特定狀態
                            exit 1 # poetry add 失敗，退出腳本
                        fi
                    else # error.log 非空，但不是 ModuleNotFoundError
                        echo "錯誤：腳本執行失敗，但未從 stderr 中偵測到明確的 'No module named' 錯誤。詳細錯誤(來自stderr)如下："
                        cat error.log
                        rm -f error.log
                        echo "INFO: Python 腳本執行失敗 (非模組缺失)，將不會自動重試。"
                        # STATUS 已經是 poetry run 的失敗狀態
                        break 
                    fi
                else # error.log 為空 (Python 腳本可能將錯誤輸出到 stdout)
                    echo "錯誤：Python 腳本執行失敗 (退出碼: $STATUS)，錯誤訊息可能在上方標準輸出中。"
                    echo "      error.log 為空，無法從 stderr 中獲取詳細錯誤。"
                    echo "INFO: Python 腳本執行失敗，將不會自動重試。"
                    # STATUS 已經是 poetry run 的失敗狀態
                    break 
                fi
            fi
        done # 結束 Python 腳本執行/重試迴圈
        
        # 現在 STATUS 必定有一個值 (0 代表成功，非0 代表失敗)
        if [ "$STATUS" -ne 0 ]; then 
             echo "提醒: 主要 Python 程式 ($SCRIPT_NAME) 上次執行以錯誤狀態 ($STATUS) 結束。"
        fi
        echo "🎉 主要腳本 ($SCRIPT_NAME) 執行流程結束。"
    fi
}
# END OF MODIFIED FUNCTION

#------------------------------------------------
# 5. 選擇主要操作
#------------------------------------------------
echo "------------------------------------------------"
echo "INFO: 所有環境設定和依賴安裝已完成。"
echo "請問您想進行什麼操作？"
echo "  1) 執行單元測試"
echo "  2) 運行主要 Python 程式"
echo "  *) 其他任意鍵或直接按 Enter 結束"
echo "------------------------------------------------"

read -p "請輸入您的選擇 (1 或 2)： " main_action_choice

case "$main_action_choice" in
    1)
        echo "INFO: 您選擇了執行單元測試。"
        run_interactive_tests
        ;;
    2)
        echo "INFO: 您選擇了運行主要 Python 程式。"
        run_main_program
        ;;
    *)
        echo "INFO: 未選擇有效操作或選擇結束。"
        ;;
esac

echo "------------------------------------------------"
echo "🎉 PythonRun.sh 執行完畢。"
from Weather_Location_API_Data.fetcher.ArcGISFetcher import MetOfficeWarningsFetcher
from Weather_Location_API_Data.utils.jsonManager.jsonManager import JSONManager
from Weather_Location_API_Data.utils.jsonManager.jsonReader import JSONReader
from Weather_Location_API_Data.utils.jsonManager.jsonFormater import JSONFormatter
from Weather_Location_API_Data.utils.jsonManager.jsonFormatVerifier import JSONFormatVerifier

from pathlib import Path
import json

from datetime import datetime

sources = [
    ("MetOffice", MetOfficeWarningsFetcher()),
]

# 定義 Schema 檔案路徑
schema_json_path = Path("NSWWS_SAMPLE_DATA_SNOWFLAKE.json") # 使用你的驗證檔案

for name, fetcher in sources:
    # 生成時間戳，用於所有輸出檔案
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    # 1. 原始資料儲存路徑，使其包含時間戳
    raw_output_json_path = Path(f"metoffice_raw_{timestamp}.json")
    # 2. 定義格式化後通過驗證的資料輸出路徑
    formatted_output_json_path = Path(f"metoffice_raw_formatted_{timestamp}.json")
    # 3. 定義未通過驗證的原始資料輸出路徑
    failed_validation_output_json_path = Path(f"metoffice_raw_failed_validation_{timestamp}.json")

    print(f">>> [{name}] Fetching raw data...")
    res = fetcher.fetch()

    if res.is_success():
        raw_json_data = res.value

        # 始終嘗試將原始資料寫入帶有時間戳的檔案
        try:
            raw_output_json_path.write_text(json.dumps(raw_json_data, ensure_ascii=False, indent=2), encoding="utf-8")
            print(f"原始資料已成功儲存到：{raw_output_json_path}")
        except Exception as e:
            print(f"❌ 儲存原始資料到 {raw_output_json_path} 失敗: {e}")
            continue # 如果無法儲存原始資料，則跳過後續的驗證和處理

        # 初始化 JSONManager 的組成部分
        # JSONReader 現在從剛剛保存的帶時間戳的原始檔案讀取
        reader = JSONReader(raw_output_json_path)
        formatter = JSONFormatter(indent=2)
        
        # JSONFormatVerifier 的初始化可能會失敗，這裡捕獲它
        try:
            verifier = JSONFormatVerifier(schema_path=schema_json_path)
        except ValueError as e:
            print(f"❌ 初始化 JSONFormatVerifier 失敗：{e}. 請檢查 schema 檔案：{schema_json_path}")
            # 由於 schema 錯誤通常是配置問題，跳過當前處理，但原始檔案已經保存
            continue 

        # 創建 JSONManager 實例
        manager = JSONManager(reader, formatter, verifier)

        # 處理 JSON 檔案 (讀取 -> 驗證 -> 格式化寫入)
        print(f">>> [{name}] Processing JSON with schema validation...")
        # 將格式化後的輸出路徑傳遞給 process 方法
        process_result = manager.process(formatted_output_json_path)

        if process_result.is_success():
            # 這裡為了演示重新讀取 features 數量，實際應用中可根據需求調整
            try:
                with open(raw_output_json_path, 'r', encoding='utf-8') as f:
                    initial_data = json.load(f)
                num_features = len(initial_data.get("features", []))
                print(f"✅ 驗證通過並成功格式化！Features 數量: {num_features}，已輸出到 {process_result.value}")
            except Exception as e:
                print(f"🚨 注意：讀取原始資料中的 features 數量時發生錯誤: {e}. 但格式化後的檔案已成功生成。")
                print(f"✅ 驗證通過並成功格式化！已輸出到 {process_result.value}")
        else:
            # 如果處理失敗 (通常是驗證失敗)，額外儲存一份帶有 "_failed_validation" 標識的副本
            print(f"❌ JSON 處理失敗: {process_result.error_msg}")
            try:
                # 再次寫入原始資料到一個新的、標識為 "failed_validation" 的檔案
                raw_output_json_path.rename(failed_validation_output_json_path)
                print(f"未通過驗證的原始資料已重新命名並儲存為：{failed_validation_output_json_path}")
            except Exception as e:
                print(f"❌ 重新命名未通過驗證的原始資料時失敗: {e}. 原始資料仍為 {raw_output_json_path}")
    else:
        print(f"❌ Fetch 失敗: {res.error_msg}")
        print(f"因此沒有原始資料被保存。")
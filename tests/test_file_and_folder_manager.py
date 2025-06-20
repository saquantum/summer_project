import shutil
import pytest
from macrolab_autoflow.utils.FileAndFolderManager import FileAndFolderManager

def test_create_new_folder(tmp_path, caplog):
    """
    測試當資料夾不存在時，create_folder 會建立該資料夾，
    並回傳其絕對路徑，同時記錄「已建立資料夾」的 log。
    """
    # 設定caplog捕捉等級：將log等級設定為INFO，只有info(及以上級別)的訊息被收集到caplog.records
    caplog.set_level("INFO")
    # 在 tmp_path 下產生一個尚未存在的子資料夾名稱（組合路徑）
    new_dir = tmp_path / "new_folder"
    returned = FileAndFolderManager.create_folder(str(new_dir))

    # 回傳值應為絕對路徑字串，且路徑必須存在
    # 驗證 create_folder 的回傳字串與 new_dir.resolve()（取得絕對路徑）相同
    assert returned == str(new_dir.resolve())
    # 確認在檔案系統上真的建立了這個目錄(new_dir.exists())，且它是一個資料夾(new_dir.is_dir)
    assert new_dir.exists() and new_dir.is_dir()

    # 檢查 logging：
    # 遍歷 caplog.records（捕捉到的 log 訊息列表），只要其中有任何一筆 rec.message 包含子字串 "已建立資料夾"，測試就通過
    assert any("已建立資料夾" in rec.message for rec in caplog.records)

def test_create_existing_folder(tmp_path, caplog):
    """
    測試當資料夾已存在時，create_folder 不會拋錯，
    並回傳該資料夾的絕對路徑，同時記錄「資料夾已存在」的 log。
    """
    # 是先動手建立資料夾
    # 準備階段：在暫存目錄下手動 (mkdir()) 建立一個名為 "existing" 的子目錄，以模擬「資料夾已存在」的情況
    exist_dir = tmp_path / "existing"
    exist_dir.mkdir()

    caplog.set_level("INFO")
    returned = FileAndFolderManager.create_folder(str(exist_dir))

    # 回傳值仍為絕對路徑字串
    assert returned == str(exist_dir.resolve())
    # 目錄依然存在
    assert exist_dir.exists() and exist_dir.is_dir()

    # 檢查 log 中「資料夾已存在」
    assert any("資料夾已存在" in rec.message for rec in caplog.records)

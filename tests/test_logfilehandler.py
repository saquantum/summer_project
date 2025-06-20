import logging
from pathlib import Path
import pytest

from macrolab_autoflow.utils.LogFileHandler import LogFileHandler


@pytest.fixture(autouse=True)
def clear_logging_handlers():
    """
    確保每個測試前後都清空 root logger 的 handlers，
    避免因為 basicConfig(force=True) 造成彼此干擾。
    """
    root_logger = logging.getLogger()
    handlers = list(root_logger.handlers)
    for h in handlers:
        root_logger.removeHandler(h)
    yield
    for h in list(root_logger.handlers):
        root_logger.removeHandler(h)


def test_log_init_GivenCustomDirAndFilename_ShouldCreateFileAndLogInfo(
    tmp_path, caplog
):
    # Arrange
    log_dir = tmp_path / "logs"
    log_fname = "mytest.log"
    caplog.set_level(logging.INFO)

    # Act
    returned = LogFileHandler.log_init(str(log_dir), log_fname)

    # Assert: 回傳正確路徑
    expected = str((log_dir / log_fname).resolve())
    assert returned == expected

    # Assert: 檔案實際建立
    assert Path(returned).exists() and Path(returned).is_file()

    # Assert: 有「已建立資料夾」與「Log file created」的 log 訊息
    messages = [rec.message for rec in caplog.records]
    assert any("已建立資料夾" in m for m in messages)
    assert any("Log file 儲存路徑" in m for m in messages)


def test_log_init_GivenShowArgs_ShouldLogConfiguration(tmp_path, caplog):
    # Arrange
    log_dir = tmp_path / "logs_show"
    log_fname = "show.log"
    caplog.set_level(logging.INFO)

    # Act: 第三個參數包含 "show detailed parameters"，會觸發 should_show=True
    returned = LogFileHandler.log_init(
        str(log_dir), log_fname, "show detailed parameters"
    )

    # 從回傳的路徑字串建立 Path 物件
    returned_log_file_path = Path(returned_path_string)

    # Assert: 檔案仍建立 (使用新的 Path 物件變數)
    assert returned_log_file_path.exists()

    # Assert: 有「Current logging configuration」的 log 訊息 (從檔案讀取)
    log_content = ""
    if returned_log_file_path.is_file():  # 確保是檔案再讀取 (使用新的 Path 物件變數)
        with open(
            returned_log_file_path, "r", encoding="utf-8"
        ) as f:  # (使用新的 Path 物件變數)
            log_content = f.read()

    assert (
        "Current logging configuration" in log_content
    ), f"預期的日誌訊息 'Current logging configuration' 未在檔案 {returned_log_file_path} 中找到。\n檔案內容：\n{log_content[:500]}..."  # (使用新的 Path 物件變數)

    # (可選) 也可以檢查另一條在 basicConfig 之後的日誌訊息
    assert (
        "Log file created" in log_content
    ), f"預期的日誌訊息 'Log file created' 未在檔案 {returned_log_file_path} 中找到。\n檔案內容：\n{log_content[:500]}..."  # (使用新的 Path 物件變數)

    # Assert: 檔案仍建立
    assert Path(returned).exists()

    # Assert: 有「Current logging configuration」的 log 訊息 (從檔案讀取)
    log_content = ""
    if returned_log_file_path.is_file():  # 確保是檔案再讀取
        with open(returned_log_file_path, "r", encoding="utf-8") as f:
            log_content = f.read()

    assert (
        "Current logging configuration" in log_content
    ), f"預期的日誌訊息 'Current logging configuration' 未在檔案 {returned_log_file_path} 中找到。\n檔案內容：\n{log_content[:500]}..."  # 顯示部分內容幫助除錯

    # (可選) 也可以檢查另一條在 basicConfig 之後的日誌訊息
    assert (
        "Log file created" in log_content
    ), f"預期的日誌訊息 'Log file created' 未在檔案 {returned_log_file_path} 中找到。\n檔案內容：\n{log_content[:500]}..."


def test_view_AfterInit_ShouldReturnValidConfigStructure(tmp_path):
    # Arrange: 先初始化一次
    log_dir = tmp_path / "logs_view"
    log_fname = "view.log"
    LogFileHandler.log_init(str(log_dir), log_fname)

    # Act
    config = LogFileHandler.view()

    # Assert: 結構完整
    assert isinstance(config, dict)
    assert "logger_level" in config
    assert "handlers" in config and isinstance(config["handlers"], list)
    # handlers 裡的每個 dict 都包含 class、filename、level、format
    for h in config["handlers"]:
        assert all(key in h for key in ["class", "filename", "level", "format"])

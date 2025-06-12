import logging
from pathlib import Path
from macrolab_autoflow.utils.FileAndFolderManager import FileAndFolderManager

# 獲取 logger 實例
## 通過模塊名稱（__name__）創建或獲取一個 looger
## 這樣做可是不同模塊的日誌分流到不同的 logger，並可針對性地設置級別或 handler
logger = logging.getLogger(__name__)



class LogFileHandler:
    @staticmethod
    def log_init(log_dir="log_file", log_filename=None, *args, **kwargs):
        """
        初始化 log 檔配置，並根據 *args 判斷是否記錄詳細的 logging 參數設定。

        參數:
          log_dir: 要存放 log 檔的資料夾名稱（預設 "log_file"）
          log_filename: log 檔案名稱；若為 None，則使用當前時間生成預設檔名。
          *args: 位置參數，會解析所有字串參數中的 tokens，
                 若其中包含 "show" 且至少包含 "detailed"、"parameter" 或 "parameters" 中的一個，
                 則在初始化後記錄詳細的 logging 配置到 log 檔中。
          **kwargs: 傳入 logging.basicConfig 的其他參數，可用來覆蓋預設值，如 filemode、level、format、force 等。

        返回:
          merged_log_filename: 產生的 log 檔案完整路徑
        """
        # 建立 log 資料夾（若不存在則建立）
        abs_log_dir = FileAndFolderManager.create_folder(log_dir)
        
        # 若未指定 log_filename，則依當前時間動態生成 log 檔案名稱
        if log_filename is None:
            # 動態產生檔名 (yyyyMMdd_HHMMSS)
            from datetime import datetime
            log_filename = datetime.now().strftime("Debugging_%Y%m%d_%H%M%S.log")
        
        # 組合完整的 log 檔案路徑
        merged_log_filename = Path(abs_log_dir) / log_filename
        merged_log_filename = str(merged_log_filename)

        
        # 顯示建立 log 資料夾與 log 檔案路徑（除錯用）
        print("已建立資料夾:", abs_log_dir)
        print(f"Log file 應該儲存在：{merged_log_filename}")
        
        logger.info("已建立資料夾: %s", abs_log_dir)
        logger.info("Log file 儲存路徑: %s", merged_log_filename)

        
        # 設定預設的 logging 配置參數
        defaults = {
            "filename": merged_log_filename,
            "filemode": "w",  # 若需要追加模式，可改為 "a"
            "level": logging.INFO,
            "format": "%(asctime)s - %(levelname)s - %(message)s",
            "force": True  # 強制重新設定 logging 配置，清除所有現有的 handler
        }
        # 覆蓋相同鍵的值： 如果 kwargs 裡有和 defaults 中相同的鍵，
        # 那 defaults 中的相應值就會被 kwargs 中的值取代
        defaults.update(kwargs)

        # 呼叫 logging.basicConfig 配置 logging
        logging.basicConfig(**defaults)
        
        # 解析 *args 中的每個字串參數，檢查是否需觸發詳細顯示
        should_show = False
        for arg in args:
            if isinstance(arg, str):
                # 將參數轉為小寫以便不區分大小寫比較
                arg_lower = arg.lower()
                # 檢查是否包含 "show" 以及至少一個詳細資訊關鍵字
                has_show_keyword = "show" in arg_lower
                has_detail_keyword = ("detailed" in arg_lower or
                                      "parameter" in arg_lower or
                                      "parameters" in arg_lower)
                
                if has_show_keyword and has_detail_keyword:
                    should_show = True
                    break  # 找到符合條件的參數，即可跳出迴圈
        
        if should_show:
            # 呼叫 view() 取得當前 logging 的設定，然後利用 pprint 整齊格式化後記錄到 log 檔中
            config = LogFileHandler.view()
            logging.info("Current logging configuration:\n%s", config)
        
        # 記錄一筆初始訊息確認 log 配置啟動
        logger.info("Log file created: %s", merged_log_filename)
        return merged_log_filename

    @staticmethod
    def view():
        """
        查看當前 logging 的內部參數配置。

        返回:
            config: 包含 root logger 以及各個 handler 設定的字典
        """
        config = {}
        logger = logging.getLogger()
        config["logger_level"] = logging.getLevelName(logger.level)
        config["handlers"] = []

        for handler in logger.handlers:
            handler_info = {}

            # 使用 match-case 檢查 handler 是否具有 baseFilename 屬性
            match handler:
                case h if hasattr(h, "baseFilename"):
                    handler_info["class"] = h.__class__.__name__
                    handler_info["filename"] = h.baseFilename
                case _:
                    handler_info["class"] = handler.__class__.__name__
                    handler_info["filename"] = None

            handler_info["level"] = logging.getLevelName(handler.level)

            # 使用 match-case 檢查 handler 是否有 formatter
            match handler.formatter:
                case None:
                    handler_info["format"] = None
                case fmt:
                    handler_info["format"] = fmt._fmt

            config["handlers"].append(handler_info)

        return config
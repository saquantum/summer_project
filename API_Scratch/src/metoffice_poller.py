"""metoffice_poller.py (OOP 版)

將原本的流程腳本拆分成 3 個核心元件：

1. **`DataProcessor`**
   - 處理原始 JSON → 驗證 → 格式化 → 儲存。
   - 內部封裝 `JSONManager`，並負責檔名與資料夾管理。
2. **`MetOfficePoller`**
   - 掌管定時排程 (預設 5 分鐘) 與優雅停機（SIGINT / SIGTERM）。
   - 每次循環呼叫 `fetcher.fetch()` 及 `processor.process()`。
3. **`CLI`**
   - 將使用者輸入參數轉成組件，並啟動 `MetOfficePoller.run()`。

整個流程仍使用 `Result[T]` 封裝成功 / 失敗訊息。
"""

from __future__ import annotations

import argparse  # 解析命令行參數
import json
import logging
import signal  # 捕捉系統信號，Ex：Ctrl+C
import sys
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional  # 類型註解

from Weather_Location_API_Data.common.Result import Result
from Weather_Location_API_Data.fetcher.ArcGISFetcher import MetOfficeWarningsFetcher
from Weather_Location_API_Data.utils.jsonManager.jsonFormater import JSONFormatter
from Weather_Location_API_Data.utils.jsonManager.jsonManager import JSONManager
from Weather_Location_API_Data.utils.jsonManager.jsonReader import JSONReader
from Weather_Location_API_Data.utils.jsonManager.jsonFormatVerifier import JSONFormatVerifier

# ────────────────────────────────────────────────────────────────
# 1) DataProcessor：調用從爬蟲、資料處理的邏輯
# ────────────────────────────────────────────────────────────────
class DataProcessor:
    """
    單責任：把抓下來的 raw_json 送進 JSONManager，
      a) 若成功 → 儲存 raw & formatted；
      b) 若失敗 → raw 重新命名為 *_failed_validation.json。
    """

    def __init__(
        self,
        schema_path: Path,    # 驗證資料路徑
        output_dir: Path,     # 被驗證資料輸出文件路徑
        indent: int = 2,      # 縮排
    ) -> None:
        self.output_dir = output_dir
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.indent = indent

        # 只初始化一次 verifier，省略重複解析 schema
        self.verifier = JSONFormatVerifier(schema_path)  # 創建一個 JSONFormatVerifier對象，用來對任意的 json 數據進行格式和類型的檢查
        self.formatter = JSONFormatter(indent=indent)

    # ────────────────────────────────────────────────────────────
    # Public API
    # ────────────────────────────────────────────────────────────
    def process(self, raw_json: dict) -> Result[Path]:
        """依目前 UTC timestamp 建立檔名，完整走一次驗證+輸出流程。"""

        # # 若沒有任何 warnings，就直接寫入 *_no_warning.json，
        # # 並回傳成功，讓上層可以決定要不要再做其他事。
        # if len(raw_json.get("features", [])) == 0:
        #     now = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        #     no_warn_path = self.output_dir / f"metoffice_no_warning_{now}.json"
        #     write_res = self._write_json(raw_json, no_warn_path)
        #     return write_res if write_res.is_success() else Result.fail(write_res.error_msg)

        # now = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        now = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
        raw_path = self.output_dir / f"metoffice_raw_{now}.json"  # 爬取下來後的原始文件 metoffice_raw.json
        formatted_path = self.output_dir / f"metoffice_raw_formatted_{now}.json"
        failed_path = self.output_dir / f"metoffice_raw_failed_validation_{now}.json"

        # 1) 永遠先把 Raw JSON 寫入
        write_res = self._write_json(raw_json, raw_path)
        if write_res.is_failure():
            return Result.fail(write_res.error_msg)

        # 2) JSONManager：讀 → 驗 → 格式化
        reader = JSONReader(raw_path)    # 先將 json資料讀進來，創建資料對象
        manager = JSONManager(reader, self.formatter, self.verifier)  # 創建 jsonManager 物件，進行 “讀 → 驗 → 格式化”
        proc_res = manager.process(formatted_path)  # JSONManager.process(formatted_path) 会校验并输出格式化文件，返回 Result[Path]

        # 3) 依驗證結果決定後續
        if proc_res.is_success():
            return Result.ok(proc_res.value)

        # 驗證失敗 → raw 檔案改名為 failed_validation
        try:
            raw_path.rename(failed_path)
        except Exception as err:
            # logging.error("重新命名驗證失敗檔案時發生錯誤: %s", err)
            print(logging.error("重新命名驗證失敗檔案時發生錯誤: %s", err))
        return Result.fail(proc_res.error_msg)

    # ────────────────────────────────────────────────────────────
    #     Private function: write into a existed json file
    # ────────────────────────────────────────────────────────────
    def _write_json(self, data: dict, path: Path) -> Result[None]:
        try:
            path.write_text(json.dumps(data, ensure_ascii=False, indent=self.indent), encoding="utf-8")
            return Result.ok(None)
        except Exception as err:
            return Result.fail(f"無法寫入原始檔 {path}: {err}")


# ────────────────────────────────────────────────────────────────
# 2) Poller
# ────────────────────────────────────────────────────────────────
class MetOfficePoller:
    """
    負責定時呼叫 fetcher 與 processor，並支援優雅停機
    """

    def __init__(
        self,
        fetcher: MetOfficeWarningsFetcher,  # 類型注解：傳入一個負責fetch天氣警告數據的實例
        processor: DataProcessor,  # 類型注解：傳入一個負責執行「從爬蟲、資料處理的邏輯」的實例
        interval_sec: int = 300,  # 預設參數：如果 user 不提供，默認 300s（5min）一次
    ) -> None:
        self.fetcher = fetcher
        self.processor = processor
        self.interval_sec = interval_sec  # 設定 polling interval
        self._running = True  # 控制循環狀態
        self._register_signal_handlers()

    # ────────────────────────────────────────────────────────────
    # Public control
    # ────────────────────────────────────────────────────────────
    def run(self) -> None:
        '''
        1. 打開一條開始日誌
        2. 只要 _running = True：
            a) 記錄循環開始時間
            b) 調用 _process_cycle 執行一次 fetch/parse
            c) 計算剩餘等待時間，sleep 保持固定週期
        3. 循環結束後，輸出結束日誌
        '''
        # logging.info("開始輪詢，每 %d 秒一次 (Ctrl+C 停止)", self.interval_sec)
        print(f"開始輪詢，每 {self.interval_sec} 秒一次 (Ctrl+C 停止)")
        try:
            while self._running:  # 初始：self.running為True，進入主循環
                start = time.time()
                self._process_cycle()
                sleep_sec = max(self.interval_sec - (time.time() - start), 0)
                if sleep_sec:
                    time.sleep(sleep_sec)
        except KeyboardInterrupt:  # 如果在 sleep 時按 Ctrl-C，也能直接跳出
            pass
        finally:
            # logging.info("Poller 已結束")
            print("Poller 已結束")

    # ────────────────────────────────────────────────────────────
    #     Private Functions
    # ────────────────────────────────────────────────────────────
    def _process_cycle(self) -> None:
        '''
        1. Fetcher
        '''
        # 1) Fetch
        fetch_res = self.fetcher.fetch()
        if fetch_res.is_failure():
            # logging.error("Fetch 失敗: %s", fetch_res.error_msg)
            print(f"Fetch 失敗: {fetch_res.error_msg}")
            return

        # NSWWS 有時會回傳 features=[]，代表目前沒有任何天氣預警。
        # 這種情況下，我們不必再跑驗證／寫檔，
        # 直接提示使用者並提早結束本輪循環即可。
        if len(fetch_res.value.get("features", [])) == 0:
            print("🔔 無天氣預警（NSWWS features 為空）")
            return

        # 2) Process
        proc_res = self.processor.process(fetch_res.value)  # type: ignore[arg-type]
        if proc_res.is_success():
            num_feats = len(fetch_res.value.get("features", []))  # type: ignore[attr-defined]
            # logging.info("✅ 驗證通過 (%d features) → %s", num_feats, proc_res.value)
            print(f"✅ 驗證通過 ({num_feats} features) → {proc_res.value}")
        else:
            # logging.warning("❌ 驗證失敗：%s", proc_res.error_msg)
            print(f"❌ 驗證失敗：{proc_res.error_msg}")

    def _register_signal_handlers(self) -> None:
        '''
        程序再收到系統終止請求，Ex：Ctrl+C 或 kill 命令）時，不是立刻應中斷，而是先完成當前一次 fetch/parse，再退出主循環
        避免中途寫文件時，導致數據損壞，為了能夠優雅地停止
        '''
        def _shutdown(signum, frame):
            '''
            1. signum參數：一個整數，表示收到的信號編號
                a) signal.SIGINT 的編號通常是 2（Ctrl+C 發出的信號）
                b) signal.SIGTERM 的編號通常為 15（kill <pid> 發出的信號）
            
            2. frame參數：當前執行的棧對象，在這裡不需要用到，但信號處理函數必須接收兩個參數

            3. for sig in (signal.SIGINT, signal.SIGTERM)
                 a) signal.SIGINT：Interrupt，通常由 Ctrl+C 觸發
                 b) signal.SIGTERM：Terminate，請求程序終止，通常由 kill 命令發送
               
               通過 signal.signal() 把兩種信號都綁定到同一個回調 _shutdown 上
               註冊後，系統一旦發出上述任意信號，Python Interpreter就會在主線程上調用 _shutdown(signum, frame)，
               而不是直接拋出 KeyboardInterrupt 異常 或 硬退出
            '''
            if self._running:  # 只在「第一次」收到訊號時印出提示、改旗標
                # logging.info("Received %s, shutting down gracefully ...", signal.Signals(signum).name)
                name = signal.Signals(signum).name
                print(f"Received {name}, shutting down gracefully …")
                ## 利用 Signals枚舉，將編號（如：2）轉換成可讀的名稱（如："SIGINT"）

                self._running = False  # 通過修改實例屬性，告訴 run() 方法的主循環「下次檢查到這個標誌就是 False，請跳出循環，並結束程序」
            else:  # 關閉流程，後續 SIGINT 直接把 sleep 中斷
                raise KeyboardInterrupt


        for sig in (signal.SIGINT, signal.SIGTERM):
            signal.signal(sig, _shutdown)


# ────────────────────────────────────────────────────────────────
# 3) CLI 入口
# ────────────────────────────────────────────────────────────────

def main(argv: Optional[list[str]] = None) -> None:  # noqa: D401
    parser = argparse.ArgumentParser(description="Met Office Warnings Poller (OOP 版)")
    parser.add_argument("--schema", type=Path, required=True, help="schema JSON (validation 標準)")
    parser.add_argument("--output-dir", type=Path, default=Path("./data"), help="輸出目錄 (default ./data)")
    parser.add_argument("--interval", type=int, default=300, help="輪詢間隔 (秒)，default 300")
    parser.add_argument("--indent", type=int, default=2, help="輸出 JSON 縮排，default 2")
    parser.add_argument(
        "--log-level",
        default="INFO",
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        help="log level",
    )

    args = parser.parse_args(argv)

    # # Logging 基本設定
    # logging.basicConfig(
    #     level=getattr(logging, args.log_level),
    #     format="%(asctime)s [%(levelname)s] %(message)s",
    #     datefmt="%Y-%m-%d %H:%M:%S",
    #     handlers=[logging.StreamHandler(sys.stdout)],
    # )

    # 建立組件
    fetcher = MetOfficeWarningsFetcher()
    processor = DataProcessor(
        schema_path=args.schema,
        output_dir=args.output_dir,
        indent=args.indent,
    )
    poller = MetOfficePoller(fetcher, processor, interval_sec=args.interval)

    # 執行
    poller.run()


if __name__ == "__main__":
    main()

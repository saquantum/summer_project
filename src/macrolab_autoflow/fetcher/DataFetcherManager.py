from __future__ import annotations

import logging
import random
import re
from typing import Optional

import requests
from bs4 import BeautifulSoup
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

from macrolab_autoflow.common.Result import Result
from macrolab_autoflow.utils.FileAndFolderManager import FileAndFolderManager
from datetime import datetime
from pathlib import Path

logger = logging.getLogger(__name__)


class DataFetcherManager:
    RAW_HTML_SAVE_DIR = "fetched_raw_html"

    def __init__(
        self,
        url: str,
        *,
        retry_total: int = 5,
        backoff_factor: float = 1.0,
        status_forcelist: list[int] = [429, 500, 502, 503, 504],
        allowed_methods: list[str] = ["GET", "HEAD"],
        use_selenium: bool = False,
    ):
        self._url = url
        self._use_selenium = use_selenium  # 初始值
        self._driver = None
        self._session = requests.Session()

        if not self._use_selenium:  # 如果不是 Selenium 模式，設定 requests 重試
            self._configure_fetch_retry(
                total=retry_total,
                backoff=backoff_factor,
                status_forcelist=status_forcelist,
                allowed_methods=allowed_methods,
            )

        if use_selenium:  # 只有當明確要求使用 Selenium 時才嘗試初始化
            from selenium import webdriver
            from selenium.webdriver.chrome.options import Options

            # from selenium.webdriver.chrome.service import Service as ChromeService
            # from webdriver_manager.chrome import ChromeDriverManager

            chrome_options = Options()
            chrome_options.add_argument("--headless")
            # --- MODIFICATION START: Corrected __init__ error handling for Selenium ---
            try:
                # self._driver = webdriver.Chrome(service=ChromeService(ChromeDriverManager().install()), options=chrome_options) # 使用 webdriver-manager
                self._driver = webdriver.Chrome(
                    options=chrome_options
                )  # 直接使用，需 ChromeDriver 在 PATH
            except Exception as e:
                logger.error(
                    f"[DataFetcherManager] 初始化 Selenium WebDriver 失敗: {e}. 請確保 ChromeDriver 已正確安裝並在 PATH 中，或考慮使用 webdriver-manager。"
                )
                self._use_selenium = False  # 初始化失敗，則將 use_selenium 設回 False
                self._driver = None
                logger.warning(
                    "由於 WebDriver 初始化失敗，此 DataFetcherManager 實例將僅使用 requests 模式。"
                )
                # 如果不是 requests 模式（即 _configure_fetch_retry 未執行），且 Selenium 也失敗，則需要設定 requests 重試
                if not self._session.adapters:  # 檢查 session 是否已配置 adapter
                    self._configure_fetch_retry(
                        total=retry_total,
                        backoff=backoff_factor,
                        status_forcelist=status_forcelist,
                        allowed_methods=allowed_methods,
                    )
            # --- MODIFICATION END ---

    # --- 公開方法 ---
    def fetcher(self) -> Result[BeautifulSoup]:
        logging.info(
            "[DataFetcherManager] 開始抓取主頁面 URL: %s (模式: %s)",
            self._url,
            "Selenium" if self._use_selenium and self._driver else "requests",
        )
        try:
            html_content_str = ""
            if self._use_selenium and self._driver:  # 確保 driver 也被成功初始化
                self._driver.get(self._url)
                self._selenium_wait_for_element(
                    "tag name", "body", timeout=random.uniform(5, 10)
                )
                html_content_str = self._driver.page_source
                try:
                    abs_save_dir_path_str = FileAndFolderManager.create_folder(
                        self.RAW_HTML_SAVE_DIR
                    )
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
                    sanitized_url_part = self._sanitize_url_to_filename(self._url)
                    filename_to_save = f"{timestamp}_{sanitized_url_part}_selenium.txt"
                    full_file_path = Path(abs_save_dir_path_str) / filename_to_save
                    with open(full_file_path, "w", encoding="utf-8") as f:
                        f.write(html_content_str)
                    logger.info(f"網頁原文 (Selenium) 已儲存到: {full_file_path}")
                except Exception as save_e:
                    logger.error(f"儲存 Selenium 網頁原文失敗: {save_e}", exc_info=True)
            else:
                if (
                    self._use_selenium and not self._driver
                ):  # 原本想用 selenium 但 driver 初始化失敗
                    logger.warning(
                        "Selenium driver 未成功初始化，將嘗試使用 requests 模式抓取主頁面。"
                    )
                html_content_str = self._download_html(self._url)

            soup = BeautifulSoup(html_content_str, "lxml")
            return Result.ok(soup)
        except Exception as e:
            logger.error("抓取主頁面 %s 失敗：%s", self._url, e, exc_info=True)
            return Result.fail(f"抓取主頁面 {self._url} 失敗: {str(e)}")

    def close(self):
        if self._use_selenium and self._driver:
            self._driver.quit()
            logger.info("Selenium WebDriver 已關閉。")
        if self._session:
            self._session.close()
            logger.info("Requests session 已關閉。")

    def get_html_from_url(self, target_url: str) -> Result[str]:
        logger.info(
            "[DataFetcherManager] 開始抓取獨立文章 URL: %s (模式: %s)",
            target_url,
            "Selenium" if self._use_selenium and self._driver else "requests",
        )
        try:
            # 如果是 selenium 模式且 driver 正常，也可以用 selenium 抓取獨立頁面
            if self._use_selenium and self._driver:
                self._driver.get(target_url)
                self._selenium_wait_for_element(
                    "tag name", "body", timeout=random.uniform(5, 10)
                )
                html_content = self._driver.page_source
                # --- MODIFICATION START: Save HTML content for Selenium mode too (for individual articles) ---
                try:
                    abs_save_dir_path_str = FileAndFolderManager.create_folder(
                        self.RAW_HTML_SAVE_DIR
                    )
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
                    sanitized_url_part = self._sanitize_url_to_filename(target_url)
                    filename_to_save = f"{timestamp}_{sanitized_url_part}_selenium.txt"
                    full_file_path = Path(abs_save_dir_path_str) / filename_to_save
                    with open(full_file_path, "w", encoding="utf-8") as f:
                        f.write(html_content)
                    logger.info(f"網頁原文 (Selenium) 已儲存到: {full_file_path}")
                except Exception as save_e:
                    logger.error(
                        f"儲存 Selenium 網頁原文失敗 for {target_url}: {save_e}",
                        exc_info=True,
                    )
                # --- MODIFICATION END ---
            else:
                html_content = self._download_html(
                    target_url
                )  # _download_html 內部會儲存檔案
            return Result.ok(html_content)
        except requests.exceptions.HTTPError as e:
            logger.error(
                "抓取獨立文章 HTTP 錯誤 %s: %s (狀態碼: %s)",
                target_url,
                e,
                e.response.status_code if e.response else "N/A",
                exc_info=False,
            )
            return Result.fail(
                f"HTTP 錯誤 {e.response.status_code if e.response else 'N/A'} 於 {target_url}: {str(e)}"
            )
        except requests.exceptions.RequestException as e:
            logger.error(
                "抓取獨立文章 Request 錯誤 %s: %s", target_url, e, exc_info=True
            )
            return Result.fail(f"請求錯誤於 {target_url}: {str(e)}")
        except Exception as e:  # 其他所有錯誤，包括 Selenium 的
            logger.error(
                "抓取獨立文章時發生未預期錯誤 %s: %s", target_url, e, exc_info=True
            )
            return Result.fail(f"未預期錯誤於 {target_url}: {str(e)}")

    # --- Private Methods ---
    def _sanitize_url_to_filename(self, url: str) -> str:
        name = re.sub(r"^https?://www\.", "", url, flags=re.IGNORECASE)
        name = re.sub(r"^https?://", "", name, flags=re.IGNORECASE)
        name = name.replace("/", "_slash_").replace(".", "_dot_")
        name = re.sub(r"[^\w_-]", "", name)
        return (name[:75] + "...") if len(name) > 75 else name

    def _download_html(self, target_url: str) -> str:
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0) "
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"
            )
        }
        resp = self._session.get(
            target_url, headers=headers, timeout=random.uniform(5, 10)
        )
        resp.raise_for_status()

        final_encoding = None
        try:
            # --- MODIFICATION START: Corrected encoding logic ---
            # 1. 嘗試從 HTTP header 的 Content-Type 獲取編碼
            content_type_header = resp.headers.get("content-type", "").lower()
            match_header_charset = re.search(r"charset=([\w.-]+)", content_type_header)
            if match_header_charset:
                header_encoding = match_header_charset.group(1).strip().lower()
                if header_encoding != "iso-8859-1":  # 忽略不可靠的 iso-8859-1
                    logger.info(
                        f"從 HTTP header Content-Type 找到編碼: {header_encoding} for URL: {target_url}"
                    )
                    final_encoding = header_encoding

            # 2. 如果 header 中沒有或不可靠，嘗試從 HTML meta 標籤偵測
            if not final_encoding:
                detected_encoding = self._detect_charset(
                    resp.content
                )  # resp.content is bytes
                logger.info(
                    f"透過 _detect_charset 偵測到編碼: {detected_encoding} for URL: {target_url} (Header編碼為: {resp.encoding or '未指定'})"
                )
                final_encoding = detected_encoding

            # 3. 如果以上都失敗，使用 requests 的 apparent_encoding
            if not final_encoding:
                final_encoding = resp.apparent_encoding
                logger.info(
                    f"使用 apparent_encoding: {final_encoding} for URL: {target_url}"
                )

            resp.encoding = (
                final_encoding  # 設定 response 物件的編碼，resp.text 會用這個
            )
            # --- MODIFICATION END ---
        except Exception as e:
            logger.warning(
                f"編碼設定/偵測過程中發生錯誤 for URL {target_url} (錯誤: {e})，將依賴 requests 預設的編碼處理。"
            )
            final_encoding = (
                resp.encoding if resp.encoding else resp.apparent_encoding
            )  # 確保 final_encoding 有值

        html_content_str = resp.text

        try:
            abs_save_dir_path_str = FileAndFolderManager.create_folder(
                self.RAW_HTML_SAVE_DIR
            )
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
            sanitized_url_part = self._sanitize_url_to_filename(target_url)
            filename_to_save = f"{timestamp}_{sanitized_url_part}.txt"
            full_file_path = Path(abs_save_dir_path_str) / filename_to_save

            # --- MODIFIED: Ensure encoding_to_save is valid ---
            encoding_to_save = final_encoding if final_encoding else "utf-8"
            # --- END MODIFICATION ---
            with open(
                full_file_path, "w", encoding=encoding_to_save, errors="replace"
            ) as f:  # Add errors='replace'
                f.write(html_content_str)
            logger.info(
                f"網頁原文已儲存到: {full_file_path} (編碼: {encoding_to_save})"
            )
        except Exception as e:
            file_path_for_log = locals().get(
                "full_file_path", f"未定義路徑於目錄({self.RAW_HTML_SAVE_DIR})"
            )
            logger.error(f"儲存網頁原文到 {file_path_for_log} 失敗: {e}", exc_info=True)

        return html_content_str

    def _configure_fetch_retry(
        self,
        *,
        total: int,
        backoff: float,
        status_forcelist: list[int],
        allowed_methods: list[str],
    ) -> None:
        # ... (此方法保持不變) ...
        retry_strategy = Retry(
            total=total,
            backoff_factor=backoff,
            status_forcelist=status_forcelist,
            allowed_methods=allowed_methods,
        )
        adapter = HTTPAdapter(max_retries=retry_strategy)
        self._session.mount("http://", adapter)
        self._session.mount("https://", adapter)

    # --- MODIFIED: _detect_charset accepts bytes, improved logic ---
    @staticmethod
    def _detect_charset(html_bytes: bytes) -> str:
        """
        從 HTML bytes 中偵測 <meta> charset，若無法偵測，預設使用 'utf-8'
        """
        temp_html_text_for_meta_parsing = ""
        # 嘗試幾種常見編碼解碼，以便 BeautifulSoup 解析 meta 標籤
        encodings_to_try = ["utf-8", "latin-1", "gbk", "big5", "iso-8859-1"]
        decoded_successfully = False
        for enc in encodings_to_try:
            try:
                temp_html_text_for_meta_parsing = html_bytes.decode(
                    enc, errors="ignore"
                )
                decoded_successfully = True
                logger.debug(f"使用編碼 {enc} 初步解碼 HTML bytes 成功。")
                break
            except UnicodeDecodeError:
                logger.debug(f"使用編碼 {enc} 初步解碼 HTML bytes 失敗。")
                continue

        if not decoded_successfully:
            logger.warning(
                "無法使用常見編碼初步解碼 HTML bytes 以偵測 charset，將返回預設 utf-8"
            )
            return "utf-8"

        try:
            soup = BeautifulSoup(temp_html_text_for_meta_parsing, "lxml")

            # 查找 <meta charset="...">
            meta_charset_tag = soup.find("meta", charset=True)
            if meta_charset_tag and meta_charset_tag.get("charset"):
                found_encoding = meta_charset_tag["charset"].strip().lower()
                logger.debug(f"透過 meta charset 找到編碼: {found_encoding}")
                return found_encoding

            # 查找 <meta http-equiv="Content-Type" content="...charset=...">
            meta_content_type_tag = soup.find(
                "meta", attrs={"http-equiv": re.compile("content-type", re.I)}
            )
            if meta_content_type_tag and meta_content_type_tag.get("content"):
                content_value = meta_content_type_tag["content"]
                match = re.search(r"charset=([\w.-]+)", content_value, re.I)
                if match:
                    found_encoding = match.group(1).strip().lower()
                    logger.debug(
                        f"透過 meta http-equiv content-type 找到編碼: {found_encoding}"
                    )
                    return found_encoding

            logger.debug("在 meta 標籤中未找到明確的 charset。")
            return "utf-8"
        except Exception as e:
            logger.warning(
                f"解析 HTML meta 標籤以偵測 charset 時發生錯誤: {e}。返回預設 utf-8。"
            )
            return "utf-8"

    # --- END MODIFICATION ---

    def _selenium_wait_for_element(
        self, by_method_str: str, value: str, timeout: float = 10
    ):  # <<< MODIFIED: Parameter name and type hints
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support import expected_conditions as EC

        by_map = {
            "id": By.ID,
            "name": By.NAME,
            "xpath": By.XPATH,
            "link text": By.LINK_TEXT,
            "partial link text": By.PARTIAL_LINK_TEXT,
            "tag name": By.TAG_NAME,
            "class name": By.CLASS_NAME,
            "css selector": By.CSS_SELECTOR,
        }
        if by_method_str not in by_map:
            raise ValueError(f"不支援的查找方式: {by_method_str}")

        if not self._driver:
            logger.error(
                "Selenium driver 尚未初始化 (在 _selenium_wait_for_element 中檢查)。"
            )
            # --- MODIFIED: Raise a more specific or descriptive error ---
            raise ReferenceError(
                "Selenium driver not initialized before calling _selenium_wait_for_element."
            )
            # --- END MODIFICATION ---

        WebDriverWait(self._driver, timeout).until(
            EC.presence_of_element_located((by_map[by_method_str], value))
        )

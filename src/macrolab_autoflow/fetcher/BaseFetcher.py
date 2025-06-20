from abc import ABC, abstractmethod
from typing import Generic, TypeVar
from macrolab_autoflow.common.Result import Result

T = TypeVar("T")
U = TypeVar("U")


class BaseFetcher(ABC, Generic[T, U]):
    """
    BaseFetcher 抽象類別，定義了「Fetch → Parse → Run」的流程骨架 (Template Method)。

    流程：
      1. run(): 外部呼叫，各子類都從這裡開始 →
      2. fetch(): 取得「原始資料」(HTML, JSON, binary...) →
      3. parse(): 解析「原始資料」成標準化的資料結構 →
      4. 回傳 Result.ok 或 Result.fail

    泛型參數：
      T: fetch() 回傳的 raw 資料類型 (e.g. BeautifulSoup, dict, bytes)
      U: parse() 回傳的標準化資料結構 (e.g. list[dict])

    子類別只需實作 fetch() 與 parse()，run() 的流程就能自動串起來。
    """

    def run(self) -> Result[U]:
        """
        流程骨架 (Template Method)：
        1. 先呼叫 fetch()
        2. 若失敗 (is_failure)，則直接回傳錯誤 Result
        3. 若成功，呼叫 parse(raw)，並將 parse() 的結果回傳
        """
        raw_res: Result[T] = self.fetch()
        if raw_res.is_failure():
            return Result.fail(raw_res.error_msg or "fetch 失敗", code=raw_res.code)
        # parse 可能也回傳 Result<U>
        return self.parse(raw_res.value)  # type: ignore

    @abstractmethod
    def fetch(self) -> Result[T]:
        """
        抓取原始資料
        回傳 Result.ok(raw) 或 Result.fail(msg)
        例：HTML 字串、BeautifulSoup、API JSON dict、檔案 bytes…
        """

    @abstractmethod
    def parse(self, raw: T) -> Result[U]:
        """
        解析原始資料，轉成統一的結構
        回傳 Result.ok(parsed) 或 Result.fail(msg)
        例：list[dict]：每篇文章的 title/url/published_at…
        """

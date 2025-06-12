from __future__ import annotations
import logging
from datetime import datetime
from dataclasses import replace
from typing import Optional, List

from notion_client import Client, APIResponseError

from macrolab_autoflow.common.Result import Result
from macrolab_autoflow.repository.entities import Article
from macrolab_autoflow.repository.Abstract_Article_Repository import AbstractArticleRepository

logger = logging.getLogger(__name__)


class NotionArticleRepository(AbstractArticleRepository):
    """
    以 Notion Database 作為儲存後端的 Article Repository 實作。
    需在環境變數中設置：
        NOTION_API_TOKEN   - 整合 token
        NOTION_DATABASE_ID - 文章資料庫 ID
    """

    # 建議以環境變數或 DI 容器注入；此處為簡化直接讀 env
    def __init__(
        self,
        api_token: str,
        database_id: str,
        property_map: dict[str, str] | None = None,   # ← NEW
    ) -> None:
        self._client = Client(auth=api_token)
        self._db_id = database_id

        # 預設對應（左＝程式用的標準名稱，右＝實際 Notion 欄位名）
        default_map = {
            "title": "Name",          # 你資料庫現在的標題欄位
            "url": "URL",            # 假設你要塞進 Text 欄
            "category": "Category",
            "published_at": "PublishedAt",  # 只是示範，請換成你的 Date 欄位
            "content": "Content",        # 也可以另外加一個 Rich-text 欄
        }
        if property_map:
            default_map.update(property_map)  # 讓呼叫端可以覆寫
        self._prop = default_map

    # -------- interface -------- #

    def add(self, article: Article) -> Result[Article]:
        """Create & return Article（含 Notion 內部 id）"""
        try:
            page = self._client.pages.create(parent={"database_id": self._db_id},
                                             properties=self._article_to_properties(article))
            # saved = article.__class__(**{**article.__dict__, "id": page["id"]})
            saved = replace(article, id=page["id"])
            logger.info("文章已寫入 Notion：%s", article.title)
            return Result.ok(saved)
        except APIResponseError as e:
            logger.error("寫入 Notion 失敗：%s", e, exc_info=True)
            return Result.fail(str(e), code=e.status)

    def get_by_url(self, url: str) -> Result[Optional[Article]]:
        try:
            query = self._client.databases.query(
                **{
                    "database_id": self._db_id,
                    "filter": {"property": "URL", "url": {"equals": url}},
                    "page_size": 1,
                }
            )
            results = query.get("results", [])
            if not results:
                return Result.ok(None)
            return Result.ok(self._page_to_article(results[0]))
        except APIResponseError as e:
            logger.error("查詢 Notion 失敗：%s", e, exc_info=True)
            return Result.fail(str(e), code=e.status)

    def list_recent(self, limit: int = 20) -> Result[List[Article]]:
        try:
            query = self._client.databases.query(
                **{
                    "database_id": self._db_id,
                    "sorts": [{"property": "PublishedAt", "direction": "descending"}],
                    "page_size": limit,
                }
            )
            articles = [self._page_to_article(pg) for pg in query.get("results", [])]
            return Result.ok(articles)
        except APIResponseError as e:
            logger.error("列出 Notion 文章失敗：%s", e, exc_info=True)
            return Result.fail(str(e), code=e.status)

    # --------- helpers --------- #

    # @staticmethod
    # def _article_to_properties(article: Article) -> dict:
    def _article_to_properties(self, article: Article) -> dict:
        """ 將 Article 轉為 Notion Database properties 結構（範例欄位：可自行修改）
            依照 self._prop 產生 Notion properties """        
        # # published_at 可能是 datetime，也可能是字串
        # if article.published_at:
        #     if isinstance(article.published_at, datetime):
        #         published_at_iso = article.published_at.isoformat()
        #     else:  # 字串：直接塞進去（TechCrunch <time> 已是 ISO-8601）
        #         published_at_iso = str(article.published_at)
        # else:
        #     published_at_iso = None

        # return {
        #     "Title":       {"title": [{"text": {"content": article.title}}]},
        #     "URL":         {"url": article.url},
        #     "Category":    {"rich_text": [{"text": {"content": article.category or ""}}]},
        #     "PublishedAt": {"date": {"start": published_at_iso}},
        #     "Content":     {
        #         "rich_text": [
        #             {
        #                 "text": {
        #                     "content": (article.content or "")[:2000]  # Notion 單欄位 2 kB 上限
        #                 }
        #             }
        #         ]
        #     },
        # }

        # 1) PublishedAt 轉 ISO-8601 / 或 None
        if article.published_at:
            if isinstance(article.published_at, datetime):
                pub_iso = article.published_at.isoformat()
            else:
                pub_iso = str(article.published_at)
        else:
            pub_iso = None

        # 2) 組 properties，用 self._prop 把程式欄位 → Notion 欄位
        return {
            self._prop["title"]: {"title": [{"text": {"content": article.title}}]},
            self._prop["url"]:   {"url": article.url},
            # --------- 這裡換成 multi_select --------- ↩️
            self._prop["category"]: {
                "multi_select": [{"name": article.category}] if article.category else []
            },
            self._prop["published_at"]: {"date": {"start": pub_iso}},
            self._prop["content"]: {
                "rich_text": [{"text": {"content": (article.content or '')[:2000]}}],
            },
        }



    # @staticmethod
    # def _page_to_article(page: dict) -> Article:
    #     prop = page["properties"]
    #     get_text = lambda p, key: "".join(t.get("plain_text", "") for t in p[key].get("title" if key=="Title" else "rich_text", []))
    #     dt = prop["PublishedAt"]["date"]["start"] if prop["PublishedAt"]["date"] else None
    #     return Article(
    #         id=page["id"],
    #         title=get_text(prop, "Title"),
    #         url=prop["URL"].get("url"),
    #         category=get_text(prop, "Category"),
    #         content=get_text(prop, "Content"),
    #         published_at=datetime.fromisoformat(dt) if dt else None,
    #     )

    def _page_to_article(self, page: dict) -> Article:
        p = page["properties"]

        # 用 self._prop 取得實際欄位名 ─┐
        dt_prop = p[self._prop["published_at"]]["date"]
                                                  # ─┘
        def get(alias: str) -> str:
            notion_key = self._prop[alias]
            src = p[notion_key]
            if notion_key == self._prop["title"]:
                pieces = src["title"]
                return "".join(t["plain_text"] for t in pieces)
            elif src.get("rich_text") is not None:
                return "".join(t["plain_text"] for t in src["rich_text"])
            elif src.get("multi_select") is not None:          # ←↩️ 新增
                return ", ".join(o["name"] for o in src["multi_select"])
            else:
                return ""

        return Article(
            id=page["id"],
            title=get("title"),
            url=p[self._prop["url"]].get("url"),
            category=get("category"),
            content=get("content"),
            published_at=(
                datetime.fromisoformat(dt_prop["start"])
                if dt_prop and dt_prop["start"] else None
            ),
        )



    # -------- health check -------- #
    def health_check(self) -> Result[dict]:
        """
        驗證 token / database 是否可用。
        成功時回傳 Database meta (dict)。
        """
        try:
            meta = self._client.databases.retrieve(database_id=self._db_id)
            return Result.ok(meta)
        except APIResponseError as e:
            return Result.fail(str(e), code=e.status)
#!/usr/bin/env python
"""
抓取 TechCrunch 最新文章 → 寫入 Notion Database
直接執行：
    poetry run python src/run_ingest_to_notion.py
或透過 PythonRun.sh：
    bash PythonRun.sh  # 選 2 再選本檔
"""
from __future__ import annotations

# ---------- 1) Imports ----------
import logging
import os
from datetime import datetime, timezone
from typing import List

from dotenv import load_dotenv

from macrolab_autoflow.common.Result import Result
from macrolab_autoflow.repository.Notion_Article_Repository import (
    NotionArticleRepository,
)
from macrolab_autoflow.repository.entities import Article
from macrolab_autoflow.service.Article_To_Repository import ArticleIngestService


# ---------- 2) 輔助函式 ----------
def write_unique(
    service: ArticleIngestService,
    articles_raw: List[dict],
) -> Result:
    """
    只把「資料庫裡還找不到相同 URL」的文章寫入。
    """
    uniques: list[Article] = []
    for a in articles_raw:
        already = service._repo.get_by_url(a["url"]).value  # type: ignore[protected-access]
        if already is None:
            uniques.append(Article(**a))

    if not uniques:
        return Result.ok([])

    return service._repo.add_many(uniques)  # type: ignore[protected-access]


# ---------- 3) 主流程 ----------
def main() -> None:
    # -- 3-1. 設定 log --
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
    )

    # -- 3-2. 讀環境變數 --
    load_dotenv()
    token = os.getenv("NOTION_API_KEY") or os.getenv("NOTION_API_TOKEN")
    db_id = os.getenv("NOTION_DATABASE_ID")
    if not (token and db_id):
        raise RuntimeError("❌  必須先設定 NOTION_API_KEY / NOTION_DATABASE_ID")

    # -- 3-3. Notion 欄位對照表 (依你的資料庫欄位名稱修改) --
    FIELD_MAP = {
        "title": "Name",
        "url": "URL",
        "category": "Category",
        "published_at": "PublishedAt",
        "content": "Content",
    }

    # -- 3-4. 建立 Repository 與 Service --
    repo = NotionArticleRepository(token, db_id, property_map=FIELD_MAP)
    service = ArticleIngestService(repo)

    # ---------- 1) 先抓 TechCrunch ----------
    fetch_res: Result = service._fetcher.run()  # type: ignore[protected-access]
    if fetch_res.is_failure():
        logging.error("抓取失敗：%s", fetch_res.error_msg)
        return

    # ---------- 2) 只寫入新文章 ----------
    res: Result = write_unique(service, fetch_res.value)

    if res.is_failure():
        logging.error("❌  寫入失敗：%s", res.error_msg)
        if res.traceback_str:
            logging.error("\n%s", res.traceback_str)
        return

    articles = res.value
    logging.info("🎉 已成功寫入 %d 篇文章至 Notion", len(articles))
    for art in articles:
        print(f" • {art.title}  ({art.url})")


if __name__ == "__main__":
    main()

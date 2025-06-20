#!/usr/bin/env python
"""Notion 連線 + 寫入 健康檢查"""
import os, logging, pprint, uuid, traceback
from datetime import datetime, timezone

from dotenv import load_dotenv

from macrolab_autoflow.repository.Notion_Article_Repository import (
    NotionArticleRepository,
)
from macrolab_autoflow.repository.entities import Article  # ← NEW

# ---------- 基本設定 ----------
logging.basicConfig(level=logging.INFO, format="%(levelname)s - %(message)s")
load_dotenv()

token = os.getenv("NOTION_API_KEY") or os.getenv("NOTION_API_TOKEN")
db_id = os.getenv("NOTION_DATABASE_ID")
if not (token and db_id):
    raise SystemExit("❌  尚未設定 NOTION_API_KEY / NOTION_DATABASE_ID")

repo = NotionArticleRepository(token, db_id)

# ---------- 1) 讀取健康檢查 ----------
meta_res = repo.health_check()
if meta_res.is_failure():
    logging.error("❌  讀取連線失敗：%s", meta_res.error_msg)
    quit(1)

meta = meta_res.value
title = meta["title"][0]["plain_text"] if meta["title"] else "(未命名)"
logging.info("✅  讀取 OK！Database title = %s", title)
print("\n前 10 個 property：")
pprint.pprint(list(meta["properties"].keys())[:10])

# ---------- 2) 寫入健康檢查 ----------
dummy_article = Article(
    title=f"Health-Check Ping {uuid.uuid4()}",
    url="https://techcrunch.com/",
    category="HealthCheck",
    published_at=datetime.now(timezone.utc),
    content="This page was created by an automated health-check script "
    "and archived immediately.",
)

add_res = repo.add(dummy_article)
if add_res.is_success():
    page_id = add_res.value.id
    print("✅  寫入 OK！Page id = %s", page_id)
    logging.info("✅  寫入 OK！Page id = %s", page_id)

    # ------- 3) 清理：立即封存該 Page -------
    try:
        repo._client.pages.update(page_id=page_id, archived=True)
        print("🗑️  測試頁面已標記為 archived=True")
        logging.info("🗑️  測試頁面已標記為 archived=True")
    except Exception as err:  # 保險起見不要讓腳本因清理失敗而報錯
        print("⚠️  無法封存測試頁面（不影響寫入測試結果）：%s", err)
        logging.warning("⚠️  無法封存測試頁面（不影響寫入測試結果）：%s", err)

else:
    print("❌  寫入失敗：%s", add_res.error_msg)
    logging.error("❌  寫入失敗：%s", add_res.error_msg)
    if add_res.traceback_str:
        print("\n詳細 Traceback ↓↓↓\n")
        print(add_res.traceback_str)

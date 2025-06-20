from __future__ import annotations
from dataclasses import dataclass
from datetime import datetime
from typing import Optional

@dataclass(slots=True, frozen=True)
class Article:
    """領域模型：文章內容與中繼資料"""
    title: str
    url: str
    category: Optional[str] = None
    published_at: Optional[datetime] = None
    content: Optional[str] = None
    id: Optional[str] = None           # 儲存端（Notion / SQL …）的主鍵

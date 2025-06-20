from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Iterable, Protocol, runtime_checkable, Optional

from macrolab_autoflow.repository.entities import Article
from macrolab_autoflow.common.Result import Result


@runtime_checkable
class SupportsArticleRepository(Protocol):
    """Python structural typing – 只要長得像，就能注入"""
    def add(self, article: Article) -> Result[Article]: ...
    def get_by_url(self, url: str) -> Result[Optional[Article]]: ...
    def list_recent(self, limit: int = 20) -> Result[list[Article]]: ...


class AbstractArticleRepository(ABC, SupportsArticleRepository):
    """Repository 對外介面；實作可用 Notion / SQL / Airtable 等"""

    @abstractmethod
    def add(self, article: Article) -> Result[Article]: ...

    @abstractmethod
    def get_by_url(self, url: str) -> Result[Optional[Article]]: ...

    @abstractmethod
    def list_recent(self, limit: int = 20) -> Result[list[Article]]: ...

    # （可選）批量操作
    def add_many(self, articles: Iterable[Article]) -> Result[list[Article]]:
        persisted: list[Article] = []
        for art in articles:
            res = self.add(art)
            if res.is_failure():
                return res
            persisted.append(res.value)
        return Result.ok(persisted)

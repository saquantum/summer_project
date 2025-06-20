from macrolab_autoflow.fetcher.TechCrunchFetcher import TechCrunchFetcher
from macrolab_autoflow.repository.Abstract_Article_Repository import (
    SupportsArticleRepository,
)
from macrolab_autoflow.repository.entities import Article
from macrolab_autoflow.common.Result import Result


class ArticleIngestService:
    """應用層：抓取 TechCrunch 最新文章 → 儲存至指定 Repository"""

    def __init__(self, repo: SupportsArticleRepository) -> None:
        self._repo = repo
        self._fetcher = TechCrunchFetcher(use_selenium=False)

    def ingest_today(self) -> Result[list[Article]]:
        fetch_res = self._fetcher.run()
        if fetch_res.is_failure():
            return Result.fail(fetch_res.error_msg)

        articles_raw = fetch_res.value  # List[dict]
        entities = [Article(**a) for a in articles_raw]

        return self._repo.add_many(entities)

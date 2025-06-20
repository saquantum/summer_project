from macrolabAUTOFLOW.src.macrolab_autoflow.repository.Abstract_Article_Repository import (
    AbstractArticleRepository,
)
from macrolab_autoflow.common.Result import Result
from macrolab_autoflow.repository.entities import Article


class SQLArticleRepository(AbstractArticleRepository):
    """待實作：使用 SQLAlchemy 連接 Postgres / MySQL …"""

    def __init__(self, connection_string: str) -> None: ...

    def add(self, article: Article) -> Result[Article]: ...

    def get_by_url(self, url: str) -> Result[Article | None]: ...

    def list_recent(self, limit: int = 20) -> Result[list[Article]]: ...

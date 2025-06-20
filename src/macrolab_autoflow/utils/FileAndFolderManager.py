import logging
from pathlib import Path

logger = logging.getLogger(__name__)


class FileAndFolderManager:
    @staticmethod
    def create_folder(folder_name: str) -> str:
        """
        在當前資料夾中建立一個新資料夾。
        如果指定的資料夾已存在，則不會再重新建立。

        參數:
            folder_name: 要建立的資料夾名稱（例如 "BalanceSheet"）。

        返回:
            該資料夾的絕對路徑。
        """
        folder_path = Path(folder_name)
        if not folder_path.exists():
            folder_path.mkdir(parents=True, exist_ok=True)
            logger.info(f"已建立資料夾: {folder_path}")
        else:
            logger.info(f"資料夾已存在: {folder_path}")

        return str(folder_path.resolve())

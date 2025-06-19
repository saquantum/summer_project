from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict

from Weather_Location_API_Data.common.Result import Result

class JSONReader:
    def __init__(self, path: Path) -> None:
        '''
        __init__：接收一個 Path，儲存為 self.path，代表要讀的 JSON 檔案位置。
        '''
        self.path = path

    def load(self) -> Result[Dict[str, Any]]:
        try:
            data = json.loads(self.path.read_text(encoding="utf-8"))
            return Result.ok(data)
        except (FileNotFoundError, json.JSONDecodeError) as err:  # noqa: B904
            return Result.fail(f"[jsonReader.py] 讀取 / 解析 JSON 失敗: {err}")
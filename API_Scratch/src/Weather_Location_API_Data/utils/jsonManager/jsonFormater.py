from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict

from Weather_Location_API_Data.common.Result import Result

class JSONFormatter:
    def __init__(self, indent: int = 2) -> None:
        '''
        __init__：接受一個 indent（縮排大小），預設為 2。
        '''
        self.indent = indent

    def dump(self, data: Dict[str, Any], path: Path) -> Result[None]:
        '''
        1. 呼叫 json.dumps 產生漂亮排版的 JSON 字串，ensure_ascii=False 保留中文文字；
        2. 用 path.write_text 寫回檔案。
        '''
        try:
            path.write_text(
                json.dumps(data, ensure_ascii=False, indent=self.indent),
                encoding="utf-8",
            )
            return Result.ok(None)
        except Exception as err:
            return Result.fail(f"[jsonFormater.py] 寫入檔案失敗: {err}")


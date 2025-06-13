#!/usr/bin/env python3
"""jsonReader: 讀取 NSWWS_SAMPLE_DATA_WFS.json，處理後輸出 processed_data.json"""
from __future__ import annotations

import json
import sys
from pathlib import Path

def main():
    if len(sys.argv) != 3:
        print("Usage: pretty_json.py <input.json> <output.json>")
        sys.exit(1)

    inp = Path(sys.argv[1])
    out = Path(sys.argv[2])

    # 1) 讀取原始 JSON
    data = json.loads(inp.read_text(encoding="utf-8"))

    # 2) 寫入漂亮縮排的 JSON
    out.write_text(
        json.dumps(data, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(f"✅ 已將 {inp} 轉成帶縮排的 {out}")


if __name__ == '__main__':
    main()

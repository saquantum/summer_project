#!/usr/bin/env python3
"""jsonReader: 讀取 NSWWS_SAMPLE_DATA_WFS.json，處理後輸出 processed_data.json"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any, Dict, List


def load_json(path: Path) -> dict:
    """讀取並回傳 JSON 資料"""
    with path.open('r', encoding='utf-8') as f:
        return json.load(f)


def process_data(data: Dict[str, Any]) -> List[Dict[str, Any]]:
    """
    """
    return data.get("features", [])


def main() -> None:
    parser = argparse.ArgumentParser(description="讀取並處理 NSWWS WFS JSON 檔案")
    parser.add_argument(
        "-i",
        "--input",
        type=Path,
        default=Path(__file__).with_name("NSWWS_SAMPLE_DATA_WFS.json"),
        help="輸入 JSON 檔案路徑（預設：與本檔同資料夾）",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        default=Path(__file__).with_name("processed_data.json"),
        help="輸出 JSON 檔案路徑（預設：processed_data.json）",
    )
    args = parser.parse_args()

    # 1) 讀檔
    raw = load_json(args.input)

    # 2) 整理
    # processed = process_data(raw)
    output_data = raw

    # 3) 輸出
    # with args.output.open("w", encoding="utf-8") as f:
    #     json.dump(processed, f, ensure_ascii=False, indent=2)

    # print(f"✅ 已處理 {len(processed)} 筆警報，存成 {args.output}")

    with args.output.open("w", encoding="utf-8") as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    # 如果你仍然想顯示筆數，可以這樣：
    features = raw.get("features", [])
    print(f"✅ 已輸出完整 JSON（{len(features)} 筆 features）到 {args.output}")


if __name__ == '__main__':
    main()

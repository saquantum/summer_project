#!/usr/bin/env python3
"""
jsonFormatVerifier.py
---------------------
比對 NSWWS WFS 來源的 JSON 與「標準格式」是否一致。

用法
----
# 只驗證必備結構
python jsonFormatVerifier.py --input metoffice_raw.json      

# 指定一個官方範本 (完整 JSON) 做嚴格比對
python jsonFormatVerifier.py \
        --input  metoffice_raw.json \
        --schema NSWWS_SAMPLE_DATA_WFS.json
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Set

# ──────────────────────────────────────────────────────────────────────────────
# 一些工具
# ──────────────────────────────────────────────────────────────────────────────
TOP_LEVEL_KEYS: Set[str] = {
    "objectIdFieldName",
    "uniqueIdField",
    "globalIdFieldName",
    "geometryType",
    "spatialReference",
    "fields",
    "features",
}

def load_json(path: Path) -> Dict[str, Any]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        sys.exit(f"❌ 找不到檔案: {path}")
    except json.JSONDecodeError as e:
        sys.exit(f"❌ 解析 JSON 失敗: {path} - {e}")

# ──────────────────────────────────────────────────────────────────────────────
# 核心驗證邏輯
# ──────────────────────────────────────────────────────────────────────────────
def verify_basic_structure(data: Dict[str, Any]) -> List[str]:
    """檢查頂層必備 key 與基本型別"""
    errs: List[str] = []

    missing = TOP_LEVEL_KEYS - data.keys()
    if missing:
        errs.append(f"缺少頂層欄位: {', '.join(sorted(missing))}")

    if not isinstance(data.get("fields"), list):
        errs.append("fields 應為 list")

    if not isinstance(data.get("features"), list):
        errs.append("features 應為 list")

    return errs

def verify_with_schema(data: Dict[str, Any], schema: Dict[str, Any]) -> List[str]:
    """
    嚴格模式：把 schema 的所有欄位/型別拿來對照，
    只做「淺層」(fields 與 features.attributes) 的比對，
    以維持腳本簡潔；若要更深入可改用 jsonschema 套件。
    """
    errs: List[str] = []

    # 1) 頂層鍵值
    for key in schema.keys():
        if key not in data:
            errs.append(f"頂層缺少欄位: {key}")

    # 2) fields 內容
    schema_fields = {f["name"]: f["type"] for f in schema.get("fields", [])}
    data_fields   = {f["name"]: f.get("type") for f in data.get("fields", [])}

    missing_fields = schema_fields.keys() - data_fields.keys()
    if missing_fields:
        errs.append(f"fields 缺少定義: {', '.join(sorted(missing_fields))}")

    # 3) features.attributes 必備欄位
    if data.get("features"):
        required_attr = set(schema_fields.keys())
        for idx, feat in enumerate(data["features"], start=1):
            attrs = feat.get("attributes", {})
            lack  = required_attr - attrs.keys()
            if lack:
                errs.append(f"features[{idx-1}].attributes 缺少: {', '.join(sorted(lack))}")
                # 只列第一筆缺失就好
                break

    return errs

# ──────────────────────────────────────────────────────────────────────────────
# CLI 入口
# ──────────────────────────────────────────────────────────────────────────────
def main() -> None:
    p = argparse.ArgumentParser(
        description="驗證爬取的 NSWWS WFS JSON 是否符合標準格式"
    )
    p.add_argument("-i", "--input", type=Path, required=True,
                   help="待驗證的 JSON 檔")
    p.add_argument("-s", "--schema", type=Path,
                   help="官方範本 JSON (若給定則做嚴格比對)")
    args = p.parse_args()

    data = load_json(args.input)

    errors = verify_basic_structure(data)

    # 若有提供 schema，再做更嚴格的比對
    if args.schema:
        schema = load_json(args.schema)
        errors.extend(verify_with_schema(data, schema))

    # ───── 結果輸出 ─────
    if errors:
        print("❌ 格式驗證失敗：")
        for e in errors:
            print("  -", e)
        sys.exit(1)
    else:
        feats = len(data.get("features", []))
        print(f"✅ 格式驗證通過！(features 數量：{feats})")

if __name__ == "__main__":
    main()

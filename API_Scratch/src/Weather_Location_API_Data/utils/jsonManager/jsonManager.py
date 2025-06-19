from __future__ import annotations
# 延遲求值型別註解，讓所有註解於執行時當字串，不會因環狀引用或尚未定義而報錯。

from pathlib import Path

from Weather_Location_API_Data.common.Result import Result
from Weather_Location_API_Data.utils.jsonManager.jsonReader import JSONReader
from Weather_Location_API_Data.utils.jsonManager.jsonFormater import JSONFormatter
from Weather_Location_API_Data.utils.jsonManager.jsonFormatVerifier import JSONFormatVerifier

# ────────────────────────────────────────────────────────────────
#     json 高階組合器
# ────────────────────────────────────────────────────────────────
class JSONManager:
    """JSON 讀取 → 以 schema 驗證 → 格式化輸出（全程使用 Result 封裝）"""
    def __init__(
        self,
        reader: JSONReader,  # 讀檔
        formatter: JSONFormatter,  # 輸出
        verifier: JSONFormatVerifier,  # 驗證
    ) -> None:
        self.reader = reader
        self.formatter = formatter
        self.verifier = verifier

    def process(self, output_path: Path) -> Result[Path]:
        '''
        讀 → 驗 → 寫三步驟，每一步都檢查 Result.is_failure()
        若失敗就回傳 fail，並帶上對應的錯誤訊息；全都成功才回傳 ok(output_path)
        '''
        # 1) 讀檔
        read_res = self.reader.load()
        if read_res.is_failure():
            return Result.fail(read_res.error_msg)

        data = read_res.value

        # 2) 驗證
        ver_res = self.verifier.verify(data)
        if ver_res.is_failure():
            return Result.fail(ver_res.error_msg)

        # 3) 輸出
        dump_res = self.formatter.dump(data, output_path)
        if dump_res.is_failure():
            return Result.fail(dump_res.error_msg)

        # 全部成功，回傳 output_path
        return Result.ok(output_path)


# ──────────────────────────────────────────────────────────────────
#     CLI – python -m ...json_manager -i in.json -s schema.json
# ──────────────────────────────────────────────────────────────────
def _cli() -> None:
    import argparse
    import sys

    p = argparse.ArgumentParser(description="JSON 讀取 → 驗證 → 格式化（Result 版）")
    
    # 要讀取的 JSON 檔
    p.add_argument("-i", "--input", type=Path, required=True, help="輸入 JSON")
    # 用來驗證的 schema JSON
    p.add_argument("-s", "--schema", type=Path, required=True, help="用來驗證的 schema JSON")
    # 輸出檔案，預設在原檔名後面加 _pretty
    p.add_argument("-o", "--output", type=Path, help="輸出路徑，預設 <input>_pretty.json")
    # 格式化縮排：2 格
    p.add_argument("--indent", type=int, default=2, help="縮排（default 2）")

    # 解析命令列引數
    # 讀取 sys.argv，把使用者輸入的選項轉成 args 物件，其中包含屬性 args.input, args.schema, args.output, args.indent
    args = p.parse_args()

    reader = JSONReader(args.input)
    formatter = JSONFormatter(indent=args.indent)
    verifier = JSONFormatVerifier(schema_path=args.schema)
    manager = JSONManager(reader, formatter, verifier)

    out_path = args.output or args.input.with_stem(f"{args.input.stem}_pretty")
    res = manager.process(out_path)

    if res.is_success():
        feats = reader.load().value.get("features", [])
        print(f"✅ 驗證通過！features = {len(feats)}，已輸出 {res.value}")
    else:
        print(f"❌ {res.error_msg}")
        sys.exit(1)  # 以 sys.exit(1) 帶非零狀態結束程式，讓外部知道執行不成功

if __name__ == "__main__":
    _cli()



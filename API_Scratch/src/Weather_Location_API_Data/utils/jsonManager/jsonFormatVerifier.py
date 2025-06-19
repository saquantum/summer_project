from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict, List, Set

from Weather_Location_API_Data.common.Result import Result
from Weather_Location_API_Data.utils.jsonManager.jsonReader import JSONReader

class JSONFormatVerifier:
    def __init__(self, schema_path: Path) -> None:
        '''
        1. self.top_keys：schema JSON 最外層所有鍵（keys）的集合，用以檢查「頂層欄位」是否完整
        2. self.field_defs：從 schema["fields"] 列表中，把每項的 "name"→"type" 存成 dict，供後續比對欄位名稱與型別
        3. self.attr_keys：取 field_defs 的鍵集合，代表每個 feature 裡 attributes 欄位應該要有哪些子欄位
        '''
        schema_res = JSONReader(schema_path).load()  # 回傳物件形態為 Result
        
        # 從 JSONReader(schema_path).load() 拿到 Result，若為 fail，拋出 ValueError 中斷
        if schema_res.is_failure():
            # 無法讀 schema 直接擋下
            raise ValueError(schema_res.error_msg)  # 建立物件時就要確定 schema 可用
        self.schema: Dict[str, Any] = schema_res.value

        # 從 schema 推導出三種檢查需求
        self.top_keys: Set[str] = set(self.schema.keys())
        self.field_defs: Dict[str, str] = {
            f["name"]: f.get("type") for f in self.schema.get("fields", [])
        }
        self.attr_keys: Set[str] = set(self.field_defs.keys())

    # 對外只暴露一個 verify
    def verify(self, data: Dict[str, Any]) -> Result[None]:
        '''
        1. 呼叫三個私有方法，依序檢查「頂層結構」、「fields 列表」、「features 內容」。
        2. 收集所有錯誤訊息到 errs 清單中。
        3. 若 errs 非空，回傳 Result.fail(...)，把每條錯誤換行並加前綴，便於閱讀；否則回傳 Result.ok(None)，表示通過。

        '''
        errs: List[str] = []
        errs += self._check_top_level(data)
        errs += self._check_fields(data)
        errs += self._check_features(data)

        if errs:
            return Result.fail("[jsonFormatVerifier.verify] \n  - " + "\n  - ".join(errs))
        return Result.ok(None)

    # ─── Private Verify 私有檢查 ───
    def _check_top_level(self, data: Dict[str, Any]) -> List[str]:  # meta data，以 List[str] 儲存資料
        '''
        求差集 schema 定義的 key，即驗證集合，減去 data 實際的 key（待驗證集合）
        若有不足，回傳一條錯誤；否則回傳 null list
        '''
        lacking = self.top_keys - data.keys()  # 差級運算
        return [f"缺少頂層欄位: {', '.join(sorted(lacking))}"] if lacking else []

    def _check_fields(self, data: Dict[str, Any]) -> List[str]:  # 以 List[str] 儲存資料
        '''
        1. input 確認：fields 這個欄位存在，且型別是 list
        2. 將 data["fields"] 中每一筆 {'name':..., 'type':...} 轉換為一個映射 name → type
        3. 用集合運算（差集）運算出 shema 要的key，哪些在 data_defs（待驗證資料集合）中沒出現
        4. 再檢查那些同名的欄位，schema 期待的 type（資料型態）和 data_defs（待驗證資料集合）中給的 type 是否一致
        5. 根據「空缺欄位」，schema 預期的 type 和 data_defs 中給的 type 一致
        '''
        if "fields" not in data or not isinstance(data["fields"], list):
            return ["fields 必須存在且為 list"]

        data_defs = {f["name"]: f.get("type") for f in data["fields"]}
        lacks = self.field_defs.keys() - data_defs.keys()  # 差級運算

        typo: List[str] = []  # "typo: "：暫存「型態不符的爛位列表」，避免因為類型檢查而崩潰
        for key in self.field_defs.keys() & data_defs.keys():
            if self.field_defs[key] != data_defs[key]:
                typo.append(f"{key} (預期為： {self.field_defs[key]} ⇒ 實際為 {data_defs[key]})")

        msgs: List[str] = []
        if lacks:  # 欄位缺失
            msgs.append(f"fields 缺少定義: {', '.join(sorted(lacks))}")
        if typo:  # 資料型態不符
            msgs.append("fields 型別不符: " + ", ".join(typo))
        return msgs

    def _check_features(self, data: Dict[str, Any]) -> List[str]:
        '''
        1. 檢查 features 是否為 List
        2. 逐筆檢查，對每個 feature_item，取其 .get("attributes", {})，比較 schema 要的 attr_keys
           若有缺失，則立即回傳該 index錯誤，否則回傳 null list
        '''
        if "features" not in data or not isinstance(data["features"], list):
            return ["features 必須存在且為 list"]

        for idx, feature_item in enumerate(data["features"]):
            attrs = feature_item.get("attributes", {})
            lack = self.attr_keys - attrs.keys()  # 差集運算
            if lack:
                return [f"features[{idx}].attributes 缺少: {', '.join(sorted(lack))}"]
        return []

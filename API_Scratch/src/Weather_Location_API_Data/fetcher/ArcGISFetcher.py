from __future__ import annotations
from datetime import datetime, timezone
from typing import List, Dict

import requests

from Weather_Location_API_Data.fetcher.BaseFetcher import BaseFetcher  # 你的抽象骨架
from Weather_Location_API_Data.common.Result import Result        # 成功/失敗包裝

class MetOfficeWarningsFetcher(BaseFetcher):
    """
    抓取英國氣象局（Met Office）National Severe Weather Warning Service
    ArcGIS FeatureServer 的即時警報。

    - 來源 API：
      https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/
      Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query
    
      https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/
      Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query
      ?f=json&where=1=1&outFields=* 

    - 回傳欄位（常用）：
        • id
        • issueDate   (ISO8601)
        • expireDate
        • severity
        • description / headline
        • regions     (字串，可自行拆成 list)
    """

    BASE = (
        "https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/"
        "Met_Office_National_Severe_Weather_Warning_Service_Live/"
        "FeatureServer/0/query"
    )

    # 預設 query string（抓全部欄位）
    DEFAULT_QS = {
        "f": "json",
        "where": "1=1",         # 不篩選，取全部
        "outFields": "*",
        "returnGeometry": "true",
    }

    def __init__(self):
        # super().__init__(self.BASE)   # 給 BaseFetcher 一個 url
        super().__init__(MetOfficeWarningsFetcher.BASE)   # 給 BaseFetcher 一個 url

    # ---------- BaseFetcher 覆寫 ---------- #
    def fetch(self) -> Result:    # 型別註解仍保留
        try:
            resp = requests.get(self.url, params=self.DEFAULT_QS, timeout=10)
            resp.raise_for_status()
            return Result.ok(resp.json())
        except Exception as err:  # noqa: WPS421
            return Result.fail(f"HTTP error: {err}")

    def parse(self, raw: dict) -> List[Dict]:
        pass
    #     records: list[dict] = []
        
    #     for feat in raw.get("features", []):
    #         attr = feat["attributes"]
            
    #         print("available attributes:", list(attr.keys()))

    #         #  try a few possible field-names, then fallback to now()
    #         ms = (
    #             attr.get("issueData") 
    #             or attr.get("IssueDate") 
    #             or attr.get("issue_date")
    #             or 0
    #         )
    #         if ms:
    #             timestamp = datetime.fromtimestamp(ms/1000, tz=timezone.utc)
    #         else:
    #             timestamp = datetime.now(timezone.utc)

    #         # 轉成與 NotionUploader 相容的統一 schema
    #         records.append(
    #             {
    #                 "title": attr.get("headline") or "Met Office Warning",
    #                 "timestamp": datetime.fromtimestamp(
    #                     attr["issueDate"] / 1000, tz=timezone.utc
    #                 ),
    #                 "content": (attr.get("description") or "")[:2000],
    #                 "source": f"MetOffice-{attr.get('severity')}",
    #                 "url": (
    #                     "https://www.metoffice.gov.uk/weather/"
    #                     f"warnings-and-advice/uk-warnings#{attr.get('id')}"
    #                 ),
    #             }
    #         )
    #     return records
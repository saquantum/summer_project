from Weather_Location_API_Data.fetcher.ArcGISFetcher import MetOfficeWarningsFetcher

# --- TechCrunch 已有，這裡插入另一來源 ---
sources = [
    ("MetOffice", MetOfficeWarningsFetcher()),
]

for name, fetcher in sources:
    # logging.info(">>> [%s] Fetching …", name)
    # res = fetcher.run()
    res = fetcher.fetch()
    # print(res.value)

    import json, pathlib
    if res.is_success():
        raw_json = res.value
        print(json.dumps(raw_json, ensure_ascii=False, indent=2)[:5000])
        pathlib.Path("metoffice_raw.json").write_text(
            json.dumps(raw_json, ensure_ascii=False, indent=2)
        )


    if res.is_failure():
        print(res.error_msg)
        continue
    # records = res.value
    # logging.info("Fetched %s records", len(records))
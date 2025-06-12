from Weather_Location_API_Data.fetcher.ArcGISFetcher import MetOfficeWarningsFetcher

# --- TechCrunch 已有，這裡插入另一來源 ---
sources = [
    ("MetOffice", MetOfficeWarningsFetcher()),
]

for name, fetcher in sources:
    # logging.info(">>> [%s] Fetching …", name)
    res = fetcher.run()

    if res.is_failure():
        print(res.error_msg)
        continue
    records = res.value
    # logging.info("Fetched %s records", len(records))
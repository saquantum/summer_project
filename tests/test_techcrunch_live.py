import pytest
from macrolab_autoflow.fetcher.TechCrunchFetcher import TechCrunchFetcher


@pytest.mark.integration
def test_techcrunch_fetcher_live():
    """
    真實對 https://techcrunch.com/ 做一次抓取並解析，
    驗證 run() 流程能正確返回 5 筆文章結構。
    """
    fetcher = TechCrunchFetcher(use_selenium=False)
    res = fetcher.run()
    # 整條流程（fetch + parse）應該成功
    assert res.is_success(), f"抓取/解析失敗：{res.error_msg}"
    articles = res.value
    # 結構檢查
    assert isinstance(articles, list), "回傳結果不是 list"
    assert len(articles) == 5, f"預期 5 篇文章，實際抓到 {len(articles)} 篇"

    for idx, art in enumerate(articles, 1):
        # 每篇都必須有非空的 title、url 開頭 http，以及 datetime 欄位 (或 None)
        assert art.get("title"), f"第 {idx} 篇缺少 title"
        assert art.get("url", "").startswith(
            "http"
        ), f"第 {idx} 篇 url 有誤: {art.get('url')}"
        # published_at 可以是 None（某些文章可能無 time 標籤），但若不為 None，它至少要是一段 ISO datetime 字串
        pa = art.get("published_at")
        if pa is not None:
            assert "T" in pa, f"第 {idx} 篇 published_at 格式異常: {pa}"

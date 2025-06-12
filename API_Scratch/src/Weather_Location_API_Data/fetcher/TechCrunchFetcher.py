from typing import List, Dict
from bs4 import BeautifulSoup
import datetime 
import logging
from urllib.parse import urljoin 

from macrolab_autoflow.common.Result import Result
from macrolab_autoflow.fetcher.DataFetcherManager import DataFetcherManager
from macrolab_autoflow.fetcher.BaseFetcher import BaseFetcher

logger = logging.getLogger(__name__)

class TechCrunchFetcher(BaseFetcher[BeautifulSoup, List[Dict]]):
    """
    TechCrunchFetcher：從 https://techcrunch.com/ 抓取最新文章
    實現 fetch() 與 parse() 方法，並透過 BaseFetcher.run() 自動串接流程。
    """

    def __init__(self, use_selenium: bool = False):
        self._manager = DataFetcherManager(
            url="https://techcrunch.com/",
            use_selenium=use_selenium
        )

    def fetch(self) -> Result[BeautifulSoup]:
        return self._manager.fetcher()

    def parse(self, raw: BeautifulSoup) -> Result[List[Dict]]:
        """
        從 BeautifulSoup 中抽取文章的標題、URL、發佈時間、分類以及主要內容。
        回傳 List[Dict]
        """
        try:
            logger.info("開始解析 TechCrunch 主頁面 HTML")

            article_elements_selectors = [
                "article.post-block",                # 舊版 TC 或其他網站常見模式
                "div.post-block",                    # 同上
                ".river-byline--story",             # TechCrunch "River" (最新文章流) 中的文章項 (猜測)
                "div.wp-block-tc-post-card",         # WordPress 區塊編輯器文章卡片 (基於您提供的 HTML)
                "li.wp-block-post",                  # WordPress 查詢迴圈中的列表項 (基於您提供的 HTML)
                "div.content-timeline__item",      # 另一種時間軸列表項的可能
                "article"                            # 最通用的文章標籤
            ]
            article_elements = []
            for selector in article_elements_selectors:
                found_elements = raw.select(selector)
                # 過濾掉空的或不包含標題連結的元素，以提高 article_elements 的質量
                for el in found_elements:
                    # 檢查是否有看起來像標題連結的元素，避免選中空的或廣告區塊
                    if el.select_one("h2 a, h3 a, a.post-block__title__link, a.wp-block-post-title__link"):
                        article_elements.append(el)
                
                if article_elements:
                    logger.info(f"在主頁使用 selector '{selector}' 初步找到了 {len(article_elements)} 個潛在文章區塊。")
                    break 
            
            if not article_elements:
                 article_elements = raw.select("div.loop-card--post-type-post") # 根據您提供的 HTML，這個可能更準確
                 if article_elements:
                     logger.info(f"使用 selector 'div.loop-card--post-type-post' 找到了 {len(article_elements)} 個潛在文章區塊。")
            
            if not article_elements:
                 article_elements = raw.select("article") # 最終 fallback
                 if article_elements:
                     logger.info(f"使用通用 selector 'article' 找到了 {len(article_elements)} 個潛在文章區塊。")


            article_elements = article_elements[:5] # 只處理前5篇

            if not article_elements:
                logger.error("解析失敗：在主頁面找不到任何可識別的文章區塊。請檢查主頁面文章列表的 CSS selector。")
                return Result.fail("解析失敗：找不到任何文章區塊，請檢查主頁面文章列表的 CSS selector 是否更新")

            articles: List[Dict] = []
            for idx, article_element in enumerate(article_elements):
                logger.debug(f"正在處理第 {idx+1}/{len(article_elements)} 個主頁文章區塊...")
                title = "N/A"
                url = None
                published_at = None
                content = "N/A"
                # +++ MODIFICATION: Initialize category +++
                category = "N/A" 
                # +++ END MODIFICATION +++

                title_link_el_selectors = [
                    "h2.post-block__title > a.post-block__title__link",
                    "h3.post-block__title > a.post-block__title__link",
                    "a.post-block__title__link",
                    "h3.loop-card__title > a.loop-card__title-link", # 從您提供的 HTML 分析
                    "h2.wp-block-post-title > a", # WordPress
                    ".entry-title a",
                    "h2 a", "h3 a", "h4 a" 
                ]
                title_link_el = None
                for sel in title_link_el_selectors:
                    title_link_el = article_element.select_one(sel)
                    if title_link_el:
                        logger.debug(f"標題連結使用 selector '{sel}' 找到。")
                        break
                
                if not title_link_el: 
                    potential_links = article_element.find_all("a", href=True)
                    for link_idx, link in enumerate(potential_links):
                        href = link.get('href', '')
                        # 判斷是否是 TechCrunch 文章連結，且不是分類/作者頁，也不是圖片主機
                        is_article_link = href.startswith("https://techcrunch.com/") and \
                                          "/category/" not in href and \
                                          "/author/" not in href and \
                                          "/tag/" not in href and \
                                          "tcrpmg.wordpress.com" not in href and \
                                          not href.endswith((".jpg", ".png", ".gif", ".jpeg", ".svg", ".webp"))
                        
                        if is_article_link:
                           link_text = link.get_text(strip=True)
                           # 標題通常較長，且連結本身不應是作者/分類區塊的一部分
                           if len(link_text) > 15 and not link.find_parent(["div", "span"], class_=lambda x: x and "byline" in x or "cat" in x or "meta" in x):
                               title_link_el = link
                               logger.debug(f"標題連結透過 fallback 邏輯找到 (第 {link_idx+1} 個潛在連結): {link_text}")
                               break
                
                if title_link_el:
                    title = title_link_el.get_text(strip=True)
                    raw_url = title_link_el.get("href", "")
                    if raw_url:
                        url = urljoin("https://techcrunch.com/", raw_url.strip())
                else:
                    logger.warning(f"第 {idx+1} 個文章區塊未找到有效標題連結。HTML 片段: {str(article_element)[:200]}...")


                # +++ MODIFICATION START: Extract category +++
                # 分類通常在 loop-card__cat-group 下的 a 或 span 標籤，class 為 loop-card__cat
                category_el_selectors = [
                    "div.loop-card__cat-group span.loop-card__cat", # 例如 "Featured"
                    "div.loop-card__cat-group a.loop-card__cat",   # 例如 "AI"
                    "span.entry-meta__item--cat a"                 # 另一種可能的模式
                ]
                category_el = None
                for sel in category_el_selectors:
                    category_el = article_element.select_one(sel)
                    if category_el:
                        logger.debug(f"分類元素使用 selector '{sel}' 找到。")
                        break
                
                if category_el:
                    category = category_el.get_text(strip=True)
                else:
                    logger.warning(f"第 {idx+1} 個文章區塊 (標題: {title}) 未找到分類。")
                # +++ MODIFICATION END +++

                time_el_selectors = [
                    "time.river-byline__time--with-author", 
                    "time.wp-block-tc23-post-time-ago",   
                    "time.loop-card__time",                # 新增一個可能的 selector
                    "div.river-byline time[datetime]",     # 更精確
                    "time[datetime]"                      
                ]
                time_el = None
                for sel in time_el_selectors:
                    time_el = article_element.select_one(sel)
                    if time_el:
                        logger.debug(f"時間元素使用 selector '{sel}' 找到。")
                        break

                if time_el and time_el.has_attr("datetime"):
                    published_at = time_el["datetime"]
                else:
                    logger.warning(f"第 {idx+1} 個文章區塊 (標題: {title}) 未找到發佈時間。")
                
                if url and url.startswith("https://techcrunch.com/"):
                    logger.info(f"準備抓取文章內容: {title} ({url})")
                    content_html_res = self._manager.get_html_from_url(url) 
                    if content_html_res.is_success() and content_html_res.value:
                        article_soup = BeautifulSoup(content_html_res.value, "lxml")
                        content_container_selectors = [
                            "div.article-content.wp-block-post-content", 
                            "div.article-content",    
                            "div.entry-content",      
                            "article.wp-block-post div.entry-content", # 更精確
                            "article"                 
                        ]
                        content_container = None
                        for sel in content_container_selectors:
                            content_container = article_soup.select_one(sel)
                            if content_container:
                                logger.debug(f"文章內容容器使用 selector '{sel}' 找到 for URL: {url}")
                                break
                                                
                        if content_container:
                            paragraphs = content_container.find_all(['p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'li', 'blockquote'])
                            content_parts = [p.get_text(strip=True) for p in paragraphs if p.get_text(strip=True)]
                            content = "\n\n".join(content_parts)
                            if not content.strip():
                                logger.warning(f"成功抓取文章頁面但未能提取有效文本內容: {url}。內容容器可能為空或提取邏輯需調整。")
                                content = "無法提取文本內容 (Content container found but text extraction failed or was empty)"
                        else:
                            logger.warning(f"在文章頁面找不到內容容器: {url}。請檢查文章內容的 CSS selector。")
                            content = "內容容器未找到 (Content container not found)"
                    elif content_html_res.is_failure():
                        logger.error(f"抓取文章內容失敗 for {url}: {content_html_res.error_msg}")
                        content = f"抓取內容失敗: {content_html_res.error_msg}"
                    else: 
                        logger.error(f"抓取文章內容成功但值為空 for {url}")
                        content = "抓取內容成功但值為空"
                elif url: 
                    logger.warning(f"URL '{url}' 不是 TechCrunch 官方 URL 或無效，跳過內容抓取。")
                    content = "非 TechCrunch URL 或無效 URL，跳過內容抓取"
                else: 
                    logger.warning(f"第 {idx+1} 個文章區塊未找到 URL，無法抓取內容。")
                    content = "URL 未找到，無法抓取內容"

                if title != "N/A" and url: 
                    articles.append({
                        "title": title,
                        "url": url,
                        # +++ MODIFICATION: Add category to the dictionary +++
                        "category": category,
                        # +++ END MODIFICATION +++
                        "published_at": published_at,
                        "content": content.strip() if content else "N/A"
                    })
                else:
                    logger.warning(f"第 {idx+1} 個文章區塊因缺少標題或 URL 而被跳過。")
            
            if not articles:
                logger.warning("雖然可能找到了文章區塊，但未能從中提取任何有效的文章資訊（標題/URL）。")
                return Result.fail("解析失敗：找到了文章區塊，但無法從中提取有效的標題和連結。")

            logger.info(f"成功解析到 {len(articles)} 篇文章的元數據和內容。")
            return Result.ok(articles)
        except Exception as e:
            logger.error(f"解析 TechCrunch HTML 時發生未預期錯誤: {e}", exc_info=True)
            # --- MODIFICATION: Include traceback in Result.fail if possible ---
            import traceback
            tb_str = traceback.format_exc()
            return Result.fail(f"解析時發生未預期錯誤: {e}", traceback_str=tb_str)
            # --- END MODIFICATION ---

if __name__ == "__main__":
    import json
    logging.basicConfig(
        level=logging.DEBUG, # <<< MODIFIED: Changed to DEBUG for more detailed output during testing
        format='%(asctime)s - %(levelname)s - [%(name)s:%(lineno)d] %(funcName)s - %(message)s' # Более подробный формат
    )

    logger.info("開始 TechCrunchFetcher 腳本直接運行...")
    fetcher = TechCrunchFetcher(use_selenium=False) # 改為 True 來測試 Selenium 模式
    res = fetcher.run()

    if res.is_failure(): 
        print(f"❌ 抓取/解析失敗：{res.error_msg}")
        if res.traceback_str: 
            print("\n詳細追蹤信息：")
            print(res.traceback_str)
        exit(1)

    articles = res.value
    if articles: 
        print(f"✅ 成功抓到 {len(articles)} 篇文章：")
        # print(json.dumps(articles, ensure_ascii=False, indent=2)) # 打印完整 JSON
        
        # --- MODIFICATION START: Print category in summary ---
        for i, article_item in enumerate(articles):
            print(f"\n--- 第 {i+1} 篇文章 ---")
            print(f"標題: {article_item.get('title')}")
            print(f"URL: {article_item.get('url')}")
            print(f"分類: {article_item.get('category')}") # 新增顯示分類
            print(f"發佈於: {article_item.get('published_at')}")
            content_preview = article_item.get("content", "")
            print(f"內容 (前 1000 字元): {content_preview[:1000] + ('...' if len(content_preview) > 100 else '')}")
            print("---------------------------------")
        # --- MODIFICATION END ---
    else:
        print("✅ 操作完成，但未抓取到任何文章。")
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import com.github.tomakehurst.wiremock.WireMockServer;

/* Logic of Test:
   1. 驗證兩階段存檔邏輯：
        - 無論回傳是否有效，程式一律先落地 raw_<ts>.json
        - 只有驗證 (validate) 通過時，才會再落地 valid_<ts>.json
   2. 驗證對各種 API 情境（正常 / 空陣列 / error 欄位 / HTTP 500）的分支行為
   3. 驗證 handleStatusCode() 與 validate() 能正確攔截錯誤，避免寫入 invalid_ 檔案
*/

/** 對 MetOfficeWarningsFetcher_modulized 的黑盒整合測試 */
public class MetOfficeWarningsFetcherModulizedBlackBoxTest {

    /* WireMock server */
    private static WireMockServer wm;

    @BeforeAll
    static void startStubServer() {
        wm = new WireMockServer(0);              // 動態分配可用port，port設為 0，讓系統自動選擇未佔用的port
        wm.start();                              // 啟動 HTTP 服務，監聽 port
        configureFor("localhost", wm.port());    // 配置 WireMock 的客戶端調用，指向剛剛啟動的Server

        /* 覆寫程式中的 System property，使 fetch() 指向本地 stub */
        System.setProperty("metoffice.url",
            "http://localhost:" + wm.port() + "/query");
    }

    @AfterAll
    static void stopStubServer() { wm.stop(); }

    @AfterEach
    void cleanDataFolder() throws Exception {
        Files.createDirectories(Path.of("data"));
        try (Stream<Path> s = Files.list(Path.of("data"))) {
            s.filter(p -> p.getFileName().toString().matches("(raw_|valid_).*\\.json"))
             .forEach(p -> p.toFile().delete());
        }
    }

    /* 工具：統計 data/ 內符合前綴的檔案數 */
    private long countFiles(String prefix) throws Exception {
        return Files.list(Path.of("data"))
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .count();
    }

    // test case 1: test Success with Alerts
    // API 正常、且回傳 至少 1 筆警報，確保在此情境下，腳本會把原始 JSON 寫進 data/raw_<timestamp>.json
    // 驗證「正常有資料」時：
    //     1. isValidResponse() 通過
    //     2. 寫檔邏輯被執行
    // 預期結果：測後檔案系統 data/ 內 存在 至少各一個以 raw_ 與 valid_ 開頭的檔案 → 斷言 assertTrue(...) 成立
    @Test
    void successWithAlerts() throws Exception {
        // 客戶端發送 GET 請求 /query時，返回 HTTP 200 以及 JSON {"features":[{...}]}
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"features\":[{\"attributes\":{\"OBJECTID\":1}}]}")));

        MetOfficeWarningsFetcher_modulized.main(new String[]{});

        assertTrue(countFiles("raw_")  >= 1, "raw file should exist");
        assertTrue(countFiles("valid_")>= 1, "valid file should exist");
    }


    // test case 2: test Success with no Alert
    // 有時 API 雖成功，但 features 是空陣列，代表「目前沒有任何天氣預警」，仍希望保存一次呼叫結果
    // 驗證空陣列分支：
    //     1. 程式應寫檔
    //     2. 執行後印出 no weather warning at <timestamp>
    //     3. 隨即結束、不中斷
    // 預期結果：data/ 內 仍有 raw_*.json 與 valid_  → assertTrue(...) 成立
    @Test
    void successNoAlerts() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"features\":[]}")));

        MetOfficeWarningsFetcher_modulized.main(new String[]{});

        assertTrue(countFiles("raw_")  >= 1);
        assertTrue(countFiles("valid_")>= 1);
    }


    // test case 3: test Error Field
    // ArcGIS 服務有時會回 200，但 body 裡帶 "error":{...}。此時應 視為失敗，且不要寫檔，以免把錯誤訊息當成有效資料
    // 驗證 isValidResponse() 能偵測 error，並在偵測到後：
    //     1. 印錯誤
    //     2. 不 建立 raw_*.json
    //     3. 提前結束
    // 預期結果：data/ 目錄 沒有 只會有 raw_*.json，不會有 valid_*.json → 斷言 assertFalse(...) 成立
    @Test
    void errorFieldInBody() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"error\":{\"code\":400,\"message\":\"bad\"}}")));

        MetOfficeWarningsFetcher_modulized.main(new String[]{});

        assertTrue (countFiles("raw_")  >= 1, "raw should still be saved");
        assertEquals(0, countFiles("valid_"), "valid file must NOT be saved");
    }


    // test case 4: test Http 500
    // API 可能直接回 500 等非 200 HTTP 狀態，程式在 response.statusCode()!=200 時必須立刻退出，不留下半成品檔案
    // 驗證高層錯誤處理：
    //    1. 收到 500 時立刻 System.err.println 並 return，
    //    2. 不進入寫檔區段
    // 預期結果：handleStatusCode() 攔截 → fetch() 拋例外 →  data/ 中找不到 raw_*.json → assertFalse(...) 成立
    @Test
    void http500() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query")).willReturn(serverError()));

        MetOfficeWarningsFetcher_modulized.main(new String[]{});

        assertEquals(0, countFiles("raw_"),   "no raw file on HTTP 500");
        assertEquals(0, countFiles("valid_"), "no valid file on HTTP 500");
    }
}

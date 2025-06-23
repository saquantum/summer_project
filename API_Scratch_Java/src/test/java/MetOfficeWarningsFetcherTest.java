import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import com.github.tomakehurst.wiremock.WireMockServer;  // 啟動一個本地 HTTP Sub Server，模擬對 /query 的請求

public class MetOfficeWarningsFetcherTest {
    // Logic of Test
    // 1. **`stubFor`**: Instructs the WireMockServer to “intercept GET requests to `/query` and return `{ "features": […] }`.”
    // 2. The main application, `MetOfficeWarningsFetcher`, uses `HttpClient` to send a request to `http://localhost:<port>/query`.
    // 3. Because we override the system property `metoffice.url`, that URL now points to the local WireMockServer.
    // 4. The WireMockServer receives the request and responds with the stubbed response (HTTP 200 + the JSON string) that was registered via `stubFor`.
    // 5. The main application receives this JSON and proceeds with writing the file or performing other logic.

    private static WireMockServer wm;

    @BeforeAll
    static void beforeAll() {
        wm = new WireMockServer(0);               // 動態分配可用port，port設為 0，讓系統自動選擇未佔用的port
        wm.start();                               // 啟動 HTTP 服務，監聽 port
        configureFor("localhost", wm.port());     // 配置 WireMock 的客戶端調用，指向剛剛啟動的Server
        System.setProperty("metoffice.url",
            "http://localhost:" + wm.port() + "/query");
    }

    @AfterAll
    static void afterAll() { wm.stop(); }

    @AfterEach
    void clean() throws Exception {
        Files.createDirectories(Path.of("data"));
        try (Stream<Path> s = Files.list(Path.of("data"))) {
            s.filter(p -> p.getFileName().toString().startsWith("raw_"))
             .forEach(p -> p.toFile().delete());
        }
    }

    // test case 1: test Success with Alerts
    // API 正常、且回傳 至少 1 筆警報，確保在此情境下，腳本會把原始 JSON 寫進 data/raw_<timestamp>.json
    // 驗證「正常有資料」時：
    //     1. isValidResponse() 通過
    //     2. 寫檔邏輯被執行
    // 預期結果：測後檔案系統 data/ 內 存在 至少一個以 raw_ 開頭的檔案 → 斷言 assertTrue(...) 成立
    @Test
    void testSuccessWithAlerts() throws Exception {
        // 客戶端發送 GET 請求 /query時，返回 HTTP 200 以及 JSON {"features":[{...}]}
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"features\":[{\"attributes\":{\"OBJECTID\":1}}]}")));

        MetOfficeWarningsFetcher.main(new String[]{});

        assertTrue(Files.list(Path.of("data"))
                .anyMatch(p -> p.getFileName().toString().startsWith("raw_")));
    }

    // test case 2: test Success with no Alert
    // 有時 API 雖成功，但 features 是空陣列，代表「目前沒有任何天氣預警」，仍希望保存一次呼叫結果
    // 驗證空陣列分支：
    //     1. 程式應寫檔
    //     2. 執行後印出 no weather warning at <timestamp>
    //     3. 隨即結束、不中斷
    // 預期結果：data/ 內 仍有 raw_*.json → assertTrue(...) 成立
    @Test
    void testSuccessNoAlerts() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"features\":[]}")));

        MetOfficeWarningsFetcher.main(new String[]{});

        // 檔案應存在
        assertTrue(Files.list(Path.of("data"))
                .anyMatch(p -> p.getFileName().toString().startsWith("raw_")));
    }

    // test case 3: test Error Field
    // ArcGIS 服務有時會回 200，但 body 裡帶 "error":{...}。此時應 視為失敗，且不要寫檔，以免把錯誤訊息當成有效資料
    // 驗證 isValidResponse() 能偵測 error，並在偵測到後：
    //     1. 印錯誤
    //     2. 不 建立 raw_*.json
    //     3. 提前結束
    // 預期結果：data/ 目錄 沒有 任何 raw_*.json → 斷言 assertFalse(...) 成立
    @Test
    void testErrorField() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(okJson("{\"error\":{\"code\":400,\"message\":\"bad\"}}")));

        MetOfficeWarningsFetcher.main(new String[]{});

        // 不應寫檔
        assertFalse(Files.list(Path.of("data"))
                 .anyMatch(p -> p.getFileName().toString().startsWith("raw_")));
    }

    // test case 4: test Http 500
    // API 可能直接回 500 等非 200 HTTP 狀態，程式在 response.statusCode()!=200 時必須立刻退出，不留下半成品檔案
    // 驗證高層錯誤處理：
    //    1. 收到 500 時立刻 System.err.println 並 return，
    //    2. 不進入寫檔區段
    // 預期結果：data/ 中找不到 raw_*.json → assertFalse(...) 成立
    @Test
    void testHttp500() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/query"))
            .willReturn(serverError()));

        MetOfficeWarningsFetcher.main(new String[]{});

        assertFalse(Files.list(Path.of("data"))
                 .anyMatch(p -> p.getFileName().toString().startsWith("raw_")));
    }
}

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * 拉取 Met Office (NSWWS) 的即時資料
 * 精簡參數：只保留 where、outFields、returnGeometry=true、f=pjson
 * 原本因為帶了 empty geometry & geometryType，API 回傳會過濾掉所有資料
 */
public class MetOfficeWarningsFetcher_modulized {
    // ArcGIS REST 服務的 query URL（token 若無則留空字串即可）
    // private static final String BASE_URL =
    //     "https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/"
    //   + "Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query"
    //   + "?where=1%3D1&outFields=*&returnGeometry=true&f=pjson&token=";

    private static final String DEFAULT_URL =
        "https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/"
        + "Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query"
        + "?where=1=1"                                  // 確保回傳全資料
        + "&outFields=*"                                // 取所有屬性欄位
        + "&returnGeometry=true"                        // 取 geometry
        + "&f=pgeojson";                                // 回傳 GeoJSON

    // 從 System property 讀取覆寫值，否則回 DEFAULT_URL
    private static String getBaseUrl() {
        return System.getProperty("metoffice.url", DEFAULT_URL);
    }

    // Workflow：fetch() → saveRaw() → printFeatures() → validate() → saveValidated() → printFeatures()，
    public static void main(String[] args) {
        try {
            // 1. fetch
            JsonNode root = fetch();

            // 2. saveRaw
            Path rawFile = saveJson(root, "raw_");
            System.out.println("Saved RAW JSON → " + rawFile);

            // 3. printFeatures (raw)
            System.out.println("=== RAW features ===");
            printFeatures(root);

            // 4. validate 
            //     - 1. HTTP Status might be 4XX or 5XX.
            //     - 2. The data contains "error" field or "features" is not a list
            if (!validate(root)) return;   // invalid => 結束

            // 5. saveValidated
            Path valFile = saveJson(root, "valid_");
            System.out.println("Saved VALIDATED JSON → " + valFile);

            // 6. printFeatures (validated)
            System.out.println("=== VALIDATED features ===");
            printFeatures(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* fetch */
    private static JsonNode fetch() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                                      .connectTimeout(Duration.ofSeconds(10))
                                      .build();

        HttpRequest req = HttpRequest.newBuilder()
                                     .uri(URI.create(getBaseUrl()))
                                     .timeout(Duration.ofSeconds(30))
                                     .header("Accept", "application/json")
                                     .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (!handleStatusCode(res.statusCode()))
            throw new IllegalStateException("HTTP error handled");

        return new ObjectMapper().readTree(res.body());
    }

    /* saveJson  */
    // 依 prefix 決定 raw_ 或 valid_
    private static Path saveJson(JsonNode root, String prefix) throws Exception {
        Path dir = Path.of("data");
        if (Files.notExists(dir)) Files.createDirectories(dir);

        String ts = String.valueOf(Instant.now().toEpochMilli());
        Path out = dir.resolve(prefix + ts + ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter()
                          .writeValue(out.toFile(), root);
        return out;
    }

    /* printFeatures */
    private static void printFeatures(JsonNode root) {
        JsonNode features = root.path("features");
        if (!features.isArray() || features.size() == 0) {
            System.out.println("no weather warning at " + Instant.now().toEpochMilli());
            return;
        }
        
        // 設定時間格式器（Europe/London 時區）
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                                 .withZone(ZoneId.of("Europe/London"));

        // 調整欄位名稱為跟 API 回傳一致的大小寫
        for (JsonNode f : features) {
            JsonNode a = f.path("attributes");
            long id    = a.path("OBJECTID").asLong();
            String s   = a.path("Severity").asText("");
            String h   = a.path("Headline").asText("");
            long from  = a.path("ValidFrom").asLong(0);
            long to    = a.path("ValidTo").asLong(0);
            String fromS = from > 0 ? fmt.format(Instant.ofEpochMilli(from)) : "—";
            String toS   = to > 0 ? fmt.format(Instant.ofEpochMilli(to)) : "—";

            System.out.printf(
                "ID:%d  嚴重度:%s  生效:%s  結束:%s  標題:%s%n", id, s, fromS, toS, h
            );
        }
    }

    /* validate */
    // 網路連線錯誤
    private static boolean validate(JsonNode root) {
        if (!isValidResponse(root)) {
            JsonNode e = root.path("error");
            System.err.printf("API error code=%s, msg=%s%n",
                              e.path("code").asText(""), e.path("message").asText(""));
            return false;
        }
        return true;
    }
    
    /* isValidResponse */
    // 網路連線成功
    // 驗證方法：檢查是否有 error 欄位，或 features 格式不對
    // 目前只能判定是否為「錯誤 error」
    // 若資料 features 為空，則打印出「no weather warnning」
    private static boolean isValidResponse(JsonNode root) {
        if (root.has("error")) {
            return false;
        }
        JsonNode features = root.path("features");
        return features.isArray();  // 如果features 不是陣列，就視為錯誤
    }

    private static boolean handleStatusCode(int code) {
        if (code == 200) return true;
        String msg;
        switch (code) {
            case 400:
                msg = "HTTP 400 Bad Request";
                break;
            case 401:
                msg = "HTTP 401 Unauthorized";
                break;
            case 403:
                msg = "HTTP 403 Forbidden";
                break;
            case 404:
                msg = "HTTP 404 Not Found";
                break;
            case 408:
                msg = "HTTP 408 Request Timeout";
                break;
            case 429:
                msg = "HTTP 429 Too Many Requests";
                break;
            case 500:
                msg = "HTTP 500 Internal Server Error";
                break;
            case 502:
                msg = "HTTP 502 Bad Gateway";
                break;
            case 503:
                msg = "HTTP 503 Service Unavailable";
                break;
            case 504:
                msg = "HTTP 504 Gateway Timeout";
                break;
            default:
                msg = "HTTP "+ Integer.toString(code);
        }
        System.err.println(msg);
        return false;
    }
}

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


/**
 * 拉取 Met Office 國家級氣象警報 (NSWWS) 的即時資料
 * 精簡參數：只保留 where、outFields、returnGeometry=false、f=pjson
 * 原本因為帶了 empty geometry & geometryType，API 回傳會過濾掉所有資料
 */
public class MetOfficeWarningsFetcher {

    // ArcGIS REST 服務的 query URL（token 若無則留空字串即可）
    // private static final String BASE_URL =
    //     "https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/"
    //   + "Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query"
    //   + "?where=1%3D1&outFields=*&returnGeometry=true&f=pjson&token=";

    private static final String BASE_URL =
        "https://services.arcgis.com/Lq3V5RFuTBC9I7kv/arcgis/rest/services/"
        + "Met_Office_National_Severe_Weather_Warning_Service_Live/FeatureServer/0/query"
        + "?where=1=1"                                  // 確保回傳全資料
        + "&outFields=*"                                // 取所有屬性欄位
        + "&returnGeometry=false"                       // 不取 geometry（避免過濾）
        + "&f=pjson";                                   // 回傳 JSON

    public static void main(String[] args) {
        // 1. 建立 HttpClient（可視需要調 proxy / time-out）
        HttpClient client = HttpClient.newBuilder()
                                      .connectTimeout(Duration.ofSeconds(10))
                                      .build();

        // 2. 建立 GET 請求
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(BASE_URL))
                                         .timeout(Duration.ofSeconds(30))
                                         .header("Accept", "application/json")
                                         .build();

        try {
            // 3. 發送請求並取得字串回應
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("HTTP error: " + response.statusCode());
                return;
            }

            // 印出 raw JSON 以便 debug 欄位名稱
            System.out.println("⏎ RAW JSON:\n" + response.body());


            // 4. ObjectMapper 解析 JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // 5. 逐筆處理 features
            JsonNode features = root.path("features");
            if (features.isMissingNode() || !features.isArray()) {
                System.out.println("No features found.");
                return;
            }

            // 6. 設定時間格式器（Europe/London 時區）
            DateTimeFormatter fmt = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.of("Europe/London"));

            for (JsonNode feature : features) {
                JsonNode attrs = feature.path("attributes");
                long objectId   = attrs.path("OBJECTID").asLong();

                // 調整欄位名稱為跟 API 回傳一致的大小寫
                String severity = attrs.path("Severity").asText();
                String headline = attrs.path("Headline").asText();
                
                // 更新時間欄位 key，並預設 0 時不顯示
                long startMs = attrs.path("ValidFrom").asLong(0);    // API 回傳的開始欄位
                long endMs = attrs.path("ValidTo").asLong(0);        // API 回傳的結束欄位
                
                // 轉換 epoch ms 為可讀字串
                String startTime = (startMs > 0)
                    ? fmt.format(Instant.ofEpochMilli(startMs))
                    : "—";
                String endTime = (endMs > 0)
                    ? fmt.format(Instant.ofEpochMilli(endMs))
                    : "—";
                
                System.out.printf(
                    "ID:%d  嚴重度:%s  生效:%s  結束:%s  標題:%s%n",
                    objectId, severity, startTime, endTime, headline
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

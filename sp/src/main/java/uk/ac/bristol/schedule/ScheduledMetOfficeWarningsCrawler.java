package uk.ac.bristol.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.bristol.exception.SpExceptions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledMetOfficeWarningsCrawler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(fixedRateString = "${metoffice.crawler.rate:600000}") // default polling rate -- 10 mins per polling
    public void scheduledCrawler() {
        crawler();
    }

    @Value("${metoffice.url}")
    private String DEFAULT_URL;

    private String getBaseUrl(String url) {
        if (url != null && !url.isBlank()) {
            return url;
        }
        return DEFAULT_URL;
    }

    // 驗證方法：檢查是否有 error 欄位，或 features 格式不對
    // 目前只能判定是否為「錯誤 error」
    // 若資料 features 為空，則打印出「no weather warning」
    private static boolean isValidResponse(JsonNode root) {
        if (root.has("error")) {
            return false;
        }
        JsonNode features = root.path("features");
        return features.isArray();  // 如果features 不是陣列，就視為錯誤
    }

    private void crawler() {
        // 1. 建立 HttpClient（可視需要調 proxy / time-out）
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 2. 建立 GET 請求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl(null)))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .build();

        try {
            // 3. 發送請求並取得字串回應
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SpExceptions.SystemException("HTTP error: " + response.statusCode());
            }

            // 4. ObjectMapper 解析 JSON
            JsonNode root = mapper.readTree(response.body());

            // 在任何寫檔動作之前，先驗證回應是否有效
            if (!isValidResponse(root)) {
                JsonNode err = root.path("error");
                throw new SpExceptions.SystemException("API 回傳錯誤 code="
                        + err.path("code").asText("")
                        + ", message=" + err.path("message").asText(""));
            }

            // 在專案根目錄下建立 data 資料夾，若已存在則跳過
            Path dataDir = Path.of("data");
            if (Files.notExists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            // 取得當前時間戳（毫秒）作為檔名一部分
            String timestamp = String.valueOf(Instant.now().toEpochMilli());

            // 組出 raw_<timestamp>.json，並將整段 JSON 寫入 data/raw.json
            Path outFile = dataDir.resolve("raw_" + timestamp + ".json");
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(outFile.toFile(), root);

            System.out.println("已將完整 JSON 結果儲存到：" + outFile.toString());


            // 無天氣預警則提示並退出
            JsonNode features = root.path("features");
            if (features.isArray() && features.size() == 0) {
                System.out.println("no weather warning at " + timestamp); // 無警報時輸出提示
                return;
            }


            // 印出 raw JSON 以便 debug 欄位名稱
            System.out.println("⏎ RAW JSON:\n" + response.body());
            System.out.println(response.body().substring(0, Math.min(200, response.body().length())) + "…");


            // // 5. 逐筆處理 features
            // JsonNode features = root.path("features");
            // if (features.isMissingNode() || !features.isArray()) {
            //     System.out.println("No features found.");
            //     return;
            // }

            // 6. 設定時間格式器（Europe/London 時區）
            DateTimeFormatter fmt = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.of("Europe/London"));

            for (JsonNode feature : features) {
                JsonNode attrs = feature.path("attributes");
                long objectId = attrs.path("OBJECTID").asLong();

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
            throw new SpExceptions.SystemException(e.getMessage());
        }
    }
}

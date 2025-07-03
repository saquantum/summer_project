package uk.ac.bristol.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledMetOfficeWarningsCrawler {

    @Value("${metoffice.url}")
    private String DEFAULT_URL;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final WarningService warningService;

    public ScheduledMetOfficeWarningsCrawler(WarningService warningService) {
        this.warningService = warningService;
    }

    @Scheduled(fixedRateString = "${metoffice.crawler.rate:600000}") // default polling rate -- 10 mins per polling
    public void scheduledCrawler() {
        try {
            MockDataInitializer.latch.await();
        } catch (InterruptedException e) {
            throw new SpExceptions.SystemException("InterruptedException threw, failed to start the scheduled crawler");
        }
        crawler();
    }

    private void crawler() {
        String response = null;
        try {
            HttpResponse<String> httpResponse = getResponse();
            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to crawl weather warning data due to HTTP error with code "
                        + httpResponse.statusCode());
            }
            response = httpResponse.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to crawl weather warning data.", e);
        }
        try {
            saveWarningData(response);
            System.out.println("Successfully stored weather warning data at "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save crawled weather warning data", e);
        }
        List<Warning> warnings;
        try {
            List<Feature> features = getFeatures(response);
            warnings = parseWarningFromGeoJSON(features);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse crawled weather warning data at "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    , e);
        }
        if (warnings.isEmpty()) {
            System.out.println("Currently no weather warning is issued, finished.");
            return;
        }
        int s = warningService.insertWarningsList(warnings);
        System.out.println("Successfully inserted or updated " + s + " weather warning records at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private String getBaseUrl(String url) {
        if (url != null && !url.isBlank()) {
            return url;
        }
        return DEFAULT_URL;
    }

    private HttpResponse<String> getResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl(null)))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void saveWarningData(String GeoJSON) throws IOException {
        Path dataDir = Path.of("data");
        if (Files.notExists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        Files.writeString(dataDir.resolve("raw_" + timestamp + ".json"), GeoJSON);
    }

    private List<Feature> getFeatures(String GeoJSON) throws JsonProcessingException {
        return mapper.readValue(GeoJSON, FeatureCollection.class).features;
    }

    private List<Warning> parseWarningFromGeoJSON(List<Feature> features) {
        if (features.isEmpty()) return new ArrayList<>();
        List<Warning> warnings = new ArrayList<>();
        for (Feature feature : features) {
            Warning warning = Warning.getWarningFromGeoJSON(feature.properties, feature.geometry);
            warnings.add(warning);
        }
        return warnings;
    }
}

class FeatureCollection {
    public String type;
    public List<Feature> features;
}

class Feature {
    public String type;
    public Long id;
    public Map<String, Object> properties;
    public Map<String, Object> geometry;
}

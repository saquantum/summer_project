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

import javax.servlet.http.HttpServletResponse;
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

    static class FeatureCollection {
        public String type;
        public List<Feature> features;
    }

    static class Feature {
        public String type;
        public Long id;
        public Map<String, Object> properties;
        public Map<String, Object> geometry;
    }

    @Scheduled(fixedRateString = "${metoffice.crawler.rate:600000}") // default polling rate -- 10 mins per polling
    public void scheduledCrawler() {
        try {
            MockDataInitializer.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new SpExceptions.SystemException("InterruptedException threw, failed to start the scheduled crawler");
        }
        crawler();
    }

    private void crawler() {
        // 1. get http response from met office site
        HttpResponse<String> httpResponse = null;
        String response = null;
        try {
            httpResponse = getResponse();
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to fetch weather warning data. " + e.getMessage());
        }
        if (httpResponse.statusCode() != HttpServletResponse.SC_OK) {
            throw new SpExceptions.SystemException("Failed to fetch weather warning data due to HTTP error with code "
                    + httpResponse.statusCode());
        }

        // 2. get GeoJSON data from http response
        response = httpResponse.body();
        try {
            saveWarningData(response);
            System.out.println("Successfully stored weather warning data at "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Failed to save fetched weather warning data at "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + ". "
                    + e.getMessage());
        }

        // 3. parse GeoJSON into Warning DTO, send notifications
        List<Warning> warnings;
        try {
            List<Feature> features = getFeatures(response);
            warnings = parseWarningFromGeoJSON(features);
        } catch (Exception e) {
            throw new SpExceptions.SystemException(
                    "Failed to parse fetched weather warning data at "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + ". " + e.getMessage());
        }
        if (!warnings.isEmpty()) {
            warningService.storeWarningsAndSendNotifications(warnings);
        } else {
            System.out.println("No recently issued weather warnings.");
        }
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

        // retry at most 3 more times
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                if (attempt == maxRetries) throw e;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                Thread.sleep(100);
            }
        }
        throw new IOException("All retries failed.");
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
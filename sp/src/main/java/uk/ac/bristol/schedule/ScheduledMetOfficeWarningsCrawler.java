package uk.ac.bristol.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
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
import java.util.*;

@Component
public class ScheduledMetOfficeWarningsCrawler {

    private final WarningMapper warningMapper;
    @Value("${metoffice.url}")
    private String DEFAULT_URL;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final WarningService warningService;
    private final ContactService contactService;
    private final AssetService assetService;

    public ScheduledMetOfficeWarningsCrawler(WarningService warningService, ContactService contactService, AssetService assetService, WarningMapper warningMapper) {
        this.warningService = warningService;
        this.contactService = contactService;
        this.assetService = assetService;
        this.warningMapper = warningMapper;
    }

    @Scheduled(fixedRateString = "${metoffice.crawler.rate:30000}") // default polling rate -- 10 mins per polling
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
        HttpResponse<String> httpResponse = null;
        String response = null;
        try {
            httpResponse = getResponse();
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to crawl weather warning data. " + e.getMessage());
        }
        if (httpResponse.statusCode() != 200) {
            throw new SpExceptions.SystemException("Failed to crawl weather warning data due to HTTP error with code "
                    + httpResponse.statusCode());
        }
        response = httpResponse.body();
        try {
            saveWarningData(response);
            System.out.println("Successfully stored weather warning data at "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Failed to save crawled weather warning data. " + e.getMessage());
        }
        List<Warning> warnings;
        try {
            List<Feature> features = getFeatures(response);
            warnings = parseWarningFromGeoJSON(features);
        } catch (Exception e) {
            throw new SpExceptions.SystemException(
                    "Failed to parse crawled weather warning data at "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + ". " + e.getMessage());
        }

        List<Warning> warningsToNotify = new ArrayList<>();

        int s = 0;
        for(Warning warning : warnings) {
            if (!warningMapper.testWarningExists(warning.getId())) {
                warningsToNotify.add(warning);
                s++;
                warningService.insertWarningsList(Collections.singletonList(warning));
                System.out.println("New warningID");
            } else if (warningMapper.testWarningDetailDiff(warning)) {
                warningsToNotify.add(warning);
                s++;
                warningService.insertWarningsList(Collections.singletonList(warning));
                System.out.println("New updated warning(Detail Diff!)");
            } else if (warningMapper.testWarningAreaDiff(warning.getId(), warning.getAreaAsJson())) {
                List<String> oldAssetIds = assetService.selectAssetIdsByWarningId(warning.getId());
                s++;
                warningService.insertWarningsList(Collections.singletonList(warning));
                List<String> assetIds = assetService.selectAssetIdsByWarningId(warning.getId());
                for (String assetId : assetIds) {
                    if (!oldAssetIds.contains(assetId)) {
                        Map<String, Object> notification = contactService.formatNotification(warning.getId(), assetId);
                        contactService.sendEmail(notification);
                    }
                }
                System.out.println("New updated warning(Area Diff!)");
            } else {
                s++;
                warningService.insertWarningsList(Collections.singletonList(warning));
                System.out.println("New updated warning(No diff!)");
            }
        }

        System.out.println("Successfully inserted or updated " + s + " weather warning records at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        for (Warning warning : warningsToNotify) {
            List<String> assetIds = assetService.selectAssetIdsByWarningId(warning.getId());
            contactService.sendAllEmails(warning, assetIds);
        }
        System.out.println("Successfully sent emails after crawling.");
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

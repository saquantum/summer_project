package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.service.PostcodeService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostcodeServiceImpl implements PostcodeService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    private PostcodesIOResult getPostcodeFromPostcodeIOService(String URL) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response == null || response.body() == null) {
            System.err.println("Failed to fetch from Postcodes.io: " + URL);
            return new PostcodesIOResult();
        }
        String responseBody = response.body();
        try {
            PostcodesIOResultList multiple = mapper.readValue(responseBody, PostcodesIOResultList.class);
            if (multiple.status == 200 && multiple.result != null) {
                return multiple.result.get(0);
            }
        } catch (Exception ignored) {
        }
        try {
            PostcodesIOResultMap single = mapper.readValue(responseBody, PostcodesIOResultMap.class);
            if (single.status == 200 && single.result != null) {
                return single.result;
            }
        } catch (Exception ignored) {
        }
        return new PostcodesIOResult();
    }

    @Override
    public Map<String, Object> getRandomPostcode() {
        String URL = "https://api.postcodes.io/random/postcodes";
        try {
            return getPostcodeFromPostcodeIOService(URL).toMap();
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    @Override
    public Map<String, String> getRandomPostcodeAddress() {
        String URL = "https://api.postcodes.io/random/postcodes";
        try {
            return getPostcodeFromPostcodeIOService(URL).toAddressMap();
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    @Override
    public Map<String, String> getColumnsOfPostcode(String postcode) {
        String URL = "https://api.postcodes.io/postcodes/" + postcode.trim().replaceAll(" ", "%20");
        try {
            System.out.println("fetch URL: " + URL);
            return getPostcodeFromPostcodeIOService(URL).toAddressMap();
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    @Override
    public Map<String, Object> getColumnsOfGeometricPoint(Point point) {
        String URL = "https://api.postcodes.io/postcodes?lon=" + point.getX() + "&lat=" + point.getY();
        try {
            System.out.println("fetch URL: " + URL);
            return getPostcodeFromPostcodeIOService(URL).toMap();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    private static class PostcodesIOResultMap {
        public Integer status;
        public PostcodesIOResult result;
    }

    private static class PostcodesIOResultList {
        public Integer status;
        public List<PostcodesIOResult> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PostcodesIOResult {
        public String postcode;
        public Double longitude;
        public Double latitude;
        public String country;
        public String region;
        public String admin_district;

        public Map<String, String> toAddressMap() {
            Map<String, String> map = new HashMap<>();
            map.put("postcode", postcode != null ? postcode : "");
            map.put("postcodeCountry", country != null ? country : "");
            map.put("postcodeRegion", (region != null && !region.isBlank())
                    ? region : (country != null ? country : ""));
            map.put("postcodeAdminDistrict", admin_district != null ? admin_district : "");
            return map;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("longitude", longitude != null ? longitude : null);
            map.put("latitude", latitude != null ? latitude : null);
            map.put("postcode", postcode != null ? postcode : "");
            map.put("country", country != null ? country : "");
            map.put("region", (region != null && !region.isBlank())
                    ? region : (country != null ? country : ""));
            map.put("district", admin_district != null ? admin_district : "");
            return map;
        }
    }
}
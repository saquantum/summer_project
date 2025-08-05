package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.PostcodeService;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostcodeServiceImpl implements PostcodeService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    private final UserMapper userMapper;

    public PostcodeServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    private Map<String, String> getPostcodeFromPostcodeIOService(String URL) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> root = mapper.readValue(response.body(), Map.class);
        Integer status =  (Integer) root.get("status");
        if(status == 404){
            System.out.println("failed to fetch " + URL);
            return null;
        }
        Object resultObj = root.get("result");
        if (!(resultObj instanceof Map)) {
            throw new SpExceptions.SystemException("Postcode.io API returned no 'result' field.");
        }
        Map<String, Object> result = (Map<String, Object>) resultObj;
        Map<String, String> map = new HashMap<>();
        map.put("postcode", (String) result.getOrDefault("postcode", ""));
        map.put("postcodeAdminDistrict", (String) result.getOrDefault("admin_district", ""));
        map.put("postcodeRegion", (String) result.getOrDefault("region", ""));
        map.put("postcodeCountry", (String) result.getOrDefault("country", ""));
        return map;
    }

    @Override
    public Map<String, String> getRandomPostcode() {
        String URL = "https://api.postcodes.io/random/postcodes";
        try {
            return getPostcodeFromPostcodeIOService(URL);
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    @Override
    public Map<String, String> getColumnsOfPostcode(String postcode) {
        String URL = "https://api.postcodes.io/postcodes/" + postcode.trim().replaceAll(" ", "%20");
        try {
            System.out.println("fetch URL: " + URL);
            return getPostcodeFromPostcodeIOService(URL);
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Failed to get postcode from Postcode.io online API.");
        }
    }

    private Map<String, List<User>> groupUserAddressPostcodeByOption(Map<String, Object> filters, String option) {
        if (option == null || option.isBlank()) {
            return new HashMap<>();
        }
        List<User> list = userMapper.selectUsers(QueryTool.formatFilters(filters), null, null, null);
        if (list == null) {
            throw new SpExceptions.SystemException("Failed to access database or data integrity is broken.");
        }
        if (list.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<User>> result = new HashMap<>();
        for (User user : list) {
            Map<String, String> address = user.getAddress();
            String postcodeCountry = address.get("postcodeCountry");
            String postcodeRegion = address.get("postcodeRegion");
            postcodeRegion = postcodeRegion.isBlank() ? postcodeCountry : postcodeRegion;
            String postcodeAdminDistrict = address.get("postcodeAdminDistrict");

            if ("country".equals(option)) {
                result.computeIfAbsent(postcodeCountry, k -> new ArrayList<>()).add(user);
            } else if ("region".equals(option)) {
                result.computeIfAbsent(postcodeRegion, k -> new ArrayList<>()).add(user);
            } else if ("district".equals(option)) {
                result.computeIfAbsent(postcodeAdminDistrict, k -> new ArrayList<>()).add(user);
            }
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Map<String, List<User>> groupUserAddressPostcodeByCountry(Map<String, Object> filters) {
        return groupUserAddressPostcodeByOption(filters, "country");
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Map<String, List<User>> groupUserAddressPostcodeByRegion(Map<String, Object> filters) {
        return groupUserAddressPostcodeByOption(filters, "region");
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Map<String, List<User>> groupUserAddressPostcodeByAdminDistrict(Map<String, Object> filters) {
        return groupUserAddressPostcodeByOption(filters, "district");
    }
}
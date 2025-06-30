package uk.ac.bristol.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssetTest {

    @Test
    public void testLocationSetterFromGeoJson() throws JsonProcessingException {
        String geoJson = """
                {
                    "type": "MultiPolygon",
                    "coordinates": [
                        [
                            [
                                [30.0, 10.0],
                                [40.0, 40.0],
                                [20.0, 40.0],
                                [10.0, 20.0],
                                [30.0, 10.0]
                            ]
                        ]
                    ]
                }
                """;

        Asset asset = new Asset();
        asset.setLocationAsJson(geoJson);

        Map<String, Object> drainArea = asset.getLocation();

        assertNotNull(drainArea);
        assertEquals("MultiPolygon", drainArea.get("type"));
        assertTrue(drainArea.containsKey("coordinates"));
        assertEquals(30.0, ((Number) (((List<?>) ((List<?>) ((List<?>) ((List<?>) asset.getLocation().get("coordinates")).get(0)).get(0)).get(0)).get(0))).doubleValue());
    }

    @Test
    public void testLocationSetterWithNullInput() throws JsonProcessingException {
        Asset asset = new Asset();
        asset.setLocationAsJson((String) null);

        Map<String, Object> result = asset.getLocation();

        assertNotNull(result);
        assertEquals("MultiPolygon", result.get("type"));

        Object coordinates = result.get("coordinates");
        assertTrue(coordinates instanceof List);

        List<?> level1 = (List<?>) coordinates;
        assertEquals(1, level1.size());

        List<?> level2 = (List<?>) level1.get(0);
        assertEquals(1, level2.size());

        List<?> level3 = (List<?>) level2.get(0);
        assertEquals(0, level3.size());
    }

    @Test
    public void testLocationAsJsonGetter() throws JsonProcessingException {
        String geoJson = """
                {
                    "type": "MultiPolygon",
                    "coordinates": [
                        [
                            [
                                [30.0, 10.0],
                                [40.0, 40.0],
                                [20.0, 40.0],
                                [10.0, 20.0],
                                [30.0, 10.0]
                            ]
                        ]
                    ]
                }
                """;

        Asset asset = new Asset();
        asset.setLocationAsJson(geoJson);

        String str = asset.getLocationAsJson();
        assertEquals("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[30.0,10.0],[40.0,40.0],[20.0,40.0],[10.0,20.0],[30.0,10.0]]]]}", str);
    }
}

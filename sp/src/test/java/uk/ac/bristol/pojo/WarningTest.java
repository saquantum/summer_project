package uk.ac.bristol.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WarningTest {

    @Test
    public void testAreaSetterFromGeoJson() throws JsonProcessingException {
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

        Warning warning = new Warning();
        warning.setAreaAsJson(geoJson);

        Map<String, Object> area = warning.getArea();

        assertNotNull(area);
        assertEquals("MultiPolygon", area.get("type"));
        assertTrue(area.containsKey("coordinates"));
        assertEquals(30.0, ((Number) (((List<?>) ((List<?>) ((List<?>) ((List<?>) warning.getArea().get("coordinates")).get(0)).get(0)).get(0)).get(0))).doubleValue());
    }

    @Test
    public void testAreaSetterWithNullInput() throws JsonProcessingException {
        Warning warning = new Warning();
        warning.setAreaAsJson((String) null);

        Map<String, Object> result = warning.getArea();

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

        Warning warning = new Warning();
        warning.setAreaAsJson(geoJson);

        String str = warning.getAreaAsJson();
        assertEquals("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[30.0,10.0],[40.0,40.0],[20.0,40.0],[10.0,20.0],[30.0,10.0]]]]}", str);
    }
}

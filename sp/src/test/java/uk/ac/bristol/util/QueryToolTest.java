package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.ac.bristol.service.MetaDataService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class QueryToolTest {

    @BeforeEach
    void setup() {
        MetaDataService mockService = mock(MetaDataService.class);
        Mockito.when(mockService.getAllRegisteredTableNames()).thenReturn(Set.of("dummy"));

        QueryToolConfig.metaDataService = mockService;
    }

    @Test
    public void testGetOrderList() {
        assertNull(QueryTool.getOrderList());
        assertNull(QueryTool.getOrderList((String[]) null));
        assertNull(QueryTool.getOrderList((List<String>) null));
        assertNull(QueryTool.getOrderList(new String[]{}));
        assertNull(QueryTool.getOrderList(List.of()));

        assertThrows(IllegalArgumentException.class, () -> {
            QueryTool.getOrderList(List.of("a"));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            QueryTool.getOrderList(List.of("a", "b", "c"));
        });

        List<Map<String, String>> orderList = QueryTool.getOrderList(List.of("id", "asc", "name", "desc"));
        assertEquals(2, orderList.size());
        assertEquals("id", orderList.get(0).get("column"));
        assertEquals("asc", orderList.get(0).get("direction"));
        assertEquals("name", orderList.get(1).get("column"));
        assertEquals("desc", orderList.get(1).get("direction"));
    }

    @Test
    public void testFormatFilters() {
        assertNull(QueryTool.formatFilters(null));
        assertNull(QueryTool.formatFilters(new HashMap<>()));

        Map<String, Object> filters = Map.of("value", "1");
        String result = QueryTool.formatFilters(filters);
        assertEquals("value = '1'", result);

        filters = Map.of(
                "name", Map.of("op", "like", "val", "john")
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("lower(name) like lower('john')", result);

        filters = Map.of(
                "age", Map.of("op", "range", "min", 18, "max", 30)
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("age >= 18 and age <= 30", result);

        filters = Map.of(
                "age", Map.of("op", "range", "min", 18)
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("age >= 18", result);

        filters = Map.of(
                "age", Map.of("op", "range", "max", 65)
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("age <= 65", result);

        filters = Map.of(
                "age", Map.of("op", "range")
        );
        Map<String, Object> finalFilters = filters;
        assertThrows(IllegalArgumentException.class, () -> QueryTool.formatFilters(finalFilters));

        filters = Map.of(
                "role", Map.of("op", "in", "list", List.of("admin", "user"))
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("role in ('admin','user')", result);

        filters = Map.of(
                "email", Map.of("op", "notNull")
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("email is not null", result);

        filters = Map.of(
                "phone", Map.of("op", "isNull")
        );
        result = QueryTool.formatFilters(filters);
        assertEquals("phone is null", result);

        filters = Map.of(
                "foo", Map.of("op", "between", "val", 5)
        );
        Map<String, Object> finalFilters1 = filters;
        assertThrows(IllegalArgumentException.class, () -> QueryTool.formatFilters(finalFilters1));

        filters = Map.of(
                "foo", Map.of("val", 123)
        );
        Map<String, Object> finalFilters2 = filters;
        assertThrows(IllegalArgumentException.class, () -> QueryTool.formatFilters(finalFilters2));

        filters = Map.of(
                "name", Map.of("op", "like")
        );
        Map<String, Object> finalFilters3 = filters;
        assertThrows(IllegalArgumentException.class, () -> QueryTool.formatFilters(finalFilters3));
    }

    @Test
    public void testUserIdentityVerification() throws IOException {

    }
}

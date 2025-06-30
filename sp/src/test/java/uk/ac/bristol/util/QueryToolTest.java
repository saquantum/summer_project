package uk.ac.bristol.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QueryToolTest {

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
}

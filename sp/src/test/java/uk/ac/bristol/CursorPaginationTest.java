package uk.ac.bristol;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.pojo.FilterItemDTO;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CursorPaginationTest {

    @Test
    public void testCursorConditionBuildsCorrectly() {
        List<Map<String, String>> orderList = List.of(
                Map.of("column", "c", "direction", "asc"),
                Map.of("column", "a", "direction", "desc"),
                Map.of("column", "d", "direction", "asc")
        );
        Map<String, Object> anchor = Map.of(
                "c", 3,
                "a", 100,
                "d", 200
        );
        List<FilterItemDTO> filters = QueryTool.formatCursoredDeepPageFilters(Map.of(), anchor, orderList);
        String rawCondition = filters.get(0).getRawVal().replaceAll("\\s+", " ").trim();
        System.out.println(rawCondition);
    }
}


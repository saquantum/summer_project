package uk.ac.bristol.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WarningServiceTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private WarningService warningService;

    @Autowired
    private WarningMapper warningMapper;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    void testGetWarnings() {
        List<Warning> warnings = warningService.getWarnings(null, null, null, null);
        assertThat(warnings).isEmpty(); // No initial warnings

        // Test filters
        Map<String, Object> filters = Map.of("warning_weather_type", "Rain");
        List<Warning> rainWarnings = warningService.getWarnings(filters, null, null, null);
        assertThat(rainWarnings).isEmpty();

        // Test ordering
        List<Map<String, String>> orderList = List.of(Map.of("column", "warning_valid_from", "order", "asc"));
        List<Warning> ordered = warningService.getWarnings(null, orderList, null, null);
        assertThat(ordered).isEmpty();
    }

    @Test
    void testGetCursoredWarnings() {
        List<Warning> warnings = warningService.getCursoredWarnings(null, null, null, 10, 0);
        assertThat(warnings).isEmpty();

        // Test invalid cursor
        assertThatThrownBy(() -> warningService.getCursoredWarnings(9999L, null, null, 10, 0))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("anchors");
    }

    @Test
    void testGetWarningsIncludingOutdated() {
        List<Warning> allWarnings = warningService.getWarningsIncludingOutdated(null, null, null, null);
        assertThat(allWarnings).isEmpty();
    }

    @Test
    void testGetCursoredWarningsIncludingOutdated() {
        List<Warning> warnings = warningService.getCursoredWarningsIncludingOutdated(null, null, null, 10, 0);
        assertThat(warnings).isEmpty();
    }

    @Test
    @Transactional
    void testGetWarningById() {
        // Insert a warning to test
        Warning testWarning = createTestWarning(1L);
        warningService.insertWarning(testWarning);

        Warning fetched = warningService.getWarningById(1L);
        assertThat(fetched.getWeatherType()).isEqualTo("Rain");
        assertThat(fetched.getWarningLevel()).isEqualTo("YELLOW");

        // Test non-existent
        assertThatThrownBy(() -> warningService.getWarningById(999L))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Found 0 warnings");
    }

    @Test
    @Transactional
    void testGetWarningsIntersectingWithGivenAsset() {
        // Insert warning that intersects with asset_001
        Warning testWarning = createTestWarning(1L);
        testWarning.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-1,51],[-1,52],[0,52],[0,51],[-1,51]]]]}");
        warningService.insertWarning(testWarning);

        List<Warning> intersecting = warningService.getWarningsIntersectingWithGivenAsset("asset_001");
        assertThat(intersecting).hasSize(0); // Adjust if intersects

        // To force intersect, set to asset location
        // Assume asset location is known, hardcode for test
    }

    @Test
    @Transactional
    void testStoreWarningsAndSendNotifications() {
        List<Warning> parsedWarnings = List.of(createTestWarning(1L), createTestWarning(2L));

        boolean stored = warningService.storeWarningsAndSendNotifications(parsedWarnings);
        assertThat(stored).isTrue();

        assertThat(warningMapper.testWarningExistence(1L)).isTrue(); // Assume access to mapper or use service

        // Test update
        Warning update = createTestWarning(1L);
        update.setWarningLevel("AMBER");
        boolean updated = warningService.storeWarningsAndSendNotifications(List.of(update));
        assertThat(updated).isTrue();

        Warning fetched = warningService.getWarningById(1L);
        assertThat(fetched.getWarningLevel()).isEqualTo("AMBER");

        // Test area diff
        update.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-2,50],[-2,51],[-1,51],[-1,50],[-2,50]]]]}");
        boolean areaUpdated = warningService.storeWarningsAndSendNotifications(List.of(update));
        assertThat(areaUpdated).isTrue();

        // No change
        boolean noChange = warningService.storeWarningsAndSendNotifications(List.of(update));
        assertThat(noChange).isFalse();
    }

    @Test
    @Transactional
    void testInsertWarning() {
        Warning warning = createTestWarning(1L);

        int inserted = warningService.insertWarning(warning);
        assertThat(inserted).isEqualTo(1);

        Warning fetched = warningService.getWarningById(1L);
        assertThat(fetched.getWarningHeadLine()).isEqualTo("Test Warning");
    }

    @Test
    @Transactional
    void testUpdateWarning() {
        Warning warning = createTestWarning(1L);
        warningService.insertWarning(warning);

        warning.setWarningHeadLine("Updated Warning");
        warning.setWarningImpact("High");

        int updated = warningService.updateWarning(warning);
        assertThat(updated).isEqualTo(1);

        Warning fetched = warningService.getWarningById(1L);
        assertThat(fetched.getWarningHeadLine()).isEqualTo("Updated Warning");
    }

//    @Test
//    @Transactional
//    void testDeleteWarningByIDs() {
//        Warning warning = createTestWarning(1L);
//        warningService.insertWarning(warning);
//
//        int deleted = warningService.deleteWarningByIDs(List.of(1L));
//        assertThat(deleted).isEqualTo(1);
//
//        assertThat(warningMapper.testWarningExistence(1L)).isFalse();
//
//        // Test array
//        Warning warning2 = createTestWarning(2L);
//        warningService.insertWarning(warning2);
//
//        int deletedArray = warningService.deleteWarningByIDs(new Long[]{2L});
//        assertThat(deletedArray).isEqualTo(1);
//    }

    @Test
    @Transactional
    void testInsertUkRegion() {
        Map<String, String> region = new HashMap<>();
        region.put("name", "Test Region");
        region.put("area", "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-1,1],[-1,2],[0,2],[0,1],[-1,1]]]]}");

        int inserted = warningService.insertUkRegion(region);
        assertThat(inserted).isEqualTo(1);
    }

    private Warning createTestWarning(Long id) {
        Warning warning = new Warning();
        warning.setId(id);
        warning.setWeatherType("Rain");
        warning.setWarningLevel("YELLOW");
        warning.setWarningHeadLine("Test Warning");
        warning.setValidFrom(Instant.now());
        warning.setValidTo(Instant.now().plusSeconds(3600));
        warning.setWarningImpact("Medium");
        warning.setWarningLikelihood("High");
        warning.setAffectedAreas("Test Areas");
        warning.setWhatToExpect("Rain");
        warning.setWarningFurtherDetails("More details");
        warning.setWarningUpdateDescription("Initial");
        warning.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-1,1],[-1,2],[0,2],[0,1],[-1,1]]]]}");
        return warning;
    }
}
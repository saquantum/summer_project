package uk.ac.bristol.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.pojo.Warning;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarningMapperTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private WarningMapper warningMapper;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    @Order(1)
    public void selectAllWarnings() {
        List<Warning> list = warningMapper.selectAllWarnings(null, null, null);
        assertNotNull(list);
    }

    @Test
    @Order(2)
    public void selectAllWarningsIncludingOutdated() {
        List<Warning> list1 = warningMapper.selectAllWarnings(null, null, null);
        assertNotNull(list1);

        List<Warning> list2 = warningMapper.selectAllWarningsIncludingOutdated(null, null, null);
        assertFalse(list2.isEmpty());

        assertTrue(list2.containsAll(list1));
    }

    @Test
    @Order(3)
    public void selectWarningById() {
        List<Warning> list = warningMapper.selectWarningById(1609L);
        assertEquals(1, list.size());
        assertEquals(1609L, list.get(0).getId());
        assertEquals("THUNDERSTORM", list.get(0).getWeatherType());
        assertEquals("2025-06-07", list.get(0).getValidFrom().atZone(ZoneId.systemDefault()).toLocalDate().toString());
        assertEquals("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.4337,52.6697],[0.4504,52.6531],[-0.3516,52.4594],[-0.7635,52.4493],[-1.1646,52.5229],[-2.0435,52.6964],[-2.8235,52.4727],[-3.2629,52.3488],[-3.6145,52.1335],[-4.3726,51.7338],[-4.7296,51.2034],[-4.6252,50.7086],[-4.3451,50.3104],[-3.7408,50.1065],[-2.5873,50.1276],[-1.1865,50.2683],[-0.9119,50.2964],[-0.5164,50.3595],[-0.1538,50.4365],[0.3296,50.5832],[1.1426,50.903],[1.3293,50.9999],[1.4832,51.1104],[1.8567,51.4814],[2.0105,51.761],[2.0544,51.9036],[2.0764,52.12],[2.0435,52.3957],[1.7963,52.5797],[1.4337,52.6697]]]]}", list.get(0).getAreaAsJson());
    }

    @Test
    @Order(4)
    public void insertWarning() throws JsonProcessingException {
        Warning warning = new Warning();
        warning.setId(1L);
        Instant now = Instant.now();
        warning.setValidFrom(now);
        warning.setValidTo(now.plusSeconds(1000));
        warning.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.4337,52.6697]]]]}");
        int n = warningMapper.insertWarning(warning);
        assertEquals(1, n);

        assertFalse(warningMapper.selectAllWarnings(null, null, null).isEmpty());
        List<Warning> list = warningMapper.selectWarningById(1L);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(now.getEpochSecond(), list.get(0).getValidFrom().getEpochSecond());
        assertEquals(now.plusSeconds(1000).getEpochSecond(), list.get(0).getValidTo().getEpochSecond());
        assertEquals("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.4337,52.6697]]]]}", list.get(0).getAreaAsJson());

        assertThrows(DuplicateKeyException.class, () -> warningMapper.insertWarning(warning));

        warning.setId(null);
        assertThrows(DataIntegrityViolationException.class, () -> warningMapper.insertWarning(warning));

        warningMapper.deleteWarningByIDs(List.of(1L));
    }

    @Test
    @Order(5)
    public void updateWarning() throws JsonProcessingException {
        Warning warning = new Warning();
        warning.setId(1L);
        warning.setWarningLevel("abc");
        warning.setWarningUpdateDescription("123");
        warning.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.4337,52.6697]]]]}");
        warningMapper.insertWarning(warning);

        Instant now = Instant.now();
        warning.setValidFrom(now);
        warning.setValidTo(now.plusSeconds(1000));
        warning.setWarningLevel("");
        warning.setWarningUpdateDescription(null);
        warningMapper.updateWarning(warning);

        List<Warning> list = warningMapper.selectWarningById(1L);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(now.getEpochSecond(), list.get(0).getValidFrom().getEpochSecond());
        assertEquals(now.plusSeconds(1000).getEpochSecond(), list.get(0).getValidTo().getEpochSecond());
        assertEquals("abc", list.get(0).getWarningLevel());
        assertEquals("123", list.get(0).getWarningUpdateDescription());

        warningMapper.deleteWarningByIDs(List.of(1L));
    }

    @Test
    @Order(6)
    public void deleteWarningByIDs() throws JsonProcessingException {
        Warning warning = new Warning();
        warning.setId(1L);
        warning.setAreaAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.4337,52.6697]]]]}");
        warningMapper.insertWarning(warning);
        warning.setId(2L);
        warningMapper.insertWarning(warning);
        assertEquals(1, warningMapper.selectWarningById(1L).size());
        assertEquals(1, warningMapper.selectWarningById(2L).size());

        int n = warningMapper.deleteWarningByIDs(List.of(1L, 2L));
        assertEquals(2, n);
        assertEquals(0, warningMapper.selectWarningById(1L).size());
        assertEquals(0, warningMapper.selectWarningById(2L).size());
    }


}
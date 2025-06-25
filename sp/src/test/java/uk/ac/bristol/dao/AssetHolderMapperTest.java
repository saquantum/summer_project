package uk.ac.bristol.dao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.pojo.AssetHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetHolderMapperTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AssetHolderMapper assetHolderMapper;

    @BeforeAll
    public void init() {
        mockDataInitializer.forceReload();
    }

    @Test
    @Order(1)
    public void selectAddressByAssetHolderId() {
        List<Map<String, String>> list = assetHolderMapper.selectAddressByAssetHolderId("user_040");
        assertEquals(1, list.size());
        assertEquals("user_040", list.get(0).get("assetHolderId"));
        assertEquals("50 Lee Spring", list.get(0).get("street"));
        assertEquals("West Harley", list.get(0).get("city"));
        assertEquals("E12 5QJ", list.get(0).get("postcode"));
        assertEquals("UK", list.get(0).get("country"));

        list = assetHolderMapper.selectAddressByAssetHolderId("user_051");
        assertEquals(0, list.size());

        list = assetHolderMapper.selectAddressByAssetHolderId("admin");
        assertEquals(0, list.size());

        list = assetHolderMapper.selectAddressByAssetHolderId("admin_001");
        assertEquals(0, list.size());
    }

    @Test
    @Order(2)
    public void insertAddress() {
        Map<String, String> testAddress = Map.of("assetHolderId", "test_001", "street", "Park Row", "city", "Bristol", "postcode", "ABC 123", "country", "UK");
        int n = assetHolderMapper.insertAddress(testAddress);
        assertEquals(1, n);
        List<Map<String, String>> list = assetHolderMapper.selectAddressByAssetHolderId("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).get("assetHolderId"));
        assertEquals("Park Row", list.get(0).get("street"));
        assertEquals("Bristol", list.get(0).get("city"));
        assertEquals("ABC 123", list.get(0).get("postcode"));
        assertEquals("UK", list.get(0).get("country"));

        testAddress = Map.of("assetHolderId", "test_002", "city", "Bristol", "country", "UK");
        n = assetHolderMapper.insertAddress(testAddress);
        assertEquals(1, n);
        list = assetHolderMapper.selectAddressByAssetHolderId("test_002");
        assertEquals(1, list.size());
        assertEquals("test_002", list.get(0).get("assetHolderId"));
        assertNull(list.get(0).get("street"));
        assertEquals("Bristol", list.get(0).get("city"));
        assertNull(list.get(0).get("postcode"));
        assertEquals("UK", list.get(0).get("country"));

        testAddress = Map.of("city", "Bristol", "country", "UK");
        Map<String, String> finalTestAddress1 = testAddress;
        assertThrows(DataIntegrityViolationException.class, () -> assetHolderMapper.insertAddress(finalTestAddress1));

        testAddress = Map.of("assetHolderId", "test_001");
        Map<String, String> finalTestAddress2 = testAddress;
        assertThrows(DuplicateKeyException.class, () -> assetHolderMapper.insertAddress(finalTestAddress2));

        assetHolderMapper.deleteAddressByAssetHolderIds(List.of("test_001", "test_002"));
    }

    @Test
    @Order(3)
    public void updateAddressByAssetHolderId() {
        Map<String, String> testAddress = Map.of("assetHolderId", "test_001", "street", "Park Row", "city", "Bristol", "postcode", "ABC 123", "country", "UK");
        assetHolderMapper.insertAddress(testAddress);
        testAddress = Map.of("assetHolderId", "test_002", "city", "Bristol", "country", "UK");
        assetHolderMapper.insertAddress(testAddress);

        testAddress = Map.of("assetHolderId", "test_002", "street", "College Green", "city", "Bristol", "postcode", "ABC 456", "country", "UK");
        int n = assetHolderMapper.updateAddressByAssetHolderId(testAddress);
        assertEquals(1, n);
        List<Map<String, String>> list = assetHolderMapper.selectAddressByAssetHolderId("test_002");
        assertEquals(1, list.size());
        assertEquals("test_002", list.get(0).get("assetHolderId"));
        assertEquals("College Green", list.get(0).get("street"));
        assertEquals("Bristol", list.get(0).get("city"));
        assertEquals("ABC 456", list.get(0).get("postcode"));
        assertEquals("UK", list.get(0).get("country"));

        testAddress = new HashMap<>();
        testAddress.put("assetHolderId", "test_001");
        testAddress.put("street", "Park");
        testAddress.put("city", null);
        testAddress.put("country", null);
        n = assetHolderMapper.updateAddressByAssetHolderId(testAddress);
        assertEquals(1, n);
        list = assetHolderMapper.selectAddressByAssetHolderId("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).get("assetHolderId"));
        assertEquals("Park", list.get(0).get("street"));
        assertEquals("Bristol", list.get(0).get("city"));
        assertEquals("ABC 123", list.get(0).get("postcode"));
        assertEquals("UK", list.get(0).get("country"));

        testAddress = Map.of("city", "Bristol", "country", "UK");
        Map<String, String> finalTestAddress = testAddress;
        assertEquals(0, assetHolderMapper.updateAddressByAssetHolderId(finalTestAddress));

        assetHolderMapper.deleteAddressByAssetHolderIds(List.of("test_001", "test_002"));
    }

    @Test
    @Order(4)
    public void deleteAddressByAssetHolderIds() {
        Map<String, String> testAddress = Map.of("assetHolderId", "test_001", "street", "Park Row", "city", "Bristol", "postcode", "ABC 123", "country", "UK");
        assetHolderMapper.insertAddress(testAddress);
        testAddress = Map.of("assetHolderId", "test_002", "city", "Bristol", "country", "UK");
        assetHolderMapper.insertAddress(testAddress);

        int n = assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("", ""));
        assertEquals(0, n);

        n = assetHolderMapper.deleteAddressByAssetHolderIds(List.of("test_001"));
        assertEquals(1, n);
        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_001").size());
        assertEquals(1, assetHolderMapper.selectAddressByAssetHolderId("test_002").size());
        n = assetHolderMapper.deleteAddressByAssetHolderIds(List.of("test_002"));
        assertEquals(1, n);
        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_001").size());
        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_002").size());

        Map<String, String> testAddress1 = Map.of("assetHolderId", "test_001");
        assertEquals(1, assetHolderMapper.insertAddress(testAddress1));
        assertEquals(1, assetHolderMapper.selectAddressByAssetHolderId("test_001").size());
        Map<String, String> testAddress2 = Map.of("assetHolderId", "test_002");
        assertEquals(1, assetHolderMapper.insertAddress(testAddress2));
        assertEquals(1, assetHolderMapper.selectAddressByAssetHolderId("test_002").size());
        Map<String, String> testAddress3 = Map.of("assetHolderId", "test_003");
        assertEquals(1, assetHolderMapper.insertAddress(testAddress3));
        assertEquals(1, assetHolderMapper.selectAddressByAssetHolderId("test_003").size());

        n = assetHolderMapper.deleteAddressByAssetHolderIds(List.of("test_001", "test_002", "test_003"));
        assertEquals(3, n);

        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_001").size());
        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_002").size());
        assertEquals(0, assetHolderMapper.selectAddressByAssetHolderId("test_003").size());
    }

    @Test
    @Order(5)
    public void selectContactPreferencesByAssetHolderId() {
        List<Map<String, Object>> list = assetHolderMapper.selectContactPreferencesByAssetHolderId("user_017");
        assertEquals(1, list.size());
        assertEquals("user_017", list.get(0).get("assetHolderId"));
        assertEquals(true, list.get(0).get("email"));
        assertEquals(false, list.get(0).get("phone"));
        assertEquals(true, list.get(0).get("post"));
        assertEquals(false, list.get(0).get("whatsapp"));
        assertEquals(false, list.get(0).get("discord"));
        assertEquals(false, list.get(0).get("telegram"));

        list = assetHolderMapper.selectContactPreferencesByAssetHolderId("user_051");
        assertEquals(0, list.size());

        list = assetHolderMapper.selectContactPreferencesByAssetHolderId("admin");
        assertEquals(0, list.size());

        list = assetHolderMapper.selectContactPreferencesByAssetHolderId("admin_001");
        assertEquals(0, list.size());
    }

    @Test
    @Order(6)
    public void insertContactPreferences() {
        Map<String, Object> testPreference = Map.of("assetHolderId", "test_001", "email", true, "phone", false, "whatsapp", true);
        int n = assetHolderMapper.insertContactPreferences(testPreference);
        assertEquals(1, n);
        List<Map<String, Object>> list = assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).get("assetHolderId"));
        assertEquals(true, list.get(0).get("email"));
        assertEquals(false, list.get(0).get("phone"));
        assertNull(list.get(0).get("post"));
        assertEquals(true, list.get(0).get("whatsapp"));
        assertNull(list.get(0).get("discord"));
        assertNull(list.get(0).get("telegram"));

        testPreference = Map.of("assetHolderId", "test_001");
        Map<String, Object> finalTestPreference1 = testPreference;
        assertThrows(DuplicateKeyException.class, () -> assetHolderMapper.insertContactPreferences(finalTestPreference1));

        testPreference = Map.of("email", true);
        Map<String, Object> finalTestPreference2 = testPreference;
        assertThrows(DataIntegrityViolationException.class, () -> assetHolderMapper.insertContactPreferences(finalTestPreference2));

        assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("test_001"));
    }

    @Test
    @Order(7)
    public void updateContactPreferencesByAssetHolderId() {
        Map<String, Object> testPreference = Map.of("assetHolderId", "test_001", "email", true, "phone", false, "whatsapp", true);
        assetHolderMapper.insertContactPreferences(testPreference);

        testPreference = new HashMap<>();
        testPreference.put("assetHolderId", "test_001");
        testPreference.put("email", false);
        testPreference.put("post", true);
        testPreference.put("telegram", null);
        int n = assetHolderMapper.updateContactPreferencesByAssetHolderId(testPreference);
        assertEquals(1, n);
        List<Map<String, Object>> list = assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).get("assetHolderId"));
        assertEquals(false, list.get(0).get("email"));
        assertEquals(false, list.get(0).get("phone"));
        assertEquals(true, list.get(0).get("post"));
        assertEquals(true, list.get(0).get("whatsapp"));
        assertNull(list.get(0).get("discord"));
        assertNull(list.get(0).get("telegram"));

        testPreference = Map.of("email", true);
        Map<String, Object> finalTestPreference = testPreference;
        assertEquals(0, assetHolderMapper.updateContactPreferencesByAssetHolderId(finalTestPreference));

        assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("test_001"));
    }

    @Test
    @Order(8)
    public void deleteContactPreferencesByAssetHolderIds() {
        Map<String, Object> testPreference = Map.of("assetHolderId", "test_001", "email", true, "phone", false, "whatsapp", true);
        assetHolderMapper.insertContactPreferences(testPreference);

        int n = assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("", ""));
        assertEquals(0, n);

        n = assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("test_001"));
        assertEquals(1, n);
        assertEquals(0, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001").size());
        Map<String, Object> testPreference1 = Map.of("assetHolderId", "test_001");
        n = assetHolderMapper.insertContactPreferences(testPreference1);
        assertEquals(1, n);
        assertEquals(1, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001").size());
        Map<String, Object> testPreference2 = Map.of("assetHolderId", "test_002");
        n = assetHolderMapper.insertContactPreferences(testPreference2);
        assertEquals(1, n);
        assertEquals(1, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001").size());
        assertEquals(1, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_002").size());
        n = assetHolderMapper.deleteContactPreferencesByAssetHolderIds(List.of("test_001", "test_002"));
        assertEquals(2, n);
        assertEquals(0, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_001").size());
        assertEquals(0, assetHolderMapper.selectContactPreferencesByAssetHolderId("test_002").size());
    }

    @Test
    @Order(9)
    public void selectAllAssetHolders() {
        List<AssetHolder> list = assetHolderMapper.selectAllAssetHolders();
        assertEquals(50, list.size());
    }

    @Test
    @Order(10)
    public void selectAssetHolderByID() {
        List<AssetHolder> list = assetHolderMapper.selectAssetHolderByID("user_010");
        assertEquals(1, list.size());
        AssetHolder assetHolder = list.get(0);
        assertEquals("user_010", assetHolder.getId());
        assertEquals("Colin Wilkinson", assetHolder.getName());
        assertEquals("moore.gordon@example.org", assetHolder.getEmail());
        assertEquals("02067 209451", assetHolder.getPhone());
        assertEquals("user_010", assetHolder.getAddressId());
        assertEquals("user_010", assetHolder.getContactPreferencesId());

        assertEquals(0, assetHolderMapper.selectAssetHolderByID("admin").size());
        assertEquals(0, assetHolderMapper.selectAssetHolderByID("admin_001").size());
        assertEquals(0, assetHolderMapper.selectAssetHolderByID("user_060").size());
    }

    @Test
    @Order(11)
    public void selectByAssetHolder() {
        AssetHolder assetHolder = new AssetHolder();
        assetHolder.setName("Colin Wilkinson");
        assetHolder.setEmail("moore.gordon@example.org");
        List<AssetHolder> list = assetHolderMapper.selectByAssetHolder(assetHolder);
        assertEquals(1, list.size());
        assertEquals("user_010", list.get(0).getId());

        assetHolder.setName(null);
        assetHolder.setPhone("02067 209451");
        list = assetHolderMapper.selectByAssetHolder(assetHolder);
        assertEquals(1, list.size());
        assertEquals("user_010", list.get(0).getId());

        assetHolder.setId("user_010");
        assetHolder.setEmail(null);
        assetHolder.setPhone(null);
        list = assetHolderMapper.selectByAssetHolder(assetHolder);
        assertEquals(1, list.size());
        assertEquals("user_010", list.get(0).getId());
    }

    @Test
    @Order(12)
    public void insertAssetHolder() {
        AssetHolder assetHolder = new AssetHolder();
        assetHolder.setId("test_001");
        assetHolder.setName("testName");
        assetHolder.setEmail("testEmail");
        assetHolder.setPhone("testPhone");
        Instant now = Instant.now();
        assetHolder.setLastModified(now);
        int n = assetHolderMapper.insertAssetHolder(assetHolder);
        assertEquals(1, n);
        List<AssetHolder> list = assetHolderMapper.selectAssetHolderByID("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).getId());
        assertEquals("testName", list.get(0).getName());
        assertEquals("testEmail", list.get(0).getEmail());
        assertEquals("testPhone", list.get(0).getPhone());
        assertTrue(Duration.between(now, list.get(0).getLastModified()).abs().toMillis() < 1);

        assetHolderMapper.deleteAssetHolderByAssetHolderIDs(List.of("test_001"));
    }

    @Test
    @Order(13)
    public void updateAssetHolder() {
        AssetHolder assetHolder = new AssetHolder();
        assetHolder.setId("test_001");
        assetHolder.setName("testName");
        assetHolder.setEmail("testEmail");
        assetHolder.setPhone("testPhone");
        Instant now = Instant.now();
        assetHolder.setLastModified(now);
        assetHolderMapper.insertAssetHolder(assetHolder);

        assetHolder = new AssetHolder();
        assetHolder.setId("test_001");
        assetHolder.setName("testName1");
        assetHolder.setEmail(null);
        assetHolder.setPhone("");
        now = Instant.now();
        assetHolder.setLastModified(now);
        int n = assetHolderMapper.updateAssetHolder(assetHolder);
        assertEquals(1, n);
        List<AssetHolder> list = assetHolderMapper.selectAssetHolderByID("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).getId());
        assertEquals("testName1", list.get(0).getName());
        assertEquals("testEmail", list.get(0).getEmail());
        assertEquals("testPhone", list.get(0).getPhone());
        assertTrue(Duration.between(now, list.get(0).getLastModified()).abs().toMillis() < 1);

        assetHolder.setId(null);
        assertEquals(0, assetHolderMapper.updateAssetHolder(assetHolder));

        assetHolderMapper.deleteAssetHolderByAssetHolderIDs(List.of("test_001"));
    }

    @Test
    @Order(14)
    public void deleteAssetHolderByAssetHolderIDs() {
        AssetHolder assetHolder = new AssetHolder();
        assetHolder.setId("test_001");
        assetHolder.setName("testName");
        assetHolder.setEmail("testEmail");
        assetHolder.setPhone("testPhone");
        Instant now = Instant.now();
        assetHolder.setLastModified(now);
        assetHolderMapper.insertAssetHolder(assetHolder);
        assetHolder.setId("test_002");
        assetHolderMapper.insertAssetHolder(assetHolder);

        List<AssetHolder> list = assetHolderMapper.selectAllAssetHolders();
        assertEquals(52, list.size());

        int n = assetHolderMapper.deleteAssetHolderByAssetHolderIDs(List.of("", ""));
        assertEquals(0, n);

        n = assetHolderMapper.deleteAssetHolderByAssetHolderIDs(List.of("test_001", "test_002"));
        assertEquals(2, n);
        assertEquals(0, assetHolderMapper.selectAssetHolderByID("test_001").size());
        assertEquals(0, assetHolderMapper.selectAssetHolderByID("test_002").size());
        list = assetHolderMapper.selectAllAssetHolders();
        assertEquals(50, list.size());
    }
}

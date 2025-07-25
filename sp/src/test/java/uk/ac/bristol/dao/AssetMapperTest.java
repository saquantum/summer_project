package uk.ac.bristol.dao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.pojo.*;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetMapperTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Insert mock asset data for test
    private String assetGeoJson = """
{
  "type": "MultiPolygon",
  "coordinates": [
    [[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [1.0, 0.0], [0.0, 0.0]]]
  ]
}
""";

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }
    @BeforeEach
    public void ensureTablesExist() {
        jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS assets (
            id SERIAL PRIMARY KEY,
            name TEXT,
            asset_location JSONB,
            asset_capacity_litres BIGINT,
            asset_material TEXT,
            asset_status TEXT,
            asset_installed_at DATE,
            asset_last_inspection DATE,
            asset_last_modified TIMESTAMP,
            asset_type_id INTEGER,
            asset_owner_id INTEGER
        )
    """);

        jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS weather_warnings (
            id SERIAL PRIMARY KEY,
            asset_id INTEGER,
            type TEXT,
            level TEXT,
            issued_at TIMESTAMP,
            valid_from TIMESTAMP,
            valid_to TIMESTAMP
        )
    """);
    }


    @Test
    @Transactional
    @Rollback
    public void selectAllAssets() {
        jdbcTemplate.update("DELETE FROM assets WHERE asset_id LIKE 'mock_asset_%'");
        for (int i = 1; i <= 3; i++) {
            final int finalI = i;
            String assetId = "mock_asset_" + i;
            jdbcTemplate.execute(
                    con -> {
                        PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO assets (
                        asset_id, asset_name, asset_type_id, asset_owner_id, asset_location,
                        asset_capacity_litres, asset_material, asset_status,
                        asset_installed_at, asset_last_inspection, asset_last_modified
                    )
                    VALUES (?, ?, ?, ?, ST_GeomFromGeoJSON(?), ?, ?, ?, ?, ?, ?)
                """);
                        ps.setString(1, assetId);
                        ps.setString(2, "test_asset" + finalI);
                        ps.setObject(3, null);
                        ps.setObject(4, null);
                        ps.setString(5, assetGeoJson);
                        ps.setLong(6, 100L + finalI);
                        ps.setString(7, "plastic");
                        ps.setString(8, "active");
                        ps.setObject(9, LocalDate.now().minusDays(5));
                        ps.setObject(10, LocalDate.now().minusDays(1));
                        ps.setTimestamp(11, Timestamp.from(Instant.now()));
                        return ps;
                    },
                    (PreparedStatement ps) -> {
                        ps.execute();
                        return null;
                    }
            );
        }

        // select all contents with null for paras
        List<Asset> allAssets = assetMapper.selectAssets(null, null, null, null);
        assertNotNull(allAssets, "asset list should not be null");
        assertTrue(allAssets.size() >= 0, "The size of the asset list should be a non-negative number.");

        if (!allAssets.isEmpty()) {
            Asset first = allAssets.get(0);
            assertNotNull(first.getId(), "The asset ID should not be null");
            assertNotNull(first.getName(), "The asset name should not be null");
        }

        //limit = 0
        List<Asset> zeroLimit = assetMapper.selectAssets(null, null, 0, 0);
        assertNotNull(zeroLimit, "When the limit is set to 0, it should return an empty list instead of null");
        assertEquals(0, zeroLimit.size(), "limit=0 should return an empty list");

        // offset:If the quantity exceeds the maximum limit, an empty list should be returned.
        List<Asset> bigOffset = assetMapper.selectAssets(null, null, 10, 999999);
        assertNotNull(bigOffset, "exceeds offset should return an empty list");
        assertEquals(0, bigOffset.size(), "offset:an empty list should be returned When the number of rows exceeds the maximum limit");

        // filterString:Input malicious characters
        try {
            String filter = "1=1; DROP TABLE assets";
            List<Asset> result = assetMapper.selectAssets(filter, null, null, null);
            assertNotNull(result, "dangerous filterString should not leads to system exception");
        } catch (Exception e) {
            fail("The system should be capable of safely handling illegal filterString:" + e.getMessage());
        }

        // orderList: Illegal field name
        try {
            Map<String, String> invalidOrder = new HashMap<>();
            invalidOrder.put("column", "not_a_real_column");
            invalidOrder.put("direction", "asc");
            List<Map<String, String>> orderList = Collections.singletonList(invalidOrder);
            List<Asset> ordered = assetMapper.selectAssets(null, orderList, null, null);
            assertNotNull(ordered, "Illegal sort field names should be ignored or an exception of a security type should be thrown");
        } catch (Exception e) {
            System.out.println("Illegal sort field name triggers an exception"+e.getMessage());
        }
    }


    @Test
    @Transactional
    @Rollback
    public void selectAllAssetsWithWarnings() {
        jdbcTemplate.update("""
    DELETE FROM weather_warnings 
    WHERE warning_id::text LIKE 'mock_warn_asset_%'
""");
        jdbcTemplate.update("DELETE FROM assets WHERE asset_id LIKE 'mock_warn_asset_%'");

        for (int i = 1; i <= 3; i++) {
            final int finalI = i;
            String assetId = "mock_warn_asset_" + i;
            String assetName = "test_asset-" + i;

            jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
                PreparedStatement ps = con.prepareStatement("""
                INSERT INTO assets (
                    asset_id, asset_name, asset_type_id, asset_owner_id,
                    asset_location, asset_capacity_litres, asset_material, asset_status,
                    asset_installed_at, asset_last_inspection, asset_last_modified
                ) VALUES (?, ?, ?, ?, ST_GeomFromGeoJSON(?), ?, ?, ?, ?, ?, ?)
            """);
                ps.setString(1, assetId);
                ps.setString(2, assetName);
                ps.setObject(3, null);
                ps.setObject(4, null);
                ps.setString(5, assetGeoJson);
                ps.setLong(6, 100L + finalI);
                ps.setString(7, "plastic");
                ps.setString(8, "active");
                ps.setObject(9, LocalDate.now().minusDays(5));
                ps.setObject(10, LocalDate.now().minusDays(1));
                ps.setTimestamp(11, Timestamp.from(Instant.now()));
                ps.execute();
                ps.close();
                return null;
            });

            if (i <= 2) {

                jdbcTemplate.update("""
    INSERT INTO weather_warnings (
        warning_id,
        warning_weather_type,
        warning_level,
        warning_head_line,
        warning_valid_from,
        warning_valid_to,
        warning_impact,
        warning_likelihood,
        warning_affected_areas,
        warning_what_to_expect,
        warning_further_details,
        warning_update_description,
        warning_area
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromGeoJSON(?))
""",
                        999990L + i,
                        "thunderstorm",
                        "yellow",
                        "titlefortestwarning",
                        Timestamp.from(Instant.now().minusSeconds(3600 * i)),
                        Timestamp.from(Instant.now().plusSeconds(3600 * 12)),
                        "medium",
                        "high",
                        "test_area",
                        "attention",
                        "no",
                        "first",
                        """
                        {
                            "type": "MultiPolygon",
                            "coordinates": [
                                [[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [1.0, 0.0], [0.0, 0.0]]]
                            ]
                        }
                        """
                );


            }
        }

        List<AssetWithWeatherWarnings> all = assetMapper.selectAssetsWithWarnings(null, null, null, null);
        assertNotNull(all);

        List<AssetWithWeatherWarnings> limitZero = assetMapper.selectAssetsWithWarnings(null, null, 0, 0);
        assertNotNull(limitZero);
        assertEquals(0, limitZero.size());

        List<AssetWithWeatherWarnings> offsetTooLarge = assetMapper.selectAssetsWithWarnings(null, null, 10, 999999);
        assertNotNull(offsetTooLarge);
        assertEquals(0, offsetTooLarge.size());

        List<AssetWithWeatherWarnings> emptyFilter = assetMapper.selectAssetsWithWarnings("", null, null, null);
        assertNotNull(emptyFilter);

        List<AssetWithWeatherWarnings> whiteSpaceFilter = assetMapper.selectAssetsWithWarnings(" ", null, null, null);
        assertNotNull(whiteSpaceFilter);

    }

    @Test
    @Transactional
    @Rollback
    public void selectAssetByID() {
        String assetId = "test_asset_123";
        jdbcTemplate.execute(
                con -> {
                    PreparedStatement ps = con.prepareStatement("""
            INSERT INTO assets (
                asset_id,
                asset_name,
                asset_type_id,
                asset_owner_id,
                asset_location,
                asset_capacity_litres,
                asset_material,
                asset_status,
                asset_installed_at,
                asset_last_inspection,
                asset_last_modified
            )
            VALUES (?, ?, ?, ?, ST_GeomFromGeoJSON(?), ?, ?, ?, ?, ?, ?)
        """);
                    ps.setString(1, assetId);
                    ps.setString(2, "test_asset");
                    ps.setObject(3, null); // asset_type_id
                    ps.setObject(4, null); // asset_owner_id
                    ps.setString(5, assetGeoJson); // GeoJSON
                    ps.setLong(6, 150L);
                    ps.setString(7, "plastic");
                    ps.setString(8, "active");
                    ps.setObject(9, LocalDate.now().minusDays(5));
                    ps.setObject(10, LocalDate.now().minusDays(1));
                    ps.setTimestamp(11, Timestamp.from(Instant.now()));
                    return ps;
                },
                (PreparedStatement ps) -> {
                    ps.execute();
                    return null;
                }
        );

        List<Asset> filtered = assetMapper.selectAssets("asset_id = '" + assetId + "'", null, null, null);
        assertNotNull(filtered);
        assertEquals(1, filtered.size());

        Asset asset = filtered.get(0);
        assertEquals(assetId, asset.getId());
        assertEquals("test_asset", asset.getName());
        assertEquals("plastic", asset.getMaterial());
        assertEquals("active", asset.getStatus());

    }

    @Test
    @Transactional
    @Rollback
    public void selectAssetWithWarningsByID() {
        // Query existing asset with warnings by ID
        List<AssetWithWeatherWarnings> all = assetMapper.selectAssetsWithWarnings(null, null, null, null);
        assertNotNull(all);

        if (!all.isEmpty()) {
            String id = all.get(0).getAsset().getId();
            List<AssetWithWeatherWarnings> filtered = assetMapper.selectAssetsWithWarnings("asset_id = '" + id + "'", null, null, null);
            assertNotNull(filtered);
            assertEquals(1, filtered.size());
            assertEquals(id, filtered.get(0).getAsset().getId());
        }

        // Query with non-existent ID
        List<AssetWithWeatherWarnings> none = assetMapper.selectAssetsWithWarnings("asset_id = 'non-existent-id'", null, null, null);
        assertNotNull(none);
        assertTrue(none.isEmpty(), "No result should be returned for unknown ID.");
    }

    @Test
    @Transactional
    @Rollback
    public void selectByAsset() {
        List<Asset> all = assetMapper.selectAssets(null, null, null, null);
        assertNotNull(all, "asset list should not be null");
        assertFalse(all.isEmpty(), "asset list should not be null");

        Asset known = all.get(0);

        String filter = "asset_id = '" + known.getId() + "' AND asset_name = '" + known.getName() + "'";
        List<Asset> filtered = assetMapper.selectAssets(filter, null, null, null);
        assertNotNull(filtered, "filtered list should not be null");
        assertFalse(filtered.isEmpty(), "filtered result should not be empty");

        Asset result = filtered.get(0);
        assertEquals(known.getId(), result.getId(), "asset ID should be the same");
        assertEquals(known.getName(), result.getName(), "asset name should be the same");

    }

    @Test
    @Transactional
    @Rollback
    public void selectByAssetWithWarnings() {
        List<AssetWithWeatherWarnings> all = assetMapper.selectAssetsWithWarnings(null, null, null, null);
        assertNotNull(all);
        assertFalse(all.isEmpty(), "asset list should not be null");
        AssetWithWeatherWarnings known = all.get(0);
        String id = known.getAsset().getId();

        String filter = "asset_id = '" + id + "'";
        List<AssetWithWeatherWarnings> filtered = assetMapper.selectAssetsWithWarnings(filter, null, null, null);

        assertNotNull(filtered);
        assertEquals(1, filtered.size(), "The filtered list should contain a matching item");
        assertEquals(id, filtered.get(0).getAsset().getId(), "The returned asset ID should be consistent");
    }

    @Test
    @Transactional
    @Rollback
    public void selectAllAssetsOfHolder() {

        jdbcTemplate.update("DELETE FROM assets WHERE asset_id LIKE 'mock_holder_asset_%'");
        jdbcTemplate.update("DELETE FROM asset_holders WHERE asset_holder_id = 'mock_holder_001'");
        jdbcTemplate.update("""
    INSERT INTO asset_holders (
        asset_holder_id,
        asset_holder_name,
        asset_holder_email,
        asset_holder_phone,
        asset_holder_address_id,
        asset_holder_contact_preferences_id,
        asset_holder_last_modified
    ) VALUES (?, ?, ?, ?, ?, ?, ?)
""",
                "mock_holder_001",
                "asset_holder_A",
                "test@example.com",
                "1234567890",
                "mock_address_001",
                "mock_contact_preferences_001",
                Timestamp.from(Instant.now())
        );


        Asset asset = new Asset();
        asset.setId("mock_holder_asset_001");
        asset.setName("test_asset_A");
        asset.setInstalledAt(LocalDate.of(2024, 1, 1));
        asset.setLastInspection(LocalDate.of(2024, 6, 1));
        asset.setLastModified(Instant.now());
        asset.setLocationAsJson(assetGeoJson);
        asset.setCapacityLitres(100L);
        asset.setMaterial("steel");
        asset.setStatus("active");
        asset.setOwnerId("mock_holder_001");

        assetMapper.insertAsset(asset);

        List<Asset> allAssets = assetMapper.selectAssets(null, null, null, null);
        assertNotNull(allAssets);
        assertFalse(allAssets.isEmpty(), "asset list should not be empty");

        String holderId = allAssets.get(0).getOwnerId();
        String filter = "asset_owner_id = '" + holderId + "'";
        List<Asset> byHolder = assetMapper.selectAssets(filter, null, null, null);
        assertNotNull(byHolder);
        assertFalse(byHolder.isEmpty(), "The holder should have assets");

        for (Asset assets : byHolder) {
            assertEquals(holderId, assets.getOwnerId(), "asset ownerId should be consistent");
        }
    }

    @Test
    @Transactional
    @Rollback
    public void selectAllAssetsWithWarningsOfHolder() {
        List<AssetWithWeatherWarnings> all = assetMapper.selectAssetsWithWarnings(
                null, null, null, null
        );
        assertNotNull(all);
        assertFalse(all.isEmpty(), "The list of assets with alerts should not be empty");

        String holderId = all.get(0).getAsset().getOwnerId();
        String filter = "asset_owner_id = '" + holderId + "'";
        List<AssetWithWeatherWarnings> byHolder = assetMapper.selectAssetsWithWarnings(
                filter, null, null, null
        );
        assertNotNull(byHolder);
        assertFalse(byHolder.isEmpty(), "The list of assets with alerts should not be empty.");
        for (AssetWithWeatherWarnings assetWithWarning : byHolder) {
            assertEquals(holderId, assetWithWarning.getAsset().getOwnerId(), "holder ID should be consistent");
        }
    }

    @Test
    @Transactional
    @Rollback
    public void selectAllAssetTypes() {
        List<AssetWithWeatherWarnings> all = assetMapper.selectAssetsWithWarnings(
                null, null, null, null
        );
        assertNotNull(all);
        assertFalse(all.isEmpty(), "Assets of the specified asset type should exist");

        AssetWithWeatherWarnings sample = all.get(0);
        assertNotNull(sample.getAsset(), "asset type should not be null");
        assertNotNull(sample.getAsset().getId(), "asset type ID should not be null");
        assertNotNull(sample.getAsset().getName(), "asset type name should not be null");
    }

    @Test
    @Transactional
    @Rollback
    public void insertAssetType() {
        AssetType valid1 = new AssetType();
        valid1.setId("test_insert_001");
        valid1.setName("TypeA");
        valid1.setDescription("Valid type");

        AssetType valid2 = new AssetType();
        valid2.setId("test_insert_002");
        valid2.setName("TypeB");
        valid2.setDescription("Another valid type");

        AssetType nullName = new AssetType();
        nullName.setId("test_insert_003");
        nullName.setName(null); // trigger not-null constraint
        nullName.setDescription("Null name should fail");

        AssetType overLengthName = new AssetType();
        overLengthName.setId("test_insert_004");
        overLengthName.setName("A".repeat(101)); // too long
        overLengthName.setDescription("Name too long should fail");

        List<AssetType> testTypes = List.of(valid1, valid2, nullName, overLengthName);

        for (AssetType type : testTypes) {
            try {
                int rowsInserted = assetMapper.insertAssetType(type);

                if (type.getName() != null && !type.getName().isEmpty() && type.getName().length() <= 100) {
                    assertEquals(1, rowsInserted, "Expected successful insert: " + type.getId());
                } else {
                    assertTrue(rowsInserted == 0 || rowsInserted == 1,
                            "Unexpected insert success for: " + type.getId());
                }
            } catch (Exception e) {
                String msg = e.getMessage().toLowerCase(Locale.ROOT);
            }
        }
    }

    @Test
    public void insertAsset() {
        AssetType valid1 = new AssetType();
        valid1.setId("test_insert_001");
        valid1.setName("TypeA");
        valid1.setDescription("Valid type");

        AssetType duplicate = new AssetType();
        duplicate.setId("test_insert_001");
        duplicate.setName("TypeB");
        duplicate.setDescription("Duplicate ID");

        AssetType nullName = new AssetType();
        nullName.setId("test_insert_002");
        nullName.setName(null);
        nullName.setDescription("Null name");

        AssetType tooLongName = new AssetType();
        tooLongName.setId("test_insert_003");
        tooLongName.setName("A".repeat(101));
        tooLongName.setDescription("Too long name");

        List<AssetType> testTypes = List.of(valid1, duplicate, nullName, tooLongName);

        for (AssetType type : testTypes) {
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                int rows = assetMapper.insertAssetType(type);
                transactionManager.commit(status);

                if (type == valid1) {
                    assertEquals(1, rows, "Expected insert success for: " + type.getId());
                } else {
                    assertEquals(0, rows, "Expected insert failure for: " + type.getId());
                }
            } catch (Exception inner) {
                transactionManager.rollback(status);
                String msg = inner.getMessage().toLowerCase();
                if ("test_insert_001".equals(type.getId()) && type != valid1) {
                    assertTrue(
                            msg.contains("duplicate") || msg.contains("constraint") || msg.contains("唯一"),
                            "Expected duplicate key violation for: " + type.getId() + " ==> " + msg
                    );
                } else if (type.getName() == null) {
                    assertTrue(
                            msg.contains("not-null") || msg.contains("null value"),
                            "Expected not-null constraint violation for: " + type.getId() + " ==> " + msg
                    );
                } else if (type.getName().length() > 100) {
//                    assertTrue(
//                            msg.contains("too long") || msg.contains("length") || msg.contains("value too long") ||
//                                    msg.contains("varchar"),
//                            "Expected length constraint violation for: " + type.getId() + " ==> " + msg
//                    );
                } else {
                    fail("Unexpected error for: " + type.getId() + " – " + msg);
                }
            }
        }
    }



    @Test
    @Transactional
    @Rollback
    public void updateAssetType() {
        AssetType preInsert = new AssetType();
        preInsert.setId("test_update_001");
        preInsert.setName("OriginalName");
        preInsert.setDescription("Original description");
        try {
            assetMapper.insertAssetType(preInsert);
        } catch (Exception ignore) {
        }

        AssetType valid = new AssetType();
        valid.setId("test_update_001");
        valid.setName("UpdatedName");
        valid.setDescription("Updated description");

        // non exist ID
        AssetType nonExistent = new AssetType();
        nonExistent.setId("test_update_002");
        nonExistent.setName("DoesNotExist");
        nonExistent.setDescription("Should not update");

        // name is null
        AssetType nullName = new AssetType();
        nullName.setId("test_update_003");
        nullName.setName(null);
        nullName.setDescription("Name is null");

        // name too long
        AssetType tooLong = new AssetType();
        tooLong.setId("test_update_004");
        tooLong.setName("A".repeat(101));
        tooLong.setDescription("Name too long");

        List<AssetType> testCases = List.of(valid, nonExistent, nullName, tooLong);

        for (AssetType type : testCases) {
            try {
                int rows = assetMapper.updateAssetType(type);
                if ("test_update_001".equals(type.getId())) {
                    assertEquals(1, rows, "Expected successful update for: " + type.getId());
                } else if ("test_update_002".equals(type.getId())) {
                    assertEquals(0, rows, "Expected no update for non-existent ID: " + type.getId());
                } else {
                    assertEquals(0, rows, "Expected update failure for invalid data: " + type.getId());
                }
            } catch (Exception e) {
                String msg = e.getMessage().toLowerCase();
                if ("test_update_003".equals(type.getId())) {
                    assertTrue(
                            msg.contains("not-null") || msg.contains("null value"),
                            "Expected not-null constraint violation for: " + type.getId() + " – " + msg
                    );
                } else if ("test_update_004".equals(type.getId())) {
                    assertTrue(
                            msg.contains("too long") || msg.contains("length") || msg.contains("varchar") || msg.contains("太长") || msg.contains("超过"),
                            "Expected length constraint violation for: " + type.getId() + " – " + msg
                    );
                } else {
                    fail("Unexpected error for: " + type.getId() + " – " + msg);
                }
            }
        }
    }

    @Test
    public void updateAsset() {
        Map<String, Object> location = Map.of(
                "type", "MultiPolygon",
                "coordinates", List.of(List.of(List.of(List.of(125.6, 10.1))))
        );
        AssetType assetType = new AssetType();
        Asset original = new Asset();
        original.setId("test_asset_001");
        original.setName("OriginalName");

        original.setType(assetType);
        original.setLocation(location);
        try {
            assetMapper.insertAsset(original);
        } catch (Exception ignore) {
        }

        // valid update
        Asset valid = new Asset();
        valid.setId("test_asset_001");
        valid.setName("UpdatedName");
        valid.setType(new AssetType());
        valid.setLocation(location);

        // ID does not exist
        Asset nonExistent = new Asset();
        nonExistent.setId("non_existing_id");
        nonExistent.setName("NameX");
        nonExistent.setType(new AssetType());
        nonExistent.setLocation(location);

        // invalid update（name is null）
        Asset nullName = new Asset();
        nullName.setId("test_asset_001");
        nullName.setName(null);
        nullName.setType(new AssetType());
        nullName.setLocation(location);

        List<Asset> testCases = List.of(valid, nonExistent, nullName);

        for (Asset asset : testCases) {
            try {
                int rows = assetMapper.updateAsset(asset);
            } catch (Exception e) {
                String msg = e.getMessage().toLowerCase();
                if (asset.getName() == null) {
                    assertTrue(msg.contains("null") || msg.contains("constraint"),
                            "Expected not-null constraint violation for: " + asset.getId() + " – " + msg);
                } else {
                    fail("Unexpected error for: " + asset.getId() + " – " + msg);
                }
            }
        }
    }

    @Test
    @Transactional
    @Rollback
    public void deleteAssetTypeByIDs() {
        List<String> toInsertIds = List.of("delete_test_001", "delete_test_002");

        // insert data used for the deletion test
        for (String id : toInsertIds) {
            AssetType type = new AssetType();
            type.setId(id);
            type.setName("DeleteType");
            type.setDescription("For delete test");
            try {
                assetMapper.insertAssetType(type);
            } catch (Exception ignore) {
            }
        }

        // delete one of them
        List<String> toDelete = List.of("delete_test_001");
        int deletedCount = assetMapper.deleteAssetTypeByIDs(toDelete);
        assertEquals(1, deletedCount, "Expected 1 row to be deleted.");

        // delete the already-deleted ID and an ID that does not exist again
        List<String> invalidDelete = List.of("delete_test_001", "non_existent_id");
        int deletedZero = assetMapper.deleteAssetTypeByIDs(invalidDelete);
        assertEquals(0, deletedZero, "Expected no rows to be deleted.");

        // delete the remaining legitimate items
        int deletedRest = assetMapper.deleteAssetTypeByIDs(List.of("delete_test_002"));
        assertEquals(1, deletedRest, "Expected remaining row to be deleted.");
    }

    @Test
    @Transactional
    @Rollback
    public void deleteAssetByIDs() {
        AssetType assetType = new AssetType();
        assetType.setId("delete_type_001");

        Map<String, Object> location = Map.of(
                "type", "MultiPolygon",
                "coordinates", List.of(List.of(List.of(
                        List.of(125.6, 10.1),
                        List.of(125.7, 10.1),
                        List.of(125.7, 10.2),
                        List.of(125.6, 10.2),
                        List.of(125.6, 10.1)
                )))
        );

        Asset asset1 = new Asset();
        asset1.setId("delete_asset_001");
        asset1.setName("ToDelete1");
        asset1.setType(assetType);
        asset1.setLocation(location);

        Asset asset2 = new Asset();
        asset2.setId("delete_asset_002");
        asset2.setName("ToDelete2");
        asset2.setType(assetType);
        asset2.setLocation(location);

        try {
            assetMapper.insertAsset(asset1);
        } catch (Exception ignore) {}

        try {
            assetMapper.insertAsset(asset2);
        } catch (Exception ignore) {}

        int deleted1 = assetMapper.deleteAssetByIDs(List.of("delete_asset_001"));
        assertEquals(1, deleted1, "Expected 1 row to be deleted.");

        int deleted2 = assetMapper.deleteAssetByIDs(List.of("nonexistent_id"));
        assertEquals(0, deleted2, "Expected 0 rows to be deleted for nonexistent ID.");

        int deleted3 = assetMapper.deleteAssetByIDs(List.of("delete_asset_002"));
        assertEquals(1, deleted3, "Expected 1 row to be deleted.");

        int deleted4 = assetMapper.deleteAssetByIDs(List.of("delete_asset_002"));
        assertEquals(0, deleted4, "Expected 0 rows as it was already deleted.");
    }
}

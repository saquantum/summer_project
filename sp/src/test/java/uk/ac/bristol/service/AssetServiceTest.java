package uk.ac.bristol.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssetServiceTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AssetService assetService;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    void testGetAssets() {
        // Test without filters or ordering, should return all 53 assets in default order (by asset_row_id asc)
        List<Asset> assets = assetService.getAssets(null, null, null, null);
        assertThat(assets).hasSize(53);
        assertThat(assets.get(0).getId()).isEqualTo("asset_001");
        assertThat(assets.get(0).getName()).isEqualTo("Permeable Pavement #230");
        assertThat(assets.get(0).getTypeId()).isEqualTo("type_004");
        assertThat(assets.get(0).getOwnerId()).isEqualTo("user_020");
        assertThat(assets.get(0).getCapacityLitres()).isEqualTo(8898L); // Long
        assertThat(assets.get(0).getMaterial()).isEqualTo("Steel"); // varchar(50)
        assertThat(assets.get(0).getStatus()).isEqualTo("inactive"); // varchar(50)
        assertThat(assets.get(0).getInstalledAt()).isEqualTo(LocalDate.parse("1975-04-04")); // LocalDate
        assertThat(assets.get(0).getLastInspection()).isEqualTo(LocalDate.parse("1998-06-15")); // LocalDate
        assertThat(assets.get(0).getLastModified()).isNotNull(); // timestamp as Instant

        // Test with filters
        Map<String, Object> filters = new HashMap<>();
        filters.put("asset_type_id", "type_004");
        filters.put("asset_status", "inactive");
        List<Asset> filteredAssets = assetService.getAssets(filters, null, null, null);
        assertThat(filteredAssets).allMatch(asset -> "type_004".equals(asset.getTypeId()) && "inactive".equals(asset.getStatus()));

        // Test with ordering
        List<Map<String, String>> orderList = List.of(Map.of("column", "asset_capacity_litres", "order", "desc"));
        List<Asset> orderedAssets = assetService.getAssets(null, orderList, 5, 0);
        assertThat(orderedAssets).hasSize(5);
        assertThat(orderedAssets.get(0).getCapacityLitres()).isGreaterThanOrEqualTo(orderedAssets.get(1).getCapacityLitres());

        // Test pagination
        List<Asset> page1 = assetService.getAssets(null, null, 10, 0);
        assertThat(page1).hasSize(10);
        List<Asset> page2 = assetService.getAssets(null, null, 10, 10);
        assertThat(page2.get(0).getId()).isEqualTo("asset_011");
    }

    @Test
    void testGetAssetsWithWarnings() {
        // Assuming no active weather warnings in mock data (as no data provided for weather_warnings), warnings should be empty
        List<AssetWithWeatherWarnings> assets = assetService.getAssetsWithWarnings(null, null, null, null);
        assertThat(assets).hasSize(53);
        assertThat(assets.get(0).getAsset().getId()).isEqualTo("asset_001");
        assertThat(assets.get(0).getWarnings()).isEmpty();

        // Test with filter on asset fields
        Map<String, Object> filters = Map.of("asset_owner_id", "user_017");
        List<AssetWithWeatherWarnings> filtered = assetService.getAssetsWithWarnings(filters, null, null, null);
        assertThat(filtered).allMatch(aww -> "user_017".equals(aww.getAsset().getOwnerId()));

        // Test ordering including weather_warnings columns (should use special select if weather column in order)
        List<Map<String, String>> orderList = List.of(Map.of("column", "warning_level", "order", "asc"));
        List<AssetWithWeatherWarnings> ordered = assetService.getAssetsWithWarnings(null, orderList, 10, 0);
        assertThat(ordered).hasSize(10); // Even if no warnings, should handle

        // Since no warnings, test that it doesn't fail
    }

    @Test
    void testGetCursoredAssetsWithWarnings() {
        // Test without cursor, simplify false
        List<AssetWithWeatherWarnings> assets = assetService.getCursoredAssetsWithWarnings(false, null, null, null, 10, 0);
        assertThat(assets).hasSize(10);
        assertThat(assets.get(0).getAsset().getId()).isEqualTo("asset_001");

        // Test with cursor
        Long lastRowId = assets.get(9).getAsset().getRowId(); // Should be 10 for asset_010
        List<AssetWithWeatherWarnings> nextPage = assetService.getCursoredAssetsWithWarnings(false, lastRowId, null, null, 10, 0);
        assertThat(nextPage).hasSize(10);
        assertThat(nextPage.get(0).getAsset().getId()).isEqualTo("asset_011");

        // Test with simplify true
        List<AssetWithWeatherWarnings> simplified = assetService.getCursoredAssetsWithWarnings(true, null, null, null, 5, 0);
        assertThat(simplified).hasSize(5);
        // Assuming simplify limits warning fields, but since no warnings, same

        // Test invalid cursor
        assertThatThrownBy(() -> assetService.getCursoredAssetsWithWarnings(false, 9999L, null, null, 10, 0))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Found 0 anchors");
    }

    @Test
    void testGetAssetById() {
        Asset asset = assetService.getAssetById("asset_003");
        assertThat(asset.getName()).isEqualTo("Water Tank #955");
        assertThat(asset.getTypeId()).isEqualTo("type_001");
        assertThat(asset.getCapacityLitres()).isEqualTo(1572L); // Long
        assertThat(asset.getMaterial()).isEqualTo("Concrete"); // varchar(50)
        assertThat(asset.getStatus()).isEqualTo("inactive"); // varchar(50)
        assertThat(asset.getInstalledAt()).isEqualTo(LocalDate.parse("2013-01-04")); // LocalDate
        assertThat(asset.getLastInspection()).isEqualTo(LocalDate.parse("2011-11-28")); // LocalDate
        assertThat(asset.getLastModified()).isNotNull(); // timestamp as Instant

        // Test non-existent id
        assertThatThrownBy(() -> assetService.getAssetById("non_existent"))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Get 0 assets for asset id non_existent");

        // Test duplicate somehow, but since unique, not possible
    }

    @Test
    void testGetAssetWithWarningsById() {
        AssetWithWeatherWarnings aww = assetService.getAssetWithWarningsById("asset_004");
        assertThat(aww.getAsset().getName()).isEqualTo("Green Roof #952");
        assertThat(aww.getWarnings()).isEmpty(); // No warnings data

        assertThatThrownBy(() -> assetService.getAssetWithWarningsById("non_existent"))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Get 0 assets for asset id non_existent");
    }

    @Test
    void testGetAssetsByOwnerId() {
        List<Asset> assets = assetService.getAssetsByOwnerId("user_003", null, null, null);
        assertThat(assets).hasSize(3); // From data: asset_005, asset_007, asset_041
        assertThat(assets).allMatch(asset -> "user_003".equals(asset.getOwnerId()));

        // With pagination
        List<Asset> limited = assetService.getAssetsByOwnerId("user_003", null, 2, 0);
        assertThat(limited).hasSize(2);
    }

    @Test
    void testGetAssetsWithWarningsByOwnerId() {
        List<AssetWithWeatherWarnings> assets = assetService.getAssetsWithWarningsByOwnerId("user_017", null, null, null);
        assertThat(assets).hasSize(4); // asset_006, asset_019, asset_023, asset_024
        assertThat(assets).allMatch(aww -> "user_017".equals(aww.getAsset().getOwnerId()));
    }

//    @Test
//    void testGetAssetTypes() {
//        // Assuming mock data inserts asset types type_001 to type_007 based on usage
//        List<AssetType> types = assetService.getAssetTypes(null, null, null, null);
//        assertThat(types.size()).isAtLeast(7); // At least the used ones
//    }

    @Test
    void testGetCursoredAssetTypes() {
        List<AssetType> types = assetService.getCursoredAssetTypes(null, null, null, 3, 0);
        assertThat(types).hasSize(3);

        Long lastRowId = types.get(2).getRowId();
        List<AssetType> next = assetService.getCursoredAssetTypes(lastRowId, null, null, 3, 0);
        assertThat(next.get(0).getRowId()).isGreaterThan(lastRowId);
    }

    @Test
    void testGetAssetIdsIntersectingWithGivenWarning() {
        // Since no warnings data, for any id should return empty
        List<String> ids = assetService.getAssetIdsIntersectingWithGivenWarning(1L);
        assertThat(ids).isEmpty();
    }

    @Test
    void testGroupAssetLocationByRegion() {
        // Assuming no uk_regions data or warnings, but method pages through assets and calls getRegionNameGivenAsset
        // If regions not loaded, should return empty map or based on logic
        Map<String, Integer> groups = assetService.groupAssetLocationByRegion(null);
        // Placeholder assertion; adjust if regions data is implied
        assertThat(groups).isEmpty(); // Since no data for regions, likely empty
    }

    @Test
    void testCountAssetsWithFilter() {
        int count = assetService.countAssetsWithFilter(null);
        assertThat(count).isEqualTo(53);

        Map<String, Object> filters = Map.of("asset_material", "Steel", "asset_status", "inactive");
        int filteredCount = assetService.countAssetsWithFilter(filters);
        assertThat(filteredCount).isLessThan(53);
    }

    @Test
    void testCompareAssetLastModified() {
        Asset asset = assetService.getAssetById("asset_001");
        Instant lastModified = asset.getLastModified();
        long before = lastModified.toEpochMilli() - 1000;
        assertThat(assetService.compareAssetLastModified("asset_001", before)).isFalse(); // Modified after

        long after = lastModified.toEpochMilli() + 1000;
        assertThat(assetService.compareAssetLastModified("asset_001", after)).isTrue(); // Not modified after
    }

    @Test
    @Transactional
    void testInsertAssetType() {
        AssetType assetType = new AssetType();
        assetType.setName("Test Type");
        assetType.setDescription("Test Description");

        int inserted = assetService.insertAssetType(assetType);
        assertThat(inserted).isEqualTo(1);
        assertThat(assetType.getId()).startsWith("type_"); // Trigger sets id

        // Verify
        List<AssetType> types = assetService.getAssetTypes(Map.of("asset_type_type_id", assetType.getId()), null, null, null);
        assertThat(types).hasSize(1);
        assertThat(types.get(0).getName()).isEqualTo("Test Type");

        // Test with manual id
        AssetType manual = new AssetType();
        manual.setId("type_test");
        manual.setName("Manual Type");
        manual.setDescription("Desc");
        int manInserted = assetService.insertAssetType(manual);
        assertThat(manInserted).isEqualTo(1);
    }

    @Test
    @Transactional
    void testInsertAsset() {
        Asset asset = new Asset();
        asset.setName("Test Asset");
        asset.setTypeId("type_001"); // Assume exists
        asset.setOwnerId("user_001"); // Assume exists
        asset.setLocationAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-0.1,51.5],[-0.1,51.6],[0,51.6],[0,51.5],[-0.1,51.5]]]]}");
        asset.setCapacityLitres(5000L); // Long
        asset.setMaterial("Plastic"); // varchar(50)
        asset.setStatus("active"); // varchar(50)
        asset.setInstalledAt(LocalDate.parse("2024-01-01")); // LocalDate
        asset.setLastInspection(LocalDate.parse("2024-06-01")); // LocalDate

        String id = assetService.insertAssetReturningId(asset);
        assertThat(id).startsWith("asset_");

        Asset inserted = assetService.getAssetById(id);
        assertThat(inserted.getName()).isEqualTo("Test Asset");
        assertThat(inserted.getLastModified()).isNotNull();

        // Test with manual id
        Asset manual = new Asset();
        manual.setId("asset_test");
        manual.setName("Manual Asset");
        manual.setTypeId("type_001");
        manual.setOwnerId("user_001");
        manual.setLocationAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-0.2,51.4],[-0.2,51.5],[-0.1,51.5],[-0.1,51.4],[-0.2,51.4]]]]}");
        manual.setCapacityLitres(3000L); // Long
        manual.setMaterial("Concrete"); // varchar(50)
        manual.setStatus("maintenance"); // varchar(50)
        manual.setInstalledAt(LocalDate.parse("2023-01-01")); // LocalDate
        manual.setLastInspection(LocalDate.parse("2023-12-01")); // LocalDate
        int ins = assetService.insertAsset(manual);
        assertThat(ins).isEqualTo(1);
    }

    @Test
    @Transactional
    void testUpdateAssetType() {
        // Assume type_001 exists; if not, insert first
        AssetType type = new AssetType();
        type.setId("type_001");
        type.setName("Updated Water Tank");
        type.setDescription("Updated Desc");

        int updated = assetService.updateAssetType(type);
        assertThat(updated).isEqualTo(1);

        List<AssetType> updatedType = assetService.getAssetTypes(Map.of("asset_type_type_id", "type_001"), null, null, null);
        assertThat(updatedType.get(0).getName()).isEqualTo("Updated Water Tank");
    }

    @Test
    @Transactional
    void testUpdateAsset() {
        Asset asset = assetService.getAssetById("asset_005");
        String originalLocation = asset.getLocationAsJson();
        asset.setName("Updated Retention Pond");
        asset.setStatus("maintenance");
        asset.setCapacityLitres(3000L);
        asset.setInstalledAt(LocalDate.parse("1970-04-10")); // LocalDate
        asset.setLastInspection(LocalDate.parse("1997-09-07")); // LocalDate

        int updated = assetService.updateAsset(asset);
        assertThat(updated).isEqualTo(1);

        Asset updatedAsset = assetService.getAssetById("asset_005");
        assertThat(updatedAsset.getName()).isEqualTo("Updated Retention Pond");
        assertThat(updatedAsset.getStatus()).isEqualTo("maintenance");
        assertThat(updatedAsset.getLastModified()).isAfter(asset.getLastModified());

        // Test updating location
        Asset locUpdate = assetService.getAssetById("asset_006");
        locUpdate.setLocationAsJson("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-1,1],[-1,2],[0,2],[0,1],[-1,1]]]]}");
        int locUpdated = assetService.updateAsset(locUpdate);
        assertThat(locUpdated).isEqualTo(1);
        // Since no warnings, no notifications sent
    }

    @Test
    @Transactional
    void testDeleteAssetTypeByIDs() {
        // Assume type_007 can be deleted if no assets reference, but from data it has
        // To test, insert a new one and delete
        AssetType temp = new AssetType();
        temp.setName("Delete Me");
        temp.setDescription("Temp");
        assetService.insertAssetType(temp);
        String id = temp.getId();
        assertThat(id).isNotNull(); // 防御性检查
        int deleted = assetService.deleteAssetTypeByIDs(Collections.singletonList(id));
        assertThat(deleted).isEqualTo(1);

        List<AssetType> check = assetService.getAssetTypes(Map.of("asset_type_type_id", id), null, null, null);
        assertThat(check).isEmpty();
    }

    @Test
    @Transactional
    void testDeleteAssetByIDs() {
        int deleted = assetService.deleteAssetByIDs(List.of("asset_050"));
        assertThat(deleted).isEqualTo(1);

        assertThatThrownBy(() -> assetService.getAssetById("asset_050"))
                .hasMessageContaining("Get 0 assets");

        // Test multiple
        int multiDeleted = assetService.deleteAssetByIDs(new String[]{"asset_051", "asset_052"});
        assertThat(multiDeleted).isEqualTo(2);
    }
}
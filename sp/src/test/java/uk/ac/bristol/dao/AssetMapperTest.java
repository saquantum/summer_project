//package uk.ac.bristol.dao;
//
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import uk.ac.bristol.MockDataInitializer;
//import uk.ac.bristol.pojo.AssetHolder;
//import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class AssetMapperTest {
//
//    @Autowired
//    private MockDataInitializer mockDataInitializer;
//
//    @Autowired
//    private AssetHolderMapper assetHolderMapper;
//
//    @Autowired
//    private AssetMapper assetMapper;
//
//    @Autowired
//    private WarningMapper warningMapper;
//
//    @BeforeAll
//    public void init() throws IOException {
//        mockDataInitializer.forceReload();
//    }
//
//    @Test
//    @Order(1)
//    public void selectAllAssets() {
//    }
//
//    @Test
//    @Order(2)
//    public void selectAllAssetsWithWarnings() {
//    }
//
//    @Test
//    @Order(3)
//    public void selectAssetByID() {
//    }
//
//    @Test
//    @Order(4)
//    public void selectAssetWithWarningsByID() {
//    }
//
//    @Test
//    @Order(5)
//    public void selectByAsset() {
//    }
//
//    @Test
//    @Order(6)
//    public void selectByAssetWithWarnings() {
//    }
//
//    @Test
//    @Order(7)
//    public void selectAllAssetsOfHolder() {
//    }
//
//    @Test
//    @Order(8)
//    public void selectAllAssetsWithWarningsOfHolder() {
//    }
//
//    @Test
//    @Order(9)
//    public void selectAllAssetTypes() {
//    }
//
//    @Test
//    @Order(10)
//    public void insertAssetType() {
//    }
//
//    @Test
//    @Order(11)
//    public void insertAsset() {
//    }
//
//    @Test
//    @Order(12)
//    public void updateAssetType() {
//    }
//
//    @Test
//    @Order(13)
//    public void updateAsset() {
//    }
//
//    @Test
//    @Order(14)
//    public void deleteAssetTypeByIDs() {
//    }
//
//    @Test
//    @Order(15)
//    public void deleteAssetByIDs() {
//    }
//
//
//}

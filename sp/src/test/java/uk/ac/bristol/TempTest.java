package uk.ac.bristol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ImportMockData;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;

@SpringBootTest
public class TempTest {

    @Autowired
    public ImportMockData importMockData;

    @Autowired
    public UserService userService;

    @Autowired
    public AssetService assetService;

    @Autowired
    public WarningService warningService;

    @Test
    public void test() {
        importMockData.resetSchema();
        importMockData.importUsers("src/main/resources/data/users.json");
        importMockData.importAssets("src/main/resources/data/asset_types.json", "src/main/resources/data/assets.json");
        importMockData.importWarnings("src/main/resources/data/arcgis-sample.json");
    }

    @Test
    public void test1() {
        System.out.println(userService.getUserByUserId("user_001"));
        System.out.println(assetService.getAssetWithWarningsById("asset_050"));
    }

}

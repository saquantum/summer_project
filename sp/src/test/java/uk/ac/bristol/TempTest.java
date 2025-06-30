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

}

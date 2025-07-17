package uk.ac.bristol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.service.*;

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
    private ContactService contactService;
    @Autowired
    private UserMapper userMapper;

    @Test
    void test1() {
        System.out.println(userService.groupUsersWithOwnedAssetsByWarningId(1000, 0L, 1111L, false, null));
    }

}

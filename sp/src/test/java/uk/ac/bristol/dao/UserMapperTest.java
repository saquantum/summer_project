package uk.ac.bristol.dao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserMapperTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AssetHolderMapper assetHolderMapper;

    @Autowired
    private UserMapper userMapper;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    @Order(1)
    public void selectAllUsers() {
        List<User> list = userMapper.selectAllUsers(null, null, null);
        assertTrue(list.size() >= 50);
        assertNotNull(list.get(30));
    }

    @Test
    @Order(2)
    public void selectUserById() {
        List<User> list = userMapper.selectUserById("user_026");
        assertEquals(1, list.size());
        User user = list.get(0);
        assertEquals("user_026", user.getId());
        assertEquals("123456", user.getPassword());
        assertEquals("user_026", user.getAssetHolderId());
        assertFalse(user.isAdmin());

        list = userMapper.selectUserById("admin");
        assertEquals(1, list.size());
        user = list.get(0);
        assertEquals("admin", user.getId());
        assertEquals("admin", user.getPassword());
        assertNull(user.getAssetHolderId());
        assertTrue(user.isAdmin());
    }

    @Test
    @Order(3)
    public void loginQuery() {
        User user1 = new User();
        user1.setId("user_026");
        user1.setPassword("123456");
        List<User> queried1 = userMapper.loginQuery(user1);
        assertEquals("user_026", queried1.get(0).getId());
        assertEquals("123456", queried1.get(0).getPassword());
        assertEquals("user_026", queried1.get(0).getAssetHolderId());
        assertFalse(queried1.get(0).isAdmin());

        User user2 = new User();
        user2.setId("admin");
        user2.setPassword("admin");
        List<User> queried2 = userMapper.loginQuery(user2);
        assertEquals("admin", queried2.get(0).getId());
        assertEquals("admin", queried2.get(0).getPassword());
        assertNull(queried2.get(0).getAssetHolderId());
        assertTrue(queried2.get(0).isAdmin());

        User user3 = new User();
        user3.setId("unknown_test");
        user3.setPassword("123456");
        assertEquals(0, userMapper.loginQuery(user3).size());
    }

    @Test
    @Order(4)
    public void selectUserByAssetHolderId() {
        List<User> list = userMapper.selectUserByAssetHolderId("user_005");
        assertEquals(1, list.size());
        User user = list.get(0);
        assertEquals("user_005", user.getId());
        assertEquals("123456", user.getPassword());
        assertEquals("user_005", user.getAssetHolderId());
        assertFalse(user.isAdmin());

        assertEquals(0, userMapper.selectUserByAssetHolderId("admin").size());
        assertEquals(0, userMapper.selectUserByAssetHolderId("user_055").size());
    }

    @Test
    @Order(5)
    public void selectUserByAdmin() {
        assertEquals(1, userMapper.selectUserByAdmin(true, null, null, null).size());
        assertEquals(50, userMapper.selectUserByAdmin(false, null, null, null).size());
    }

    @Test
    @Order(6)
    public void selectAllAssetHoldersWithAccumulator() {
        // いまだ未完成でテスト必要はない
    }

    @Test
    @Order(7)
    public void insertUser() {
        AssetHolder assetHolder = new AssetHolder();
        assetHolder.setId("test_001");
        assetHolder.setName("test_001");
        assetHolder.setEmail("test_001");
        assetHolder.setPhone("test_001");
        assetHolderMapper.insertAssetHolder(assetHolder);

        User user = new User();
        user.setId("test_001");
        user.setPassword("123456");
        user.setAssetHolderId("test_001");
        user.setAdmin(false);
        assertEquals(1, userMapper.insertUser(user));
        List<User> list = userMapper.selectUserById("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).getId());
        assertEquals("123456", list.get(0).getPassword());
        assertEquals("test_001", list.get(0).getAssetHolderId());
        assertFalse(list.get(0).isAdmin());

        assertThrows(DuplicateKeyException.class, () -> userMapper.insertUser(user));

        User admin = new User();
        admin.setId("test_002");
        admin.setPassword("admin");
        admin.setAssetHolderId(null);
        admin.setAdmin(true);
        assertEquals(1, userMapper.insertUser(admin));
        list = userMapper.selectUserById("test_002");
        assertEquals(1, list.size());
        assertEquals("test_002", list.get(0).getId());
        assertEquals("admin", list.get(0).getPassword());
        assertNull(list.get(0).getAssetHolderId());
        assertTrue(list.get(0).isAdmin());

        assetHolderMapper.deleteAssetHolderByAssetHolderIDs(List.of("test_001", "test_002"));
        userMapper.deleteUserByIds(List.of("test_001", "test_002"));
    }

    @Test
    @Order(8)
    public void updateUserByUserId() {
        User user = new User();
        user.setId("test_001");
        user.setPassword("123456");
        user.setAssetHolderId("user_001");
        user.setAdmin(false);
        userMapper.insertUser(user);
        List<User> list = userMapper.selectUserById("test_001");
        assertEquals("test_001", list.get(0).getId());
        assertEquals("123456", list.get(0).getPassword());
        assertEquals("user_001", list.get(0).getAssetHolderId());
        assertFalse(list.get(0).isAdmin());

        user.setPassword("qwerty");
        user.setAssetHolderId("user_033");
        user.setAdmin(true);
        int n = userMapper.updateUserByUserId(user);
        assertEquals(1, n);
        list = userMapper.selectUserById("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).getId());
        assertEquals("qwerty", list.get(0).getPassword());
        assertEquals("user_033", list.get(0).getAssetHolderId());
        assertTrue(list.get(0).isAdmin());

        user.setPassword("");
        user.setAssetHolderId(null);
        n = userMapper.updateUserByUserId(user);
        assertEquals(1, n);
        list = userMapper.selectUserById("test_001");
        assertEquals(1, list.size());
        assertEquals("test_001", list.get(0).getId());
        assertEquals("qwerty", list.get(0).getPassword());
        assertEquals("user_033", list.get(0).getAssetHolderId());
        assertTrue(list.get(0).isAdmin());

        userMapper.deleteUserByIds(List.of("test_001", "test_002"));
    }

    @Test
    @Order(9)
    public void deleteUserByAssetHolderIDs() {
        User user = new User();
        user.setId("test_001");
        user.setPassword("123456");
        user.setAssetHolderId("test_holder_001");
        userMapper.insertUser(user);
        user.setId("test_002");
        user.setPassword("123456");
        user.setAssetHolderId("test_holder_002");
        userMapper.insertUser(user);

        assertEquals(1, userMapper.selectUserByAssetHolderId("test_holder_001").size());
        assertEquals(1, userMapper.selectUserByAssetHolderId("test_holder_002").size());

        int n = userMapper.deleteUserByAssetHolderIDs(List.of("test_holder_001", "test_holder_002"));
        assertEquals(2, n);
        assertEquals(0, userMapper.selectUserByAssetHolderId("test_holder_001").size());
        assertEquals(0, userMapper.selectUserByAssetHolderId("test_holder_002").size());
    }

    @Test
    @Order(10)
    public void deleteUserByIds() {
        User user = new User();
        user.setId("test_001");
        user.setPassword("123456");
        userMapper.insertUser(user);
        user.setId("test_002");
        user.setPassword("123456");
        userMapper.insertUser(user);

        assertEquals(1, userMapper.selectUserById("test_001").size());
        assertEquals(1, userMapper.selectUserById("test_002").size());

        int n = userMapper.deleteUserByIds(List.of("test_001", "test_002"));
        assertEquals(2, n);
        assertEquals(0, userMapper.selectUserById("test_001").size());
        assertEquals(0, userMapper.selectUserById("test_002").size());
    }
}

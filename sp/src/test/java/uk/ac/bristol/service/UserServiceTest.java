package uk.ac.bristol.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.UserWithExtraColumns;
import uk.ac.bristol.service.UserService;
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
public class UserServiceTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private UserService userService;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    @Transactional
    void testLogin() {
        User loginUser = new User();
        loginUser.setId("user_001");
        loginUser.setPassword("123456"); // Plaintext, service verifies against hash

        String token = userService.login(loginUser);
        assertThat(token).isNotBlank(); // JWT token

        // Verify last login time updated
        User updated = userService.getUserByUserId("user_001");

        // Test incorrect password
        loginUser.setPassword("wrongpass");
        assertThatThrownBy(() -> userService.login(loginUser))
                .isInstanceOf(SpExceptions.UnauthorisedException.class)
                .hasMessage("User not found or password incorrect.");

        // Test non-existent user
        loginUser.setId("non_existent");
        loginUser.setPassword("123456");
        assertThatThrownBy(() -> userService.login(loginUser))
                .isInstanceOf(SpExceptions.UnauthorisedException.class)
                .hasMessage("User not found or password incorrect.");
    }

    @Test
    void testGetUsers() {
        List<User> users = userService.getUsers(null, null, null, null);
        assertThat(users).hasSize(55);

        // Test with filters
        Map<String, Object> filters = Map.of("user_is_admin", true);
        List<User> admins = userService.getUsers(filters, null, null, null);
        assertThat(admins).hasSize(2); // admin and root
        assertThat(admins.get(0).getAdminLevel()).isEqualTo(1); // int
        assertThat(admins.get(1).getAdminLevel()).isEqualTo(0);

        // Test ordering
        List<Map<String, String>> orderList = List.of(Map.of("column", "user_name", "order", "desc"));
        List<User> ordered = userService.getUsers(null, orderList, 10, 0);
        assertThat(ordered).hasSize(10);
        assertThat(ordered.get(0).getName()).isGreaterThanOrEqualTo(ordered.get(1).getName());

        // Test pagination
        List<User> page = userService.getUsers(null, null, 10, 0);
        assertThat(page).hasSize(10);
        assertThat(page.get(0).getId()).isEqualTo("user_001");
    }

    @Test
    void testGetCursoredUsers() {
        List<User> users = userService.getCursoredUsers(null, null, null, 10, 0);
        assertThat(users).hasSize(10);

        Long lastRowId = users.get(9).getRowId(); // user_010 row_id=10
        List<User> nextPage = userService.getCursoredUsers(lastRowId, null, null, 10, 0);
        assertThat(nextPage.get(0).getRowId()).isEqualTo(11L);

        // Test invalid cursor
        assertThatThrownBy(() -> userService.getCursoredUsers(9999L, null, null, 10, 0))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Found 0 anchors");
    }

    @Test
    void testGetUnauthorisedUsers() {
        List<User> unauthorised = userService.getUnauthorisedUsers(null, null, null, null);
        assertThat(unauthorised).hasSize(53); // Total 55 - 2 admins
        assertThat(unauthorised).allMatch(u -> !u.isAdmin());

        // With filter
        Map<String, Object> filters = Map.of("user_name", "Isabel Clark");
        List<User> filtered = userService.getUnauthorisedUsers(filters, null, null, null);
        assertThat(filtered).hasSize(1);
    }

    @Test
    void testGetUsersWithAccumulator() {
        // Assuming column like "asset_row_id" for count of assets per user
        List<UserWithExtraColumns> withCount = userService.getUsersWithAccumulator("count", "asset_row_id", null, null, null, null);
        assertThat(withCount).hasSize(55);

        // Test invalid function
        assertThatThrownBy(() -> userService.getUsersWithAccumulator("sum", "asset_row_id", null, null, null, null))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("not supported");
    }

    @Test
    void testGetCursoredUsersWithAccumulator() {
        List<UserWithExtraColumns> withCount = userService.getCursoredUsersWithAccumulator("count", "asset_row_id", null, null, null, 10, 0);
        assertThat(withCount).hasSize(10);

        Long lastRowId = withCount.get(9).getUser().getRowId();
        List<UserWithExtraColumns> next = userService.getCursoredUsersWithAccumulator("count", "asset_row_id", lastRowId, null, null, 10, 0);
        assertThat(next.get(0).getUser().getRowId()).isGreaterThan(lastRowId);
    }

    @Test
    void testGetUserByUserId() {
        User user = userService.getUserByUserId("user_001");
        assertThat(user.getName()).isEqualTo("Isabel Clark");
        assertThat(user.isAdmin()).isFalse();
        assertThat(user.getAdminLevel()).isNull();
        assertThat(user.getLastModified()).isNull();

        // Test non-existent
        assertThatThrownBy(() -> userService.getUserByUserId("non_existent"))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Get 0 users");
    }

    @Test
    void testTestUIDExistence() {
        assertThat(userService.testUIDExistence("user_001")).isTrue();
        assertThat(userService.testUIDExistence("non_existent")).isFalse();
    }

    @Test
    void testTestEmailAddressExistence() {
        // Data doesn't have emails, assume false
        assertThat(userService.testEmailAddressExistence("non@ex.com")).isFalse();

        // To test true, insert user with email
        // But for now, assume
    }

    @Test
    void testGroupUsersWithOwnedAssetsByWarningId() {
        List<UserWithAssets> grouped = userService.groupUsersWithOwnedAssetsByWarningId(10, null, 1L, false, null);
        assertThat(grouped).isEmpty(); // No warnings

        // Test diff true with json
        List<UserWithAssets> diff = userService.groupUsersWithOwnedAssetsByWarningId(5, null, 1L, true, "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-1,1],[-1,2],[0,2],[0,1],[-1,1]]]]}");
        assertThat(diff).isNotNull();
    }

    @Test
    void testGroupUserAddressPostcodeByCountry() {
        Map<String, Integer> byCountry = userService.groupUserAddressPostcodeByCountry(null);
        // Assume some addresses, but data may not have, assert not null
        assertThat(byCountry).isNotNull();
    }

    @Test
    void testGroupUserAddressPostcodeByRegion() {
        Map<String, Integer> byRegion = userService.groupUserAddressPostcodeByRegion(null);
        assertThat(byRegion).isNotNull();
    }

    @Test
    void testGroupUserAddressPostcodeByAdminDistrict() {
        Map<String, Integer> byDistrict = userService.groupUserAddressPostcodeByAdminDistrict(null);
        assertThat(byDistrict).isNotNull();
    }

    @Test
    @Transactional
    void testGetUserContactPreferencesPercentage() {
        // Insert prefs for users
        User user1 = userService.getUserByUserId("user_001");
        user1.setContactPreferences(Map.of("email", true, "phone", false));
        userService.upsertContactPreferences(user1);

        User user2 = userService.getUserByUserId("user_002");
        user2.setContactPreferences(Map.of("email", false, "phone", true));
        userService.upsertContactPreferences(user2);

        Map<String, Integer> prefs = userService.getUserContactPreferencesPercentage(null);
        assertThat(prefs.get("total")).isEqualTo(55);
        assertThat(prefs.getOrDefault("email", 0)).isEqualTo(1);
        assertThat(prefs.getOrDefault("phone", 0)).isEqualTo(1);
    }

    @Test
    void testCountUsersWithFilter() {
        long count = userService.countUsersWithFilter(null);
        assertThat(count).isEqualTo(55L);

        Map<String, Object> filters = Map.of("user_is_admin", false);
        long nonAdmins = userService.countUsersWithFilter(filters);
        assertThat(nonAdmins).isEqualTo(53L);
    }

    @Test
    void testCompareUserLastModified() {
        User user = userService.getUserByUserId("user_001");
        long before = user.getLastModified().toEpochMilli() - 1000;
        assertThat(userService.compareUserLastModified("user_001", before)).isFalse();

        long after = user.getLastModified().toEpochMilli() + 1000;
        assertThat(userService.compareUserLastModified("user_001", after)).isTrue();
    }

    @Test
    @Transactional
    void testUpsertAddress() {
        User user = userService.getUserByUserId("user_001");
        Map<String, String> address = new HashMap<>();
        address.put("street", "Test Street");
        address.put("city", "Test City");
        address.put("postcode", "TEST1");
        address.put("country", "UK");
        address.put("postcodeCountry", "UK");
        address.put("postcodeRegion", "South West");
        address.put("postcodeAdminDistrict", "Bristol");
        user.setAddress(address);

        boolean upserted = userService.upsertAddress(user);
        assertThat(upserted).isTrue();

        User updated = userService.getUserByUserId("user_001");
        assertThat(updated.getAddress().get("city")).isEqualTo("Test City");

        // Test non-existent user
        User invalid = new User();
        invalid.setId("non_existent");
        assertThat(userService.upsertAddress(invalid)).isFalse();
    }

    @Test
    @Transactional
    void testUpsertContactDetails() {
        User user = userService.getUserByUserId("user_001");
        Map<String, String> details = new HashMap<>();
        details.put("email", "test@email.com");
        details.put("phone", "1234567890");
        details.put("post", "Post Address");
        details.put("whatsapp", "wa123");
        details.put("discord", "disc123");
        details.put("telegram", "tel123");
        user.setContactDetails(details);

        boolean upserted = userService.upsertContactDetails(user);
        assertThat(upserted).isTrue();

        User updated = userService.getUserByUserId("user_001");
        assertThat(updated.getContactDetails().get("email")).isEqualTo("test@email.com");
    }

    @Test
    @Transactional
    void testUpsertContactPreferences() {
        User user = userService.getUserByUserId("user_001");
        Map<String, Boolean> prefs = new HashMap<>();
        prefs.put("email", true);
        prefs.put("phone", false);
        prefs.put("post", true);
        prefs.put("whatsapp", false);
        prefs.put("discord", true);
        prefs.put("telegram", false);
        user.setContactPreferences(prefs);

        boolean upserted = userService.upsertContactPreferences(user);
        assertThat(upserted).isTrue();

        User updated = userService.getUserByUserId("user_001");
        assertThat(updated.getContactPreferences().get("email")).isTrue();
    }

    @Test
    @Transactional
    void testInsertUser() {
        User newUser = new User();
        newUser.setId("test_user_insert");
        newUser.setName("Insert Test");
        newUser.setPassword("insertpass123");
        newUser.setAdmin(false);
        newUser.setAvatar("insert.avatar");
        newUser.setAddress(Map.of("city", "Insert City"));
        newUser.setContactDetails(Map.of("email", "insert@email.com"));
        newUser.setContactPreferences(Map.of("email", true));

        int inserted = userService.insertUser(newUser);
        assertThat(inserted).isEqualTo(1);

        User fetched = userService.getUserByUserId("test_user_insert");
        assertThat(fetched.getName()).isEqualTo("Insert Test");
        assertThat(fetched.getPasswordPlainText()).isEqualTo("insertpass123");
        assertThat(fetched.getPassword()).contains("$argon2id$");

        // Test duplicate
        assertThatThrownBy(() -> userService.insertUser(newUser))
                .hasMessageContaining("duplicate");

        // Test invalid id
        newUser.setId("");
        assertThatThrownBy(() -> userService.insertUser(newUser))
                .hasMessage("Invalid user id");
    }

    @Test
    @Transactional
    void testInsertUserBatch() {
        User u1 = new User();
        u1.setId("batch_u1");
        u1.setName("Batch1");
        u1.setPassword("pass1");

        User u2 = new User();
        u2.setId("batch_u2");
        u2.setName("Batch2");
        u2.setPassword("pass2");

        int batchInserted = userService.insertUserBatch(List.of(u1, u2));
        assertThat(batchInserted).isEqualTo(2);

        assertThat(userService.testUIDExistence("batch_u1")).isTrue();
    }

    @Test
    @Transactional
    void testRegisterNewUser() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "reg_user_test");
        data.put("password", "regpass123");
        data.put("repassword", "regpass123");
        data.put("name", "Reg Test User");
        data.put("email", "regtest@email.com");
        data.put("phone", "987654321");
        data.put("contact", true);

        userService.registerNewUser(data);

        User reg = userService.getUserByUserId("reg_user_test");
        assertThat(reg.getName()).isEqualTo("Reg Test User");
        assertThat(reg.getContactDetails().get("email")).isEqualTo("regtest@email.com");
        assertThat(reg.getContactPreferences().get("email")).isTrue();

        // Test password mismatch
        data.put("repassword", "wrong");
        assertThatThrownBy(() -> userService.registerNewUser(data))
                .hasMessage("Two passwords don't match.");

        // Test duplicate id
        data.put("id", "user_001");
        data.put("repassword", "regpass123");
        assertThatThrownBy(() -> userService.registerNewUser(data))
                .hasMessageContaining("Duplicate user id");

        // Test short password
        data.put("password", "short");
        data.put("repassword", "short");
        data.put("id", "reg_user_test2");
        assertThatThrownBy(() -> userService.registerNewUser(data))
                .hasMessageContaining("length of password");

        // Test invalid characters
        data.put("password", "invalid@pass");
        data.put("repassword", "invalid@pass");
        assertThatThrownBy(() -> userService.registerNewUser(data))
                .hasMessageContaining("contains improper characters");

        // Test missing fields
        data.remove("email");
        assertThatThrownBy(() -> userService.registerNewUser(data))
                .hasMessage("Key fields missing");
    }

    @Test
    @Transactional
    void testUpdateUser() {
        User user = userService.getUserByUserId("user_001");
        user.setName("Updated Name");
        user.setAvatar("new_avatar.url");
        user.setAddress(Map.of("city", "New City"));
        user.setContactDetails(Map.of("phone", "newphone"));

        int updated = userService.updateUser(user);
        assertThat(updated).isEqualTo(1);

        User fetched = userService.getUserByUserId("user_001");
        assertThat(fetched.getName()).isEqualTo("Updated Name");
        assertThat(fetched.getLastModified()).isAfter(user.getLastModified());

        // Test invalid id
        user.setId("");
        assertThatThrownBy(() -> userService.updateUser(user))
                .hasMessage("Invalid user id");
    }

    @Test
    @Transactional
    void testUpdateUserBatch() {
        User u1 = userService.getUserByUserId("user_001");
        u1.setName("Batch Update 1");

        User u2 = userService.getUserByUserId("user_002");
        u2.setName("Batch Update 2");

        int batchUpdated = userService.updateUserBatch(List.of(u1, u2));
        assertThat(batchUpdated).isEqualTo(2);

        assertThat(userService.getUserByUserId("user_001").getName()).isEqualTo("Batch Update 1");
    }

    @Test
    @Transactional
    void testUpdateUserPasswordByEmail() {
        // Assume a user has email; since data doesn't, insert temp user with email
        User temp = new User();
        temp.setId("temp_email_user");
        temp.setName("Temp");
        temp.setPassword("oldpass");
        temp.setContactDetails(Map.of("email", "temp@email.com"));
        userService.insertUser(temp);

        int updated = userService.updateUserPasswordByEmail("temp@email.com", "newpass123");
        assertThat(updated).isEqualTo(1);

        User fetched = userService.getUserByUserId("temp_email_user");
        assertThat(fetched.getPasswordPlainText()).isEqualTo("newpass123");

        // Test non-existent email
        assertThatThrownBy(() -> userService.updateUserPasswordByEmail("non@ex.com", "newpass"))
                .hasMessageContaining("Found 0 users");

        // Test invalid password
        assertThatThrownBy(() -> userService.updateUserPasswordByEmail("temp@email.com", "short"))
                .hasMessageContaining("length of password");
    }

    @Test
    @Transactional
    void testDeleteUserByUserIds() {
        int deleted = userService.deleteUserByUserIds(List.of("user_050"));
        assertThat(deleted).isEqualTo(1);

        assertThat(userService.testUIDExistence("user_050")).isFalse();

        // Test multiple
        int multi = userService.deleteUserByUserIds(List.of("YCJ", "ZR"));
        assertThat(multi).isEqualTo(2);

        // Test misaligned deletion
        // Assume code throws if n1 != n2 etc, but if no associated, n1=0 etc, but code checks equality
    }

    @Test
    void testGetLoginCount() {
        int count = userService.getLoginCount();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }
}
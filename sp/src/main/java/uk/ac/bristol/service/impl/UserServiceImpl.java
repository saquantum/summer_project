package uk.ac.bristol.service.impl;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.UserWithExtraColumns;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final MetaDataMapper metaDataMapper;
    private final PermissionConfigService permissionConfigService;

    public UserServiceImpl(UserMapper userMapper, MetaDataMapper metaDataMapper, PermissionConfigService permissionConfigService) {
        this.userMapper = userMapper;
        this.metaDataMapper = metaDataMapper;
        this.permissionConfigService = permissionConfigService;
    }

    // checks uid and password, returns a jwt token
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public String login(User user) {
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", user.getId())),
                null, null, null);
        if (list.size() != 1) {
            throw new SpExceptions.UnauthorisedException("User not found or password incorrect.");
        }

        if (!verifyPassword(user.getPassword(), userMapper.selectPasswordByUserId(user.getId()))) {
            throw new SpExceptions.UnauthorisedException("User not found or password incorrect.");
        }

        User u = list.get(0);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", u.getId());
        claims.put("isAdmin", u.isAdmin());
        return JwtUtil.generateJWT(claims);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<User> getUsers(Map<String, Object> filters,
                               List<Map<String, String>> orderList,
                               Integer limit,
                               Integer offset) {
        return userMapper.selectUsers(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList("user_row_id", orderList, "users", "address", "contact_details", "contact_preferences", "permission_configs"),
                limit, offset);
    }

    @Override
    public List<User> getCursoredUsers(Long lastUserRowId, Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        return userMapper.selectUsers(
                QueryTool.formatCursoredDeepPageFilters("user_row_id", lastUserRowId, filters),
                QueryTool.filterOrderList("user_row_id", orderList, "users", "address", "contact_details", "contact_preferences", "permission_configs"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<User> getUnauthorisedUsers(Map<String, Object> filters,
                                           List<Map<String, String>> orderList,
                                           Integer limit,
                                           Integer offset) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put("user_is_admin", false);
        return userMapper.selectUsers(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList("user_row_id", orderList, "users", "address", "contact_details", "contact_preferences", "permission_configs"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<UserWithExtraColumns> getUsersWithAccumulator(String function,
                                                              String column,
                                                              Map<String, Object> filters,
                                                              List<Map<String, String>> orderList,
                                                              Integer limit,
                                                              Integer offset) {
        if ("count".equalsIgnoreCase(function)) {
            return userMapper.selectUsersWithAccumulator(
                    function, column,
                    QueryTool.formatFilters(filters),
                    QueryTool.filterOrderList("user_row_id", orderList, "users", "address", "contact_details", "contact_preferences", "permission_configs", "accumulation"),
                    limit, offset);
        }
        throw new SpExceptions.GetMethodException("function " + function + " is not supported at current stage");
    }

    @Override
    public List<UserWithExtraColumns> getCursoredUsersWithAccumulator(String function, String column, Long lastUserRowId, Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        if ("count".equalsIgnoreCase(function)) {
            return userMapper.selectUsersWithAccumulator(
                    function, column,
                    QueryTool.formatCursoredDeepPageFilters("user_row_id", lastUserRowId, filters),
                    QueryTool.filterOrderList("user_row_id", orderList, "users", "address", "contact_details", "contact_preferences", "permission_configs", "accumulation"),
                    limit, offset);
        }
        throw new SpExceptions.GetMethodException("function " + function + " is not supported at current stage");
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public User getUserByUserId(String uid) {
        List<User> user = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", uid)),
                null, null, null);
        if (user.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + user.size() + " users using user id " + uid);
        }
        return user.get(0);
    }

    @Override
    public Long getUserRowIdByUserId(String uid) {
        return userMapper.selectUserRowIdByUserId(uid);
    }

    // true: UID exists
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean testUIDExistence(String id) {
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", id)),
                null, null, null);
        return !list.isEmpty();
    }

    // true: email exists
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean testEmailAddressExistence(String email) {
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("contact_details_email", email)),
                null, null, null
        );
        return !list.isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<UserWithAssets> groupUsersWithOwnedAssetsByWarningId(Integer limit, Long cursor, Long waringId, boolean getDiff, String newAreaAsJson) {
        return userMapper.groupUsersWithOwnedAssetsByWarningId(limit, cursor, waringId, getDiff, newAreaAsJson);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public long countUsersWithFilter(Map<String, Object> filters) {
        return userMapper.countUsers(QueryTool.formatFilters(filters));
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean compareUserLastModified(String uid, Long timestamp) {
        return !getUserByUserId(uid)
                .getLastModified()
                .isAfter(Instant.ofEpochMilli(timestamp));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public boolean upsertAddress(User user) {
        if (user.getAddress() == null || user.getId() == null || !testUIDExistence(user.getId())) {
            return false;
        }
        if (userMapper.upsertAddressByUserId(user.getId(), user.getAddress())) {
            metaDataMapper.increaseTotalCountByTableName("address", 1);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public boolean upsertContactDetails(User user) {
        if (user.getContactDetails() == null || user.getId() == null || !testUIDExistence(user.getId())) {
            return false;
        }
        if (userMapper.upsertContactDetailsByUserId(user.getId(), user.getContactDetails())) {
            metaDataMapper.increaseTotalCountByTableName("contact_details", 1);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public boolean upsertContactPreferences(User user) {
        if (user.getContactPreferences() == null || user.getId() == null || !testUIDExistence(user.getId())) {
            return false;
        }
        if (userMapper.upsertContactPreferencesByUserId(user.getId(), user.getContactPreferences())) {
            metaDataMapper.increaseTotalCountByTableName("contact_preferences", 1);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            throw new SpExceptions.BadRequestException("Invalid user id");
        }
        user.setId(user.getId());
        user.setName(user.getName());
        user.setPasswordPlainText(user.getPassword());
        user.setPassword(hashPassword(user.getPassword()));
        int n = userMapper.insertUser(user);
        if (n != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert user " + user.getId());
        }
        metaDataMapper.increaseTotalCountByTableName("users", 1);
        upsertAddress(user);
        upsertContactDetails(user);
        upsertContactPreferences(user);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertUserBatch(List<User> list) {
        for (User user : list) {
            insertUser(user);
        }
        return list.size();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void registerNewUser(Map<String, String> data) {
        String id = data.get("id").trim();
        String password = data.get("password").trim();
        String repassword = data.get("repassword").trim();
        String name = data.get("name").trim();
        String email = data.get("email").trim();
        String phone = data.get("phone").trim();

        if (id.isBlank() || password.isBlank() || repassword.isBlank() || name.isBlank() || email.isBlank() || phone.isBlank()) {
            throw new SpExceptions.BadRequestException("Key fields missing during registration.");
        }

        // check uid not duplicate
        if (testUIDExistence(id)) {
            throw new SpExceptions.BadRequestException("Duplicate user id, failed to register.");
        }

        // check password validity
        passwordValidation(password);
        if (!password.equals(repassword)) {
            throw new SpExceptions.BadRequestException("Two passwords don't match.");
        }

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setPassword(password);
        user.setAddress(Map.of());
        user.setContactDetails(Map.of("email", email, "phone", phone));
        user.setContactPreferences(Map.of());
        insertUser(user);

        int n = permissionConfigService.insertPermissionConfig(new PermissionConfig(id));
        if (n != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert permission config for user " + id);
        }
        metaDataMapper.increaseTotalCountByTableName("permission_configs", 1);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            throw new SpExceptions.BadRequestException("Invalid user id");
        }
        int n = userMapper.updateUserByUserId(user);
        upsertAddress(user);
        upsertContactDetails(user);
        upsertContactPreferences(user);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateUserBatch(List<User> list) {
        for (User user : list) {
            updateUser(user);
        }
        return list.size();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateUserPasswordByEmail(String email, String password) {
        passwordValidation(password);
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("contact_details_email", email)),
                null, null, null);
        if (list.size() != 1) {
            throw new SpExceptions.GetMethodException("Found" + list.size() + " users by email");
        }
        User user = list.get(0);
        user.setPasswordPlainText(password);
        user.setPassword(hashPassword(password));
        return userMapper.updateUserPasswordByUserId(user);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteUserByUserIds(List<String> ids) {
        int n1 = userMapper.deleteAddressByUserIds(ids);
        int n2 = userMapper.deleteContactDetailsByUserIds(ids);
        int n3 = userMapper.deleteContactPreferencesByUserIds(ids);
        int n4 = userMapper.deleteUserByUserIds(ids);
        if (n1 != n2 || n2 != n3 || n3 != n4) {
            throw new SpExceptions.DeleteMethodException("Failed to delete users by ids due to misaligned deletion");
        }
        metaDataMapper.increaseTotalCountByTableName("users", -n1);
        return n1;
    }

    private void passwordValidation(String password) {
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new SpExceptions.BadRequestException("The length of password should be between 6 and 64 characters");
        }
        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$")) {
            throw new SpExceptions.BadRequestException("Invalid password: empty or contains improper characters");
        }
    }

    private String hashPassword(String password) {
        Argon2Function argon2 = Argon2Function.getInstance(
                256,
                1,
                1,
                4,
                Argon2.ID
        );

        Hash hash = Password.hash(password)
                .addRandomSalt(4)
                .with(argon2);

        return hash.getResult();
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        Argon2Function argon2 = Argon2Function.getInstance(
                256,
                1,
                1,
                4,
                Argon2.ID
        );

        return Password.check(plainPassword, hashedPassword)
                .with(argon2);
    }
}

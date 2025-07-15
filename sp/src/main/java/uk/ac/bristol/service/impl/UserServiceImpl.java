package uk.ac.bristol.service.impl;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithExtraColumns;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final MetaDataMapper metaDataMapper;
    private final AssetHolderMapper assetHolderMapper;
    private final UserMapper userMapper;
    private final AssetMapper assetMapper;

    public UserServiceImpl(MetaDataMapper metaDataMapper, AssetHolderMapper assetHolderMapper, UserMapper userMapper, AssetMapper assetMapper) {
        this.metaDataMapper = metaDataMapper;
        this.assetHolderMapper = assetHolderMapper;
        this.userMapper = userMapper;
        this.assetMapper = assetMapper;
    }

    // checks uid and password, returns a jwt token
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public User login(User user) {
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", user.getId())),
                null, null, null);
        if (list.size() != 1) {
            throw new SpExceptions.NotFoundException("There is no such user with id " + user.getId());
        }

        if (!verifyPassword(user.getPassword(), userMapper.selectPasswordByUserId(user.getId()))) {
            throw new SpExceptions.ForbiddenException("Password is incorrect.");
        }

        User u = list.get(0);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", u.getId());
        if (u.getAssetHolderId() != null) {
            claims.put("assetHolderId", u.getAssetHolderId());
        }
        claims.put("isAdmin", u.isAdmin());
        u.setToken(JwtUtil.generateJWT(claims));
        return u;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<User> getAllUsers(Map<String, Object> filters,
                                  List<Map<String, String>> orderList,
                                  Integer limit,
                                  Integer offset) {
        return userMapper.selectUsers(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "users", "asset_holders"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<User> getAllUsersWithAssetHolder(Map<String, Object> filters,
                                                 List<Map<String, String>> orderList,
                                                 Integer limit,
                                                 Integer offset) {
        return userMapper.selectUsers(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "users", "asset_holders"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<User> getAllUnauthorisedUsersWithAssetHolder(Map<String, Object> filters,
                                                             List<Map<String, String>> orderList,
                                                             Integer limit,
                                                             Integer offset) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put("user_is_admin", false);
        return userMapper.selectUsers(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "users", "asset_holders"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Map<String, Object>> getAllAssetHoldersWithAssetIds(Map<String, Object> filters,
                                                                    List<Map<String, String>> orderList,
                                                                    Integer limit,
                                                                    Integer offset) {
        List<AssetHolder> assetHolders = assetHolderMapper.selectAssetHolders(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "asset_holders"),
                limit, offset);
        List<Asset> assets = assetMapper.selectAssets(null, null, null, null);
        Map<String, List<String>> mapping = new HashMap<>();
        assets.forEach(asset -> {
            if (!mapping.containsKey(asset.getOwnerId())) {
                mapping.put(asset.getOwnerId(), new ArrayList<>());
            }
            mapping.get(asset.getOwnerId()).add(asset.getId());
        });
        List<Map<String, Object>> result = new ArrayList<>();
        for (AssetHolder ah : assetHolders) {
            List<String> ids = mapping.getOrDefault(ah.getId(), new ArrayList<>());
            result.add(Map.of("assetHolder", ah, "assetIds", ids));
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<UserWithExtraColumns> getAllUsersWithAccumulator(String function,
                                                                 String column,
                                                                 Map<String, Object> filters,
                                                                 List<Map<String, String>> orderList,
                                                                 Integer limit,
                                                                 Integer offset) {
        if ("count".equalsIgnoreCase(function)) {
            return userMapper.selectUsersWithAccumulator(
                    function, "asset_id",
                    QueryTool.formatFilters(filters),
                    QueryTool.filterOrderList(orderList, "users", "asset_holders", "accumulation"),
                    limit, offset);
        }
        throw new SpExceptions.GetMethodException("function " + function + " is not supported at current stage");
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public User getUserByAssetHolderId(String aid) {
        List<User> user = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("asset_holder_id", aid)),
                null, null, null);
        if (user.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + user.size() + " users using asset holder id " + aid);
        }
        return user.get(0);
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean testUIDExistence(String id) {
        List<User> list = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", id)),
                null, null, null);
        return !list.isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean testEmailExistence(String email) {
        Boolean b = assetHolderMapper.testEmailAddressExistence(email);
        return b != null && b;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public int countUsersWithFilter(Map<String, Object> filters) {
        return userMapper.selectUsers(QueryTool.formatFilters(filters), null, null, null).size();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public int countAssetHoldersWithFilter(Map<String, Object> filters) {
        return assetHolderMapper.selectAssetHolders(QueryTool.formatFilters(filters), null, null, null).size();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertUser(User user) {
        if (user.getAssetHolder() != null) {
            AssetHolder emptyAH = new AssetHolder();
            emptyAH.setAddressId(UUID.randomUUID().toString());
            emptyAH.setContactPreferencesId(UUID.randomUUID().toString());

            AssetHolder ah = user.getAssetHolder();
            ah.setId(assetHolderMapper.generateAssetHolderId(emptyAH));
            ah.setAddressId(ah.getId());
            ah.getAddress().put("assetHolderId", ah.getAddressId());
            ah.setContactPreferencesId(ah.getId());
            ah.getContactPreferences().put("assetHolderId", ah.getContactPreferencesId());
            ah.setLastModified(Instant.now());

            int n3 = assetHolderMapper.updateAssetHolder(ah);
            if (n3 != 1) {
                throw new SpExceptions.PostMethodException("Failed to insert asset holder for user " + user.getId());
            }

            int n1 = assetHolderMapper.insertAddress(ah.getAddress());
            if (n1 != 1) {
                throw new SpExceptions.PostMethodException("Failed to insert address for user " + user.getId());
            }

            int n2 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
            if (n2 != 1) {
                throw new SpExceptions.PostMethodException("Failed to insert contact preferences for user " + user.getId());
            }

            metaDataMapper.increaseTotalCountByTableName("asset_holders", 1);
            metaDataMapper.increaseTotalCountByTableName("address", 1);
            metaDataMapper.increaseTotalCountByTableName("contact_preferences", 1);

            user.setAssetHolderId(user.getAssetHolder().getId());
        }

        user.setPasswordPlainText(user.getPassword());
        user.setPassword(hashPassword(user.getPassword()));

        int n4 = userMapper.insertUser(user);
        if (n4 != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert user " + user.getId());
        }
        metaDataMapper.increaseTotalCountByTableName("users", 1);
        return n4;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int registerNewUser(User user) {
        if (user.getId() == null) {
            throw new SpExceptions.PostMethodException("No valid user id is provided.");
        }
        List<User> duplicateUsers = userMapper.selectUsers(
                QueryTool.formatFilters(Map.of("user_id", user.getId())),
                null, null, null);
        if (!duplicateUsers.isEmpty()) {
            throw new SpExceptions.PostMethodException("Duplicate user id, failed to register.");
        }

        if (user.getPassword() == null) {
            throw new SpExceptions.PostMethodException("No valid password is provided.");
        }
        if (user.getPassword().length() < 6) {
            throw new SpExceptions.PostMethodException("Password is too short.");
        }

        if (user.getAssetHolder() == null) {
            throw new SpExceptions.PostMethodException("No valid user personal profile is provided.");
        }

        AssetHolder ah = user.getAssetHolder();

        if (ah.getName() == null || ah.getName().isBlank()) {
            throw new SpExceptions.PostMethodException("No valid user's name is provided.");
        }

        if (ah.getEmail() == null || ah.getEmail().isBlank()) {
            throw new SpExceptions.PostMethodException("No valid user's email is provided.");
        }

        if (ah.getPhone() == null || ah.getPhone().isBlank()) {
            throw new SpExceptions.PostMethodException("No valid user's phone is provided.");
        }

        String assetHolderId = "$" + user.getId();
        user.setAssetHolderId(assetHolderId);
        ah.setId(assetHolderId);
        ah.setAddressId(assetHolderId);
        if (ah.getAddress() == null) {
            Map<String, String> address = new HashMap<>();
            ah.setAddress(address);
        }
        ah.getAddress().put("assetHolderId", ah.getAddressId());
        ah.setContactPreferencesId(assetHolderId);
        if (ah.getContactPreferences() == null) {
            Map<String, Object> cp = new HashMap<>();
            ah.setContactPreferences(cp);
        }
        ah.getContactPreferences().put("assetHolderId", ah.getContactPreferencesId());
        ah.setLastModified(Instant.now());

        int n3 = assetHolderMapper.insertAssetHolder(ah);
        if (n3 != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert asset holder for user " + user.getId());
        }
        int n1 = assetHolderMapper.insertAddress(ah.getAddress());
        if (n1 != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert address for user " + user.getId());
        }
        int n2 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
        if (n2 != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert contact preferences for user " + user.getId());
        }

        user.setPassword(hashPassword(user.getPassword()));

        int n4 = userMapper.insertUser(user);
        if (n4 != 1) {
            throw new SpExceptions.PostMethodException("Failed to insert user " + user.getId());
        }
        metaDataMapper.increaseTotalCountByTableName("asset_holders", 1);
        metaDataMapper.increaseTotalCountByTableName("address", 1);
        metaDataMapper.increaseTotalCountByTableName("contact_preferences", 1);
        metaDataMapper.increaseTotalCountByTableName("users", 1);
        return n4;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateUser(User user) {

        user.setPassword(hashPassword(user.getPassword()));

        int n1 = userMapper.updateUserByUserId(user);
        Integer n2 = null;
        if (user.getAssetHolder() != null) {
            n2 = this.updateAssetHolder(user.getAssetHolder());
        }
        if (n2 != null && n1 != n2) {
            throw new SpExceptions.PutMethodException("Error updating user " + user.getId() + ": affected rows not compatible");
        }
        return n1;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateAssetHolder(AssetHolder ah) {
        String aid = ah.getId();
        if (aid == null || aid.isBlank()) {
            throw new SpExceptions.BadRequestException("No valid asset holder id is provided.");
        }
        Integer n1 = null, n2 = null;
        if (ah.getAddress() != null) {
            ah.setAddressId(aid);
            ah.getAddress().put("assetHolderId", aid);
            n1 = assetHolderMapper.updateAddressByAssetHolderId(ah.getAddress());
        }
        if (ah.getContactPreferences() != null) {
            ah.setContactPreferencesId(aid);
            ah.getContactPreferences().put("assetHolderId", aid);
            n2 = assetHolderMapper.updateContactPreferencesByAssetHolderId(ah.getContactPreferences());
        }
        ah.setLastModified(Instant.now());
        int n3 = assetHolderMapper.updateAssetHolder(ah);
        if ((n1 != null && n3 != n1) || (n2 != null && n3 != n2)) {
            throw new SpExceptions.PutMethodException("Error updating asset holder " + ah.getId() + ": affected rows not compatible");
        }
        return n3;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updatePasswordByEmail(String email, String password) {
        List<User> list = userMapper.selectUsersPuttingAssetHoldersTableMain(
                QueryTool.formatFilters(Map.of("asset_holder_email", email)),
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
    public int deleteUserByUserIds(String[] ids) {
        return this.deleteUserByUserIds(Arrays.asList(ids));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteUserByUserIds(List<String> ids) {
        int sum = 0;
        for (String id : ids) {
            User user = this.getUserByUserId(id);
            Integer n1 = null;
            if (user.getAssetHolderId() != null) {
                n1 = this.deleteAssetHolderByAssetHolderIds(new String[]{user.getAssetHolderId()});
            }
            int n2 = userMapper.deleteUserByIds(new String[]{id});
            if (n1 != null && n1 != n2) {
                throw new SpExceptions.DeleteMethodException("Error in deleting user " + id + ": affected rows not compatible");
            }
            sum += n2;
        }
        metaDataMapper.increaseTotalCountByTableName("users", -sum);
        return sum;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteUserByAssetHolderIds(String[] ids) {
        return this.deleteUserByAssetHolderIds(Arrays.asList(ids));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteUserByAssetHolderIds(List<String> ids) {
        int sum = 0;
        for (String id : ids) {
            int n1 = this.deleteAssetHolderByAssetHolderIds(new String[]{id});
            if (n1 == 0) {
                throw new SpExceptions.GetMethodException("Error in deleting asset holder " + id + ": the asset holder does not exist");
            }
            int n2 = userMapper.deleteUserByAssetHolderIDs(new String[]{id});
            if (n1 != n2) {
                throw new SpExceptions.DeleteMethodException("Error in deleting user " + id + ": affected rows not compatible");
            }
            sum += n2;
        }
        metaDataMapper.increaseTotalCountByTableName("users", -sum);
        return sum;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetHolderByAssetHolderIds(String[] ids) {
        return this.deleteAssetHolderByAssetHolderIds(Arrays.asList(ids));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetHolderByAssetHolderIds(List<String> ids) {
        int n1 = assetHolderMapper.deleteAddressByAssetHolderIds(ids);
        int n2 = assetHolderMapper.deleteContactPreferencesByAssetHolderIds(ids);
        int n3 = assetHolderMapper.deleteAssetHolderByAssetHolderIDs(ids);
        if (n1 != n2 || n3 != n2) {
            throw new SpExceptions.DeleteMethodException("Error deleting asset holders: affected rows not compatible");
        }
        metaDataMapper.increaseTotalCountByTableName("asset_holders", -n1);
        return n1;
    }

    private String hashPassword(String password) {
        Argon2Function argon2 = Argon2Function.getInstance(
                19456,
                1,
                1,
                256,
                Argon2.ID
        );

        Hash hash = Password.hash(password)
                .addRandomSalt(16)
                .with(argon2);

        return hash.getResult();
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        Argon2Function argon2 = Argon2Function.getInstance(
                19456,
                1,
                1,
                256,
                Argon2.ID
        );

        return Password.check(plainPassword, hashedPassword)
                .with(argon2);
    }
}

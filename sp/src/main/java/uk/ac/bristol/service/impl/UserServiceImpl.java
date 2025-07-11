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
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.*;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class UserServiceImpl implements UserService {

    private final MetaDataMapper metaDataMapper;
    private final PermissionConfigService permissionConfigService;
    private final AssetHolderMapper assetHolderMapper;
    private final UserMapper userMapper;
    private final AssetMapper assetMapper;

    public UserServiceImpl(MetaDataMapper metaDataMapper, PermissionConfigService permissionConfigService, AssetHolderMapper assetHolderMapper, UserMapper userMapper, AssetMapper assetMapper) {
        this.metaDataMapper = metaDataMapper;
        this.permissionConfigService = permissionConfigService;
        this.assetHolderMapper = assetHolderMapper;
        this.userMapper = userMapper;
        this.assetMapper = assetMapper;
    }

    // checks uid and password, returns a jwt token
    @Override
    public User login(User user) {
        List<User> list = userMapper.selectUserById(user.getId());
        if (list.size() != 1) {
            return null;
        }

        if (!verifyPassword(user.getPassword(), userMapper.selectPasswordById(user.getId()))) {
            return null;
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

    // get all users without asset holder part
    @Override
    public List<User> getAllUsers(List<Map<String, String>> orderList,
                                  Integer limit,
                                  Integer offset) {
        return userMapper.selectAllUsers(QueryTool.filterOrderList(orderList, "user"), limit, offset);
    }

    // get address and contact preferences for the asset holder
    private AssetHolder prepareAssetHolder(AssetHolder assetHolder) {
        if (assetHolder == null) return null;
        List<Map<String, String>> address = assetHolderMapper.selectAddressByAssetHolderId(assetHolder.getId());
        if (address.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + address.size() + " addresses for asset holder " + assetHolder.getId());
        }
        List<Map<String, Object>> contactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(assetHolder.getId());
        if (contactPreferences.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + contactPreferences.size() + " contact preferences for asset holder " + assetHolder.getId());
        }
        assetHolder.setAddress(address.get(0));
        assetHolder.setContactPreferences(contactPreferences.get(0));
        return assetHolder;
    }

    private User prepareUser(User user) {
        if (user == null) return null;
        List<PermissionConfig> config = permissionConfigService.getPermissionConfigByUserId(user.getId());
        if (config.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + config.size() + " permission configs for user " + user.getId());
        }
        user.setPermissionConfig(config.get(0));
        return user;
    }

    // This method returns all users, with asset holder info if possible
    @Override
    public List<User> getAllUsersWithAssetHolder(List<Map<String, String>> orderList,
                                                 Integer limit,
                                                 Integer offset) {
        List<UserWithAssetHolder> list = userMapper.selectAllUsersWithAssetHolder(QueryTool.filterOrderList(orderList, "user", "asset_holder"), limit, offset);
        List<User> result = new ArrayList<>();
        for (UserWithAssetHolder uwa : list) {
            User user = uwa.getUser();
            user.setAssetHolder(this.prepareAssetHolder(uwa.getAssetHolder()));
            result.add(this.prepareUser(user));
        }
        return result;
    }

    // This method only returns users with asset holder info (i.e. excluding admins)
    @Override
    public List<User> getAllUnauthorisedUsersWithAssetHolder(List<Map<String, String>> orderList,
                                                             Integer limit,
                                                             Integer offset) {
        List<UserWithAssetHolder> list = userMapper.selectAllUnauthorisedUsersWithAssetHolder(QueryTool.filterOrderList(orderList, "user", "asset_holder"), limit, offset);
        List<User> result = new ArrayList<>();
        for (UserWithAssetHolder uwa : list) {
            User user = uwa.getUser();
            user.setAssetHolder(this.prepareAssetHolder(uwa.getAssetHolder()));
            result.add(this.prepareUser(user));
        }
        return result;
    }

    // get all asset holders along with address and contact preferences
    @Override
    public List<AssetHolder> getAllAssetHolders(List<Map<String, String>> orderList,
                                                Integer limit,
                                                Integer offset) {
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders(QueryTool.filterOrderList(orderList, "asset_holder"), limit, offset);
        assetHolders.forEach(this::prepareAssetHolder);
        return assetHolders;
    }

    @Override
    public List<Map<String, Object>> getAllAssetHoldersWithAssetIds(List<Map<String, String>> orderList,
                                                                    Integer limit,
                                                                    Integer offset) {
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders(QueryTool.filterOrderList(orderList, "asset_holder"), limit, offset);
        List<Asset> assets = assetMapper.selectAllAssets(null, null, null);
        Map<String, List<String>> mapping = new HashMap<>();
        assets.forEach(asset -> {
            if (!mapping.containsKey(asset.getOwnerId())) {
                mapping.put(asset.getOwnerId(), new ArrayList<>());
            }
            mapping.get(asset.getOwnerId()).add(asset.getId());
        });
        List<Map<String, Object>> result = new ArrayList<>();
        for (AssetHolder ah : assetHolders) {
            ah = prepareAssetHolder(ah);
            List<String> ids = mapping.getOrDefault(ah.getId(), new ArrayList<>());
            result.add(Map.of("assetHolder", ah, "assetIds", ids));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllUsersWithAccumulator(String function,
                                                                String column,
                                                                Map<String, Object> filters,
                                                                List<Map<String, String>> orderList,
                                                                Integer limit,
                                                                Integer offset) {
        if ("count".equalsIgnoreCase(function)) {
            String filterString = QueryTool.formatFilters(filters);
            List<UserWithAssetHolder> list = userMapper.selectAllUsersWithAccumulator(function, column, filterString, QueryTool.filterOrderList(orderList, "user", "asset_holder", "accumulation"), limit, offset);
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserWithAssetHolder uwa : list) {
                User user = uwa.getUser();
                user.setAssetHolder(this.prepareAssetHolder(uwa.getAssetHolder()));
                result.add(Map.of("user", this.prepareUser(user), function, uwa.getAccumulation()));
            }
            return result;
        }
        throw new SpExceptions.GetMethodException("function " + function + " is not supported at current stage");
    }

    @Override
    public List<User> getAllAdmins(List<Map<String, String>> orderList,
                                   Integer limit,
                                   Integer offset) {

        return userMapper.selectUserByAdmin(true, QueryTool.filterOrderList(orderList, "user"), limit, offset);
    }

    @Override
    public User getUserByAssetHolderId(String assetHolderId,
                                       List<Map<String, String>> orderList,
                                       Integer limit,
                                       Integer offset) {
        List<User> user = userMapper.selectUserByAssetHolderId(assetHolderId);
        if (user.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + user.size() + " users using asset holder id " + assetHolderId);
        }

        List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByIDs(List.of(assetHolderId), QueryTool.filterOrderList(orderList, "asset_holder"), limit, offset);
        if (assetHolder.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + assetHolder.size() + " asset holders using asset holder id " + assetHolderId);
        }
        user.get(0).setAssetHolder(this.prepareAssetHolder(assetHolder.get(0)));
        return this.prepareUser(user.get(0));
    }

    @Override
    public User getUserByUserId(String uid) {
        List<User> user = userMapper.selectUserById(uid);
        if (user.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + user.size() + " users using user id " + uid);
        }
        if (user.get(0).getAssetHolderId() != null) {
            List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByIDs(
                    List.of(user.get(0).getAssetHolderId()), null, null, null);
            if (assetHolder.size() != 1) {
                throw new SpExceptions.GetMethodException("Get " + assetHolder.size() + " asset holders using asset holder id " + user.get(0).getAssetHolderId());
            }
            user.get(0).setAssetHolder(this.prepareAssetHolder(assetHolder.get(0)));
        }
        return this.prepareUser(user.get(0));
    }

    @Override
    public boolean testUIDExistence(String id) {
        List<User> list = userMapper.selectUserById(id);
        return !list.isEmpty();
    }

    @Override
    public boolean testEmailExistence(String email) {
        Boolean b = assetHolderMapper.testEmailAddressExistence(email);
        return b != null && b;
    }

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

    @Override
    public int registerNewUser(User user) {
        if (user.getId() == null) {
            throw new SpExceptions.PostMethodException("No valid user id is provided.");
        }
        List<User> duplicateUsers = userMapper.selectUserById(user.getId());
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

    @Override
    public int deleteUserByUserIds(String[] ids) {
        return this.deleteUserByUserIds(Arrays.asList(ids));
    }

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

    @Override
    public int deleteUserByAssetHolderIds(String[] ids) {
        return this.deleteUserByAssetHolderIds(Arrays.asList(ids));
    }

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

    @Override
    public int deleteAssetHolderByAssetHolderIds(String[] ids) {
        return this.deleteAssetHolderByAssetHolderIds(Arrays.asList(ids));
    }

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

    @Override
    public int updatePasswordByEmail(String email, String password) {
        List<User> list = userMapper.selectUserByEmailAddress(email);
        if (list.size() != 1) {
            throw new SpExceptions.GetMethodException("Found" + list.size() + " users by email");
        }

        User user = list.get(0);

        user.setPasswordPlainText(password);
        user.setPassword(hashPassword(password));

        return userMapper.updateUserPasswordByUserId(user);
    }

    private String hashPassword(String password) {
        Argon2Function argon2 = Argon2Function.getInstance(
                19456,
                2,
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
                2,
                1,
                256,
                Argon2.ID
        );

        return Password.check(plainPassword, hashedPassword)
                .with(argon2);
    }
}

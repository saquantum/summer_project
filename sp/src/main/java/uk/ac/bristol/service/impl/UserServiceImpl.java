package uk.ac.bristol.service.impl;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssetHolder;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.*;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class UserServiceImpl implements UserService {

    private final AssetHolderMapper assetHolderMapper;

    private final UserMapper userMapper;

    private final AssetMapper assetMapper;

    public UserServiceImpl(AssetHolderMapper assetHolderMapper, UserMapper userMapper, AssetMapper assetMapper) {
        this.assetHolderMapper = assetHolderMapper;
        this.userMapper = userMapper;
        this.assetMapper = assetMapper;
    }

    // checks uid and password, returns a jwt token
    @Override
    public User login(User user) {
        List<User> list = userMapper.loginQuery(user);
        if (list.size() != 1) return null;
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
            throw new RuntimeException("Get " + address.size() + " addresses for asset holder " + assetHolder.getId());
        }
        List<Map<String, Object>> contactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(assetHolder.getId());
        if (contactPreferences.size() != 1) {
            throw new RuntimeException("Get " + contactPreferences.size() + " contact preferences for asset holder " + assetHolder.getId());
        }
        assetHolder.setAddress(address.get(0));
        assetHolder.setContactPreferences(contactPreferences.get(0));
        return assetHolder;
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
            result.add(user);
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
            result.add(user);
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
        for (AssetHolder holder : assetHolders) {
            List<String> ids = mapping.getOrDefault(holder.getId(), new ArrayList<>());
            result.add(Map.of("assetHolder", holder, "assetIds", ids));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllUsersWithAccumulator(String function,
                                                                String column,
                                                                List<Map<String, String>> orderList,
                                                                Integer limit,
                                                                Integer offset) {
        if ("count".equalsIgnoreCase(function)) {
            List<UserWithAssetHolder> list = userMapper.selectAllUsersWithAccumulator(function, column, QueryTool.filterOrderList(orderList, "user", "asset_holder", "accumulation"), limit, offset);
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserWithAssetHolder uwa : list) {
                User user = uwa.getUser();
                user.setAssetHolder(this.prepareAssetHolder(uwa.getAssetHolder()));
                result.add(Map.of("user", user, function, uwa.getAccumulation()));
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
            throw new RuntimeException("Get " + user.size() + " users using asset holder id " + assetHolderId);
        }

        List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByIDs(List.of(assetHolderId), QueryTool.filterOrderList(orderList, "asset_holder"), limit, offset);
        if (assetHolder.size() != 1) {
            throw new RuntimeException("Get " + assetHolder.size() + " asset holders using asset holder id " + assetHolderId);
        }
        user.get(0).setAssetHolder(this.prepareAssetHolder(assetHolder.get(0)));
        return user.get(0);
    }

    @Override
    public User getUserByUserId(String uid) {
        List<User> user = userMapper.selectUserById(uid);
        if (user.size() != 1) {
            throw new RuntimeException("Get " + user.size() + " users using user id " + uid);
        }
        if (user.get(0).getAssetHolderId() != null) {
            List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByIDs(
                    List.of(user.get(0).getAssetHolderId()), null, null, null);
            if (assetHolder.size() != 1) {
                throw new RuntimeException("Get " + assetHolder.size() + " asset holders using asset holder id " + user.get(0).getAssetHolderId());
            }
            user.get(0).setAssetHolder(this.prepareAssetHolder(assetHolder.get(0)));
        }
        return user.get(0);
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
            AssetHolder ah = user.getAssetHolder();
            ah.setLastModified(Instant.now());

            int n1 = assetHolderMapper.insertAddress(ah.getAddress());
            if (n1 != 1) {
                throw new RuntimeException("Failed to insert address for user " + user.getId());
            }

            int n2 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
            if (n2 != 1) {
                throw new RuntimeException("Failed to insert contact preferences for user " + user.getId());
            }

            if (ah.getId() == null || ah.getId().isBlank()) {
                int n0 = assetHolderMapper.insertAssetHolderAutoId(ah);
                if (n0 != 1) {
                    throw new RuntimeException("Failed to insert asset holder using auto ID for user " + user.getId());
                }
            } else {
                int n3 = assetHolderMapper.insertAssetHolder(ah);
                if (n3 != 1) {
                    throw new RuntimeException("Failed to insert asset holder for user " + user.getId());
                }
            }
        }
        int n4 = userMapper.insertUser(user);
        if (n4 != 1) {
            throw new RuntimeException("Failed to insert user " + user.getId());
        }
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

        int n1 = assetHolderMapper.insertAddress(ah.getAddress());
        if (n1 != 1) {
            throw new RuntimeException("Failed to insert address for user " + user.getId());
        }
        int n2 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
        if (n2 != 1) {
            throw new RuntimeException("Failed to insert contact preferences for user " + user.getId());
        }
        int n3 = assetHolderMapper.insertAssetHolder(ah);
        if (n3 != 1) {
            throw new RuntimeException("Failed to insert asset holder for user " + user.getId());
        }
        int n4 = userMapper.insertUser(user);
        if (n4 != 1) {
            throw new RuntimeException("Failed to insert user " + user.getId());
        }
        return n4;
    }

    @Override
    public int updateUser(User user) {
        int n1 = userMapper.updateUserByUserId(user);
        Integer n2 = null;
        if (user.getAssetHolder() != null) {
            n2 = this.updateAssetHolder(user.getAssetHolder());
        }
        if (n2 != null && n1 != n2) {
            throw new RuntimeException("Error updating user " + user.getId() + ": affected rows not compatible");
        }
        return n1;
    }

    @Override
    public int updateAssetHolder(AssetHolder assetHolder) {
        assetHolder.setLastModified(Instant.now());
        int n1 = assetHolderMapper.updateAddressByAssetHolderId(assetHolder.getAddress());
        int n2 = assetHolderMapper.updateContactPreferencesByAssetHolderId(assetHolder.getContactPreferences());
        int n3 = assetHolderMapper.updateAssetHolder(assetHolder);
        if (n2 != n1 || n3 != n2) {
            throw new RuntimeException("Error updating asset holder " + assetHolder.getId() + ": affected rows not compatible");
        }
        return n1;
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
                throw new RuntimeException("Error in deleting user " + id + ": affected rows not compatible");
            }
            sum += n2;
        }
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
                throw new RuntimeException("Error in deleting asset holder " + id + ": the asset holder does not exist");
            }
            int n2 = userMapper.deleteUserByIds(new String[]{id});
            if (n1 != n2) {
                throw new RuntimeException("Error in deleting user " + id + ": affected rows not compatible");
            }
            sum += n2;
        }
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
            throw new RuntimeException("Error deleting asset holders: affected rows not compatible");
        }
        return n1;
    }

    @Override
    public int updatePasswordByEmail(String email, String password) {
        List<String> list = assetHolderMapper.selectAssetHolderIdByEmail(email);
        if (list.size() != 1) {
            throw new RuntimeException("Error finding asset holder by email");
        }
        User user = new User();
        user.setId(list.get(0));
        user.setPassword(password);
        return updateUser(user);
    }
}

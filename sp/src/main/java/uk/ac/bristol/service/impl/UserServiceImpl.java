package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        claims.put("assetHolderId", u.getAssetHolderId());
        claims.put("isAdmin", u.isAdmin());
        u.setToken(JwtUtil.generateJWT(claims));
        return u;
    }

    // get all users without asset holder part
    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }

    // get address and contact preferences for the asset holder
    private AssetHolder prepareAssetHolder(AssetHolder assetHolder) {
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
    public List<User> getAllUsersWithAssetHolder() {
        List<User> users = userMapper.selectAllUsers();
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders();
        Map<String, AssetHolder> mapping = assetHolders.stream()
                .collect(Collectors.toMap(AssetHolder::getId, ah -> ah));
        for (User user : users) {
            if (user.getAssetHolderId() == null) continue;
            AssetHolder ah = mapping.get(user.getAssetHolderId());
            if (ah == null) throw new RuntimeException("No asset holder for user " + user.getId());
            user.setAssetHolder(this.prepareAssetHolder(ah));
        }
        return users;
    }

    // This method only returns users with asset holder info (i.e. excluding admins)
    @Override
    public List<User> getAllUnauthorisedUsersWithAssetHolder() {
        List<User> users = userMapper.selectAllUsers();
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders();
        Map<String, AssetHolder> mapping = assetHolders.stream()
                .collect(Collectors.toMap(AssetHolder::getId, ah -> ah));
        return users.stream()
                .filter(user -> mapping.containsKey(user.getAssetHolderId()))
                .map(user -> {
                    user.setAssetHolder(this.prepareAssetHolder(mapping.get(user.getAssetHolderId())));
                    return user;
                })
                .collect(Collectors.toList());
    }

    // get all asset holders along with address and contact preferences
    @Override
    public List<AssetHolder> getAllAssetHolders() {
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders();
        assetHolders.forEach(this::prepareAssetHolder);
        return assetHolders;
    }

    @Override
    public List<Map<String, Object>> getAllAssetHoldersWithAssetIds() {
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders();
        List<Asset> assets = assetMapper.selectAllAssets();
        Map<String, List<String>> mapping = new HashMap<>();
        assets.forEach(asset -> {
            if (!mapping.containsKey(asset.getOwnerId())) {
                mapping.put(asset.getOwnerId(), new ArrayList<>());
            } else {
                mapping.get(asset.getOwnerId()).add(asset.getId());
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        for (AssetHolder holder : assetHolders) {
            List<String> ids = mapping.getOrDefault(holder.getId(), new ArrayList<>());
            result.add(Map.of("assetHolder", holder, "assetIds", ids));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllUsersWithAccumulator(String function, String column) {
        if("count".equalsIgnoreCase(function)) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Map<String, Object>> rawList = userMapper.selectAllAssetHoldersWithAccumulator(function, column);
            Map<String, Map<String, Object>> mapping = rawList.stream()
                    .collect(Collectors.toMap(m -> (String) m.get("id"),
                            m -> Map.of("assetHolder", prepareAssetHolder(mapper.convertValue(m, AssetHolder.class)), function, m.get("accumulation"))));

            List<User> users = userMapper.selectAllUsers();
            List<Map<String, Object>> result = new ArrayList<>();
            for (User user : users) {
                if (user.getAssetHolderId() == null) continue;
                Map<String, Object> tmp = mapping.get(user.getAssetHolderId());
                if (tmp == null) throw new RuntimeException("No asset holder for user " + user.getId());
                user.setAssetHolder((AssetHolder) tmp.get("assetHolder"));
                result.add(Map.of("user", user, function, tmp.get(function)));
            }
            return result;
        }
        throw new SpExceptions.GetMethodException("function " + function + " is not supported at current stage");
    }

    @Override
    public List<User> getAllAdmins() {
        return userMapper.selectUserByAdmin(true);
    }

    @Override
    public User getUserByAssetHolderId(String assetHolderId) {
        List<User> user = userMapper.selectUserByAssetHolderId(assetHolderId);
        if (user.size() != 1) {
            throw new RuntimeException("Get " + user.size() + " users using asset holder id " + assetHolderId);
        }
        List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByID(assetHolderId);
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
            List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByID(user.get(0).getAssetHolderId());
            if (assetHolder.size() != 1) {
                throw new RuntimeException("Get " + assetHolder.size() + " asset holders using asset holder id " + user.get(0).getAssetHolderId());
            }
            user.get(0).setAssetHolder(this.prepareAssetHolder(assetHolder.get(0)));
        }
        return user.get(0);
    }

    @Override
    public int insertUser(User user) {
        Integer n2 = null;
        if (user.getAssetHolder() != null) {
            AssetHolder ah = user.getAssetHolder();
            ah.setLastModified(Instant.now());
            n2 = assetHolderMapper.insertAddress(ah.getAddress());
            int n3 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
            int n4 = assetHolderMapper.insertAssetHolder(ah);
            if (n2 != n3 || n3 != n4) {
                throw new RuntimeException("Error inserting user " + user.getId() + ": affected rows not compatible");
            }
        }
        int n1 = userMapper.insertUser(user);
        if (n2 != null && n1 != n2) {
            throw new RuntimeException("Error inserting user " + user.getId() + ": affected rows not compatible");
        }
        return n1;
    }

    @Override
    public int updateUser(User user) {
        int n1 = userMapper.updateUser(user);
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
}

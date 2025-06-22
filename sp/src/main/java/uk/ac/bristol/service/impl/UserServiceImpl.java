package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class UserServiceImpl implements UserService {

    private final AssetHolderMapper assetHolderMapper;

    private final UserMapper userMapper;

    public UserServiceImpl(AssetHolderMapper assetHolderMapper, UserMapper userMapper) {
        this.assetHolderMapper = assetHolderMapper;
        this.userMapper = userMapper;
    }

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

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }

    @Override
    public List<User> getAllUsersWithAssetHolder() {
        List<User> users = userMapper.selectAllUsers();
        List<AssetHolder> assetHolders = assetHolderMapper.selectAllAssetHolders();
        Map<String, AssetHolder> mapping = assetHolders.stream()
                .collect(Collectors.toMap(AssetHolder::getId, ah -> ah));
        for (User user : users) {
            AssetHolder ah = mapping.get(user.getAssetHolderId());
            if (ah == null) throw new RuntimeException("No asset holder for user " + user.getId());
            user.setAssetHolder(ah);
        }
        return users;
    }

    @Override
    public List<AssetHolder> getAllAssetHolders() {
        return assetHolderMapper.selectAllAssetHolders();
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
        user.get(0).setAssetHolder(assetHolder.get(0));
        return user.get(0);
    }

    @Override
    public User getUserByUserId(String uid) {
        List<User> user = userMapper.selectUserById(uid);
        if (user.size() != 1) {
            throw new RuntimeException("Get " + user.size() + " users using user id " + uid);
        }
        List<AssetHolder> assetHolder = assetHolderMapper.selectAssetHolderByID(user.get(0).getAssetHolderId());
        if (assetHolder.size() != 1) {
            throw new RuntimeException("Get " + assetHolder.size() + " asset holders using asset holder id " + user.get(0).getAssetHolderId());
        }
        user.get(0).setAssetHolder(assetHolder.get(0));
        return user.get(0);
    }

    @Override
    public int insertUser(User user) {
        int n1 = userMapper.insertUser(user);
        Integer n2 = null;
        if (user.getAssetHolder() != null) {
            AssetHolder ah = user.getAssetHolder();
            n2 = assetHolderMapper.insertAddress(ah.getAddress());
            int n3 = assetHolderMapper.insertContactPreferences(ah.getContactPreferences());
            int n4 = assetHolderMapper.insertAssetHolder(ah);
            if (n2 != n3 || n3 != n4) {
                throw new RuntimeException("Error inserting user " + user.getId() + ": affected rows not compatible");
            }
        }
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
        int sum = 0;
        for (String id : ids) {
            int n1 = assetHolderMapper.deleteAddressByAssetHolderId(id);
            int n2 = assetHolderMapper.deleteContactPreferencesByAssetHolderId(id);
            int n3 = assetHolderMapper.deleteAssetHolderByAssetHolderIDs(new String[]{id});
            if (n1 != n2 || n3 != n2) {
                throw new RuntimeException("Error deleting asset holder " + id + ": affected rows not compatible");
            }
            sum += n1;
        }
        return sum;
    }
}

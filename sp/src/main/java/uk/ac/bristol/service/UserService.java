package uk.ac.bristol.service;

import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;

import java.util.List;

public interface UserService {

    User login(User user);

    List<User> getAllUsers();

    List<User> getAllUsersWithAssetHolder();

    List<AssetHolder> getAllAssetHolders();

    List<User> getAllAdmins();

    User getUserByAssetHolderId(String assetHolderId);

    User getUserByUserId(String id);

    int insertUser(User user);

    int updateUser(User user);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteUserByUserIds(String[] ids);
    int deleteUserByUserIds(List<String> ids);

    int deleteUserByAssetHolderIds(String[] ids);
    int deleteUserByAssetHolderIds(List<String> ids);

    int deleteAssetHolderByAssetHolderIds(String[] ids);
    int deleteAssetHolderByAssetHolderIds(List<String> ids);
}

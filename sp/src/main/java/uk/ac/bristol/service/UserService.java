package uk.ac.bristol.service;

import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    User login(User user);

    List<User> getAllUsers(List<Map<String, String>> orderList,
                           Integer limit,
                           Integer offset);

    List<User> getAllUsersWithAssetHolder(List<Map<String, String>> orderList,
                                          Integer limit,
                                          Integer offset);

    List<User> getAllUnauthorisedUsersWithAssetHolder(List<Map<String, String>> orderList,
                                                      Integer limit,
                                                      Integer offset);

    List<AssetHolder> getAllAssetHolders(List<Map<String, String>> orderList,
                                         Integer limit,
                                         Integer offset);

    List<Map<String, Object>> getAllAssetHoldersWithAssetIds(List<Map<String, String>> orderList,
                                                             Integer limit,
                                                             Integer offset);

    List<Map<String, Object>> getAllUsersWithAccumulator(String function,
                                                         String column,
                                                         List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset);

    List<User> getAllAdmins(List<Map<String, String>> orderList,
                            Integer limit,
                            Integer offset);

    User getUserByAssetHolderId(String assetHolderId,
                                List<Map<String, String>> orderList,
                                Integer limit,
                                Integer offset);

    User getUserByUserId(String id,
                         List<Map<String, String>> orderList,
                         Integer limit,
                         Integer offset);

    int insertUser(User user);

    int registerNewUser(User user);

    int updateUser(User user);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteUserByUserIds(String[] ids);
    int deleteUserByUserIds(List<String> ids);

    int deleteUserByAssetHolderIds(String[] ids);
    int deleteUserByAssetHolderIds(List<String> ids);

    int deleteAssetHolderByAssetHolderIds(String[] ids);
    int deleteAssetHolderByAssetHolderIds(List<String> ids);
}

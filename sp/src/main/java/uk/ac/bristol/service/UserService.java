package uk.ac.bristol.service;

import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.UserWithExtraColumns;

import java.util.List;
import java.util.Map;

public interface UserService {

    User login(User user);

    List<User> getAllUsers(Map<String, Object> filters,
                           List<Map<String, String>> orderList,
                           Integer limit,
                           Integer offset);

    List<User> getAllUsersWithAssetHolder(Map<String, Object> filters,
                                          List<Map<String, String>> orderList,
                                          Integer limit,
                                          Integer offset);

    List<User> getAllUnauthorisedUsersWithAssetHolder(Map<String, Object> filters,
                                                      List<Map<String, String>> orderList,
                                                      Integer limit,
                                                      Integer offset);

    List<Map<String, Object>> getAllAssetHoldersWithAssetIds(Map<String, Object> filters,
                                                             List<Map<String, String>> orderList,
                                                             Integer limit,
                                                             Integer offset);

    List<UserWithExtraColumns> getAllUsersWithAccumulator(String function,
                                                          String column,
                                                          Map<String, Object> filters,
                                                          List<Map<String, String>> orderList,
                                                          Integer limit,
                                                          Integer offset);

    User getUserByAssetHolderId(String aid);

    User getUserByUserId(String uid);

    Long getUserRowIdByUserId(String uid);

    boolean testUIDExistence(String id);

    boolean testEmailAddressExistence(String email);

    List<UserWithAssets> groupUsersWithOwnedAssetsByWarningId(Integer limit,
                                                              Long cursor,
                                                              Long waringId,
                                                              boolean getDiff,
                                                              String newAreaAsJson);

    int countUsersWithFilter(Map<String, Object> filters);

    boolean compareUserLastModified(String uid, Long timestamp);

    int insertUser(User user);

    void registerNewUser(User user);

    int updateUser(User user);

    int updateAssetHolder(AssetHolder assetHolder);

    int updatePasswordByEmail(String email, String password);

    int deleteUserByUserIds(String[] ids);

    int deleteUserByUserIds(List<String> ids);

    int deleteUserByAssetHolderIds(String[] ids);

    int deleteUserByAssetHolderIds(List<String> ids);

    int deleteAssetHolderByAssetHolderIds(String[] ids);

    int deleteAssetHolderByAssetHolderIds(List<String> ids);
}

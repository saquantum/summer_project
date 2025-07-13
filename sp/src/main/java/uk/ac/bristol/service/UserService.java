package uk.ac.bristol.service;

import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;

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

    List<AssetHolder> getAllAssetHolders(Map<String, Object> filters,
                                         List<Map<String, String>> orderList,
                                         Integer limit,
                                         Integer offset);

    List<Map<String, Object>> getAllAssetHoldersWithAssetIds(Map<String, Object> filters,
                                                             List<Map<String, String>> orderList,
                                                             Integer limit,
                                                             Integer offset);

    List<Map<String, Object>> getAllUsersWithAccumulator(String function,
                                                         String column,
                                                         Map<String, Object> filters,
                                                         List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset);

    User getUserByAssetHolderId(String assetHolderId);

    User getUserByUserId(String id);

    boolean testUIDExistence(String id);

    boolean testEmailExistence(String email);

    Long countUsersWithFilter(Map<String, Object> filters);

    Long countAssetHoldersWithFilter(Map<String, Object> filters);

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

    int updatePasswordByEmail(String email, String password);
}

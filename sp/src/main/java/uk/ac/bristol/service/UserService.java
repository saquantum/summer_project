package uk.ac.bristol.service;

import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.UserWithExtraColumns;

import java.util.List;
import java.util.Map;

public interface UserService {

    String login(User user);

    List<User> getUsers(Map<String, Object> filters,
                        List<Map<String, String>> orderList,
                        Integer limit,
                        Integer offset);

    List<User> getUnauthorisedUsers(Map<String, Object> filters,
                                    List<Map<String, String>> orderList,
                                    Integer limit,
                                    Integer offset);

    List<UserWithExtraColumns> getUsersWithAccumulator(String function,
                                                       String column,
                                                       Map<String, Object> filters,
                                                       List<Map<String, String>> orderList,
                                                       Integer limit,
                                                       Integer offset);

    User getUserByUserId(String uid);

    Long getUserRowIdByUserId(String uid);

    boolean testUIDExistence(String id);

    boolean testEmailAddressExistence(String email);

    List<UserWithAssets> groupUsersWithOwnedAssetsByWarningId(Integer limit,
                                                              Long cursor,
                                                              Long waringId,
                                                              boolean getDiff,
                                                              String newAreaAsJson);

    long countUsersWithFilter(Map<String, Object> filters);

    boolean compareUserLastModified(String uid, Long timestamp);

    boolean upsertAddress(User user);

    boolean upsertContactDetails(User user);

    boolean upsertContactPreferences(User user);

    int insertUser(User user);

    int insertUserBatch(List<User> list);

    void registerNewUser(Map<String, String> data);

    int updateUser(User user);

    int updateUserBatch(List<User> list);

    int updateUserPasswordByEmail(String email, String password);

    int deleteUserByUserIds(List<String> ids);
}

package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssetHolder;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<User> selectAllUsers(@Param("orderList") List<Map<String, String>> orderList,
                              @Param("limit") Integer limit,
                              @Param("offset") Integer offset);

    List<User> selectUserById(@Param("id") String id);

    List<User> loginQuery(User user);

    List<User> selectUserByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    List<User> selectUserByAdmin(@Param("isAdmin") Boolean isAdmin,
                                 @Param("orderList") List<Map<String, String>> orderList,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);

    List<UserWithAssetHolder> selectAllUsersWithAssetHolder(@Param("orderList") List<Map<String, String>> orderList,
                                                            @Param("limit") Integer limit,
                                                            @Param("offset") Integer offset);

    List<UserWithAssetHolder> selectAllUnauthorisedUsersWithAssetHolder(@Param("orderList") List<Map<String, String>> orderList,
                                                                        @Param("limit") Integer limit,
                                                                        @Param("offset") Integer offset);

    List<UserWithAssetHolder> selectAllUsersWithAccumulator(@Param("function") String function,
                                                            @Param("column") String column,
                                                            @Param("orderList") List<Map<String, String>> orderList,
                                                            @Param("limit") Integer limit,
                                                            @Param("offset") Integer offset);

    int insertUser(User user);

    int updateUserByUserId(User user);

    int deleteUserByAssetHolderIDs(@Param("ids") String[] assetHolderIds);

    int deleteUserByAssetHolderIDs(@Param("ids") List<String> assetHolderIds);

    int deleteUserByIds(@Param("ids") String[] ids);

    int deleteUserByIds(@Param("ids") List<String> ids);

    String selectUidByAid(@Param("id") String aid);
}

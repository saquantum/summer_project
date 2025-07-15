package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithExtraColumns;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<User> selectUsers(@Param("filterString") String filterString,
                           @Param("orderList") List<Map<String, String>> orderList,
                           @Param("limit") Integer limit,
                           @Param("offset") Integer offset);

    List<User> selectUsersPuttingAssetHoldersTableMain(@Param("filterString") String filterString,
                                                       @Param("orderList") List<Map<String, String>> orderList,
                                                       @Param("limit") Integer limit,
                                                       @Param("offset") Integer offset);

    List<User> selectUsersWithoutAssociation(@Param("filterString") String filterString,
                                             @Param("orderList") List<Map<String, String>> orderList,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    List<UserWithExtraColumns> selectUsersWithAccumulator(@Param("function") String function,
                                                          @Param("column") String column,
                                                          @Param("filterString") String filterString,
                                                          @Param("orderList") List<Map<String, String>> orderList,
                                                          @Param("limit") Integer limit,
                                                          @Param("offset") Integer offset);

    String selectPasswordByUserId(@Param("id") String id);

    int countUsers(@Param("filterString") String filterString);

    int insertUser(User user);

    int updateUserByUserId(User user);

    int updateUserPasswordByUserId(User user);

    int deleteUserByAssetHolderIDs(@Param("ids") String[] assetHolderIds);

    int deleteUserByAssetHolderIDs(@Param("ids") List<String> assetHolderIds);

    int deleteUserByIds(@Param("ids") String[] ids);

    int deleteUserByIds(@Param("ids") List<String> ids);
}

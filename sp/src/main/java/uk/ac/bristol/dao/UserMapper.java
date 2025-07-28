package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.FilterItemDTO;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.UserWithExtraColumns;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    // selecting

    List<User> selectUsers(@Param("filterList") List<FilterItemDTO> filterList,
                           @Param("orderList") List<Map<String, String>> orderList,
                           @Param("limit") Integer limit,
                           @Param("offset") Integer offset);

    List<User> selectUsersWithoutAssociation(@Param("filterList") List<FilterItemDTO> filterList,
                                             @Param("orderList") List<Map<String, String>> orderList,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    List<UserWithExtraColumns> selectUsersWithAccumulator(@Param("function") String function,
                                                          @Param("column") String column,
                                                          @Param("filterList") List<FilterItemDTO> filterList,
                                                          @Param("orderList") List<Map<String, String>> orderList,
                                                          @Param("limit") Integer limit,
                                                          @Param("offset") Integer offset);

    String selectPasswordByUserId(@Param("id") String id);

    Long selectUserRowIdByUserId(@Param("id") String id);

    // grouping

    List<UserWithAssets> groupUsersWithOwnedAssetsByWarningId(@Param("limit") Integer limit,
                                                              @Param("cursor") Long cursor,
                                                              @Param("warningId") Long warningId,
                                                              @Param("getDiff") boolean getDiff,
                                                              @Param("newArea") String newAreaAsJson);

    // counting

    long countUsers(@Param("filterList") List<FilterItemDTO> filterList);

    // write & delete

    int insertUser(User user);

    int updateUserByUserId(User user);

    int updateUserPasswordByUserId(User user);

    int deleteUserByUserIds(@Param("ids") List<String> ids);

    /* address */

    List<Map<String, String>> selectAddressByUserId(@Param("id") String userId);

    boolean upsertAddressByUserId(@Param("id") String uid, Map<String, String> map);

    int deleteAddressByUserIds(@Param("ids") List<String> ids);

    /* contact details */

    List<Map<String, String>> selectContactDetailsByUserId(@Param("id") String userId);

    boolean upsertContactDetailsByUserId(@Param("id") String uid, Map<String, String> map);

    int deleteContactDetailsByUserIds(@Param("ids") List<String> ids);

    /* contact preference */

    List<Map<String, Boolean>> selectContactPreferencesByUserId(@Param("id") String userId);

    boolean upsertContactPreferencesByUserId(@Param("id") String uid, Map<String, Boolean> map);

    int deleteContactPreferencesByUserIds(@Param("ids") List<String> ids);
}

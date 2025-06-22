package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectAllUsers();

    List<User> selectUserById(@Param("id") String id);

    List<User> loginQuery(User user);

    List<User> selectUserByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    List<User> selectUserByAdmin(@Param("isAdmin") Boolean isAdmin);

    int insertUser(User user);

    int updateUser(User user);

    int deleteUserByAssetHolderIDs(@Param("ids") String[] assetHolderIds);
    int deleteUserByAssetHolderIDs(@Param("ids") List<String> assetHolderIds);

    int deleteUserByIds(@Param("ids") String[] ids);
    int deleteUserByIds(@Param("ids") List<String> ids);
}

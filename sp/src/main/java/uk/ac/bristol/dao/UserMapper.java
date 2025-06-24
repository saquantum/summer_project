package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.ObjectError;
import uk.ac.bristol.pojo.User;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<User> selectAllUsers();

    List<User> selectUserById(@Param("id") String id);

    List<User> loginQuery(User user);

    List<User> selectUserByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    List<User> selectUserByAdmin(@Param("isAdmin") Boolean isAdmin);

    List<Map<String, Object>> selectAllAssetHoldersWithAccumulator(@Param("function") String function, @Param("column") String column);

    int insertUser(User user);

    int updateUser(User user);

    int deleteUserByAssetHolderIDs(@Param("ids") String[] assetHolderIds);

    int deleteUserByAssetHolderIDs(@Param("ids") List<String> assetHolderIds);

    int deleteUserByIds(@Param("ids") String[] ids);

    int deleteUserByIds(@Param("ids") List<String> ids);
}

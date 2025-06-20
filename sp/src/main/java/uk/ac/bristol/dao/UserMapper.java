package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserAsAssetHolder> selectUserByAssetHolderId(@Param("id") Integer id);

    int insertUser(User user);

    int updateUser(User user);

    List<User> loginQuery(User user);
}

package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.UserEmail;

import java.util.List;

@Mapper
public interface UserEmailMapper {

    void insertEmail(UserEmail userEmail);

    List<UserEmail> selectAllEmails();

    boolean existsByEmail(@Param("email") String email);

    int deleteByEmail(String email);

    boolean existsByUid(@Param("uid") String uid);

    int deleteAll();
}
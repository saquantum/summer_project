package org.example.mail.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.mail.pojo.UserEmail;

import java.rmi.server.UID;
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
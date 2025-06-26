package org.example.mail.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.mail.pojo.UserDiscord;

import java.util.List;

@Mapper
public interface UserDiscordMapper {

    void insertDiscord(UserDiscord userDiscord);

    List<UserDiscord> selectAllDiscords();

    boolean existsByDiscord(@Param("discord") String discord);

    int deleteByDiscord(String discord);

    int deleteAll();
}

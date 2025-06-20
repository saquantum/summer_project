package org.example.mail.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.mail.pojo.UserWhatsApp;
import java.util.List;

@Mapper
public interface UserWhatsAppMapper {
    int insert(UserWhatsApp userWhatsApp);
    List<UserWhatsApp> selectAll();
    boolean existsByWhatsApp(@Param("phoneNumber") String phoneNumber);
}

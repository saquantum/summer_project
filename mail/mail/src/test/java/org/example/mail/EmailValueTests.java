package org.example.mail;

import org.example.mail.dao.UserEmailMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class EmailValueTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserEmailMapper emailMapper;

    @Test
    public void testAddValidEmail() throws Exception {         //有效邮箱
        mockMvc.perform(post("/api/user/add")
                        .param("email", "1971026049@qq.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email added successfully!"));
    }

    @Test
    public void testAddInvalidEmail1() throws Exception {   //仅有@前面的部分
        mockMvc.perform(post("/api/user/add")
                        .param("email", "bad-email"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddInvalidEmail2() throws Exception {    //仅有数字
        mockMvc.perform(post("/api/user/add")
                        .param("email", "123456"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddInvalidEmail3() throws Exception {     //空字符串
        mockMvc.perform(post("/api/user/add")
                        .param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddInvalidEmail4() throws Exception {      //无效邮箱1
        mockMvc.perform(post("/api/user/add")
                        .param("email", "123456@w.w.w.w.w"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddInvalidEmail5() throws Exception {      //无效邮箱2
        mockMvc.perform(post("/api/user/add")
                        .param("email", "123456@c"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddInvalidEmail6() throws Exception {      //无效邮箱3
        mockMvc.perform(post("/api/user/add")
                        .param("email", "1971026049@qqqq.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email!"));
    }

    @Test
    public void testAddSameEmail() throws Exception {        //重复邮箱
        mockMvc.perform(post("/api/user/add")
                        .param("email", "1971026049@qq.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email added successfully!"));
        mockMvc.perform(post("/api/user/add")
                        .param("email", "1971026049@qq.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists!"));
    }

    @AfterEach
    public void cleanDatabase() {
        emailMapper.deleteAll();
    }
}
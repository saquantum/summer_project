package com.ourrainwater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourrainwater.controller.CrudController;
import com.ourrainwater.dao.SqlMapper;
import com.ourrainwater.service.SqlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SpApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqlMapper sqlMapper;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private CrudController crudController;

    @Test
    public void testPostgre(){
        System.out.println(sqlService.selectAllAssetHolders());
        System.out.println(sqlService.selectAllAssets());
    }

//    @Test
//    public void testExceptionHandler() throws Exception {
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/locations")).andReturn();
//        String json = mvcResult.getResponse().getContentAsString();
//        ResponseResult response = objectMapper.readValue(json, ResponseResult.class);
//        System.out.println(response);
//    }

}

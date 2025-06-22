package uk.ac.bristol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import uk.ac.bristol.controller.CrudController;
import uk.ac.bristol.service.SqlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SpApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private CrudController crudController;

//    @Test
//    public void testExceptionHandler() throws Exception {
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/locations")).andReturn();
//        String json = mvcResult.getResponse().getContentAsString();
//        ResponseResult response = objectMapper.readValue(json, ResponseResult.class);
//        System.out.println(response);
//    }

}

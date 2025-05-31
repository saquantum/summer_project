package com.ourrainwater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourrainwater.controller.LocationController;
import com.ourrainwater.controller.ResponseResult;
import com.ourrainwater.dao.LocationMapper;
import com.ourrainwater.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationController locationController;

    @Test
    public void testExceptionHandler() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/locations")).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        ResponseResult response = objectMapper.readValue(json, ResponseResult.class);
        System.out.println(response);
    }

}

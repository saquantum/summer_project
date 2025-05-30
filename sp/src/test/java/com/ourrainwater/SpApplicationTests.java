package com.ourrainwater;

import com.ourrainwater.dao.LocationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpApplicationTests {

    @Autowired
    private LocationMapper locationMapper;

    @Test
    public void test1() {
        System.out.println(locationMapper.selectLocationByID(1));
    }

}

package uk.ac.bristol.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.MockDataInitializer;

import java.io.IOException;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssetServiceTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }
}

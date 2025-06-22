package uk.ac.bristol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.service.ImportMockData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TempTest {

    @Autowired
    public ImportMockData importMockData;

    @Test
    public void test() {
        importMockData.resetSchema();
        importMockData.importUsers();
    }

}

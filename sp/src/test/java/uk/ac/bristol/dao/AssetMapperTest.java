package uk.ac.bristol.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssetMapperTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AssetHolderMapper assetHolderMapper;

    @Autowired
    private AssetMapper assetMapper;

    @BeforeAll
    public void init() {
        mockDataInitializer.forceReload();
    }

    @Test
    public void test1(){
        List<AssetWithWeatherWarnings> list = assetMapper.selectAllAssetsWithWarningsOfHolder("user_010");
        System.out.println(list);
    }

}

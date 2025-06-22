package uk.ac.bristol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import uk.ac.bristol.service.ImportMockData;

public class MockDataInitializer implements CommandLineRunner {

    @Autowired
    private ImportMockData importMockData;

    @Override
    public void run(String... args) throws Exception {
        importMockData.resetAndImport();
    }
}

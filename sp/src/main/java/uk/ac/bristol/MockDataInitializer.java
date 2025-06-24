package uk.ac.bristol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.ac.bristol.service.ImportMockData;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class MockDataInitializer implements CommandLineRunner {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Value("${mock-data.state}")
    private String STATE_FILE_PATH;
    @Value("${mock-data.assets}")
    private String ASSETS_FILE_PATH;
    @Value("${mock-data.asset-types}")
    private String ASSET_TYPES_FILE_PATH;
    @Value("${mock-data.users}")
    private String USERS_FILE_PATH;
    @Value("${mock-data.warnings}")
    private String WARNINGS_FILE_PATH;

    @Autowired
    private ImportMockData importMockData;

    private boolean shouldImport(File mockDataFile) throws IOException {
        File stateFile = new File(STATE_FILE_PATH);
        if (!stateFile.exists()) {
            return true;
        }
        Map<String, String> state = mapper.readValue(stateFile, Map.class);
        Instant lastUpdated = Instant.parse(state.getOrDefault("mock_data_last_updated", "2000-01-01T00:00:00Z"));
        Instant fileModified = Instant.ofEpochMilli(mockDataFile.lastModified());
        return fileModified.isAfter(lastUpdated);
    }

    private void updateImportTime() throws IOException {
        File stateFile = new File(STATE_FILE_PATH);
        stateFile.getParentFile().mkdirs();
        Map<String, String> state = Map.of("mock_data_last_updated", Instant.now().toString());
        mapper.writeValue(stateFile, state);
    }

    public void forceReload(){
        importMockData.resetSchema();
        importMockData.importUsers(USERS_FILE_PATH);
        importMockData.importAssets(ASSET_TYPES_FILE_PATH, ASSETS_FILE_PATH);
        importMockData.importWarnings(WARNINGS_FILE_PATH);
    }

    @Override
    public void run(String... args) throws Exception {
        boolean flag = false;
        if (shouldImport(new File(USERS_FILE_PATH))
                | shouldImport(new File(ASSET_TYPES_FILE_PATH))
                || shouldImport(new File(ASSETS_FILE_PATH))
                || shouldImport(new File(WARNINGS_FILE_PATH))) {
            importMockData.resetSchema();
            flag = true;
        }

        if (shouldImport(new File(USERS_FILE_PATH))) {
            importMockData.importUsers(USERS_FILE_PATH);

        } else {
            System.out.println("Users file skipped");
        }
        if (shouldImport(new File(ASSET_TYPES_FILE_PATH)) || shouldImport(new File(ASSETS_FILE_PATH))) {
            importMockData.importAssets(ASSET_TYPES_FILE_PATH, ASSETS_FILE_PATH);
        } else {
            System.out.println("Assets file skipped");
        }
        if (shouldImport(new File(WARNINGS_FILE_PATH))) {
            importMockData.importWarnings(WARNINGS_FILE_PATH);
        } else {
            System.out.println("Warnings file skipped");
        }
        if (flag) {
            updateImportTime();
        }
    }
}

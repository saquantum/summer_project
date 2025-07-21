package uk.ac.bristol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.ac.bristol.service.ImportMockData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Component
public class MockDataInitializer implements CommandLineRunner {

    public static final CountDownLatch latch = new CountDownLatch(1);

    private final ImportMockData importMockData;
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
    @Value("${mock-data.notifications}")
    private String NOTIFICATION_FILE_PATH;

    public MockDataInitializer(ImportMockData importMockData) throws IOException {
        this.importMockData = importMockData;
    }

    private InputStream getClasspathStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

    private boolean shouldImport(String key) throws IOException {
        File stateFile = new File(STATE_FILE_PATH);
        if (!stateFile.exists()) {
            stateFile.getParentFile().mkdirs();
            mapper.writeValue(stateFile, Map.of("users", false, "assets", false, "warnings", false, "templates", false));
        }
        Map<String, Boolean> state = mapper.readValue(stateFile, Map.class);
        return !Boolean.TRUE.equals(state.get(key));
    }

    private void markAsImported(String key) throws IOException {
        File stateFile = new File(STATE_FILE_PATH);
        Map<String, Boolean> state = stateFile.exists()
                ? mapper.readValue(stateFile, Map.class)
                : new HashMap<>();
        if (state == null) {
            state = new HashMap<>();
        }
        state.put(key, true);
        mapper.writeValue(stateFile, state);
    }

    public void forceReload() throws IOException {
        importMockData.resetSchema();
        importMockData.importUsers(getClasspathStream(USERS_FILE_PATH));
        importMockData.importAssets(getClasspathStream(ASSET_TYPES_FILE_PATH), getClasspathStream(ASSETS_FILE_PATH));
        importMockData.importWarnings(getClasspathStream(WARNINGS_FILE_PATH));
        importMockData.importTemplates(getClasspathStream(NOTIFICATION_FILE_PATH));
    }

    @Override
    public void run(String... args) throws Exception {
        if (shouldImport("users")
                || shouldImport("assets")
                || shouldImport("warnings")
                || shouldImport("templates")) {
            importMockData.resetSchema();
        }

        if (shouldImport("users")) {
            importMockData.importUsers(getClasspathStream(USERS_FILE_PATH));
            markAsImported("users");

        } else {
            System.out.println("Users file skipped");
        }

        if (shouldImport("assets")) {
            importMockData.importAssets(getClasspathStream(ASSET_TYPES_FILE_PATH), getClasspathStream(ASSETS_FILE_PATH));
            markAsImported("assets");
        } else {
            System.out.println("Assets file skipped");
        }

        if (shouldImport("warnings")) {
            importMockData.importWarnings(getClasspathStream(WARNINGS_FILE_PATH));
            markAsImported("warnings");
        } else {
            System.out.println("Warnings file skipped");
        }

        if (shouldImport("templates")) {
            importMockData.importTemplates(getClasspathStream(NOTIFICATION_FILE_PATH));
            markAsImported("templates");
        } else {
            System.out.println("Templates file skipped");
        }
        latch.countDown();
    }
}

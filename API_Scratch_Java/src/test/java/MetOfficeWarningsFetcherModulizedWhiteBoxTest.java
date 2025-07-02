import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.*;

/** White-box tests — 直接呼叫私有方法 */
public class MetOfficeWarningsFetcherModulizedWhiteBoxTest {

    /* ------------------------------------------------------------
     * 抓取欲測類別 & 方法物件，只做一次反射成本最低
     * 為了覆蓋私有方法，利用反射繞開權限限制，取得 Method 對象，然後用 method.invoke() 調用
     * ---------------------------------------------------------- */
    private static Class<?> MetOfficeWarningsFetcherClazz;
    private static Method mHandleStatus;
    private static Method mIsValid;
    private static Method mValidate;
    private static Method mSaveJson;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void reflectPrivateMethods() throws Exception {
        // Take the class object
        MetOfficeWarningsFetcherClazz = Class.forName("MetOfficeWarningsFetcher_modulized");

        // private static boolean handleStatusCode(int)
        mHandleStatus = MetOfficeWarningsFetcherClazz.getDeclaredMethod("handleStatusCode", int.class);
        mHandleStatus.setAccessible(true);

        // private static boolean isValidResponse(JsonNode)
        mIsValid = MetOfficeWarningsFetcherClazz.getDeclaredMethod("isValidResponse", JsonNode.class);
        mIsValid.setAccessible(true);

        // private static boolean validate(JsonNode)
        mValidate = MetOfficeWarningsFetcherClazz.getDeclaredMethod("validate", JsonNode.class);
        mValidate.setAccessible(true);

        // private static Path saveJson(JsonNode, String)
        mSaveJson = MetOfficeWarningsFetcherClazz.getDeclaredMethod("saveJson", JsonNode.class, String.class);
        mSaveJson.setAccessible(true);
    }

    /* -----------------------------------------
     *  Test: handleStatusCode()
     * --------------------------------------- */
    @Test
    @DisplayName("HTTP 200 should return true")
    void handleStatus_200_true() throws Exception {
        boolean ok = (boolean) mHandleStatus.invoke(null, 200);
        assertTrue(ok, "200 should be view as success");
    }

    @Test
    @DisplayName("Except for 200, others HTTP status should return false")
    void handleStatus_non200_false() throws Exception {
        int[] codes = {400, 401, 404, 500, 502, 503};  // 依序傳入不同 code
        for (int c : codes) {
            assertFalse((boolean) mHandleStatus.invoke(null, c),
                        "status "+c+" shoud return false");
        }
    }

    /* -----------------------------------------
     *  Test: isValidResponse() and validate()
     *     - "isValid" is the sufficient condition of "validate"
     *       => if (isValid) then (validate).
     * --------------------------------------- */
    @Test
    @DisplayName("features is array → isValid true, validate true")
    void validFeaturesArray() throws Exception {
        JsonNode okJson = MAPPER.readTree("{\"features\":[]}");
        assertTrue((boolean) mIsValid.invoke(null, okJson));
        assertTrue((boolean) mValidate.invoke(null, okJson));
    }

    @Test
    @DisplayName("body contains error → isValid false, validate false")
    void bodyWithError() throws Exception {
        JsonNode errJson = MAPPER.readTree("{\"error\":{\"code\":123}}");
        assertFalse((boolean) mIsValid.invoke(null, errJson));
        assertFalse((boolean) mValidate.invoke(null, errJson));
    }

    @Test
    @DisplayName("features is not list → isValid false")
    void featuresNotArray() throws Exception {
        JsonNode bad = MAPPER.readTree("{\"features\":{}}");
        assertFalse((boolean) mIsValid.invoke(null, bad));
    }

    /* -----------------------------------------
     *  Test saveJson()
     *  - Check：
     *    1. the file has been created
     *    2. the prefix aligned with the form of "raw_*.json / valid_*.json"
     * --------------------------------------- */
    @Test
    @DisplayName("saveJson() whould create a file with correct prefix in the folder: data/")
    void saveJsonCreatesFile() throws Exception {
        // 準備一個空 JSON
        JsonNode dummy = MAPPER.readTree("{}");

        // 呼叫 saveJson(dummy, "prefix_")
        Path p = (Path) mSaveJson.invoke(null, dummy, "prefix_");

        // 斷言檔案存在且前綴正確
        assertTrue(Files.exists(p), "The file should be stored correctly");
        assertTrue(p.getFileName().toString().startsWith("prefix_"));

        // 清理
        Files.deleteIfExists(p);
    }

    /* -----------------------------------------
     *  Test: validate() + saveJson()
     *  - Mimic the workflow: "raw → validate → valid"
     * --------------------------------------- */
    @Test
    @DisplayName("If the data contains the error filed, then only the raw_*.json will be created (valid_*.json will not).")
    void rawButNoValidOnError() throws Exception {
        JsonNode errJson = MAPPER.readTree("{\"error\":{\"code\":400}}");

        // 1) raw
        Path raw = (Path) mSaveJson.invoke(null, errJson, "raw_");
        // 2) validate
        boolean ok = (boolean) mValidate.invoke(null, errJson);
        if (ok) { // 不應走到此分支
            mSaveJson.invoke(null, errJson, "valid_");
        }

        long rawCnt   = countByPrefix("raw_");
        long validCnt = countByPrefix("valid_");

        assertEquals(1, rawCnt, "Should leave only a raw_*.json");
        assertEquals(0, validCnt, "Should not crate valid_*.json");

        // 清理
        Files.deleteIfExists(raw);
    }

    /* Private Function：Count the files & Delete ------------------- */
    private long countByPrefix(String prefix) throws Exception {
        Files.createDirectories(Path.of("data"));
        try (Stream<Path> s = Files.list(Path.of("data"))) {
            return s.filter(p -> p.getFileName().toString().startsWith(prefix)).count();
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        try (Stream<Path> s = Files.list(Path.of("data"))) {
            s.filter(p -> p.getFileName().toString()
                           .matches("(raw_|valid_|unit_).*\\.json"))
             .forEach(p -> p.toFile().delete());
        }
    }
}

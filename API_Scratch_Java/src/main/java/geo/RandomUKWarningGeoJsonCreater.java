import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.*;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class RandomUKWarningGeoJsonCreater {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RND = new Random();

    /* ↔ UK 粗略 Bounding Box（deg） */
    private static final double MIN_LON = -8.0;   // 西經
    private static final double MAX_LON =  2.0;   // 東經
    private static final double MIN_LAT = 49.0;   // 北緯
    private static final double MAX_LAT = 60.0;   // 北緯

    public static void main(String[] args) throws Exception {

        /* 0. 檔案輸出目錄 -------------------------------------------------- */
        Path dir = Path.of("data");
        if (Files.notExists(dir)) Files.createDirectories(dir);
        String ts  = String.valueOf(Instant.now().toEpochMilli());
        Path out = dir.resolve("random_warning_" + ts + ".json");

        /* 1. 隨機產生中心點 ------------------------------------------------ */
        double centerLon = randDouble(MIN_LON, MAX_LON);
        double centerLat = randDouble(MIN_LAT, MAX_LAT);

        /* 2. 產生近似圓 polygon（36 點） ---------------------------------- */
        ArrayNode ring = MAPPER.createArrayNode();      // 第一個線環 (LinearRing)
        int sides = 36;
        double radius = 0.3;                            // 半徑 (deg)

        for (int i = 0; i <= sides; i++) {              // <= 使其閉合，首尾同點
            double angle = 2 * Math.PI * i / sides;
            double lon = centerLon + radius * Math.cos(angle);
            double lat = centerLat + radius * Math.sin(angle);
            ArrayNode coord = MAPPER.createArrayNode()
                                    .add(round6(lon))
                                    .add(round6(lat));
            ring.add(coord);
        }

        /* 3. 組 Feature --------------------------------------------------- */
        ObjectNode feature = MAPPER.createObjectNode();
        feature.put("type", "Feature");
        feature.put("id", RND.nextInt(10_000) + 1000);          // 隨機 id

        // geometry
        ObjectNode geometry = MAPPER.createObjectNode();
        geometry.put("type", "Polygon");
        geometry.set("coordinates",
            MAPPER.createArrayNode()        // 外圍 array
                  .add(ring));              // 只有一個線環
        feature.set("geometry", geometry);

        // properties
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("OBJECTID", feature.get("id").asInt());
        prop.put("weathertype", sample(new String[]{"RAIN","SNOW","THUNDERSTORM"}));
        prop.put("warninglevel", sample(new String[]{"YELLOW","AMBER","RED"}));
        prop.put("warningheadline", "Auto-generated test warning");
        long from  = Instant.now().toEpochMilli();
        long to    = from + 6 * 60 * 60 * 1_000;  // +6 小時
        prop.put("validfromdate", from);
        prop.put("validtodate",   to);
        prop.put("GlobalID", UUID.randomUUID().toString());
        prop.put("warningImpact", "2/Low");
        prop.put("warningLikelihood", "3/Likely");
        prop.put("warningVersion", "1.0");
        prop.put("affectedAreas", "Auto-generated polygon around random centre");
        prop.put("whatToExpect",   "Auto-generated demo data.");
        prop.putNull("warningUpdateDescription");
        prop.put("issuedDate", from);
        prop.put("issuedDateString", "N/A");
        prop.put("validFromDateString", "N/A");
        prop.put("validToDateString", "N/A");
        prop.put("modifiedDateString", "N/A");
        feature.set("properties", prop);

        /* 4. 組 FeatureCollection ----------------------------------------- */
        ObjectNode fc = MAPPER.createObjectNode();
        fc.put("type", "FeatureCollection");
        fc.set("features", MAPPER.createArrayNode().add(feature));

        /* 5. 輸出 JSON ---------------------------------------------------- */
        MAPPER.writerWithDefaultPrettyPrinter()
              .writeValue(out.toFile(), fc);

        System.out.println("✅ (fake warning)  GeoJSON saved to  " + out.toAbsolutePath());
    }

    /* ----------- helpers ----------- */

    /** 生成 [min,max] 之間的隨機 double */
    private static double randDouble(double min, double max) {
        return min + RND.nextDouble() * (max - min);
    }

    /** 取自陣列中隨機一個值 */
    private static String sample(String[] arr) {
        return arr[RND.nextInt(arr.length)];
    }

    /** ⤴ 讓輸出小數保留六位，提高可讀性 */
    private static double round6(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }
}

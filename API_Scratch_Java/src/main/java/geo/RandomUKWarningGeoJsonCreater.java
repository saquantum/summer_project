package geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * 產生「更真實」外觀的 GeoJSON 多邊形
 * －在中心周圍隨機撒點，取凸包
 */
public class RandomUKWarningGeoJsonCreater {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RND = new Random();

    /* UK 大約邊界 */
    private static final double MIN_LON = -8.0, MAX_LON = 2.0;
    private static final double MIN_LAT = 49.0, MAX_LAT = 60.0;

    public static void main(String[] args) throws Exception {
        
        Path dir = Path.of("data");
        if (Files.notExists(dir)) Files.createDirectories(dir);

        // 1. 檔名用 timestamp
        String ts = String.valueOf(Instant.now().toEpochMilli());
        Path geoJsonFile = dir.resolve("random_warning_" + ts + ".json");
        Path htmlFile    = dir.resolve("random_warning_" + ts + ".html");

        // 2. 隨機中心
        double centerLon = rand(MIN_LON, MAX_LON);
        double centerLat = rand(MIN_LAT, MAX_LAT);

        // 3. 產生一堆隨機點
        List<Point> pts = new ArrayList<>();
        double radius = 0.3;        // 半徑 (deg)
        int count = 100;          // 100 點

        for (int i = 0; i < count; i++) {
            // 在圓內隨機分布：r = R * sqrt(u)，θ = 2πv
            double u = RND.nextDouble(),
                   v = RND.nextDouble();
            double r = radius * Math.sqrt(u);
            double theta = 2 * Math.PI * v;
            pts.add(new Point(
                centerLon + r * Math.cos(theta),
                centerLat + r * Math.sin(theta)
            ));
        }

        // 4. 計算凸包（Monotonic Chain）
        List<Point> hull = convexHull(pts);

        // 5. 把 hull 轉成 GeoJSON 坐標陣列
        ArrayNode ring = MAPPER.createArrayNode();
        for (Point p : hull) {
            ring.add(
                MAPPER.createArrayNode()
                      .add(round6(p.x))
                      .add(round6(p.y))
            );
        }

        // 6. 組 Feature
        ObjectNode feature = MAPPER.createObjectNode();
        feature.put("type", "Feature");
        feature.put("id", RND.nextInt(9_000) + 1000);

        ObjectNode geom = MAPPER.createObjectNode();
        geom.put("type", "Polygon")
            .set("coordinates", MAPPER.createArrayNode().add(ring));
        feature.set("geometry", geom);

        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("OBJECTID", feature.get("id").asInt());
        prop.put("weathertype", sample("RAIN","SNOW","THUNDERSTORM"));
        prop.put("warninglevel" , sample("YELLOW","AMBER","RED"));
        prop.put("warningheadline", "Auto-generated test warning");
        long now = Instant.now().toEpochMilli();
        prop.put("validfromdate", now);
        prop.put("validtodate"  , now + 6 * 3600_000);
        prop.put("GlobalID", UUID.randomUUID().toString());
        feature.set("properties", prop);

        // 7. FeatureCollection
        ObjectNode fc = MAPPER.createObjectNode();
        fc.put("type", "FeatureCollection")
          .set("features", MAPPER.createArrayNode().add(feature));

        // 8. 輸出
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(geoJsonFile.toFile(), fc);
        System.out.println("✅ (fake GeoJson) GeoJSON saved to " + geoJsonFile);

        // 9. 產生並輸出 HTML (略，同之前)
        Files.writeString(htmlFile, htmlTemplate(ts));
        System.out.println("✅ HTML   saved to " + htmlFile);
    }

    // — helpers ——————————————————————————————————————————————————

    /** 隨機取 double in [min,max] */
    private static double rand(double min, double max) {
        return min + RND.nextDouble()*(max-min);
    }
    private static String sample(String... arr) {
        return arr[RND.nextInt(arr.length)];
    }
    private static double round6(double v) {
        return Math.round(v*1_000_000d)/1_000_000d;
    }

    /** 2D 點 */
    private static class Point implements Comparable<Point> {
        double x,y;
        Point(double x,double y){this.x=x;this.y=y;}
        // 排序：先 x 再 y
        public int compareTo(Point o){
            int cx=Double.compare(this.x,o.x);
            return cx!=0?cx:Double.compare(this.y,o.y);
        }
    }

    /* Monotonic chain 凸包算法 */
    private static List<Point> convexHull(List<Point> pts) {
        Collections.sort(pts);
        List<Point> lo = new ArrayList<>(), hi = new ArrayList<>();
        for (Point p : pts) {
            while (lo.size()>=2 && cross(lo.get(lo.size()-2), lo.get(lo.size()-1), p) <= 0)
                lo.remove(lo.size()-1);
            lo.add(p);
        }
        for (int i=pts.size()-1;i>=0;i--){
            Point p=pts.get(i);
            while (hi.size()>=2 && cross(hi.get(hi.size()-2), hi.get(hi.size()-1), p) <= 0)
                hi.remove(hi.size()-1);
            hi.add(p);
        }
        lo.remove(lo.size()-1);
        hi.remove(hi.size()-1);
        lo.addAll(hi);
        return lo;
    }

    /* 計算三點叉積 (p→q)×(p→r) */
    private static double cross(Point p, Point q, Point r) {
        return (q.x-p.x)*(r.y-p.y) - (q.y-p.y)*(r.x-p.x);
    }

    /* 產生 HTML */
    private static String htmlTemplate(String ts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n")
          .append("<html>\n")
          .append("<head>\n")
          .append("  <meta charset=\"utf-8\" />\n")
          .append("  <title>Random Warning ").append(ts).append("</title>\n")
          .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("  <link rel=\"stylesheet\" ")
          .append("href=\"https://unpkg.com/leaflet/dist/leaflet.css\"/>\n")
          .append("  <style>html,body,#map{height:100%;margin:0}</style>\n")
          .append("</head>\n")
          .append("<body>\n")
          .append("  <div id=\"map\"></div>\n")
          .append("  <script src=\"https://unpkg.com/leaflet/dist/leaflet.js\"></script>\n")
          .append("  <script>\n")
          .append("    const map = L.map('map').setView([54.5,-2],6);\n")
          .append("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{\n")
          .append("      maxZoom:19, attribution:'© OpenStreetMap'\n")
          .append("    }).addTo(map);\n")
          .append("\n")
          .append("    fetch('random_warning_").append(ts).append(".json')\n")
          .append("      .then(r=>r.json())\n")
          .append("      .then(geo=>{\n")
          .append("        const layer = L.geoJSON(geo, {\n")
          .append("          style: {color:'red', weight:2, fillOpacity:0.3}\n")
          .append("        }).addTo(map);\n")
          .append("        map.fitBounds(layer.getBounds());\n")
          .append("      })\n")
          .append("      .catch(err=>alert('讀取失敗:'+err));\n")
          .append("  </script>\n")
          .append("</body>\n")
          .append("</html>\n");
        return sb.toString();
    }
}

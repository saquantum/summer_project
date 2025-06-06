package com.ourrainwater.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * <a href="https://mybatis.org/mybatis-3/configuration.html#typeHandlers">type handlers</a>
 */

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class PolygonTypeHandler extends BaseTypeHandler<Map<String, Object>> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GeometryFactory factory = new GeometryFactory();
    private final GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    private final GeoJsonReader geoJsonReader = new GeoJsonReader(factory);
    private final WKBReader wkbReader = new WKBReader(factory);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            Map geometry = (Map) parameter.get("geometry");
            String geoJson = mapper.writeValueAsString(geometry);
            Geometry geom = geoJsonReader.read(geoJson);
            ps.setObject(i, geom);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to convert GeoJSON to Geometry", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertWkbToFeature(rs.getObject(columnName));
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertWkbToFeature(rs.getObject(columnIndex));
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertWkbToFeature(cs.getObject(columnIndex));
    }

    private Map<String, Object> convertWkbToFeature(Object obj) {
        if (obj == null) return null;

        try {
            String hex = null;
            if (obj instanceof PGobject) {
                hex = ((PGobject) obj).getValue();
            }

            if (hex == null || hex.trim().isEmpty()) return null;

            byte[] wkb = hexStringToByteArray(hex);
            Geometry geometry = wkbReader.read(wkb);
            Map geometryMap = mapper.readValue(geoJsonWriter.write(geometry), Map.class);
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            feature.put("geometry", geometryMap);
            feature.put("properties", new HashMap<>());
            return feature;
        } catch (Exception e) {
            throw new RuntimeException("Failed conversion to GeoJSON", e);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

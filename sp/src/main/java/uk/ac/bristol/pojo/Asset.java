package uk.ac.bristol.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class Asset {
    private Integer id;
    private String name;
    private Map<String, Object> drainArea;
    private Integer assetHolderId;
    private Instant lastModified;

    public Asset() {
    }

    public Asset(Integer id) {
        this.id = id;
    }

    public Asset(String name) {
        this.name = name;
    }

    public Asset(Integer id, String name, Map<String, Object> drainArea, Integer assetHolderId) {
        this.id = id;
        this.name = name;
        this.drainArea = drainArea;
        this.assetHolderId = assetHolderId;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", drainArea=" + drainArea +
                ", assetHolderId=" + assetHolderId +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getDrainArea() {
        return drainArea;
    }

    public void setDrainArea(String geoJson) {
        if (geoJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.drainArea = mapper.readValue(geoJson, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getAssetHolderId() {
        return assetHolderId;
    }

    public void setAssetHolderId(Integer assetHolderId) {
        this.assetHolderId = assetHolderId;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}

package uk.ac.bristol.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class Asset {
    private Long id;
    private String name;
    private String region;
    private Map<String, Object> drainArea;
    private Long assetHolderId;
    private Instant lastModified;

    public Asset() {
    }

    public Asset(Long id) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // to be handled by Jackson to respond to front-end
    public Map<String, Object> getDrainArea() {
        return drainArea;
    }

    // convert to String for SQL, ignored when responding to front-end
    @JsonIgnore
    public String getDrainAreaAsJson() {
        try {
            return new ObjectMapper().writeValueAsString(drainArea);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDrainArea(String geoJson) throws JsonProcessingException {
        if (geoJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            this.drainArea = mapper.readValue(geoJson, new TypeReference<>() {
            });
        }
    }

    public Long getAssetHolderId() {
        return assetHolderId;
    }

    public void setAssetHolderId(Long assetHolderId) {
        this.assetHolderId = assetHolderId;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}

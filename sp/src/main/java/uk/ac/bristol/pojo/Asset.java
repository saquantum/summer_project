package uk.ac.bristol.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Asset {
    private String id;
    private String name;
    @JsonAlias("type_id")
    private String typeId;
    private AssetType type;
    @JsonAlias("owner_id")
    private String ownerId;
    private Map<String, Object> location;
    @JsonAlias("capacity_litres")
    private Long capacityLitres;
    private String material;
    private String status;
    @JsonAlias("installed_at")
    private LocalDate installedAt;
    @JsonAlias("last_inspection")
    private LocalDate lastInspection;
    private Instant lastModified;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String toString() {
        return "Asset{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", typeId='" + typeId + '\'' +
                ", type=" + type +
                ", ownerId='" + ownerId + '\'' +
                ", location=" + location +
                ", capacityLitres=" + capacityLitres +
                ", material='" + material + '\'' +
                ", status='" + status + '\'' +
                ", installedAt=" + installedAt +
                ", lastInspection=" + lastInspection +
                ", lastModified=" + lastModified +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // for front-end
    public Map<String, Object> getLocation() {
        return location;
    }

    // for front-end
    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    // for back-end persistence
    @JsonIgnore
    public String getLocationAsJson() {
        try {
            return objectMapper.writeValueAsString(location);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // for back-end persistence
    public void setLocationAsJson(String geoJson){
        try {
            if (geoJson == null || geoJson.isBlank()) {
                this.location = Map.ofEntries(Map.entry("type", "MultiPolygon"), Map.entry("coordinates", List.of(List.of(List.of()))));
                return;
            }
            this.location = objectMapper.readValue(geoJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getCapacityLitres() {
        return capacityLitres;
    }

    public void setCapacityLitres(Long capacityLitres) {
        this.capacityLitres = capacityLitres;
    }

    public String getMaterial() {
        return material;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDate installedAt) {
        this.installedAt = installedAt;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getLastInspection() {
        return lastInspection;
    }

    public void setLastInspection(LocalDate lastInspection) {
        this.lastInspection = lastInspection;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}

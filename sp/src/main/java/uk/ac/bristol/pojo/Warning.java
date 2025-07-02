package uk.ac.bristol.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Warning {
    private Long id;
    private String weatherType;
    private String warningLevel;
    private String warningHeadLine;
    private Instant validFrom;
    private Instant validTo;
    private String warningImpact;
    private String warningLikelihood;
    private String affectedAreas;
    private String whatToExpect;
    private String warningFurtherDetails;
    private String warningUpdateDescription;
    private Map<String, Object> area;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Warning() {
    }

    @Override
    public String toString() {
        return "Warning{" +
                "id=" + id +
                ", weatherType='" + weatherType + '\'' +
                ", warningLevel='" + warningLevel + '\'' +
                ", warningHeadLine='" + warningHeadLine + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", warningImpact='" + warningImpact + '\'' +
                ", warningLikelihood='" + warningLikelihood + '\'' +
                ", warningUpdateDescription='" + warningUpdateDescription + '\'' +
                ", area=" + area +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(String weatherType) {
        this.weatherType = weatherType;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }

    public String getWarningHeadLine() {
        return warningHeadLine;
    }

    public void setWarningHeadLine(String warningHeadLine) {
        this.warningHeadLine = warningHeadLine;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public String getWarningImpact() {
        return warningImpact;
    }

    public void setWarningImpact(String warningImpact) {
        this.warningImpact = warningImpact;
    }

    public String getWarningLikelihood() {
        return warningLikelihood;
    }

    public void setWarningLikelihood(String warningLikelihood) {
        this.warningLikelihood = warningLikelihood;
    }

    public String getAffectedAreas() {
        return affectedAreas;
    }

    public void setAffectedAreas(String affectedAreas) {
        this.affectedAreas = affectedAreas;
    }

    public String getWhatToExpect() {
        return whatToExpect;
    }

    public void setWhatToExpect(String whatToExpect) {
        this.whatToExpect = whatToExpect;
    }

    public String getWarningFurtherDetails() {
        return warningFurtherDetails;
    }

    public void setWarningFurtherDetails(String warningFurtherDetails) {
        this.warningFurtherDetails = warningFurtherDetails;
    }

    public String getWarningUpdateDescription() {
        return warningUpdateDescription;
    }

    public void setWarningUpdateDescription(String warningUpdateDescription) {
        this.warningUpdateDescription = warningUpdateDescription;
    }

    // for front-end
    public Map<String, Object> getArea() {
        return area;
    }

    // for front-end
    public void setArea(Map<String, Object> area) {
        this.area = area;
    }

    // for back-end persistence
    @JsonIgnore
    public String getAreaAsJson() {
        try {
            return new ObjectMapper().writeValueAsString(area);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // for back-end persistence
    public void setAreaAsJson(String geoJson) throws JsonProcessingException {
        if (geoJson == null || geoJson.isBlank()) {
            this.area = Map.ofEntries(Map.entry("type", "MultiPolygon"), Map.entry("coordinates", List.of(List.of(List.of()))));
            return;
        }
        this.area = objectMapper.readValue(geoJson, new TypeReference<>() {
        });
    }

    public static Warning getWarningFromGeoJSON(Map<String, Object> properties, Map<String, Object> geometry){
        Warning warning = new Warning();
        warning.setId(((Number) properties.get("OBJECTID")).longValue());
        warning.setWeatherType((String) properties.get("weathertype"));
        warning.setWarningLevel((String) properties.get("warninglevel"));
        warning.setWarningHeadLine((String) properties.get("warningheadline"));
        warning.setValidFrom(Instant.ofEpochMilli(((Number) properties.get("validfromdate")).longValue()));
        warning.setValidTo(Instant.ofEpochMilli(((Number) properties.get("validtodate")).longValue()));
        warning.setWarningImpact((String) properties.get("warningImpact"));
        warning.setWarningLikelihood((String) properties.get("warningLikelihood"));
        warning.setAffectedAreas((String) properties.get("affectedAreas"));
        warning.setWhatToExpect((String) properties.get("whatToExpect"));
        warning.setWarningFurtherDetails((String) properties.get("warningFurtherDetails"));
        warning.setWarningUpdateDescription((String) properties.get("warningUpdateDescription"));
        warning.setArea(geometry);

        return warning;
    }
}

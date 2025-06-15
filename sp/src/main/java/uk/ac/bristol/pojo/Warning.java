package uk.ac.bristol.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class Warning {
    private Integer id;
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
    private Map<String, Object> polygon;

    public Warning() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Map<String, Object> getPolygon() {
        return polygon;
    }

    public void setPolygon(String geoJson) {
        if (geoJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.polygon = mapper.readValue(geoJson, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

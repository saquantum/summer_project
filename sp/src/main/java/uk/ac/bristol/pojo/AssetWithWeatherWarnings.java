package uk.ac.bristol.pojo;

import java.util.List;

public class AssetWithWeatherWarnings {
    private Asset asset;
    private List<Warning> warnings;

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }
}

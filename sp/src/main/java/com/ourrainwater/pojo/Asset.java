package com.ourrainwater.pojo;

import java.util.Map;

public class Asset {
    private Integer id;
    private String name;
    private Map<String, Object> drainArea;
    private Integer assetHolderId;

    public Asset() {
    }

    public Asset(Integer id){
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

    public void setDrainArea(Map<String, Object> drainArea) {
        this.drainArea = drainArea;
    }

    public Integer getAssetHolderId() {
        return assetHolderId;
    }

    public void setAssetHolderId(Integer assetHolderId) {
        this.assetHolderId = assetHolderId;
    }
}

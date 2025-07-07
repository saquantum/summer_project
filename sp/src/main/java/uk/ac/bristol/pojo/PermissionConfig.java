package uk.ac.bristol.pojo;

public class PermissionConfig {
    private Long userId;
    private Boolean canCreateAsset;
    private Boolean canSetPolygonOnCreate;
    private Boolean canUpdateAssetFields;
    private Boolean canUpdatePolygon;
    private Boolean canDeleteOwnAsset;
    private Boolean canUpdateSelfProfile;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getCanCreateAsset() {
        return canCreateAsset;
    }

    public void setCanCreateAsset(Boolean canCreateAsset) {
        this.canCreateAsset = canCreateAsset;
    }

    public Boolean getCanSetPolygonOnCreate() {
        return canSetPolygonOnCreate;
    }

    public void setCanSetPolygonOnCreate(Boolean canSetPolygonOnCreate) {
        this.canSetPolygonOnCreate = canSetPolygonOnCreate;
    }

    public Boolean getCanUpdateAssetFields() {
        return canUpdateAssetFields;
    }

    public void setCanUpdateAssetFields(Boolean canUpdateAssetFields) {
        this.canUpdateAssetFields = canUpdateAssetFields;
    }

    public Boolean getCanUpdatePolygon() {
        return canUpdatePolygon;
    }

    public void setCanUpdatePolygon(Boolean canUpdatePolygon) {
        this.canUpdatePolygon = canUpdatePolygon;
    }

    public Boolean getCanDeleteOwnAsset() {
        return canDeleteOwnAsset;
    }

    public void setCanDeleteOwnAsset(Boolean canDeleteOwnAsset) {
        this.canDeleteOwnAsset = canDeleteOwnAsset;
    }

    public Boolean getCanUpdateSelfProfile() {
        return canUpdateSelfProfile;
    }

    public void setCanUpdateSelfProfile(Boolean canUpdateSelfProfile) {
        this.canUpdateSelfProfile = canUpdateSelfProfile;
    }
}

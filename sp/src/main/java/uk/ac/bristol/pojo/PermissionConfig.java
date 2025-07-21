package uk.ac.bristol.pojo;

public class PermissionConfig {
    private String userId;
    private Boolean canCreateAsset;
    private Boolean canSetPolygonOnCreate;
    private Boolean canUpdateAssetFields;
    private Boolean canUpdateAssetPolygon;
    private Boolean canDeleteAsset;
    private Boolean canUpdateProfile;

    public PermissionConfig() {
    }

    public PermissionConfig(String userId) {
        this.userId = userId;
    }

    public PermissionConfig(String userId, Boolean canCreateAsset, Boolean canSetPolygonOnCreate, Boolean canUpdateAssetFields, Boolean canUpdateAssetPolygon, Boolean canDeleteAsset, Boolean canUpdateProfile) {
        this.userId = userId;
        this.canCreateAsset = canCreateAsset;
        this.canSetPolygonOnCreate = canSetPolygonOnCreate;
        this.canUpdateAssetFields = canUpdateAssetFields;
        this.canUpdateAssetPolygon = canUpdateAssetPolygon;
        this.canDeleteAsset = canDeleteAsset;
        this.canUpdateProfile = canUpdateProfile;
    }

    @Override
    public String toString() {
        return "PermissionConfig{" +
                "userId='" + userId + '\'' +
                ", canCreateAsset=" + canCreateAsset +
                ", canSetPolygonOnCreate=" + canSetPolygonOnCreate +
                ", canUpdateAssetFields=" + canUpdateAssetFields +
                ", canUpdateAssetPolygon=" + canUpdateAssetPolygon +
                ", canDeleteAsset=" + canDeleteAsset +
                ", canUpdateProfile=" + canUpdateProfile +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public Boolean getCanUpdateAssetPolygon() {
        return canUpdateAssetPolygon;
    }

    public void setCanUpdateAssetPolygon(Boolean canUpdateAssetPolygon) {
        this.canUpdateAssetPolygon = canUpdateAssetPolygon;
    }

    public Boolean getCanDeleteAsset() {
        return canDeleteAsset;
    }

    public void setCanDeleteAsset(Boolean canDeleteAsset) {
        this.canDeleteAsset = canDeleteAsset;
    }

    public Boolean getCanUpdateProfile() {
        return canUpdateProfile;
    }

    public void setCanUpdateProfile(Boolean canUpdateProfile) {
        this.canUpdateProfile = canUpdateProfile;
    }
}

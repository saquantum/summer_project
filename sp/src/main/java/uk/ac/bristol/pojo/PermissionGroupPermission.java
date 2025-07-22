package uk.ac.bristol.pojo;

public class PermissionGroupPermission {
    private Long groupId;
    private Boolean canCreateAsset;
    private Boolean canSetPolygonOnCreate;
    private Boolean canUpdateAssetFields;
    private Boolean canUpdateAssetPolygon;
    private Boolean canDeleteAsset;
    private Boolean canUpdateProfile;

    public Long getGroupId() { return groupId; }

    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Boolean getCanCreateAsset() { return canCreateAsset; }

    public void setCanCreateAsset(Boolean canCreateAsset) { this.canCreateAsset = canCreateAsset; }

    public Boolean getCanSetPolygonOnCreate() { return canSetPolygonOnCreate; }

    public void setCanSetPolygonOnCreate(Boolean canSetPolygonOnCreate) { this.canSetPolygonOnCreate = canSetPolygonOnCreate; }

    public Boolean getCanUpdateAssetFields() { return canUpdateAssetFields; }

    public void setCanUpdateAssetFields(Boolean canUpdateAssetFields) { this.canUpdateAssetFields = canUpdateAssetFields; }

    public Boolean getCanUpdateAssetPolygon() { return canUpdateAssetPolygon; }

    public void setCanUpdateAssetPolygon(Boolean canUpdateAssetPolygon) { this.canUpdateAssetPolygon = canUpdateAssetPolygon; }

    public Boolean getCanDeleteAsset() { return canDeleteAsset; }

    public void setCanDeleteAsset(Boolean canDeleteAsset) { this.canDeleteAsset = canDeleteAsset; }

    public Boolean getCanUpdateProfile() { return canUpdateProfile; }

    public void setCanUpdateProfile(Boolean canUpdateProfile) { this.canUpdateProfile = canUpdateProfile; }
}

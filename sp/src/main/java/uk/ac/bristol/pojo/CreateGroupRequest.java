package uk.ac.bristol.pojo; // 和你现有 POJO 同一包，按实际包名调整

public class CreateGroupRequest {
    private String groupName;
    private String description;

    private Boolean canCreateAsset;
    private Boolean canSetPolygonOnCreate;
    private Boolean canUpdateAssetFields;
    private Boolean canUpdateAssetPolygon;
    private Boolean canDeleteAsset;
    private Boolean canUpdateProfile;

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

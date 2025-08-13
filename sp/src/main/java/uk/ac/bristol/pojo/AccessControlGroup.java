package uk.ac.bristol.pojo;

public class AccessControlGroup {
    private Long rowId;
    private String name;
    private String description;
    private Boolean canCreateAsset = false;
    private Boolean canSetPolygonOnCreate = false;
    private Boolean canUpdateAssetFields = false;
    private Boolean canUpdateAssetPolygon = false;
    private Boolean canDeleteAsset = false;
    private Boolean canUpdateProfile = false;

    private static final boolean canCreateAssetDefault = false;
    private static final boolean canSetPolygonOnCreateDefault = false;
    private static final boolean canUpdateAssetFieldsDefault = true;
    private static final boolean canUpdateAssetPolygonDefault = false;
    private static final boolean canDeleteAssetDefault = false;
    private static final boolean canUpdateProfileDefault = true;

    public AccessControlGroup() {
        this(
                "default", "default",
                canCreateAssetDefault,
                canSetPolygonOnCreateDefault,
                canUpdateAssetFieldsDefault,
                canUpdateAssetPolygonDefault,
                canDeleteAssetDefault,
                canUpdateProfileDefault
        );
    }

    public AccessControlGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public AccessControlGroup(String name, String description,
                              boolean canCreateAsset,
                              boolean canSetPolygonOnCreate,
                              boolean canUpdateAssetFields,
                              boolean canUpdateAssetPolygon,
                              boolean canDeleteAsset,
                              boolean canUpdateProfile) {
        this.name = name;
        this.description = description;
        this.canCreateAsset = canCreateAsset;
        this.canSetPolygonOnCreate = canSetPolygonOnCreate;
        this.canUpdateAssetFields = canUpdateAssetFields;
        this.canUpdateAssetPolygon = canUpdateAssetPolygon;
        this.canDeleteAsset = canDeleteAsset;
        this.canUpdateProfile = canUpdateProfile;
    }

    public static AccessControlGroup defaultGroup() {
        return new AccessControlGroup();
    }

    @Override
    public String toString() {
        return "AccessControlGroup{" +
                "canUpdateProfile=" + canUpdateProfile +
                ", canDeleteAsset=" + canDeleteAsset +
                ", canUpdateAssetPolygon=" + canUpdateAssetPolygon +
                ", canUpdateAssetFields=" + canUpdateAssetFields +
                ", canSetPolygonOnCreate=" + canSetPolygonOnCreate +
                ", canCreateAsset=" + canCreateAsset +
                ", name='" + name + '\'' +
                '}';
    }

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) name = "default";
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) description = "default";
        this.description = description;
    }

    public Boolean getCanCreateAsset() {
        return canCreateAsset;
    }

    public void setCanCreateAsset(Boolean canCreateAsset) {
        if (canCreateAsset == null) canCreateAsset = canCreateAssetDefault;
        this.canCreateAsset = canCreateAsset;
    }

    public Boolean getCanSetPolygonOnCreate() {
        return canSetPolygonOnCreate;
    }

    public void setCanSetPolygonOnCreate(Boolean canSetPolygonOnCreate) {
        if (canSetPolygonOnCreate == null) canSetPolygonOnCreate = canSetPolygonOnCreateDefault;
        this.canSetPolygonOnCreate = canSetPolygonOnCreate;
    }

    public Boolean getCanUpdateAssetFields() {
        return canUpdateAssetFields;
    }

    public void setCanUpdateAssetFields(Boolean canUpdateAssetFields) {
        if (canUpdateAssetFields == null) canUpdateAssetFields = canUpdateAssetFieldsDefault;
        this.canUpdateAssetFields = canUpdateAssetFields;
    }

    public Boolean getCanUpdateAssetPolygon() {
        return canUpdateAssetPolygon;
    }

    public void setCanUpdateAssetPolygon(Boolean canUpdateAssetPolygon) {
        if (canUpdateAssetPolygon == null) canUpdateAssetPolygon = canUpdateAssetPolygonDefault;
        this.canUpdateAssetPolygon = canUpdateAssetPolygon;
    }

    public Boolean getCanDeleteAsset() {
        return canDeleteAsset;
    }

    public void setCanDeleteAsset(Boolean canDeleteAsset) {
        if (canDeleteAsset == null) canDeleteAsset = canDeleteAssetDefault;
        this.canDeleteAsset = canDeleteAsset;
    }

    public Boolean getCanUpdateProfile() {
        return canUpdateProfile;
    }

    public void setCanUpdateProfile(Boolean canUpdateProfile) {
        if (canUpdateProfile == null) canUpdateProfile = canUpdateProfileDefault;
        this.canUpdateProfile = canUpdateProfile;
    }

    // a placeholder to silence reflection exception on AccessControlGroupAssociation
    // ------------- DO NOT DELETE this method!!!!!
    public void setUserRowId(Long rowId) {
    }
    // -------------
}

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

    public static AccessControlGroup systemShutdown;

    static {
        systemShutdown = new AccessControlGroup(
                "system shutdown", "system shutdown",
                true,
                true,
                true,
                true,
                true,
                true);
        systemShutdown.setRowId(-1L);
    }

    public AccessControlGroup() {
        initDefaultGroupConsideringShutdown();
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

    public AccessControlGroup(AccessControlGroup that) {
        this.rowId = that.getRowId();
        this.name = that.getName();
        this.description = that.getDescription();
        this.canCreateAsset = that.getCanCreateAsset() == null || that.getCanCreateAsset();
        this.canSetPolygonOnCreate = that.getCanSetPolygonOnCreate() == null || that.getCanSetPolygonOnCreate();
        this.canUpdateAssetFields = that.getCanUpdateAssetFields() == null || that.getCanUpdateAssetFields();
        this.canUpdateAssetPolygon = that.getCanUpdateAssetPolygon() == null || that.getCanUpdateAssetPolygon();
        this.canDeleteAsset = that.getCanDeleteAsset() == null || that.getCanDeleteAsset();
        this.canUpdateProfile = that.getCanUpdateProfile() == null || that.getCanUpdateProfile();
    }

    public AccessControlGroup(AccessControlGroupInterface that) {
        this.rowId = that.getRowId();
        this.name = that.getName();
        this.description = that.getDescription();
        this.canCreateAsset = that.getCanCreateAsset() == null || that.getCanCreateAsset();
        this.canSetPolygonOnCreate = that.getCanSetPolygonOnCreate() == null || that.getCanSetPolygonOnCreate();
        this.canUpdateAssetFields = that.getCanUpdateAssetFields() == null || that.getCanUpdateAssetFields();
        this.canUpdateAssetPolygon = that.getCanUpdateAssetPolygon() == null || that.getCanUpdateAssetPolygon();
        this.canDeleteAsset = that.getCanDeleteAsset() == null || that.getCanDeleteAsset();
        this.canUpdateProfile = that.getCanUpdateProfile() == null || that.getCanUpdateProfile();
    }

    public static AccessControlGroup defaultGroup() {
        return new AccessControlGroup(
                "default", "default",
                canCreateAssetDefault,
                canSetPolygonOnCreateDefault,
                canUpdateAssetFieldsDefault,
                canUpdateAssetPolygonDefault,
                canDeleteAssetDefault,
                canUpdateProfileDefault
        );
    }

    private void initDefaultGroupConsideringShutdown(){
        boolean hasOffSwitches = systemShutdown.hasOffSwitches();
        this.name = "default";
        this.description = hasOffSwitches ? "default settings affected by system shutdown" : "default";
        this.canCreateAsset = hasOffSwitches ? canCreateAssetDefault : canCreateAssetDefault && systemShutdown.getCanCreateAsset();
        this.canSetPolygonOnCreate = hasOffSwitches ? canSetPolygonOnCreateDefault : canSetPolygonOnCreateDefault && systemShutdown.getCanSetPolygonOnCreate();
        this.canUpdateAssetFields = hasOffSwitches ? canUpdateAssetFieldsDefault : canUpdateAssetFieldsDefault && systemShutdown.getCanUpdateAssetFields();
        this.canUpdateAssetPolygon = hasOffSwitches ? canUpdateAssetPolygonDefault : canUpdateAssetPolygonDefault && systemShutdown.getCanUpdateAssetPolygon();
        this.canDeleteAsset = hasOffSwitches ? canDeleteAssetDefault : canDeleteAssetDefault && systemShutdown.getCanDeleteAsset();
        this.canUpdateProfile = hasOffSwitches ? canUpdateProfileDefault : canUpdateProfileDefault && systemShutdown.getCanUpdateProfile();
    }

    public static AccessControlGroup formatSystemShutdown(AccessControlGroup group){
        group.setCanCreateAsset(group.getCanCreateAsset() && systemShutdown.getCanCreateAsset());
        group.setCanSetPolygonOnCreate(group.getCanSetPolygonOnCreate() && systemShutdown.getCanSetPolygonOnCreate());
        group.setCanUpdateAssetFields(group.getCanUpdateAssetFields() && systemShutdown.getCanUpdateAssetFields());
        group.setCanUpdateAssetPolygon(group.getCanUpdateAssetPolygon() && systemShutdown.getCanUpdateAssetPolygon());
        group.setCanDeleteAsset(group.getCanDeleteAsset() && systemShutdown.getCanDeleteAsset());
        group.setCanUpdateProfile(group.getCanUpdateProfile() && systemShutdown.getCanUpdateProfile());
        return group;
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

    public boolean hasOffSwitches() {
        return !this.canCreateAsset || !this.canSetPolygonOnCreate || !this.canUpdateAssetFields || !this.canUpdateAssetPolygon || !this.canDeleteAsset || !this.canUpdateProfile;
    }

    public boolean hasOnSwitches() {
        return this.canCreateAsset || this.canSetPolygonOnCreate || this.canUpdateAssetFields || this.canUpdateAssetPolygon || this.canDeleteAsset || this.canUpdateProfile;
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

    public static class AccessControlGroupInterface{
        public Long rowId;
        public String name;
        public String description;
        public Boolean canCreateAsset;
        public Boolean canSetPolygonOnCreate;
        public Boolean canUpdateAssetFields;
        public Boolean canUpdateAssetPolygon;
        public Boolean canDeleteAsset;
        public Boolean canUpdateProfile;

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
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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
}

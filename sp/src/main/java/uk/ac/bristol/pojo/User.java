package uk.ac.bristol.pojo;

public class User {
    private String id;
    private String password;
    private String assetHolderId;
    private AssetHolder assetHolder;
    private PermissionConfig permissionConfig;
    private boolean admin = false;
    private String token;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", assetHolderId='" + assetHolderId + '\'' +
                ", assetHolder=" + assetHolder +
                ", admin=" + admin +
                ", token='" + token + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAssetHolderId() {
        return assetHolderId;
    }

    public void setAssetHolderId(String assetHolderId) {
        this.assetHolderId = assetHolderId;
    }

    public AssetHolder getAssetHolder() {
        return assetHolder;
    }

    public void setAssetHolder(AssetHolder assetHolder) {
        this.assetHolder = assetHolder;
    }

    public PermissionConfig getPermissionConfig() {
        return permissionConfig;
    }

    public void setPermissionConfig(PermissionConfig permissionConfig) {
        this.permissionConfig = permissionConfig;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

package uk.ac.bristol.pojo;

public class User {
    private Integer id;
    private String username;
    private String password;
    private Integer assetHolderId;
    private boolean admin;
    private String token;

    public User() {
    }

    public User(Integer id, String username, String password, Integer assetHolderId, boolean admin, String token) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.assetHolderId = assetHolderId;
        this.admin = admin;
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", assetHolderId=" + assetHolderId +
                ", admin=" + admin +
                ", token='" + token + '\'' +
                '}';
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAssetHolderId() {
        return assetHolderId;
    }

    public void setAssetHolderId(Integer assetHolderId) {
        this.assetHolderId = assetHolderId;
    }
}

package uk.ac.bristol.pojo;

import java.util.List;

public class UserWithAssets {
    private User user;
    private List<Asset> assets;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }
}

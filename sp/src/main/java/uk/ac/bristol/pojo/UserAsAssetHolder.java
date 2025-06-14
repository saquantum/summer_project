package uk.ac.bristol.pojo;

public class UserAsAssetHolder {
    private User user;
    private AssetHolder assetHolder;
    private int assetCount;

    public UserAsAssetHolder() {
    }

    public UserAsAssetHolder(User user, AssetHolder assetHolder) {
        this.user = user;
        this.assetHolder = assetHolder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AssetHolder getAssetHolder() {
        return assetHolder;
    }

    public void setAssetHolder(AssetHolder assetHolder) {
        this.assetHolder = assetHolder;
    }

    public int getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(int assetCount) {
        this.assetCount = assetCount;
    }
}

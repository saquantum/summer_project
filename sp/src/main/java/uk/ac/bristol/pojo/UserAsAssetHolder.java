package uk.ac.bristol.pojo;

public class UserAsAssetHolder {
    private User user;
    private AssetHolder assetHolder;
    private long assetCount;

    public UserAsAssetHolder() {
    }

    public UserAsAssetHolder(User user, AssetHolder assetHolder) {
        this.user = user;
        this.assetHolder = assetHolder;
    }

    @Override
    public String toString() {
        return "UserAsAssetHolder{" +
                "user=" + user +
                ", assetHolder=" + assetHolder +
                ", assetCount=" + assetCount +
                '}';
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

    public long getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(long assetCount) {
        this.assetCount = assetCount;
    }
}

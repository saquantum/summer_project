package uk.ac.bristol.pojo;

public class UserWithAssetHolder {
    private User user;
    private AssetHolder assetHolder;
    private Double accumulation;

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

    public Double getAccumulation() {
        return accumulation;
    }

    public void setAccumulation(Double accumulation) {
        this.accumulation = accumulation;
    }
}

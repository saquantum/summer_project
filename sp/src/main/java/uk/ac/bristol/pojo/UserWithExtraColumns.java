package uk.ac.bristol.pojo;

public class UserWithExtraColumns {
    private User user;
    private Double accumulation;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getAccumulation() {
        return accumulation;
    }

    public void setAccumulation(Double accumulation) {
        this.accumulation = accumulation;
    }
}

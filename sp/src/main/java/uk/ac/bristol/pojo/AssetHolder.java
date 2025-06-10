package uk.ac.bristol.pojo;

public class AssetHolder {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String contactPreference;

    public AssetHolder() {
    }

    public AssetHolder(Integer id) {
        this.id = id;
    }

    public AssetHolder(String name) {
        this.name = name;
    }

    public AssetHolder(int id, String name, String email, String phone, String contactPreference) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.contactPreference = contactPreference;
    }

    @Override
    public String toString() {
        return "AssetHolder{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", contactPreference='" + contactPreference + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContactPreference() {
        return contactPreference;
    }

    public void setContactPreference(String contactPreference) {
        this.contactPreference = contactPreference;
    }
}

package uk.ac.bristol.pojo;

import java.time.Instant;

public class AssetHolder {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String contactPreference;
    private Instant lastModified;

    public AssetHolder() {
    }

    public AssetHolder(Integer id) {
        this.id = id;
    }

    public AssetHolder(String name) {
        this.name = name;
    }

    public AssetHolder(Integer id, String name, String email, String phone, String contactPreference) {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}

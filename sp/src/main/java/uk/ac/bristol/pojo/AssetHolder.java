package uk.ac.bristol.pojo;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AssetHolder {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String addressId;
    private Map<String, String> address;
    private String contactPreferencesId;
    private Map<String, Object> contactPreferences;
    private Instant lastModified;

    public static final Set<String> addressDetails = Set.of("assetHolderId", "street", "city", "postcode", "country");
    public static final Set<String> contactOptions = Set.of("assetHolderId", "email", "phone", "post", "whatsapp", "discord", "telegram");

    @Override
    public String toString() {
        return "AssetHolder{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", addressId='" + addressId + '\'' +
                ", address=" + address +
                ", contactPreferencesId='" + contactPreferencesId + '\'' +
                ", contactPreferences=" + contactPreferences +
                ", lastModified=" + lastModified +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        Map<String, String> map = new HashMap<>();
        for (String detail : addressDetails) {
            map.put(detail, address != null ? address.get(detail) : null);
        }
        this.address = map;
    }

    public Map<String, Object> getContactPreferences() {
        return contactPreferences;
    }

    @JsonSetter("contact_preferences")
    public void setContactPreferences(Map<String, Object> contactPreferences) {
        Map<String, Object> map = new HashMap<>();
        for (String option : contactOptions) {
            if(!option.equals("assetHolderId")
                    && contactPreferences != null
                    && contactPreferences.containsKey(option)
                    && contactPreferences.get(option).getClass() != Boolean.class) {
                throw new RuntimeException("options of contact preferences should only contain boolean values");
            }
            map.put(option, contactPreferences != null ? contactPreferences.get(option) : null);
        }
        this.contactPreferences = map;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getContactPreferencesId() {
        return contactPreferencesId;
    }

    public void setContactPreferencesId(String contactPreferencesId) {
        this.contactPreferencesId = contactPreferencesId;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}

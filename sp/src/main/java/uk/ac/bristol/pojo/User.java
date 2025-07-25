package uk.ac.bristol.pojo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class User {
    private String id;
    private String password;
    private String passwordPlainText;
    private boolean admin = false;
    private Integer adminLevel;
    private String avatar;
    private String name;
    private PermissionConfig permissionConfig;
    private Map<String, String> address;
    private Map<String, String> contactDetails;
    private Map<String, Boolean> contactPreferences;
    private Instant lastModified;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", passwordPlainText='" + passwordPlainText + '\'' +
                ", admin=" + admin +
                ", adminLevel=" + adminLevel +
                ", name='" + name + '\'' +
                ", address=" + address +
                ", contactDetails=" + contactDetails +
                ", contactPreferences=" + contactPreferences +
                ", lastModified=" + lastModified +
                '}';
    }

    public static final Set<String> addressDetails = Set.of("street", "city", "postcode", "country");
    public static final Set<String> contactOptions = Set.of("email", "phone", "post", "whatsapp", "discord", "telegram");


    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id != null) {
            this.id = id.trim();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = password.trim();
        }
    }

    public String getPasswordPlainText() {
        return passwordPlainText;
    }

    public void setPasswordPlainText(String passwordPlainText) {
        if (passwordPlainText != null) {
            this.passwordPlainText = passwordPlainText.trim();
        }
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Integer getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(Integer adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
    }

    public PermissionConfig getPermissionConfig() {
        return permissionConfig;
    }

    public void setPermissionConfig(PermissionConfig permissionConfig) {
        this.permissionConfig = permissionConfig;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        if (address == null) {
            throw new IllegalArgumentException("trying to set address to be null");
        }
        Map<String, String> map = new HashMap<>();
        for (String detail : addressDetails) {
            map.put(detail, address.getOrDefault(detail, ""));
        }
        this.address = map;
    }

    public void clearAddress() {
        this.address = null;
    }

    public Map<String, String> getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(Map<String, String> contactDetails) {
        if (contactDetails == null) {
            throw new IllegalArgumentException("trying to set contact details to be null");
        }
        Map<String, String> map = new HashMap<>();
        for (String option : contactOptions) {
            map.put(option, contactDetails.getOrDefault(option, ""));
        }
        this.contactDetails = map;
    }

    public void clearContactDetails() {
        this.contactDetails = null;
    }

    public Map<String, Boolean> getContactPreferences() {
        return contactPreferences;
    }

    public void setContactPreferences(Map<String, Boolean> contactPreferences) {
        if (contactPreferences == null) {
            throw new IllegalArgumentException("trying to set contact preferences to be null");
        }
        Map<String, Boolean> map = new HashMap<>();
        for (String option : contactOptions) {
            map.put(option, contactPreferences.getOrDefault(option, false));
        }
        this.contactPreferences = map;
    }

    public void clearContactPreferences() {
        this.contactPreferences = null;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}

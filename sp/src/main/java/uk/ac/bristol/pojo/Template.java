package uk.ac.bristol.pojo;

public class Template {

    private Long id;
    private String assetTypeId;
    private String warningType;
    private String severity;
    private String contactChannel;
    private String title;
    private String body;

    public Template() {
    }

    public Template(String assetTypeId, String warningType, String severity, String contactChannel) {
        this.assetTypeId = assetTypeId;
        this.warningType = warningType;
        this.severity = severity;
        this.contactChannel = contactChannel;
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", assetTypeId='" + assetTypeId + '\'' +
                ", warningType='" + warningType + '\'' +
                ", severity='" + severity + '\'' +
                ", contactChannel='" + contactChannel + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetTypeId() {
        return assetTypeId;
    }

    public void setAssetTypeId(String assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getContactChannel() {
        return contactChannel;
    }

    public void setContactChannel(String contactChannel) {
        this.contactChannel = contactChannel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

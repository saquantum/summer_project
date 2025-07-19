package uk.ac.bristol.pojo;

import java.sql.Timestamp;

public class PermissionGroup {
    private Long groupId;
    private String groupName;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Long getGroupId() { return groupId; }

    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

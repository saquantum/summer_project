package uk.ac.bristol.pojo;

import java.util.List;

public class UserGroupDTO {
    private String groupName;
    private List<User> users;

    public UserGroupDTO(String groupName, List<User> users) { this.groupName = groupName; this.users = users; }

    public String getGroupName() { return groupName; }

    public List<User> getUsers() { return users; }

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public void setUsers(List<User> users) { this.users = users; }
}
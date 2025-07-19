package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionGroup;
import java.util.List;

public interface PermissionGroupService {

    List<PermissionGroup> getAllGroups();

    PermissionGroup getGroupById(Long groupId);

    void addGroup(PermissionGroup group);

    void updateGroup(PermissionGroup group);

    void deleteGroup(Long groupId);
}
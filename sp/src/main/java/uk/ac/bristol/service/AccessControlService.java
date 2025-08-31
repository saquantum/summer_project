package uk.ac.bristol.service;

import uk.ac.bristol.pojo.AccessControlGroup;

import java.util.List;
import java.util.Map;

public interface AccessControlService {

    // assign user-group mappings

    Map<String, Integer> assignUsersToGroupByFilter(String groupName, Map<String, Object> filters);

    // groups

    List<AccessControlGroup> getAccessControlGroups(Map<String, Object> filters,
                                                    List<Map<String, String>> orderList,
                                                    Integer limit,
                                                    Integer offset);

    List<AccessControlGroup> getCursoredAccessControlGroups(Long lastGroupId,
                                                            Map<String, Object> filters,
                                                            List<Map<String, String>> orderList,
                                                            Integer limit,
                                                            Integer offset);

    List<AccessControlGroup> getAccessControlGroupByUserId(String uid);

    List<AccessControlGroup> getAccessControlGroupByGroupName(String groupName);

    int insertAccessControlGroup(AccessControlGroup accessControlGroup);

    int updateAccessControlGroup(AccessControlGroup accessControlGroup);

    int deleteAccessControlGroupByRowIds(Long[] rowIds);

    int deleteAccessControlGroupByRowIds(List<Long> rowIds);
}

package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AccessControlMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.AccessControlGroup;
import uk.ac.bristol.service.AccessControlService;
import uk.ac.bristol.util.QueryTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final AccessControlMapper accessControlMapper;
    private final MetaDataMapper metaDataMapper;

    public AccessControlServiceImpl(AccessControlMapper accessControlMapper, MetaDataMapper metaDataMapper) {
        this.accessControlMapper = accessControlMapper;
        this.metaDataMapper = metaDataMapper;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public Map<String, Integer> assignUsersToGroupByFilter(String groupName, Map<String, Object> filters) {
        if (groupName == null || groupName.isBlank()) {
            throw new SpExceptions.GetMethodException("The provided name of access control group is null or blank.");
        }
        List<AccessControlGroup> list = getAccessControlGroupByGroupName(groupName);
        if (list.isEmpty()) {
            throw new SpExceptions.GetMethodException("The provided access control group is undefined.");
        }
        if (list.size() > 1) {
            throw new SpExceptions.GetMethodException("The provided name leads to" + list.size() + "access control groups, which might indicates compromised database integrity.");
        }

        return accessControlMapper.upsertUserAccessControlGroupMapping(groupName, QueryTool.formatFilters(filters));
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AccessControlGroup> getAccessControlGroups(Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        return accessControlMapper.selectAccessControlGroups(
                QueryTool.formatFilters(filters),
                QueryTool.formatOrderList("access_control_group_row_id", orderList, "access_control_groups"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AccessControlGroup> getCursoredAccessControlGroups(Long lastGroupId, Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        Map<String, Object> anchor = null;
        if (lastGroupId != null) {
            List<Map<String, Object>> list = accessControlMapper.selectAccessControlGroupAnchor(lastGroupId);
            if (list.size() != 1) {
                throw new SpExceptions.GetMethodException("Found " + list.size() + " anchors using access control group id " + lastGroupId);
            }
            anchor = list.get(0);
        }
        List<Map<String, String>> formattedOrderList = QueryTool.formatOrderList("access_control_group_row_id", orderList, "access_control_groups");
        return accessControlMapper.selectAccessControlGroups(
                QueryTool.formatCursoredDeepPageFilters(filters, anchor, formattedOrderList),
                formattedOrderList,
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AccessControlGroup> getAccessControlGroupByUserId(String uid) {
        if (uid == null || uid.isBlank()) {
            return new ArrayList<>();
        }
        return accessControlMapper.selectAccessControlGroupsByUserId(uid);
    }

    @Override
    public List<AccessControlGroup> getAccessControlGroupByGroupName(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return new ArrayList<>();
        }
        return accessControlMapper.selectAccessControlGroups(
                QueryTool.formatFilters(Map.of("access_control_group_name", groupName.trim())),
                null, null, null);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertAccessControlGroup(AccessControlGroup accessControlGroup) {
        if(accessControlGroup != null){
            String name =  accessControlGroup.getName();
            if (name == null || !name.matches("^[A-Za-z0-9_]+$")) {
                throw new IllegalArgumentException("Name must contain only letters, numbers and underscores.");
            }
        }
        int n = accessControlMapper.insertAccessControlGroup(accessControlGroup);
        metaDataMapper.increaseTotalCountByTableName("access_control_groups", n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateAccessControlGroup(AccessControlGroup accessControlGroup) {
        if(accessControlGroup != null){
            String name =  accessControlGroup.getName();
            if (name == null || !name.matches("^[A-Za-z0-9_]+$")) {
                throw new IllegalArgumentException("Name must contain only letters, numbers and underscores.");
            }
        }
        return accessControlMapper.updateAccessControlGroup(accessControlGroup);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAccessControlGroupByRowIds(Long[] rowIds) {
        int n = accessControlMapper.deleteAccessControlGroupByRowIds(rowIds);
        metaDataMapper.increaseTotalCountByTableName("access_control_groups", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAccessControlGroupByRowIds(List<Long> rowIds) {
        int n = accessControlMapper.deleteAccessControlGroupByRowIds(rowIds);
        metaDataMapper.increaseTotalCountByTableName("access_control_groups", -n);
        return n;
    }

}


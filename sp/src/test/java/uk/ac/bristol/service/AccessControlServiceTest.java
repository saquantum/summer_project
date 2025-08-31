package uk.ac.bristol.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.AccessControlGroup;
import uk.ac.bristol.service.AccessControlService;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccessControlServiceTest {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private AccessControlService accessControlService;

    private String testGroup1Name = "test_group1_" + System.currentTimeMillis();
    private String testGroup2Name = "test_group2_" + System.currentTimeMillis();

    @BeforeAll
    public void init() throws IOException {
        mockDataInitializer.forceReload();
    }

    @Test
    @Transactional
    void testAssignUsersToGroupByFilter() {
        // Create a new group for testing to avoid duplicates
        AccessControlGroup group = new AccessControlGroup();
        group.setName(testGroup1Name);
        group.setDescription("Test Group 1");
        group.setCanCreateAsset(true);
        accessControlService.insertAccessControlGroup(group);

        Map<String, Object> filters = Map.of("user_id", "user_001"); // Assume user_001 exists

        Map<String, Integer> result = accessControlService.assignUsersToGroupByFilter(testGroup1Name, filters);
        long totalInserted = ((Number) result.get("inserted")).longValue() + ((Number) result.get("updated")).longValue();
        assertThat(totalInserted).isEqualTo(1L);

        List<AccessControlGroup> groupsForUser = accessControlService.getAccessControlGroupByUserId("user_001");
        assertThat(groupsForUser).anyMatch(g -> testGroup1Name.equals(g.getName()));

        // Test assigning again (update)
        Map<String, Integer> updateResult = accessControlService.assignUsersToGroupByFilter(testGroup1Name, filters);
        long totalUpdated = ((Number) updateResult.get("inserted")).longValue() + ((Number) updateResult.get("updated")).longValue();
        assertThat(totalUpdated).isEqualTo(1L);
        assertThat(((Number) updateResult.get("updated")).longValue()).isEqualTo(1L);

        // Test non-existent group
        assertThatThrownBy(() -> accessControlService.assignUsersToGroupByFilter("non_existent", filters))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("The provided access control group is undefined.");

        // Test blank group name
        assertThatThrownBy(() -> accessControlService.assignUsersToGroupByFilter("", filters))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("The provided name of access control group is null or blank.");
    }

    @Test
    @Transactional
    void testGetAccessControlGroups() {
        // Insert test group 1
        AccessControlGroup group1 = new AccessControlGroup();
        group1.setName(testGroup1Name);
        group1.setDescription("Test Group 1");
        group1.setCanCreateAsset(true);
        accessControlService.insertAccessControlGroup(group1);

        // Insert test group 2 to have multiple for ordering test
        AccessControlGroup group2 = new AccessControlGroup();
        group2.setName(testGroup2Name);
        group2.setDescription("Test Group 2");
        group2.setCanCreateAsset(false);
        accessControlService.insertAccessControlGroup(group2);

        List<AccessControlGroup> groups = accessControlService.getAccessControlGroups(null, null, null, null);
        assertThat(groups).hasSize(2);
        assertThat(groups).anyMatch(g -> testGroup1Name.equals(g.getName()));
        assertThat(groups).anyMatch(g -> testGroup2Name.equals(g.getName()));

        // Test filters
        Map<String, Object> filters = Map.of("access_control_can_create_asset", true);
        List<AccessControlGroup> filtered = accessControlService.getAccessControlGroups(filters, null, null, null);
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getCanCreateAsset()).isTrue();

        // Test ordering
        List<Map<String, String>> orderList = List.of(Map.of("column", "access_control_group_name", "order", "asc"));
        List<AccessControlGroup> ordered = accessControlService.getAccessControlGroups(null, orderList, null, null);
        assertThat(ordered).hasSize(2);
        assertThat(ordered.get(0).getName()).isLessThanOrEqualTo(ordered.get(1).getName());

        // Test pagination
        List<AccessControlGroup> paged = accessControlService.getAccessControlGroups(null, null, 1, 0);
        assertThat(paged).hasSize(1);
    }

    @Test
    @Transactional
    void testGetCursoredAccessControlGroups() {
        // Insert groups if needed
        AccessControlGroup group1 = new AccessControlGroup();
        group1.setName(testGroup1Name);
        group1.setDescription("Test Group 1");
        accessControlService.insertAccessControlGroup(group1);

        AccessControlGroup group2 = new AccessControlGroup();
        group2.setName(testGroup2Name);
        group2.setDescription("Test Group 2");
        accessControlService.insertAccessControlGroup(group2);

        List<AccessControlGroup> groups = accessControlService.getCursoredAccessControlGroups(null, null, null, 1, 0);
        assertThat(groups).hasSize(1);

        Long lastGroupId = groups.get(0).getRowId();
        List<AccessControlGroup> next = accessControlService.getCursoredAccessControlGroups(lastGroupId, null, null, 1, 0);
        assertThat(next).hasSize(1);
        assertThat(next.get(0).getRowId()).isGreaterThan(lastGroupId);

        // Test invalid lastGroupId
        assertThatThrownBy(() -> accessControlService.getCursoredAccessControlGroups(999999L, null, null, 1, 0))
                .isInstanceOf(SpExceptions.GetMethodException.class)
                .hasMessageContaining("Found 0 anchors");
    }

    @Test
    @Transactional
    void testGetAccessControlGroupByUserId() {
        // Create group and assign user
        AccessControlGroup group = new AccessControlGroup();
        group.setName(testGroup1Name);
        group.setDescription("Test Group 1");
        accessControlService.insertAccessControlGroup(group);

        Map<String, Object> filters = Map.of("user_id", "user_001");
        accessControlService.assignUsersToGroupByFilter(testGroup1Name, filters);

        List<AccessControlGroup> groups = accessControlService.getAccessControlGroupByUserId("user_001");
        assertThat(groups).anyMatch(g -> testGroup1Name.equals(g.getName()));

        // Test user with no groups (assume user_003 has none)
        List<AccessControlGroup> noGroups = accessControlService.getAccessControlGroupByUserId("user_003");
        assertThat(noGroups).isEmpty();

        // Test blank or null uid
        assertThat(accessControlService.getAccessControlGroupByUserId("")).isEmpty();
        assertThat(accessControlService.getAccessControlGroupByUserId(null)).isEmpty();
    }

    @Test
    @Transactional
    void testGetAccessControlGroupByGroupName() {
        AccessControlGroup group = new AccessControlGroup();
        group.setName(testGroup1Name);
        group.setDescription("Test Group 1");
        group.setCanCreateAsset(true);
        accessControlService.insertAccessControlGroup(group);

        List<AccessControlGroup> fetched = accessControlService.getAccessControlGroupByGroupName(testGroup1Name);
        assertThat(fetched).hasSize(1);
        assertThat(fetched.get(0).getName()).isEqualTo(testGroup1Name);
        assertThat(fetched.get(0).getDescription()).isEqualTo("Test Group 1");
        assertThat(fetched.get(0).getCanCreateAsset()).isTrue();

        // Test non-existent
        assertThat(accessControlService.getAccessControlGroupByGroupName("non_existent")).isEmpty();

        // Test blank
        assertThat(accessControlService.getAccessControlGroupByGroupName("")).isEmpty();
        assertThat(accessControlService.getAccessControlGroupByGroupName(null)).isEmpty();
    }

    @Test
    @Transactional
    void testInsertAccessControlGroup() {
        AccessControlGroup group = new AccessControlGroup();
        group.setName(testGroup1Name);
        group.setDescription("New group description");
        group.setCanCreateAsset(true);
        group.setCanSetPolygonOnCreate(false);
        group.setCanUpdateAssetFields(true);
        group.setCanUpdateAssetPolygon(false);
        group.setCanDeleteAsset(false);
        group.setCanUpdateProfile(true);

        int inserted = accessControlService.insertAccessControlGroup(group);
        assertThat(inserted).isEqualTo(1);

        List<AccessControlGroup> insertedGroup = accessControlService.getAccessControlGroupByGroupName(testGroup1Name);
        assertThat(insertedGroup).hasSize(1);
        assertThat(insertedGroup.get(0).getDescription()).isEqualTo("New group description");
        assertThat(insertedGroup.get(0).getCanCreateAsset()).isTrue();
        assertThat(insertedGroup.get(0).getCanSetPolygonOnCreate()).isFalse();

        // Test invalid name
        AccessControlGroup invalidName = new AccessControlGroup();
        invalidName.setName("invalid-name!");
        assertThatThrownBy(() -> accessControlService.insertAccessControlGroup(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must contain only letters, numbers and underscores.");
    }

    @Test
    @Transactional
    void testUpdateAccessControlGroup() {
        AccessControlGroup temp = new AccessControlGroup();
        temp.setName(testGroup1Name);
        temp.setDescription("Initial desc");
        temp.setCanCreateAsset(true);
        accessControlService.insertAccessControlGroup(temp);
        Long rowId = temp.getRowId();

        AccessControlGroup update = new AccessControlGroup();
        update.setRowId(rowId);
        update.setName("updated_" + testGroup1Name);
        update.setDescription("Updated desc");
        update.setCanCreateAsset(false);
        update.setCanDeleteAsset(true);

        int updated = accessControlService.updateAccessControlGroup(update);
        assertThat(updated).isEqualTo(1);

        List<AccessControlGroup> checked = accessControlService.getAccessControlGroupByGroupName("updated_" + testGroup1Name);
        assertThat(checked.get(0).getDescription()).isEqualTo("Updated desc");
        assertThat(checked.get(0).getCanCreateAsset()).isFalse();
        assertThat(checked.get(0).getCanDeleteAsset()).isTrue();

        // Test invalid name
        AccessControlGroup invalid = new AccessControlGroup();
        invalid.setRowId(rowId);
        invalid.setName("invalid!");
        assertThatThrownBy(() -> accessControlService.updateAccessControlGroup(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must contain only letters, numbers and underscores.");

        // Test updating without name
        AccessControlGroup noNameUpdate = new AccessControlGroup();
        noNameUpdate.setRowId(rowId);
        noNameUpdate.setDescription("No name change");
        int noNameUpdated = accessControlService.updateAccessControlGroup(noNameUpdate);
        assertThat(noNameUpdated).isEqualTo(1);
    }

    @Test
    @Transactional
    void testDeleteAccessControlGroupByRowIds() {
        AccessControlGroup temp = new AccessControlGroup();
        temp.setName(testGroup1Name);
        temp.setDescription("To delete");
        accessControlService.insertAccessControlGroup(temp);
        Long rowId = temp.getRowId();

        int deleted = accessControlService.deleteAccessControlGroupByRowIds(List.of(rowId));
        assertThat(deleted).isEqualTo(1);

        assertThat(accessControlService.getAccessControlGroupByGroupName(testGroup1Name)).isEmpty();

        // Test array version
        AccessControlGroup temp2 = new AccessControlGroup();
        temp2.setName(testGroup2Name);
        temp2.setDescription("To delete 2");
        accessControlService.insertAccessControlGroup(temp2);
        Long rowId2 = temp2.getRowId();

        int deletedArray = accessControlService.deleteAccessControlGroupByRowIds(new Long[]{rowId2});
        assertThat(deletedArray).isEqualTo(1);

        // Test deleting non-existent
        int zero = accessControlService.deleteAccessControlGroupByRowIds(List.of(999999L));
        assertThat(zero).isEqualTo(0);
    }
}
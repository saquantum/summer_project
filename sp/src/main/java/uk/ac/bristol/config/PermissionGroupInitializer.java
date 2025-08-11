package uk.ac.bristol.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.ac.bristol.pojo.CreateGroupRequest;
import uk.ac.bristol.pojo.PermissionGroup;
import uk.ac.bristol.service.PermissionGroupService;

import java.io.InputStream;
import java.util.List;

@Component
public class PermissionGroupInitializer implements CommandLineRunner {

    private final PermissionGroupService permissionGroupService;
    private final ObjectMapper objectMapper;

    public PermissionGroupInitializer(PermissionGroupService permissionGroupService, ObjectMapper objectMapper) {
        this.permissionGroupService = permissionGroupService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        try (InputStream is = getClass().getResourceAsStream("/data/permission.json")) {
            if (is == null) {
                System.out.println("permission.json not found in resources/data/");
                return;
            }

            List<CreateGroupRequest> groupRequests =
                    objectMapper.readValue(is, new TypeReference<List<CreateGroupRequest>>() {});

            for (CreateGroupRequest request : groupRequests) {
                PermissionGroup existingGroup = permissionGroupService.getGroupByName(request.getGroupName());
                if (existingGroup == null) {
                    permissionGroupService.createGroupWithPermissions(request);
                    System.out.println("Created group: " + request.getGroupName());
                } else {
                    System.out.println("Group already exists: " + request.getGroupName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
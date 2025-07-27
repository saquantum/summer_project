package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
@Service
public class ImportMockDataImpl implements ImportMockData {

    private final Settings settings;
    private final PermissionConfigService permissionConfigService;
    private final UserService userService;
    private final AssetService assetService;
    private final WarningService warningService;
    private final ContactService contactService;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public ImportMockDataImpl(Settings settings,
                              PermissionConfigService permissionConfigService,
                              UserService userService,
                              AssetService assetService,
                              WarningService warningService,
                              ContactService contactService) {
        this.settings = settings;
        this.permissionConfigService = permissionConfigService;
        this.userService = userService;
        this.assetService = assetService;
        this.warningService = warningService;
        this.contactService = contactService;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void resetSchema() {
        settings.resetSchema();
        settings.createTableMetaData();
        settings.createUsers("users");
        settings.createAddress("address");
        settings.createContactDetails("contact_details");
        settings.createContactPreferences("contact_preferences");
        settings.createAssetTypes("asset_types");
        settings.createAssets("assets");
        settings.createWeatherWarnings("weather_warnings");
        settings.createNotificationTemplates("templates");
        settings.createPermissionConfigs("permission_configs");
        settings.createUserInboxes("inboxes");
        settings.createPermissionGroups("permission_groups");
        settings.createGroupPermissions("group_permissions");
        settings.createGroupMembers("group_members");
        settings.createImageStorage("image_storage");
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void importUsers(InputStream usersInputStream) {
        List<Map<String, Object>> parsed = null;
        try {
            parsed = mapper.readValue(usersInputStream, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Loading Users failed.", e);
        }
        for (Map<String, Object> map : parsed) {
            User user = new User();
            user.setId((String) map.get("id"));
            user.setName((String) map.get("name"));
            user.setAddress((Map<String, String>) map.get("address"));
            user.setContactDetails(Map.of("email", (String) map.get("email"), "phone", (String) map.get("phone")));
            user.setContactPreferences((Map<String, Boolean>) map.get("contact_preferences"));
            user.setPassword("123456");
            user.setAdmin(false);
            user.setAvatar("https://cdn-icons-png.flaticon.com/512/149/149071.png");
            userService.insertUser(user);
            permissionConfigService.insertPermissionConfig(new PermissionConfig(user.getId()));
            // insert template welcome messages
            LocalDateTime now = LocalDateTime.now();
            contactService.insertInboxMessageToUser(Map.of(
                    "userId", user.getId(),
                    "hasRead", false,
                    "issuedDate", now,
                    "validUntil", now.plusYears(100),
                    "title", user.getName() + ", your account has been activated",
                    "message", "If you encounter any issues using our system, please feel free to contact us.")
            );
            contactService.insertInboxMessageToUser(Map.of(
                    "userId", user.getId(),
                    "hasRead", false,
                    "issuedDate", now.plus(Duration.ofMillis(1000L)),
                    "validUntil", now.plusYears(100),
                    "title", "Please check and confirm details of your assets",
                    "message", "The administrator should already have prepared the assets of yours which can be viewed from \"My Assets\" panel. Please do confirm they are accurate to receive correct notifications.")
            );
        }
        // register admin
        User admin = new User();
        admin.setId("admin");
        admin.setPassword("admin");
        admin.setAdmin(true);
        admin.setAdminLevel(1);
        userService.insertUser(admin);
        permissionConfigService.insertPermissionConfig(new PermissionConfig("admin"));

        User root = new User();
        root.setId("root");
        root.setPassword("root");
        root.setAdmin(true);
        root.setAdminLevel(0);
        userService.insertUser(root);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void importAssets(InputStream typesInputStream, InputStream assetsInputStream) {
        List<AssetType> types = null;
        List<Asset> assets = null;
        try {
            types = mapper.readValue(typesInputStream, new TypeReference<List<AssetType>>() {
            });
            assets = mapper.readValue(assetsInputStream, new TypeReference<List<Asset>>() {
            });
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Loading Assets failed.", e);
        }
        for (AssetType type : types) {
            assetService.insertAssetType(type);
        }
        for (Asset asset : assets) {
            assetService.insertAsset(asset);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void importWarnings(InputStream warningsInputStream) {
        try {
            List<Map<String, Object>> list = mapper.readValue(warningsInputStream, new TypeReference<List<Map<String, Object>>>() {
            });

            for (Map<String, Object> map : list) {
                List<Map<String, Object>> features = (List<Map<String, Object>>) map.get("features");
                if (features != null) {
                    for (Map<String, Object> feature : features) {
                        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");

                        Warning warning = Warning.getWarningFromGeoJSON(properties, geometry);

                        warningService.insertWarning(warning);
                    }
                }
            }
        } catch (Exception e) {
            throw new SpExceptions.SystemException("Loading Warnings failed.", e);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void importTemplates(InputStream notificationTemplatesInputStream) {
        try {
            List<Map<String, Object>> templates = mapper.readValue(notificationTemplatesInputStream, new TypeReference<List<Map<String, Object>>>() {
            });

            List<String> warningTypes = List.of("Rain", "Thunderstorm", "Wind", "Snow", "Lightning", "Ice", "Heat", "Fog");
            List<String> severities = List.of("YELLOW", "AMBER", "RED");
            List<String> assetTypeIds = List.of("type_001", "type_002", "type_003", "type_004", "type_005", "type_006", "type_007");
            List<String> channels = List.of("email", "phone", "post");

            Random r = new Random();
            for (String warningType : warningTypes) {
                for (String severity : severities) {
                    for (String assetTypeId : assetTypeIds) {
                        int idx = r.nextInt(templates.size());
                        for (String channel : channels) {
                            Template template = new Template();
                            template.setAssetTypeId(assetTypeId);
                            template.setWarningType(warningType);
                            template.setSeverity(severity);
                            template.setContactChannel(channel);
                            template.setTitle(warningType + templates.get(idx).get("title").toString());
                            template.setBody("<h3>Dear {{contact_name}}:</h3><p></p><img src=\"https://s3.bmp.ovh/imgs/2025/07/22/e8f6d4e43207e112.png\" loading=\"lazy\" style=\"display: block; height: auto; margin: 1.5rem 0; max-width: 100%; max-height: 100%;\"><p><br></p><p>Protect your asset. If it sent by SMS, keep it short. If it is sent by email, insert helpful images and links.</p><p><br><a rel=\"noopener noreferrer nofollow\" href=\" \" style=\"color: #409eff; text-decoration: underline; font-weight: bold;\"><strong> Click This Link</strong></a></p>");

                            if (!contactService.getNotificationTemplateByTypes(template).isEmpty()) {
                                contactService.updateNotificationTemplateMessageByTypes(template);
                            } else {
                                contactService.insertNotificationTemplate(template);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Loading Templates failed.", e);
        }
    }
}

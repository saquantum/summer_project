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

    @Override
    public void resetSchema() {
        settings.resetSchema();
        settings.createTableMetaData();
        settings.createAssetHolders("asset_holders");
        settings.createAddress("address");
        settings.createContactPreferences("contact_preferences");
        settings.createUsers("users");
        settings.createAssetTypes("asset_types");
        settings.createAssets("assets");
        settings.createWeatherWarnings("weather_warnings");
        settings.createNotificationTemplates("templates");
        settings.createPermissionConfigs("permission_configs");
        settings.createUserInboxes("inboxes");
    }

    @Override
    public void importUsers(InputStream usersInputStream) {
        List<AssetHolder> assetHolders = null;
        try {
            assetHolders = mapper.readValue(usersInputStream, new TypeReference<List<AssetHolder>>() {
            });
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Loading Users failed." + e.getMessage());
        }
        for (AssetHolder assetHolder : assetHolders) {
            User user = new User();
            user.setId(assetHolder.getId());
            user.setAssetHolder(assetHolder);
            user.setPassword("123456");
            user.setAdmin(false);
            userService.insertUser(user);
            permissionConfigService.insertPermissionConfig(new PermissionConfig(user.getId()));
        }
        // register admin
        User admin = new User();
        admin.setId("admin");
        admin.setPassword("admin");
        admin.setAdmin(true);
        userService.insertUser(admin);
        permissionConfigService.insertPermissionConfig(new PermissionConfig("admin"));
    }

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
            throw new SpExceptions.SystemException("Loading Assets failed." + e.getMessage());
        }
        for (AssetType type : types) {
            assetService.insertAssetType(type);
        }
        for (Asset asset : assets) {
            asset.setOwnerId("$" + asset.getOwnerId());
            assetService.insertAsset(asset);
        }
    }

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
            throw new SpExceptions.SystemException("Loading Warnings failed." + e.getMessage());
        }
    }

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
                            template.setBody(templates.get(idx).get("body").toString());

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
            throw new SpExceptions.SystemException("Loading Templates failed." + e.getMessage());
        }
    }
}

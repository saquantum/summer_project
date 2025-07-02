package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ImportMockData;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ImportMockDataImpl implements ImportMockData {

    private final Settings settings;
    private final UserService userService;
    private final AssetService assetService;
    private final WarningService warningService;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public ImportMockDataImpl(Settings settings, UserService userService, AssetService assetService, WarningService warningService) {
        this.settings = settings;
        this.userService = userService;
        this.assetService = assetService;
        this.warningService = warningService;
    }

    @Override
    public void resetSchema() {
        settings.resetSchema();
        settings.createAddress();
        settings.createContactPreferences();
        settings.createAssetHolders();
        settings.createUsers();
        settings.createAssetTypes();
        settings.createAssets();
        settings.createWeatherWarnings();
        settings.createNotificationTemplates();
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

            assetHolder.setId("$" + assetHolder.getId());

            assetHolder.setAddressId(assetHolder.getId());
            assetHolder.getAddress().put("assetHolderId", assetHolder.getAddressId());
            assetHolder.setContactPreferencesId(assetHolder.getId());
            assetHolder.getContactPreferences().put("assetHolderId", assetHolder.getContactPreferencesId());

            user.setAssetHolderId(assetHolder.getId());
            user.setAssetHolder(assetHolder);
            user.setPassword("123456");
            user.setAdmin(false);
            userService.insertUser(user);
        }
        // register admin
        User admin = new User();
        admin.setId("admin");
        admin.setPassword("admin");
        admin.setAdmin(true);
        userService.insertUser(admin);
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
    public void importWarnings(InputStream warningsInputStream, InputStream JSConverterInputStream) {
        try {
            List<Map<String, Object>> list = mapper.readValue(warningsInputStream, new TypeReference<List<Map<String, Object>>>() {
            });

            for (Map<String, Object> map : list) {
                List<Map<String, Object>> features = (List<Map<String, Object>>) map.get("features");
                if (features != null) {
                    for (Map<String, Object> feature : features) {
                        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");

                        Warning warning = new Warning();
                        warning.setId(((Number) properties.get("OBJECTID")).longValue());
                        warning.setWeatherType((String) properties.get("weathertype"));
                        warning.setWarningLevel((String) properties.get("warninglevel"));
                        warning.setWarningHeadLine((String) properties.get("warningheadline"));
                        warning.setValidFrom(Instant.ofEpochMilli(((Number) properties.get("validfromdate")).longValue()));
                        warning.setValidTo(Instant.ofEpochMilli(((Number) properties.get("validtodate")).longValue()));
                        warning.setWarningImpact((String) properties.get("warningImpact"));
                        warning.setWarningLikelihood((String) properties.get("warningLikelihood"));
                        warning.setAffectedAreas((String) properties.get("affectedAreas"));
                        warning.setWhatToExpect((String) properties.get("whatToExpect"));
                        warning.setWarningFurtherDetails((String) properties.get("warningFurtherDetails"));
                        warning.setWarningUpdateDescription((String) properties.get("warningUpdateDescription"));
                        warning.setArea(geometry);

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
            List<Map<String, Object>> templatesMapper = mapper.readValue(notificationTemplatesInputStream, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> map : templatesMapper) {
                String asset_type_id = (String) map.get("asset_type_id");
                String weather_warning_type = (String) map.get("weather_warning_type");
                String severity = (String) map.get("severity");
                String message = (String) map.get("message");

                Templates template = new Templates();
                template.setAssetType(asset_type_id);
                template.setWeatherType(weather_warning_type);
                template.setSeverity(severity);
                template.setMessage(message);

                warningService.insertNotificationTemplate(template);
            }
        } catch (IOException e) {
            throw new SpExceptions.SystemException("Loading Templates failed." + e.getMessage());
        }
    }
}

package uk.ac.bristol.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.ImportMockData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ImportMockDataImpl implements ImportMockData {

    private final Settings settings;
    private final AssetHolderMapper assetHolderMapper;
    private final UserMapper userMapper;

    public ImportMockDataImpl(Settings settings,  AssetHolderMapper assetHolderMapper, UserMapper userMapper) {
        this.settings = settings;
        this.assetHolderMapper = assetHolderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void resetAndImport() {
        this.resetSchema();
        this.importUsers();
        this.importAssets();
        this.importWarnings();
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
    }

    @Override
    public void importUsers() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        List<AssetHolder> assetHolders = null;
        try {
            assetHolders = mapper.readValue(new File("src/main/resources/data/users.json"), new TypeReference<List<AssetHolder>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (AssetHolder assetHolder : assetHolders) {
            assetHolder.setAddressId(assetHolder.getId());
            Map<String, String> address = assetHolder.getAddress();
            address.put("assetHolderId", assetHolder.getAddressId());
            assetHolderMapper.insertAddress(address);

            assetHolder.setContactPreferencesId(assetHolder.getId());
            Map<String, Object> contactPreferences = assetHolder.getContactPreferences();
            contactPreferences.put("assetHolderId", assetHolder.getContactPreferencesId());
            assetHolderMapper.insertContactPreferences(contactPreferences);

            assetHolderMapper.insertAssetHolder(assetHolder);

            User user = new User();
            user.setAssetHolderId(assetHolder.getId());
            user.setId(assetHolder.getId());
            user.setPassword("123456");
            user.setAdmin(false);

            userMapper.insertUser(user);
        }

        List<User> queryUser = userMapper.selectUserByAssetHolderId("user_001");
        User user = queryUser.get(0);
        List<AssetHolder> queryAssetHolder = assetHolderMapper.selectAssetHolderByID("user_001");
        AssetHolder ah =  queryAssetHolder.get(0);
        ah.setAddress(assetHolderMapper.selectAddressByAssetHolderId(ah.getId()).get(0));
        ah.setContactPreferences(assetHolderMapper.selectContactPreferencesByAssetHolderId(ah.getId()).get(0));
        user.setAssetHolder(ah);
        System.out.println(user);
    }

    @Override
    public void importAssets() {

    }

    @Override
    public void importWarnings() {

    }
}

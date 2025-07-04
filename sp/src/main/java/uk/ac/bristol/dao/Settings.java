package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface Settings {

    void resetSchema();

    void createAddress();

    void createContactPreferences();

    void createAssetHolders();

    void createUsers();

    void createAssetTypes();

    void createAssets();

    void createWeatherWarnings();

    void createNotificationTemplates();

    // 👉 新增总权限设定表
    void createGlobalPermissionConfig();

    // 👉 新增个别用戶权限设定表
    void createUserPermissionConfig();
}

package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface Settings {

    void resetSchema();

    void createTableMetaData();

    void createAddress(@Param("tableName") String tableName);

    void createContactPreferences(@Param("tableName") String tableName);

    void createAssetHolders(@Param("tableName") String tableName);

    void createUsers(@Param("tableName") String tableName);

    void createAssetTypes(@Param("tableName") String tableName);

    void createAssets(@Param("tableName") String tableName);

    void createWeatherWarnings(@Param("tableName") String tableName);

    void createNotificationTemplates(@Param("tableName") String tableName);

    void createPermissionConfigs(@Param("tableName") String tableName);

    void createUserInboxes(@Param("tableName") String tableName);
}

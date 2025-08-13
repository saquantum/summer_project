package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface Settings {

    void resetSchema();

    void createTableMetaData();

    void createAddress(@Param("tableName") String tableName);

    void createContactPreferences(@Param("tableName") String tableName);

    void createContactDetails(@Param("tableName") String tableName);

    void createUsers(@Param("tableName") String tableName);

    void createAssetTypes(@Param("tableName") String tableName);

    void createAssets(@Param("tableName") String tableName);

    void createWeatherWarnings(@Param("tableName") String tableName);

    void createUKRegions(@Param("tableName") String tableName);

    void createNotificationTemplates(@Param("tableName") String tableName);

    void createAccessControlGroups(@Param("tableName") String tableName);

    void createUserAccessControlGroupMapping(@Param("tableName") String tableName);

    void createUserInboxes(@Param("tableName") String tableName);
}

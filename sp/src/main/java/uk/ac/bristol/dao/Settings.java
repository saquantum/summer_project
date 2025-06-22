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
}

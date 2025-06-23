package uk.ac.bristol.service;

public interface ImportMockData {

    void resetSchema();

    void importAssets(String types, String assets);

    void importUsers(String filepath);

    void importWarnings(String filepath);
}

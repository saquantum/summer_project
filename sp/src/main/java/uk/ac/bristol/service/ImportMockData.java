package uk.ac.bristol.service;

public interface ImportMockData {

    void resetSchema();

    void importAssets();

    void importUsers();

    void importWarnings();
}

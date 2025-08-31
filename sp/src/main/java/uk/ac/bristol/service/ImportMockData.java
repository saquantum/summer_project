package uk.ac.bristol.service;

import java.io.InputStream;

public interface ImportMockData {

    void resetSchema();

    void importUsers(InputStream usersInputStream);

    void importAssets(InputStream typesInputStream, InputStream assetsInputStream);

    void importWarnings(InputStream warningsInputStream, InputStream UKMapInputStream);

    void importTemplates(InputStream notificationTemplatesInputStream);
}

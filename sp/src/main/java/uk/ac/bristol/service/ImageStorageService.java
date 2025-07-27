package uk.ac.bristol.service;

public interface ImageStorageService {

    void insertImage(String cid, String image);

    void deleteImage(String cid);

    String selectImage(String cid);
}

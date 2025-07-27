package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import uk.ac.bristol.dao.ImageStorageMapper;
import uk.ac.bristol.service.ImageStorageService;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    private final ImageStorageMapper imageStorageMapper;

    public ImageStorageServiceImpl(ImageStorageMapper imageStorageMapper) {
        this.imageStorageMapper = imageStorageMapper;
    }

    @Override
    public void insertImage(String cid, String image) {
        imageStorageMapper.insertImage(cid, image);
    }

    @Override
    public void deleteImage(String cid) {
        imageStorageMapper.deleteImage(cid);
    }

    @Override
    public String selectImage(String cid) {
        return imageStorageMapper.selectImage(cid);
    }
}

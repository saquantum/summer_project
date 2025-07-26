package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImageStorageMapper {

    void insertImage(@Param("cid") String cid, @Param("image") String image);

    void deleteImage(@Param("cid") String cid);

    String selectImage(@Param("cid") String cid);
}

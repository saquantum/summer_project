package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import uk.ac.bristol.pojo.PermissionConfig;

@Mapper
public interface PermissionConfigMapper {

    @Select("SELECT * FROM permission_config WHERE user_id = #{userId}")
    PermissionConfig findByUserId(Long userId);

    @Insert("INSERT INTO permission_config(user_id, can_create_asset, can_set_polygon_on_create, can_update_asset_fields, can_update_polygon, can_delete_own_asset, can_update_self_profile) VALUES (#{userId}, #{canCreateAsset}, #{canSetPolygonOnCreate}, #{canUpdateAssetFields}, #{canUpdatePolygon}, #{canDeleteOwnAsset}, #{canUpdateSelfProfile})")
    void insert(PermissionConfig permissionConfig);

    @Update("UPDATE permission_config SET can_create_asset = #{canCreateAsset}, can_set_polygon_on_create = #{canSetPolygonOnCreate}, can_update_asset_fields = #{canUpdateAssetFields}, can_update_polygon = #{canUpdatePolygon}, can_delete_own_asset = #{canDeleteOwnAsset}, can_update_self_profile = #{canUpdateSelfProfile} WHERE user_id = #{userId}")
    void update(PermissionConfig permissionConfig);
}

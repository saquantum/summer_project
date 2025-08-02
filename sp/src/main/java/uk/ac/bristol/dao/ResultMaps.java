package uk.ac.bristol.dao;

import java.util.Map;

public class ResultMaps {
    private ResultMaps() {
        throw new IllegalArgumentException("static class");
    }

    public static Map<String, String> address = Map.ofEntries(
            Map.entry("street", "address_street"),
            Map.entry("city", "address_city"),
            Map.entry("postcode", "address_postcode"),
            Map.entry("country", "address_country")
    );

    public static Map<String, String> contactPreferences = Map.ofEntries(
            Map.entry("email", "contact_preferences_email"),
            Map.entry("phone", "contact_preferences_phone"),
            Map.entry("post", "contact_preferences_post"),
            Map.entry("whatsapp", "contact_preferences_whatsapp"),
            Map.entry("discord", "contact_preferences_discord"),
            Map.entry("telegram", "contact_preferences_telegram")
    );

    public static Map<String, String> contactDetails = Map.ofEntries(
            Map.entry("email", "contact_details_email"),
            Map.entry("phone", "contact_details_phone"),
            Map.entry("post", "contact_details_post"),
            Map.entry("whatsapp", "contact_details_whatsapp"),
            Map.entry("discord", "contact_details_discord"),
            Map.entry("telegram", "contact_details_telegram")
    );

    public static Map<String, String> user = Map.ofEntries(
            Map.entry("id", "user_id"),
            Map.entry("rowId", "user_row_id"),
            Map.entry("admin", "user_is_admin"),
            Map.entry("adminLevel", "user_admin_level"),
            Map.entry("avatar", "user_avatar"),
            Map.entry("name", "user_name"),
            Map.entry("lastModified", "user_last_modified")
    );

    public static Map<String, String> asset = Map.ofEntries(
            Map.entry("id", "asset_id"),
            Map.entry("rowId", "asset_row_id"),
            Map.entry("name", "asset_name"),
            Map.entry("typeId", "asset_type_id"),
            Map.entry("ownerId", "asset_owner_id"),
            Map.entry("locationAsJson", "ST_AsGeoJSON(asset_location) as asset_location"),
            Map.entry("capacityLitres", "asset_capacity_litres"),
            Map.entry("material", "asset_material"),
            Map.entry("status", "asset_status"),
            Map.entry("installedAt", "asset_installed_at"),
            Map.entry("lastInspection", "asset_last_inspection"),
            Map.entry("lastModified", "asset_last_modified")
    );

    public static Map<String, String> assetType = Map.ofEntries(
            Map.entry("id", "asset_type_type_id"),
            Map.entry("rowId", "asset_type_row_id"),
            Map.entry("name", "asset_type_name"),
            Map.entry("description", "asset_type_description")
    );

    public static Map<String, String> warning = Map.ofEntries(
            Map.entry("id", "warning_id"),
            Map.entry("weatherType", "warning_weather_type"),
            Map.entry("warningLevel", "warning_level"),
            Map.entry("warningHeadLine", "warning_head_line"),
            Map.entry("validFrom", "warning_valid_from"),
            Map.entry("validTo", "warning_valid_to"),
            Map.entry("warningImpact", "warning_impact"),
            Map.entry("warningLikelihood", "warning_likelihood"),
            Map.entry("affectedAreas", "warning_affected_areas"),
            Map.entry("whatToExpect", "warning_what_to_expect"),
            Map.entry("warningFurtherDetails", "warning_further_details"),
            Map.entry("warningUpdateDescription", "warning_update_description"),
            Map.entry("areaAsJson", "ST_AsGeoJSON(warning_area) as warning_area")
    );

    public static Map<String, String> template = Map.ofEntries(
            Map.entry("id", "template_id"),
            Map.entry("assetTypeId", "template_asset_type_id"),
            Map.entry("warningType", "template_weather_warning_type"),
            Map.entry("severity", "template_severity"),
            Map.entry("contactChannel", "template_contact_channel"),
            Map.entry("title", "template_title"),
            Map.entry("body", "template_body")
    );

    public static Map<String, String> permissionConfig = Map.ofEntries(
            Map.entry("userId", "permission_config_user_id"),
            Map.entry("rowId", "permission_config_row_id"),
            Map.entry("canCreateAsset", "permission_config_can_create_asset"),
            Map.entry("canSetPolygonOnCreate", "permission_config_can_set_polygon_on_create"),
            Map.entry("canUpdateAssetFields", "permission_config_can_update_asset_fields"),
            Map.entry("canUpdateAssetPolygon", "permission_config_can_update_asset_polygon"),
            Map.entry("canDeleteAsset", "permission_config_can_delete_asset"),
            Map.entry("canUpdateProfile", "permission_config_can_update_profile")
    );

    public static Map<String, String> inboxMessage = Map.ofEntries(
            Map.entry("rowId", "inbox_row_id"),
            Map.entry("userId", "inbox_user_id"),
            Map.entry("hasRead", "inbox_has_read"),
            Map.entry("issuedDate", "inbox_issued_date"),
            Map.entry("validUntil", "inbox_valid_until"),
            Map.entry("title", "inbox_title"),
            Map.entry("message", "inbox_message")
    );

    public static Map<String, String> metaData = Map.ofEntries(
            Map.entry("tableName", "table_name"),
            Map.entry("totalCount", "total_count")
    );

    public static Map<String, Map<String, String>> tableFieldMappings = Map.ofEntries(
            Map.entry("address", address),
            Map.entry("contact_preferences", contactPreferences),
            Map.entry("contact_details", contactDetails),
            Map.entry("users", user),
            Map.entry("assets", asset),
            Map.entry("asset_types", assetType),
            Map.entry("weather_warnings", warning),
            Map.entry("templates", template),
            Map.entry("permission_configs", permissionConfig),
            Map.entry("inboxes", inboxMessage),
            Map.entry("table_meta_data", metaData)
    );
}

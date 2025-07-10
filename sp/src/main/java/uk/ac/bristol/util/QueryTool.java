package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.service.MetaDataService;
import uk.ac.bristol.service.PermissionConfigService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public final class QueryTool {

    private QueryTool() {
        throw new IllegalStateException("Utility class");
    }

    private static String formatFilterValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values are not allowed in filters");
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        }
        return value.toString();
    }

    public static String formatFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<String> result  = new ArrayList<>();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String column = entry.getKey();
            Object condition = entry.getValue();

            if(!(condition instanceof Map)) {
                result.add(column + "=" + formatFilterValue(condition));
            }

            Map<String, Object> conditionMap = (Map<String, Object>) condition;

            if(!conditionMap.containsKey("op")) {
                throw new IllegalArgumentException("Filter conditions must contain operator");
            }
            String operator = (String) conditionMap.get("op");

            if("like".equalsIgnoreCase(operator)) {
                if(!conditionMap.containsKey("val")) {
                    throw new IllegalArgumentException("Like conditions must have a value");
                }
                if(conditionMap.get("val") == null) {
                    throw new IllegalArgumentException("Value of like conditions must not be null");
                }
                result.add(column + " like " + "'" + formatFilterValue(conditionMap.get("val")) + "'");
            }

            if("range".equalsIgnoreCase(operator)) {
                Object min = conditionMap.get("min");
                Object max = conditionMap.get("max");

                if (min != null && max != null) {
                    result.add(column + " >= " + formatFilterValue(min) +
                            " and " + column + " <= " + formatFilterValue(max));
                }
                else if (min != null) {
                    result.add(column + " >= " + formatFilterValue(min));
                }
                else if (max != null) {
                    result.add(column + " <= " + formatFilterValue(max));
                }else{
                    throw new IllegalArgumentException("Range conditions must have a min or a max value");
                }
            }
        }

        return String.join(" and ", result);
    }

    public static List<Map<String, String>> getOrderList(List<String> items) {
        if (items == null || items.isEmpty()) return null;
        return getOrderList(items.toArray(String[]::new));
    }

    public static List<Map<String, String>> getOrderList(String... items) {
        if (items == null || items.length == 0) return null;
        if (items.length % 2 != 0) throw new IllegalArgumentException("getOrderList: items length must be even");
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < items.length; i += 2) {
            Map<String, String> map = new HashMap<>();
            map.put("column", items[i]);
            map.put("direction", items[i + 1]);
            list.add(map);
        }
        return list;
    }

    public static boolean userIdentityVerification(HttpServletResponse response, HttpServletRequest request, String uid, String aid) {
        if ((uid == null || uid.isEmpty()) && (aid == null || aid.isEmpty())) {
            return false;
        }
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request));
        } catch (IOException e) {
            return false;
        }
        Boolean isAdmin = (Boolean) claims.get("isAdmin");
        if (isAdmin == null) {
            return false;
        }
        String id = (String) claims.get("id");
        String assetHolderId = (String) claims.get("assetHolderId");

        if (id != null && uid != null && id.equals(uid)) {
            return true;
        }

        if (assetHolderId != null && aid != null && assetHolderId.equals(aid)) {
            return true;
        }

        return false;
    }

    private final static List<String> registeredColumnPrefixes = List.of(
            "asset_holder",
            "asset_type",
            "asset",
            "user",
            "address",
            "contact_preferences",
            "warning",
            "template");

    private final static Set<String> registeredTables = Set.of("table_meta_data",
            "asset_holders",
            "asset_types",
            "assets",
            "users",
            "address",
            "contact_preferences",
            "weather_warnings",
            "templates");

    // TODO: user meta data mapper -> filterRegisteredColumnsInTables
    public static List<Map<String, String>> filterOrderList(List<Map<String, String>> originalList, String... prefixes) {
        if (originalList == null || originalList.isEmpty() || prefixes == null || prefixes.length == 0) return null;

        Set<String> prefixesSet = new HashSet<>(Arrays.asList(prefixes));

        List<Map<String, String>> list = new ArrayList<>();
        for (Map<String, String> item : originalList) {
            String column = item.get("column");
            boolean matched = false;
            String matchedPrefix = null;
            // 1. match registered prefixes by sequence, get first matched prefix
            for (String prefix : registeredColumnPrefixes) {
                if (column.startsWith(prefix)) {
                    matched = true;
                    matchedPrefix = prefix;
                    break;
                }
            }
            // 2. add to result if the first matched prefix is in parameter @prefixes
            if (matched) {
                if (prefixesSet.contains(matchedPrefix)) {
                    list.add(item);
                }
            }
            // 3. if the column does not match registered prefixes but is in parameter @prefixes, keep it
            else {
                for (String prefix : prefixes) {
                    if (column.startsWith(prefix)) {
                        list.add(item);
                        break;
                    }
                }
            }
        }
        return list;
    }

    public static PermissionConfig getUserPermissions(String uid, String aid) {
        List<PermissionConfig> adminConfig = QueryToolConfig.permissionConfigService.getPermissionConfigByUserId("admin");
        if (adminConfig.size() != 1) {
            throw new SpExceptions.SystemException("Found " + adminConfig.size() + " global permission configs");
        }

        List<PermissionConfig> list;
        PermissionConfig config;
        if (uid != null) {
            list = QueryToolConfig.permissionConfigService.getPermissionConfigByUserId(uid);
        } else if (aid != null) {
            list = QueryToolConfig.permissionConfigService.getPermissionConfigByAssetHolderId(aid);
        } else {
            throw new SpExceptions.BusinessException("No valid uid or aid is received");
        }
        if (list.size() != 1) {
            throw new SpExceptions.SystemException("Found " + list.size() + " permission configs for user " + uid);
        }
        config = list.get(0);
        return new PermissionConfig(null,
                adminConfig.get(0).getCanCreateAsset() && config.getCanCreateAsset(),
                adminConfig.get(0).getCanSetPolygonOnCreate() && config.getCanSetPolygonOnCreate(),
                adminConfig.get(0).getCanUpdateAssetFields() && config.getCanUpdateAssetFields(),
                adminConfig.get(0).getCanUpdateAssetPolygon() && config.getCanUpdateAssetPolygon(),
                adminConfig.get(0).getCanDeleteAsset() && config.getCanDeleteAsset(),
                adminConfig.get(0).getCanUpdateProfile() && config.getCanUpdateProfile()
        );
    }
}

@Component
class QueryToolConfig {
    @Autowired
    public MetaDataService metaDataService0;
    @Autowired
    public PermissionConfigService permissionConfigService0;

    public static MetaDataService metaDataService;
    public static PermissionConfigService permissionConfigService;

    @PostConstruct
    public void init() {
        metaDataService = this.metaDataService0;
        permissionConfigService = this.permissionConfigService0;
    }
}

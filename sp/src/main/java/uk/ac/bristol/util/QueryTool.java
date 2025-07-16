package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.MetaDataService;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.service.UserService;

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
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value.toString() + "'";
    }

    public static String formatFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String column = entry.getKey();
            Object condition = entry.getValue();

            if (!(condition instanceof Map)) {
                result.add(column + " = " + formatFilterValue(condition));
                continue;
            }

            Map<String, Object> conditionMap = (Map<String, Object>) condition;

            if (!conditionMap.containsKey("op")) {
                throw new IllegalArgumentException("Filter conditions must contain operator");
            }
            String operator = (String) conditionMap.get("op");

            if ("like".equalsIgnoreCase(operator)) {
                if (!conditionMap.containsKey("val")) {
                    throw new IllegalArgumentException("Like conditions must have a value");
                }
                if (conditionMap.get("val") == null) {
                    throw new IllegalArgumentException("Value of like conditions must not be null");
                }
                result.add("lower(" + column + ") like lower(" + formatFilterValue(conditionMap.get("val")) + ")");
            } else if ("range".equalsIgnoreCase(operator)) {
                Object min = conditionMap.get("min");
                Object max = conditionMap.get("max");

                if (min != null && max != null) {
                    result.add(column + " >= " + formatFilterValue(min) +
                            " and " + column + " <= " + formatFilterValue(max));
                } else if (min != null) {
                    result.add(column + " >= " + formatFilterValue(min));
                } else if (max != null) {
                    result.add(column + " <= " + formatFilterValue(max));
                } else {
                    throw new IllegalArgumentException("Range conditions must have a min or a max value");
                }
            } else if ("in".equalsIgnoreCase(operator)) {
                if (!conditionMap.containsKey("list") || conditionMap.get("list") == null) {
                    throw new IllegalArgumentException("In conditions must have a list of values");
                }
                List<Object> list = (List<Object>) conditionMap.get("list");
                List<String> values = new ArrayList<>();
                for (Object o : list) {
                    values.add(formatFilterValue(o));
                }
                result.add(column + " in (" + String.join(",", values) + ")");
            } else if ("notNull".equalsIgnoreCase(operator)) {
                result.add(column + " is not null");
            } else if ("isNull".equalsIgnoreCase(operator)) {
                result.add(column + " is null");
            } else {
                throw new IllegalArgumentException("Only 'like', 'range' and 'in' are supported operators");
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
        if (isAdmin) {
            return true;
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

    public static boolean verifyAssetOwnership(String assetId, String uid, String aid) {
        if (assetId == null) {
            return false;
        }
        if (uid == null && aid == null) {
            return false;
        }
        List<Asset> asset = QueryToolConfig.assetService.getAssetById(assetId);
        if (asset.size() != 1) return false;
        if (uid != null) {
            User user = QueryToolConfig.userService.getUserByUserId(uid);
            if (user.getAssetHolder() == null) return false;
            if (!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        if (aid != null) {
            User user = QueryToolConfig.userService.getUserByAssetHolderId(aid);
            if (user.getAssetHolder() == null) return false;
            if (!Objects.equals(user.getAssetHolder().getId(), asset.get(0).getOwnerId())) return false;
        }
        return true;
    }

    private final static Set<String> registeredTables = QueryToolConfig.metaDataService.getAllRegisteredTableNames();

    public static List<Map<String, String>> filterOrderList(List<Map<String, String>> originalList, String... tablesAndColumns) {
        if (originalList == null || originalList.isEmpty() || tablesAndColumns == null || tablesAndColumns.length == 0) {
            return null;
        }

        // 1. separate tables and permitted columns from input
        Set<String> tables = new HashSet<>();
        Set<String> columns = new HashSet<>();
        for (String s : tablesAndColumns) {
            if (registeredTables.contains(s)) {
                tables.add(s);
            } else {
                columns.add(s);
            }
        }

        // 2. get columns that belong to given tables
        Set<String> registeredColumns = QueryToolConfig.metaDataService.filterRegisteredColumnsInTables(
                new ArrayList<>(tables),
                originalList.stream().map(item -> item.get("column")).toList());

        // 3. filter columns using two filters
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> item : originalList) {
            String column = item.get("column");
            if (registeredColumns.contains(column) || columns.contains(column)) {
                result.add(item);
            }
        }

        return result;
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
        return new PermissionConfig(uid,
                adminConfig.get(0).getCanCreateAsset() && config.getCanCreateAsset(),
                adminConfig.get(0).getCanSetPolygonOnCreate() && config.getCanSetPolygonOnCreate(),
                adminConfig.get(0).getCanUpdateAssetFields() && config.getCanUpdateAssetFields(),
                adminConfig.get(0).getCanUpdateAssetPolygon() && config.getCanUpdateAssetPolygon(),
                adminConfig.get(0).getCanDeleteAsset() && config.getCanDeleteAsset(),
                adminConfig.get(0).getCanUpdateProfile() && config.getCanUpdateProfile()
        );
    }

    public static String formatPaginationLimit(FilterDTO filter) {
        if (filter != null && filter.hasLimit()) {
            if (filter.getLimit() > Code.PAGINATION_MAX_LIMIT) {
                filter.setLimit(Code.PAGINATION_MAX_LIMIT);
                return "The pagination limit you provided is too large and has been replaced by" + Code.PAGINATION_MAX_LIMIT;
            } else {
                return null;
            }
        }
        return null;
    }
}

@Component
class QueryToolConfig {
    @Autowired
    public MetaDataService metaDataService0;
    @Autowired
    public PermissionConfigService permissionConfigService0;
    @Autowired
    public AssetService assetService0;
    @Autowired
    public UserService userService0;

    public static MetaDataService metaDataService;
    public static PermissionConfigService permissionConfigService;
    public static AssetService assetService;
    public static UserService userService;

    @PostConstruct
    public void init() {
        metaDataService = this.metaDataService0;
        permissionConfigService = this.permissionConfigService0;
        assetService = this.assetService0;
        userService = this.userService0;
    }
}

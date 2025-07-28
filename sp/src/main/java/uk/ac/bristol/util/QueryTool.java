package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
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

    public static List<FilterItemDTO> formatFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>();
        }

        List<FilterItemDTO> filterList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String column = entry.getKey();
            Object condition = entry.getValue();

            if (!(condition instanceof Map)) {
                filterList.add(FilterItemDTO.eq(column, condition));
                continue;
            }

            Map<String, Object> conditionMap;
            try {
                conditionMap = (Map<String, Object>) condition;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Invalid condition expression");
            }

            if (!conditionMap.containsKey("op")) {
                throw new IllegalArgumentException("Filter conditions must contain operator");
            }
            String operator = (String) conditionMap.get("op");

            if ("like".equalsIgnoreCase(operator)) {
                Object val = conditionMap.get("val");
                if (val == null) {
                    throw new IllegalArgumentException("Value of like conditions must not be null");
                }
                filterList.add(FilterItemDTO.like(column, val));
            } else if ("range".equalsIgnoreCase(operator)) {
                Object min = conditionMap.get("min");
                Object max = conditionMap.get("max");
                if (min == null && max == null) {
                    throw new IllegalArgumentException("Range conditions must have a min or a max value");
                }
                filterList.add(FilterItemDTO.range(column, min, max));
            } else if ("in".equalsIgnoreCase(operator)) {
                List<Object> list;
                try {
                    list = (List<Object>) conditionMap.get("list");
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("In conditions must have a correctly formatted list of values");
                }
                if (list == null || list.isEmpty()) {
                    throw new IllegalArgumentException("In conditions must have a list of values");
                }
                filterList.add(FilterItemDTO.in(column, list));
            } else if ("notNull".equalsIgnoreCase(operator)) {
                filterList.add(FilterItemDTO.isNull(column));
            } else if ("isNull".equalsIgnoreCase(operator)) {
                filterList.add(FilterItemDTO.notNull(column));
            } else {
                throw new IllegalArgumentException("Only 'like', 'range', 'in', 'notNull' and 'isNull' are currently supported operators");
            }
        }
        return filterList;
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

    public static boolean userIdentityVerification(HttpServletResponse response, HttpServletRequest request, String uid) {
        if (uid == null || uid.isEmpty()) {
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
        return id != null && id.equals(uid);
    }

    public static boolean verifyAssetOwnership(String assetId, String uid) {
        if (assetId == null || assetId.isBlank() || uid == null || uid.isBlank()) {
            return false;
        }
        Asset asset = QueryToolConfig.assetService.getAssetById(assetId);
        User user = QueryToolConfig.userService.getUserByUserId(uid);
        if (user.isAdmin()) {
            return false;
        }
        return Objects.equals(user.getId(), asset.getOwnerId());
    }

    // if any table are inserted or deleted during runtime, should consider modify this setup
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

    public static PermissionConfig getUserPermissions(String uid) {
        if (uid == null) {
            throw new SpExceptions.BusinessException("No valid uid is provided");
        }

        PermissionConfig adminConfig = QueryToolConfig.permissionConfigService.getPermissionConfigByUserId("admin");
        PermissionConfig config = QueryToolConfig.permissionConfigService.getPermissionConfigByUserId(uid);
        return new PermissionConfig(uid,
                adminConfig.getCanCreateAsset() && config.getCanCreateAsset(),
                adminConfig.getCanSetPolygonOnCreate() && config.getCanSetPolygonOnCreate(),
                adminConfig.getCanUpdateAssetFields() && config.getCanUpdateAssetFields(),
                adminConfig.getCanUpdateAssetPolygon() && config.getCanUpdateAssetPolygon(),
                adminConfig.getCanDeleteAsset() && config.getCanDeleteAsset(),
                adminConfig.getCanUpdateProfile() && config.getCanUpdateProfile()
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

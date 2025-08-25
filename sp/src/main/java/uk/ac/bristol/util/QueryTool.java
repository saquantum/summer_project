package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.AccessControlService;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.MetaDataService;
import uk.ac.bristol.service.UserService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class QueryTool {

    private QueryTool() {
        throw new IllegalStateException("Utility class");
    }

    // used in filters. blacklisted columns are not allowed in filters
    public final static Set<String> registeredColumns;

    // used in order list.
    public final static Map<String, Set<String>> registeredTableColumnMap;
    public final static Map<String, ColumnTriple> columnAsKeyMap = new HashMap<>();

    // initialisation of cached metadata
    static {
        Set<String> columnBlacklist = Set.of(
                "user_password",
                "user_password_plaintext"
        );

        registeredColumns = QueryToolConfig.metaDataService.getAllRegisteredColumnNamesWithBlacklist(
                List.copyOf(columnBlacklist)
        );
        List<ColumnTriple> list = QueryToolConfig.metaDataService.getAllTableColumnMapsWithBlacklist(
                columnBlacklist
        );
        registeredTableColumnMap = list.stream()
                .collect(Collectors.groupingBy(
                        ColumnTriple::getTableName,
                        Collectors.mapping(ColumnTriple::getColumnName, Collectors.toSet())
                ));
        for (ColumnTriple item : list) {
            columnAsKeyMap.putIfAbsent(item.getColumnName(), item);
        }
    }

    public static List<FilterItemDTO> formatFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>();
        }

        // check columns from the filter are valid
        filters.forEach((key, value) -> {
            if (!registeredColumns.contains(key)) {
                throw new IllegalArgumentException("Column Not Found");
            }
        });

        List<FilterItemDTO> filterList = new ArrayList<>();

        // loop through all key-value pairs
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String column = entry.getKey();
            Object condition = entry.getValue();

            // eq condition
            if (!(condition instanceof Map)) {
                filterList.add(FilterItemDTO.eq(column, condition));
                continue;
            }

            // other mapped conditions
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
                filterList.add(FilterItemDTO.notNull(column));
            } else if ("isNull".equalsIgnoreCase(operator)) {
                filterList.add(FilterItemDTO.isNull(column));
            } else {
                throw new IllegalArgumentException("Only 'like', 'range', 'in', 'notNull' and 'isNull' are currently supported operators");
            }
        }
        return filterList;
    }

    // convert values to safe strings to avoid SQL injection
    private static String wrapValue(Object val) {
        if (val == null) {
            throw new IllegalArgumentException("To wrap a value it must not be null");
        }
        if (val instanceof String) {
            return "'" + val.toString().replace("'", "''") + "'";
        }
        if (val instanceof java.sql.Date) {
            return "'" + ((java.sql.Date) val).toLocalDate().toString() + "'";
        }
        if (val instanceof java.util.Date) {
            return "'" + new java.sql.Timestamp(((java.util.Date) val).getTime()).toString() + "'";
        }
        return val.toString();
    }

    // NOTICE: formatOrderList should always insert row id of main table to the rightmost and only then this method works correctly
    // maybe we want to diminish this tight coupling...
    public static List<FilterItemDTO> formatCursoredDeepPageFilters(Map<String, Object> filters,
                                                                    Map<String, Object> anchor,
                                                                    List<Map<String, String>> formattedOrderList) {
        if (anchor == null || anchor.isEmpty() || formattedOrderList == null || formattedOrderList.isEmpty()) {
            return formatFilters(filters);
        }

        List<FilterItemDTO> result = new ArrayList<>();

        // format the cursor condition, according to the order list
        List<String> cursorConditionParts = new ArrayList<>();
        for (int i = 0; i < formattedOrderList.size(); i++) {
            Map<String, String> item = formattedOrderList.get(i);
            String column = item.get("column");
            String direction = item.get("direction");

            try {
                List<String> subParts = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    String col = formattedOrderList.get(j).get("column");
                    String val = wrapValue(anchor.get(col));
                    subParts.add(col + " = " + val);
                }

                String anchorVal = wrapValue(anchor.get(column));
                String op = "asc".equalsIgnoreCase(direction) ? " > " : " < ";
                subParts.add(column + op + anchorVal);

                cursorConditionParts.add("(" + String.join(" and ", subParts) + ")");
            } catch (IllegalArgumentException e) {
                throw new SpExceptions.SystemException(
                        "A column is found from the formatted order list," +
                        " but failed to be found from the anchor," +
                        " which indicates the joint tables of the query does not match permitted tables when formatting order list");
            }
        }
        // join condition segments with OR and append the condition as a raw string
        result.add(FilterItemDTO.raw(String.join(" or ", cursorConditionParts)));
        result.addAll(formatFilters(filters));
        return result;
    }

    public static List<Map<String, String>> getOrderList(List<String> items) {
        return buildOrderList(items);
    }

    public static List<Map<String, String>> getOrderList(String... items) {
        if (items == null || items.length == 0) return new ArrayList<>();
        return buildOrderList(Arrays.asList(items));
    }

    private static List<Map<String, String>> buildOrderList(List<String> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();
        if (items.size() % 2 != 0) {
            throw new IllegalArgumentException("Order list length must be even (column-direction pairs)");
        }
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 2) {
            // column and direction should be placed alternatively
            String column = items.get(i).trim();
            String direction = items.get(i + 1).trim().toLowerCase();

            if (column.isBlank()) {
                throw new IllegalArgumentException("getOrderList: column name must not be empty");
            }

            if (!"asc".equals(direction) && !"desc".equals(direction)) {
                throw new IllegalArgumentException("getOrderList: direction must be 'asc' or 'desc'");
            }
            list.add(Map.of("column", column, "direction", direction));
        }
        return list;
    }

    public static List<Map<String, String>> formatOrderList(String mainTableRowIdName,
                                                            List<Map<String, String>> originalList,
                                                            String... tablesAndColumns) {
        if (mainTableRowIdName == null) {
            throw new SpExceptions.SystemException("The name of main table's row ID must not be null during query, a method from the service layer is wrong.");
        }

        if (originalList == null || originalList.isEmpty() || tablesAndColumns == null || tablesAndColumns.length == 0) {
            return new ArrayList<>(List.of(Map.of("column", mainTableRowIdName, "direction", "asc")));
        }

        // 1. separate tables and permitted temporarily registered columns from input
        Set<String> tables = new HashSet<>();
        Set<String> columns = new HashSet<>();
        for (String s : tablesAndColumns) {
            if (registeredTableColumnMap.containsKey(s)) {
                tables.add(s);
            } else {
                columns.add(s);
            }
        }

        // 2. filter columns using two sets and exclude the row id
        List<Map<String, String>> filtered = originalList.stream()
                .filter(item -> {
                    String column = item.get("column");
                    if (mainTableRowIdName.equalsIgnoreCase(column)) return false;
                    ColumnTriple triple = columnAsKeyMap.get(column);
                    String table = triple == null ? null : triple.getTableName();
                    return (table != null && tables.contains(table)) || columns.contains(column);
                })
                .toList();

        // 3. insert row id to the rightmost to stabilize deep page cursor
        List<Map<String, String>> result = new ArrayList<>(filtered);
        result.add(Map.of("column", mainTableRowIdName, "direction", "asc"));
        return result;
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

    public static String formatPaginationLimit(FilterDTO filter) {
        if (filter == null) return null;
        if (filter.hasLimit()) {
            boolean hasInvalidOffset = filter.hasOffset() && (filter.getOffset() / filter.getLimit()) + 1 > Code.PAGINATION_MAX_PAGE;
            // 1. has limit and invalid offset -> wrong
            if (hasInvalidOffset) {
                throw new IllegalArgumentException("Max page exceeded, no page jumping is allowed. Please switch to the deep page endpoints.");
            }
            // 2. has limit, and has valid offset or no offset -> examine limit
            if (filter.getLimit() > Code.PAGINATION_MAX_LIMIT) {
                filter.setLimit(Code.PAGINATION_MAX_LIMIT);
                return "The pagination limit you provided is too large and has been replaced by " + Code.PAGINATION_MAX_LIMIT;
            } else {
                return null;
            }
        }
        // 3. no limit but has offset -> wrong
        if (!filter.hasLimit() && filter.hasOffset()) {
            throw new IllegalArgumentException("Raw use of offset without limit is not allowed.");
        }
        return null;
    }

    public static AccessControlGroup getAccessControlGroupByUserId(String uid) {
        List<AccessControlGroup> list = QueryToolConfig.accessControlService.getAccessControlGroupByUserId(uid);
        if (list.size() != 1) {
            return new AccessControlGroup();
        } else {
            return AccessControlGroup.formatSystemShutdown(list.get(0));
        }
    }
}

@Component
class QueryToolConfig {
    @Autowired
    public MetaDataService metaDataService0;
    @Autowired
    public AssetService assetService0;
    @Autowired
    public UserService userService0;
    @Autowired
    public AccessControlService accessControlService0;

    public static MetaDataService metaDataService;
    public static AssetService assetService;
    public static UserService userService;
    public static AccessControlService accessControlService;

    @PostConstruct
    public void init() {
        metaDataService = this.metaDataService0;
        assetService = this.assetService0;
        userService = this.userService0;
        accessControlService = this.accessControlService0;
    }
}

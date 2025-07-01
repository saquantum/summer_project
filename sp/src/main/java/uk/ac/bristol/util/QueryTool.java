package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public final class QueryTool {

    private QueryTool() {
        throw new IllegalStateException("Utility class");
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
            "warning");

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
}

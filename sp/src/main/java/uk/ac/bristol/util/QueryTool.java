package uk.ac.bristol.util;

import java.util.*;

public class QueryTool {

    public static List<Map<String, String>> getOrderList(List<String> items){
        return getOrderList(items.toArray(String[]::new));
    }

    public static List<Map<String, String>> getOrderList(String... items){
        if(items.length % 2 != 0) throw new IllegalArgumentException("getOrderList: items length must be even");
        List<Map<String, String>> list = new ArrayList<>();
        for(int i = 0; i < items.length; i+=2){
            Map<String, String> map = new HashMap<>();
            map.put("column",  items[i]);
            map.put("direction", items[i+1]);
            list.add(map);
        }
        return list;
    }
}

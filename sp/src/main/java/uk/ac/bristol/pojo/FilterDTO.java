package uk.ac.bristol.pojo;

import java.util.Map;

public class FilterDTO {
    private Map<String, Object> filters;
    private String orderList;
    private Integer limit;
    private Integer offset;

    public FilterDTO() {
    }

    public FilterDTO(Integer limit) {
        this.limit = limit;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public boolean hasFilters() {
        return filters != null;
    }

    public String[] getOrderList() {
        if (orderList == null) return null;
        return orderList.split(",");
    }

    public void setOrderList(String orderList) {
        this.orderList = orderList;
    }

    public boolean hasOrderList() {
        return orderList != null;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }
}

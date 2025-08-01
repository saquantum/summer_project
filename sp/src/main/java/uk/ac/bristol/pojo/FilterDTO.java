package uk.ac.bristol.pojo;

import uk.ac.bristol.exception.SpExceptions;

import java.util.Map;

public class FilterDTO {
    private Map<String, Object> filters;
    private Long lastRowId;
    private String orderList;
    private Integer limit;
    private Integer offset;

    public FilterDTO() {
    }

    public FilterDTO(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
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

    public Long getLastRowId() {
        return lastRowId;
    }

    public void setLastRowId(String lastRowId) {
        try {
            this.lastRowId = Long.valueOf(lastRowId);
        } catch (NumberFormatException e) {
            throw new SpExceptions.BadRequestException("Last row ID is not a long-integer.");
        }
    }

    public boolean hasLastRowId() {
        return lastRowId != null;
    }

    public String[] getOrderList() {
        if (orderList == null) return null;
        String[] items = orderList.split(",");
        if (items.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid orderList: must contain column-direction pairs");
        }
        return items;
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

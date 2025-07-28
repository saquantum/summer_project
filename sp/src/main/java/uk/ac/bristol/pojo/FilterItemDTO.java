package uk.ac.bristol.pojo;

import java.util.List;

public class FilterItemDTO {
    private String column;
    private String op;
    private Object val;
    private Object min;
    private Object max;
    private List<Object> list;

    public static FilterItemDTO eq(String column, Object val) {
        if (!(val instanceof String) && !(val instanceof Number) && !(val instanceof Boolean)) {
            throw new IllegalArgumentException("val of an eq condition must be a string, a number or a boolean");
        }
        return new FilterItemDTO(column, "is", val);
    }

    public static FilterItemDTO like(String column, Object val) {
        if (!(val instanceof String)) {
            throw new IllegalArgumentException("val of a like condition must be a string");
        }
        return new FilterItemDTO(column, "like", val);
    }

    public static FilterItemDTO range(String column, Object min, Object max) {
        if (min == null && max == null) {
            throw new IllegalArgumentException("range condition must have min or max");
        }
        if (min != null && !(min instanceof String || min instanceof Number)) {
            throw new IllegalArgumentException("min must be a string or number");
        }
        if (max != null && !(max instanceof String || max instanceof Number)) {
            throw new IllegalArgumentException("max must be a string or number");
        }
        if (min != null && max != null) {
            boolean bothString = min instanceof String && max instanceof String;
            boolean bothNumber = min instanceof Number && max instanceof Number;
            if (!bothString && !bothNumber) {
                throw new IllegalArgumentException("min and max must be both strings or both numbers");
            }
        }
        return new FilterItemDTO(column, "range", min, max);
    }

    public static FilterItemDTO in(String column, List<Object> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("list for in condition must not be null or empty");
        }
        return new FilterItemDTO(column, "in", list);
    }

    public static FilterItemDTO isNull(String column) {
        return new FilterItemDTO(column, "isNull");
    }

    public static FilterItemDTO notNull(String column) {
        return new FilterItemDTO(column, "notNull");
    }

    @Override
    public String toString() {
        return "FilterItemDTO{" +
                "column='" + column + '\'' +
                ", op='" + op + '\'' +
                ", val=" + val +
                ", min=" + min +
                ", max=" + max +
                ", list=" + list +
                '}';
    }

    private FilterItemDTO(String column, String op) {
        this.column = column;
        this.op = op;
    }

    private FilterItemDTO(String column, String op, Object val) {
        this.column = column;
        this.op = op;
        this.val = val;
    }

    private FilterItemDTO(String column, String op, Object min, Object max) {
        this.column = column;
        this.op = op;
        this.min = min;
        this.max = max;
    }

    private FilterItemDTO(String column, String op, List<Object> list) {
        this.column = column;
        this.list = list;
        this.op = op;
    }

    public String getColumn() {
        return column;
    }

    public String getOp() {
        return op;
    }

    public Object getVal() {
        return val;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }

    public List<Object> getList() {
        return list;
    }
}

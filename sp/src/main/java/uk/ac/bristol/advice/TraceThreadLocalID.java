package uk.ac.bristol.advice;

public class TraceThreadLocalID {
    private static final ThreadLocal<String> threadTraceId = new ThreadLocal<>();

    public static void set(String traceId) {
        threadTraceId.set(traceId);
    }

    public static String get() {
        return threadTraceId.get();
    }

    public static void clear() {
        threadTraceId.remove();
    }
}

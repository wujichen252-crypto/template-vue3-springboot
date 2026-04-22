package com.template.common.result;

public class RequestIdUtil {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    public static String getCurrentId() {
        String requestId = REQUEST_ID_HOLDER.get();
        if (requestId == null) {
            requestId = java.util.UUID.randomUUID().toString().replace("-", "");
            REQUEST_ID_HOLDER.set(requestId);
        }
        return requestId;
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
    }
}

package http.handlers;

public enum HttpMethod {
    GET, POST, DELETE, UNKNOWN;

    public static HttpMethod fromString(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}

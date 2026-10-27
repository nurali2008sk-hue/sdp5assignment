package kz.delivery.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

public class NoSuitableRouteException extends RuntimeException {

    private final Map<String, String> rejectedRoutes;

    public NoSuitableRouteException(String message) {
        this(message, Map.of());
    }

    public NoSuitableRouteException(String message, Map<String, String> rejectedRoutes) {
        super(message);
        this.rejectedRoutes = Collections.unmodifiableMap(new LinkedHashMap<>(rejectedRoutes));
    }

    public Map<String, String> getRejectedRoutes() {
        return rejectedRoutes;
    }
}
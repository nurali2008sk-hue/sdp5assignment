package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Проверяет жёсткие ограничения (hard constraints).
 * Маршрут, нарушивший хотя бы одно, не может победить, какой бы хороший score он ни имел.
 */
public final class RouteConstraintChecker {

    /** Результат фильтрации: подходящие маршруты и причины отклонения остальных. */
    public record FilterResult(List<Route> validRoutes, Map<String, String> rejected) {}

    private RouteConstraintChecker() {
    }

    /** Возвращает причину отклонения или null, если маршрут подходит. */
    public static String violation(Route route, DeliveryRequest request) {
        if (!route.available()) {
            return "Route is unavailable.";
        }
        if (route.cost() > request.maxBudget()) {
            return "Budget exceeded (" + route.cost() + " > " + request.maxBudget() + ").";
        }
        if (route.totalMinutes() > request.maxDeliveryMinutes()) {
            return "Delivery deadline exceeded (" + route.totalMinutes()
                    + " min > " + request.maxDeliveryMinutes() + " min).";
        }
        if (route.carbonEmission() > request.maxCarbonEmission()) {
            return "Emission limit exceeded (" + route.carbonEmission()
                    + " kg > " + request.maxCarbonEmission() + " kg).";
        }
        return null;
    }

    /**
     * Отфильтровывает маршруты.
     *
     * @throws IllegalArgumentException если request null или в списке есть null-элемент
     * @throws NoSuitableRouteException если список пуст или ни один маршрут не подошёл
     */
    public static FilterResult filter(List<Route> routes, DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request must not be null");
        }
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("No routes were provided.");
        }

        List<Route> valid = new ArrayList<>();
        Map<String, String> rejected = new LinkedHashMap<>();

        for (Route route : routes) {
            if (route == null) {
                throw new IllegalArgumentException("Route list must not contain null elements");
            }
            String reason = violation(route, request);
            if (reason == null) {
                valid.add(route);
            } else {
                rejected.put(route.id(), reason);
            }
        }

        if (valid.isEmpty()) {
            throw new NoSuitableRouteException(
                    "No route satisfies the constraints of request #" + request.id() + ".", rejected);
        }
        return new FilterResult(valid, rejected);
    }
}
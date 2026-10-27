package kz.delivery.planner;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.strategy.RouteDecision;
import kz.delivery.strategy.RouteStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Context. Не знает ни об одном конкретном алгоритме:
 * зависит только от абстракции RouteStrategy и делегирует ей работу.
 */
public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Strategy must not be null");
    }

    /** Смена алгоритма во время работы программы. */
    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Strategy must not be null");
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }
}
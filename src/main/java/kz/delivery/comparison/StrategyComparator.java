package kz.delivery.comparison;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.List;

/** Прогоняет один запрос через все переданные стратегии и собирает результаты. */
public class StrategyComparator {

    private final List<RouteStrategy> strategies;

    public StrategyComparator(List<RouteStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one strategy is required for comparison");
        }
        this.strategies = List.copyOf(strategies);
    }

    public static StrategyComparator withDefaultStrategies() {
        return new StrategyComparator(List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy()
        ));
    }

    public List<ComparisonResult> compare(List<Route> routes, DeliveryRequest request) {
        RoutePlanner planner = new RoutePlanner(strategies.get(0));
        List<ComparisonResult> results = new ArrayList<>();

        for (RouteStrategy strategy : strategies) {
            planner.setStrategy(strategy);   // тот же Context, разные алгоритмы
            try {
                results.add(ComparisonResult.success(planner.plan(routes, request)));
            } catch (NoSuitableRouteException e) {
                results.add(ComparisonResult.failure(strategy.getStrategyName(), e.getMessage()));
            }
        }
        return results;
    }

    public String report(List<Route> routes, DeliveryRequest request) {
        StringBuilder sb = new StringBuilder("DELIVERY REQUEST #").append(request.id()).append("\n\n");
        for (ComparisonResult result : compare(routes, request)) {
            sb.append(result.toText()).append('\n');
        }
        return sb.toString();
    }
}
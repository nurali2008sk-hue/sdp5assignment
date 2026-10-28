package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;

import java.util.Comparator;
import java.util.List;


public class PremiumRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        RouteConstraintChecker.FilterResult filtered = RouteConstraintChecker.filter(routes, request);

        Route best = filtered.validRoutes().stream()
                .min(Comparator.comparingInt(Route::trafficDelayMinutes)
                        .thenComparingInt(Route::totalMinutes)
                        .thenComparingDouble(Route::cost))
                .orElseThrow();

        List<String> reasons = List.of(
                "Traffic delay: " + best.trafficDelayMinutes() + " min (lowest among valid routes)",
                "Final time: " + best.totalMinutes() + " min",
                "Premium prioritizes reliability; cost is not optimized"
        );

        return new RouteDecision(getStrategyName(), best, best.trafficDelayMinutes(),
                reasons, filtered.rejected());
    }

    @Override
    public String getStrategyName() {
        return "Premium Strategy";
    }
}
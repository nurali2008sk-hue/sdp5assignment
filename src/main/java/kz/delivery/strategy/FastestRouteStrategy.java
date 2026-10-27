package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;

import java.util.Comparator;
import java.util.List;


public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        RouteConstraintChecker.FilterResult filtered = RouteConstraintChecker.filter(routes, request);

        Route best = filtered.validRoutes().stream()
                .min(Comparator.comparingInt(Route::totalMinutes)
                        .thenComparingDouble(Route::cost))   // при равном времени выбираем дешевле
                .orElseThrow();                              // сюда не попадём: filter гарантирует непустой список

        List<String> reasons = List.of(
                "Estimated time: " + best.estimatedMinutes() + " min",
                "Traffic penalty: +" + best.trafficDelayMinutes() + " min",
                "Final time: " + best.totalMinutes() + " min"
        );

        return new RouteDecision(getStrategyName(), best, best.totalMinutes(),
                reasons, filtered.rejected());
    }

    @Override
    public String getStrategyName() {
        return "Fastest Strategy";
    }
}

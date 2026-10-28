package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class EcoFriendlyRouteStrategy implements RouteStrategy {

    private static final double DEFAULT_EMISSION_WEIGHT = 1.0;
    private static final double DEFAULT_DISTANCE_WEIGHT = 0.05;

    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() {
        this(DEFAULT_EMISSION_WEIGHT, DEFAULT_DISTANCE_WEIGHT);
    }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        if (emissionWeight < 0 || distanceWeight < 0) {
            throw new IllegalArgumentException("Eco weights must not be negative");
        }
        if (emissionWeight == 0 && distanceWeight == 0) {
            throw new IllegalArgumentException("At least one eco weight must be positive");
        }
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        RouteConstraintChecker.FilterResult filtered = RouteConstraintChecker.filter(routes, request);

        Route best = filtered.validRoutes().stream()
                .min(Comparator.comparingDouble(this::ecoScore)
                        .thenComparingDouble(Route::cost))   // при равном score выбираем дешевле
                .orElseThrow();

        List<String> reasons = List.of(
                String.format(Locale.ROOT, "CO2: %.2f kg x %.2f = %.3f",
                        best.carbonEmission(), emissionWeight, best.carbonEmission() * emissionWeight),
                String.format(Locale.ROOT, "Distance: %.1f km x %.2f = %.3f",
                        best.distanceKm(), distanceWeight, best.distanceKm() * distanceWeight),
                String.format(Locale.ROOT, "Eco score: %.3f (lower is better)", ecoScore(best))
        );

        return new RouteDecision(getStrategyName(), best, ecoScore(best), reasons, filtered.rejected());
    }


    public double ecoScore(Route route) {
        return route.carbonEmission() * emissionWeight + route.distanceKm() * distanceWeight;
    }

    @Override
    public String getStrategyName() {
        return "Eco Strategy";
    }
}
package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

/**
 * Многокритериальная стратегия: взвешенная сумма нормализованных показателей.
 * Веса настраиваются и обязаны давать в сумме 1.0.
 */
public class BalancedRouteStrategy implements RouteStrategy {

    private static final double EPSILON = 1e-9;

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    /** Конфигурация по умолчанию для обычной доставки. */
    public BalancedRouteStrategy() {
        this(0.40, 0.30, 0.10, 0.20);
    }

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        if (timeWeight < 0 || costWeight < 0 || distanceWeight < 0 || emissionWeight < 0) {
            throw new IllegalArgumentException("Weights must not be negative");
        }
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > EPSILON) {
            throw new IllegalArgumentException(
                    "Weights must sum to 1.0, but the sum is " + sum);
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        RouteConstraintChecker.FilterResult filtered = RouteConstraintChecker.filter(routes, request);
        List<Route> valid = filtered.validRoutes();

        // Диапазоны считаем только по допустимым маршрутам
        Range time = Range.of(valid, Route::totalMinutes);
        Range cost = Range.of(valid, Route::cost);
        Range distance = Range.of(valid, Route::distanceKm);
        Range emission = Range.of(valid, Route::carbonEmission);

        Route best = valid.stream()
                .min(Comparator.comparingDouble((Route r) -> score(r, time, cost, distance, emission))
                        .thenComparingDouble(Route::cost))   // при равном score выбираем дешевле
                .orElseThrow();

        double nTime = time.normalize(best.totalMinutes());
        double nCost = cost.normalize(best.cost());
        double nDistance = distance.normalize(best.distanceKm());
        double nEmission = emission.normalize(best.carbonEmission());
        double finalScore = score(best, time, cost, distance, emission);

        List<String> reasons = List.of(
                String.format(Locale.ROOT, "Time:     normalized %.3f x weight %.2f = %.3f",
                        nTime, timeWeight, nTime * timeWeight),
                String.format(Locale.ROOT, "Cost:     normalized %.3f x weight %.2f = %.3f",
                        nCost, costWeight, nCost * costWeight),
                String.format(Locale.ROOT, "Distance: normalized %.3f x weight %.2f = %.3f",
                        nDistance, distanceWeight, nDistance * distanceWeight),
                String.format(Locale.ROOT, "Emission: normalized %.3f x weight %.2f = %.3f",
                        nEmission, emissionWeight, nEmission * emissionWeight),
                String.format(Locale.ROOT, "Total score: %.3f (lower is better)", finalScore)
        );

        return new RouteDecision(getStrategyName(), best, finalScore, reasons, filtered.rejected());
    }

    private double score(Route route, Range time, Range cost, Range distance, Range emission) {
        return time.normalize(route.totalMinutes()) * timeWeight
                + cost.normalize(route.cost()) * costWeight
                + distance.normalize(route.distanceKm()) * distanceWeight
                + emission.normalize(route.carbonEmission()) * emissionWeight;
    }

    @Override
    public String getStrategyName() {
        return "Balanced Strategy";
    }

    /** Диапазон значений одного критерия + min-max нормализация. */
    private record Range(double min, double max) {

        static Range of(List<Route> routes, ToDoubleFunction<Route> extractor) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Route route : routes) {
                double value = extractor.applyAsDouble(route);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            return new Range(min, max);
        }

        double normalize(double value) {
            if (max == min) {
                return 0.0;   // все маршруты равны по этому критерию, он не влияет на выбор
            }
            return (value - min) / (max - min);
        }
    }
}
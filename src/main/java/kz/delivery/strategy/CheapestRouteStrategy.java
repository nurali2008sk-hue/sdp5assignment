package kz.delivery.strategy;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CheapestRouteStrategy implements RouteStrategy {

    private static final double DEFAULT_COST_PER_KM = 15.0;
    private static final double DEFAULT_SERVICE_FEE = 200.0;
    private static final Map<DeliveryPriority, Double> DEFAULT_SURCHARGES = Map.of(
            DeliveryPriority.URGENT, 0.20,
            DeliveryPriority.STANDARD, 0.05,
            DeliveryPriority.ECONOMY, 0.0,
            DeliveryPriority.ECO, 0.0
    );

    private final double costPerKm;
    private final double serviceFee;
    private final Map<DeliveryPriority, Double> surchargeRates;

    public CheapestRouteStrategy() {
        this(DEFAULT_COST_PER_KM, DEFAULT_SERVICE_FEE, DEFAULT_SURCHARGES);
    }

    public CheapestRouteStrategy(double costPerKm, double serviceFee,
                                 Map<DeliveryPriority, Double> surchargeRates) {
        if (costPerKm < 0 || serviceFee < 0) {
            throw new IllegalArgumentException("costPerKm and serviceFee must not be negative");
        }
        this.costPerKm = costPerKm;
        this.serviceFee = serviceFee;
        this.surchargeRates = Map.copyOf(surchargeRates);
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        RouteConstraintChecker.FilterResult filtered = RouteConstraintChecker.filter(routes, request);

        Map<String, String> rejected = new LinkedHashMap<>(filtered.rejected());
        List<Route> candidates = new ArrayList<>();

        // Бюджет проверяем по ПОЛНОЙ стоимости (с надбавками), а не только по базе
        for (Route route : filtered.validRoutes()) {
            double total = totalCost(route, request);
            if (total > request.maxBudget()) {
                rejected.put(route.id(), String.format(Locale.ROOT,
                        "Budget exceeded after surcharges (%.2f > %.2f).", total, request.maxBudget()));
            } else {
                candidates.add(route);
            }
        }

        if (candidates.isEmpty()) {
            throw new NoSuitableRouteException(
                    "No route fits the budget of request #" + request.id() + ".", rejected);
        }

        Route best = candidates.stream()
                .min(Comparator.comparingDouble((Route r) -> totalCost(r, request))
                        .thenComparingInt(Route::totalMinutes))  // при равной цене выбираем быстрее
                .orElseThrow();

        double surcharge = best.cost() * surchargeRate(request);
        List<String> reasons = List.of(
                String.format(Locale.ROOT, "Base cost: %.2f", best.cost()),
                String.format(Locale.ROOT, "Distance cost: %.1f km x %.2f = %.2f",
                        best.distanceKm(), costPerKm, best.distanceKm() * costPerKm),
                String.format(Locale.ROOT, "Priority surcharge (%s): %.2f",
                        request.priority(), surcharge),
                String.format(Locale.ROOT, "Service fee: %.2f", serviceFee),
                String.format(Locale.ROOT, "Total cost: %.2f", totalCost(best, request))
        );

        return new RouteDecision(getStrategyName(), best, totalCost(best, request), reasons, rejected);
    }


    public double totalCost(Route route, DeliveryRequest request) {
        return route.cost()
                + route.distanceKm() * costPerKm
                + route.cost() * surchargeRate(request)
                + serviceFee;
    }

    private double surchargeRate(DeliveryRequest request) {
        return surchargeRates.getOrDefault(request.priority(), 0.0);
    }

    @Override
    public String getStrategyName() {
        return "Cheapest Strategy";
    }
}

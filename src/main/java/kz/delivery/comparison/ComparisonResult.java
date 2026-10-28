package kz.delivery.comparison;

import kz.delivery.domain.Route;
import kz.delivery.strategy.RouteDecision;

import java.util.Locale;


public record ComparisonResult(String strategyName, RouteDecision decision, String failureReason) {

    public static ComparisonResult success(RouteDecision decision) {
        return new ComparisonResult(decision.strategyName(), decision, null);
    }

    public static ComparisonResult failure(String strategyName, String reason) {
        return new ComparisonResult(strategyName, null, reason);
    }

    public boolean succeeded() {
        return decision != null;
    }

    public String toText() {
        StringBuilder sb = new StringBuilder(strategyName).append(":\n");
        if (succeeded()) {
            Route route = decision.selectedRoute();
            sb.append(String.format(Locale.ROOT, "  Route %s%n", route.id()));
            sb.append(String.format(Locale.ROOT, "  Time: %d min%n", route.totalMinutes()));
            sb.append(String.format(Locale.ROOT, "  Cost: %.2f%n", route.cost()));
            sb.append(String.format(Locale.ROOT, "  CO2: %.1f kg%n", route.carbonEmission()));
            sb.append(String.format(Locale.ROOT, "  Score: %.2f%n", decision.score()));
        } else {
            sb.append("  No suitable route: ").append(failureReason).append('\n');
        }
        return sb.toString();
    }
}

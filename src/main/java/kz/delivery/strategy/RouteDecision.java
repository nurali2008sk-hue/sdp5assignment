package kz.delivery.strategy;

import kz.delivery.domain.Route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public record RouteDecision(
        String strategyName,
        Route selectedRoute,
        double score,
        List<String> reasons,
        Map<String, String> rejectedRoutes
) {

    public RouteDecision {
        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("Strategy name must not be blank");
        }
        if (selectedRoute == null) {
            throw new IllegalArgumentException("Selected route must not be null");
        }
        reasons = List.copyOf(reasons);
        rejectedRoutes = Collections.unmodifiableMap(new LinkedHashMap<>(rejectedRoutes));
    }


    public String explain() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy: ").append(strategyName).append('\n');
        sb.append("Selected: Route ").append(selectedRoute.id()).append('\n');
        sb.append("Reason:\n");
        reasons.forEach(r -> sb.append("  ").append(r).append('\n'));
        rejectedRoutes.forEach((id, why) ->
                sb.append("Route ").append(id).append(" rejected: ").append(why).append('\n'));
        return sb.toString();
    }
}

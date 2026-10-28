package kz.delivery;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.ECO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EcoFriendlyRouteStrategyTest {

    private final EcoFriendlyRouteStrategy strategy = new EcoFriendlyRouteStrategy();

    @Test
    void picksLowestEcoScoreOnDemoData() {
        RouteDecision decision = strategy.selectRoute(DemoData.routes(), request(ECO, 6000, 60));

        assertEquals("E", decision.selectedRoute().id());
        assertEquals(1.45, decision.score(), 1e-6);   // 0.9 kg * 1.0 + 11 km * 0.05
    }

    @Test
    void emissionLimitRejectsRouteWithBetterScore() {
        Route lowScoreHighCo2 = route("A", 5, 20, 1000, 2.0, 0, true);   // score 2.25
        Route higherScoreLowCo2 = route("B", 40, 20, 1000, 0.8, 0, true); // score 2.80
        List<Route> routes = List.of(lowScoreHighCo2, higherScoreLowCo2);

        // без лимита побеждает A
        assertEquals("A", strategy.selectRoute(routes, request(ECO, 5000, 100)).selectedRoute().id());

        // с лимитом 1.0 кг маршрут A запрещён, побеждает B
        DeliveryRequest limited = new DeliveryRequest(2, ECO, 5000, 100, 1.0, true);
        assertEquals("B", strategy.selectRoute(routes, limited).selectedRoute().id());
    }

    @Test
    void invalidWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(0, 0));
    }
}
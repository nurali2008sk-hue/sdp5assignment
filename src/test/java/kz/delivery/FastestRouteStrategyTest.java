package kz.delivery;

import kz.delivery.domain.Route;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static kz.delivery.domain.DeliveryPriority.URGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastestRouteStrategyTest {

    private final FastestRouteStrategy strategy = new FastestRouteStrategy();

    @Test
    void considersTrafficNotOnlyEstimatedMinutes() {
        Route lowEstimateHeavyTraffic = route("X", 10, 20, 1000, 1.0, 15, true);   // 35 min total
        Route higherEstimateLightTraffic = route("Y", 10, 30, 1000, 1.0, 2, true); // 32 min total

        RouteDecision decision = strategy.selectRoute(
                List.of(lowEstimateHeavyTraffic, higherEstimateLightTraffic),
                request(STANDARD, 5000, 100));

        assertEquals("Y", decision.selectedRoute().id());
        assertEquals(32, decision.score(), 1e-9);
    }

    @Test
    void unavailableRouteCannotWinEvenIfFastest() {
        Route fastButClosed = route("F", 10, 10, 1000, 1.0, 0, false);
        Route slowOpen = route("S", 10, 30, 1000, 1.0, 0, true);

        RouteDecision decision = strategy.selectRoute(
                List.of(fastButClosed, slowOpen), request(STANDARD, 5000, 100));

        assertEquals("S", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("F").contains("unavailable"));
    }

    @Test
    void equalTimeIsResolvedByLowerCost() {
        Route pricey = route("P", 10, 30, 3000, 1.0, 0, true);
        Route cheap = route("Q", 10, 30, 2000, 1.0, 0, true);

        RouteDecision decision = strategy.selectRoute(
                List.of(pricey, cheap), request(STANDARD, 5000, 100));

        assertEquals("Q", decision.selectedRoute().id());
    }

    @Test
    void differentBudgetProducesDifferentWinner() {
        List<Route> routes = DemoData.routes();

        // при бюджете 6000 маршрут F (7500) запрещён, побеждает B
        assertEquals("B", strategy.selectRoute(routes, request(URGENT, 6000, 60)).selectedRoute().id());
        // при бюджете 10000 F разрешён и он быстрее всех
        assertEquals("F", strategy.selectRoute(routes, request(URGENT, 10000, 60)).selectedRoute().id());
    }
}
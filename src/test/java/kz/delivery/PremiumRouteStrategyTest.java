package kz.delivery;

import kz.delivery.domain.Route;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.PremiumRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PremiumRouteStrategyTest {

    private final PremiumRouteStrategy strategy = new PremiumRouteStrategy();

    @Test
    void picksLowestTrafficDelayOnDemoData() {
        RouteDecision decision = strategy.selectRoute(DemoData.routes(), request(STANDARD, 6000, 60));

        assertEquals("E", decision.selectedRoute().id());
        assertEquals(2.0, decision.score(), 1e-9);   // 2 минуты пробок, минимум среди допустимых
    }

    @Test
    void prefersReliabilityWhereFastestPrefersSpeed() {
        Route fastButJammed = route("FAST", 10, 20, 1000, 1.0, 10, true);   // 30 мин, 10 мин пробок
        Route slowButStable = route("STABLE", 10, 40, 1000, 1.0, 0, true);  // 40 мин, без пробок
        List<Route> routes = List.of(fastButJammed, slowButStable);

        assertEquals("FAST", new FastestRouteStrategy()
                .selectRoute(routes, request(STANDARD, 5000, 100)).selectedRoute().id());
        assertEquals("STABLE", strategy
                .selectRoute(routes, request(STANDARD, 5000, 100)).selectedRoute().id());
    }

    @Test
    void stillRespectsHardConstraints() {
        Route stableButOverBudget = route("STABLE", 10, 40, 9000, 1.0, 0, true);
        Route jammedWithinBudget = route("JAMMED", 10, 20, 1000, 1.0, 10, true);

        RouteDecision decision = strategy.selectRoute(
                List.of(stableButOverBudget, jammedWithinBudget), request(STANDARD, 5000, 100));

        assertEquals("JAMMED", decision.selectedRoute().id());
    }
}

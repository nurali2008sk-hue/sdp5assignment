package kz.delivery;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import kz.delivery.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoutePlannerTest {

    @Test
    void sameInstanceSwitchesAlgorithmsAtRuntime() {
        List<Route> routes = DemoData.routes();
        DeliveryRequest request = new DeliveryRequest(101, STANDARD, 6000, 60, false);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("B", planner.plan(routes, request).selectedRoute().id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("D", planner.plan(routes, request).selectedRoute().id());
        assertEquals("Cheapest Strategy", planner.getStrategy().getStrategyName());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("E", planner.plan(routes, request).selectedRoute().id());

        planner.setStrategy(new BalancedRouteStrategy());
        assertEquals("H", planner.plan(routes, request).selectedRoute().id());
    }

    @Test
    void plannerWorksWithAnyRouteStrategyImplementation() {
        List<Route> routes = List.of(route("FIRST", 10, 30, 1000, 1.0, 0, true));

        // заглушка: планировщик ничего не знает о её алгоритме, только об интерфейсе
        RouteStrategy stub = new RouteStrategy() {
            @Override
            public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
                return new RouteDecision("Stub", routes.get(0), 0, List.of("stub reason"), Map.of());
            }

            @Override
            public String getStrategyName() {
                return "Stub";
            }
        };

        RouteDecision decision = new RoutePlanner(stub).plan(routes, request(STANDARD, 5000, 60));

        assertEquals("Stub", decision.strategyName());
        assertEquals("FIRST", decision.selectedRoute().id());
    }

    @Test
    void nullStrategyIsRejected() {
        assertThrows(NullPointerException.class, () -> new RoutePlanner(null));

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertThrows(NullPointerException.class, () -> planner.setStrategy(null));
    }
}

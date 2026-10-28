package kz.delivery;

import kz.delivery.domain.Route;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalancedRouteStrategyTest {

    @Test
    void picksBestCompromiseOnDemoData() {
        RouteDecision decision = new BalancedRouteStrategy()
                .selectRoute(DemoData.routes(), request(STANDARD, 6000, 60));

        assertEquals("H", decision.selectedRoute().id());
        assertEquals(0.3526, decision.score(), 0.001);
    }

    @Test
    void weightsNotSummingToOneAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.1));    // сумма 0.9
        assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3));   // сумма 1.0, но вес отрицательный
    }

    @Test
    void floatingPointWeightsSummingToOneAreAccepted() {
        // 0.1 + 0.2 + 0.3 + 0.4 в double не ровно 1.0, допуск должен это принять
        assertDoesNotThrow(() -> new BalancedRouteStrategy(0.1, 0.2, 0.3, 0.4));
    }

    @Test
    void weightsDecideTheWinner() {
        Route fastExpensive = route("FAST", 10, 10, 5000, 1.0, 0, true);
        Route slowCheap = route("CHEAP", 10, 60, 1000, 1.0, 0, true);
        List<Route> routes = List.of(fastExpensive, slowCheap);

        RouteDecision timeFirst = new BalancedRouteStrategy(1.0, 0.0, 0.0, 0.0)
                .selectRoute(routes, request(STANDARD, 10000, 100));
        RouteDecision costFirst = new BalancedRouteStrategy(0.0, 1.0, 0.0, 0.0)
                .selectRoute(routes, request(STANDARD, 10000, 100));

        assertEquals("FAST", timeFirst.selectedRoute().id());
        assertEquals("CHEAP", costFirst.selectedRoute().id());
    }

    @Test
    void singleRouteDoesNotCauseDivisionByZero() {
        Route only = route("ONLY", 10, 30, 1000, 1.0, 0, true);

        RouteDecision decision = new BalancedRouteStrategy()
                .selectRoute(List.of(only), request(STANDARD, 5000, 60));

        assertEquals("ONLY", decision.selectedRoute().id());
        assertFalse(Double.isNaN(decision.score()));
        assertEquals(0.0, decision.score(), 1e-9);   // все критерии равны, норма 0
    }
}

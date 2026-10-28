package kz.delivery;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.PremiumRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import kz.delivery.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static kz.delivery.domain.DeliveryPriority.URGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintsAndFailuresTest {

    private final FastestRouteStrategy fastest = new FastestRouteStrategy();

    @Test
    void routeViolatingBudgetCannotWinDespiteBestScore() {
        Route great = route("GREAT", 10, 5, 9000, 1.0, 0, true);   // идеальное время, но дорого
        Route ok = route("OK", 10, 30, 1000, 1.0, 0, true);

        RouteDecision decision = fastest.selectRoute(List.of(great, ok), request(STANDARD, 5000, 100));

        assertEquals("OK", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("GREAT").contains("Budget exceeded"));
    }

    @Test
    void budgetBoundaryIsInclusive() {
        Route exactlyOnBudget = route("EXACT", 10, 30, 5000, 1.0, 0, true);

        RouteDecision decision = fastest.selectRoute(List.of(exactlyOnBudget), request(STANDARD, 5000, 100));

        assertEquals("EXACT", decision.selectedRoute().id());
    }

    @Test
    void deadlineBoundaryIsInclusiveAndLateRouteIsRejected() {
        Route onDeadline = route("P", 10, 55, 1000, 1.0, 5, true);   // 60 мин итого, ровно в дедлайн
        Route late = route("Q", 10, 61, 1000, 1.0, 0, true);         // 61 мин, опоздание

        RouteDecision decision = fastest.selectRoute(List.of(onDeadline, late), request(STANDARD, 5000, 60));

        assertEquals("P", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("Q").contains("deadline"));
    }

    @Test
    void noValidRouteThrowsWithReasonForEveryRoute() {
        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> fastest.selectRoute(DemoData.routes(), request(URGENT, 100, 60)));

        assertEquals(8, e.getRejectedRoutes().size());
        assertTrue(e.getRejectedRoutes().get("G").contains("unavailable"));
        assertTrue(e.getRejectedRoutes().get("A").contains("Budget exceeded"));
    }

    @Test
    void everyStrategyRejectsEmptyRouteList() {
        List<RouteStrategy> all = List.of(
                new FastestRouteStrategy(), new CheapestRouteStrategy(), new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(), new PremiumRouteStrategy());

        for (RouteStrategy strategy : all) {
            assertThrows(NoSuitableRouteException.class,
                    () -> strategy.selectRoute(List.of(), request(STANDARD, 5000, 60)),
                    strategy.getStrategyName() + " must reject an empty list");
        }
    }

    @Test
    void negativeValuesAreRejectedByDomainModel() {
        assertThrows(IllegalArgumentException.class, () -> route("X", 10, 30, -5, 1.0, 0, true));   // цена
        assertThrows(IllegalArgumentException.class, () -> route("X", -1, 30, 100, 1.0, 0, true));  // расстояние
        assertThrows(IllegalArgumentException.class, () -> route("X", 10, 30, 100, -1.0, 0, true)); // выбросы
    }

    @Test
    void missingRequestDataIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryRequest(1, null, 6000, 60, false));      // нет приоритета
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryRequest(1, STANDARD, 0, 60, false));     // нулевой бюджет
        assertThrows(IllegalArgumentException.class,
                () -> fastest.selectRoute(DemoData.routes(), null));       // нет запроса
    }

    @Test
    void nullElementInsideRouteListIsRejected() {
        List<Route> withNull = Arrays.asList(route("A", 10, 30, 1000, 1.0, 0, true), null);

        assertThrows(IllegalArgumentException.class,
                () -> fastest.selectRoute(withNull, request(STANDARD, 5000, 60)));
    }

    @Test
    void allRoutesUnavailableIsReportedAsNoSuitableRoute() {
        List<Route> closed = List.of(
                route("A", 10, 30, 1000, 1.0, 0, false),
                route("B", 10, 20, 1000, 1.0, 0, false));

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> fastest.selectRoute(closed, request(STANDARD, 5000, 60)));

        assertEquals(2, e.getRejectedRoutes().size());
        assertTrue(e.getRejectedRoutes().values().stream().allMatch(v -> v.contains("unavailable")));
    }
}

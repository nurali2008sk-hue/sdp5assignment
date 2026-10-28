package kz.delivery;

import kz.delivery.comparison.ComparisonResult;
import kz.delivery.comparison.StrategyComparator;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.PremiumRouteStrategy;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyComparatorTest {

    private final List<Route> routes = DemoData.routes();
    private final DeliveryRequest request = new DeliveryRequest(105, STANDARD, 6000, 60, false);

    @Test
    void runsAllDefaultStrategiesOnTheSameRequest() {
        List<ComparisonResult> results = StrategyComparator.withDefaultStrategies().compare(routes, request);

        Map<String, String> winners = winnersByStrategy(results);
        assertEquals(4, results.size());
        assertEquals("B", winners.get("Fastest Strategy"));
        assertEquals("D", winners.get("Cheapest Strategy"));
        assertEquals("E", winners.get("Eco Strategy"));
        assertEquals("H", winners.get("Balanced Strategy"));
    }

    @Test
    void differentStrategiesCanDisagreeOnTheSameRequest() {
        StrategyComparator comparator = new StrategyComparator(
                List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()));

        List<ComparisonResult> results = comparator.compare(routes, request);

        String fastestRoute = results.get(0).decision().selectedRoute().id();
        String cheapestRoute = results.get(1).decision().selectedRoute().id();
        assertNotEquals(fastestRoute, cheapestRoute);
    }

    @Test
    void failingStrategyDoesNotBreakTheComparison() {

        Route onlyRoute = TestData.route("R", 0, 10, 1000, 1.0, 0, true);
        DeliveryRequest tight = new DeliveryRequest(1, STANDARD, 1000, 60, false);
        StrategyComparator comparator = new StrategyComparator(
                List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()));

        List<ComparisonResult> results = comparator.compare(List.of(onlyRoute), tight);

        assertTrue(results.get(0).succeeded());
        assertFalse(results.get(1).succeeded());
        assertNotNull(results.get(1).failureReason());
    }

    @Test
    void reportContainsRequestIdStrategyNamesAndRoutes() {
        String report = StrategyComparator.withDefaultStrategies().report(routes, request);

        assertTrue(report.contains("DELIVERY REQUEST #105"));
        assertTrue(report.contains("Fastest Strategy:"));
        assertTrue(report.contains("Route B"));
        assertTrue(report.contains("Balanced Strategy:"));
    }

    @Test
    void comparatorRequiresAtLeastOneStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new StrategyComparator(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new StrategyComparator(null));
    }

    @Test
    void newFifthStrategyJoinsComparisonWithoutChangingTheComparator() {
        StrategyComparator comparator = new StrategyComparator(List.of(
                new FastestRouteStrategy(), new CheapestRouteStrategy(), new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(), new PremiumRouteStrategy()));

        Map<String, String> winners = winnersByStrategy(comparator.compare(routes, request));

        assertEquals(5, winners.size());
        assertEquals("E", winners.get("Premium Strategy"));
    }

    private Map<String, String> winnersByStrategy(List<ComparisonResult> results) {
        return results.stream().collect(Collectors.toMap(
                ComparisonResult::strategyName,
                r -> r.decision().selectedRoute().id()));
    }
}
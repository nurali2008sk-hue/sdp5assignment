package kz.delivery;

import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.TestData.route;
import static kz.delivery.domain.DeliveryPriority.ECONOMY;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static kz.delivery.domain.DeliveryPriority.URGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheapestRouteStrategyTest {

    private final CheapestRouteStrategy strategy = new CheapestRouteStrategy();

    @Test
    void totalCostFollowsDocumentedFormula() {
        Route route = route("R", 10, 30, 1000, 1.0, 0, true);

        // 1000 (base) + 10*15 (distance) + 1000*0.05 (STANDARD surcharge) + 200 (fee) = 1400
        assertEquals(1400.0, strategy.totalCost(route, request(STANDARD, 5000, 60)), 1e-6);
    }

    @Test
    void prioritySurchargeChangesTotalCost() {
        Route route = route("R", 10, 30, 1000, 1.0, 0, true);

        assertEquals(1550.0, strategy.totalCost(route, request(URGENT, 5000, 60)), 1e-6);   // +20%
        assertEquals(1350.0, strategy.totalCost(route, request(ECONOMY, 5000, 60)), 1e-6);  // +0%
    }

    @Test
    void picksLowestTotalCostAndRejectsOverBudgetAfterSurcharges() {
        RouteDecision decision = strategy.selectRoute(DemoData.routes(), request(ECONOMY, 3000, 70));

        assertEquals("D", decision.selectedRoute().id());
        assertEquals(1970.0, decision.score(), 1e-6);
        // H проходит по базе (3000), но с надбавками 3402.5 > 3000
        assertTrue(decision.rejectedRoutes().get("H").contains("after surcharges"));
    }

    @Test
    void budgetIsCheckedAgainstFullCostNotOnlyBase() {
        // база 1000 укладывается в бюджет 1000, но полная стоимость 1250 нет
        Route route = route("R", 0, 30, 1000, 1.0, 0, true);

        assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(route), request(STANDARD, 1000, 60)));
    }
}

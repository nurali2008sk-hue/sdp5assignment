package kz.delivery;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.selector.SelectionRule;
import kz.delivery.selector.StrategySelector;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.PremiumRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.delivery.TestData.request;
import static kz.delivery.domain.DeliveryPriority.ECO;
import static kz.delivery.domain.DeliveryPriority.ECONOMY;
import static kz.delivery.domain.DeliveryPriority.STANDARD;
import static kz.delivery.domain.DeliveryPriority.URGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StrategySelectorTest {

    private final StrategySelector selector = StrategySelector.withDefaultRules();

    @Test
    void mapsEachPriorityToItsStrategy() {
        assertInstanceOf(FastestRouteStrategy.class, selector.select(request(URGENT, 6000, 60)));
        assertInstanceOf(CheapestRouteStrategy.class, selector.select(request(ECONOMY, 6000, 60)));
        assertInstanceOf(EcoFriendlyRouteStrategy.class, selector.select(request(ECO, 6000, 60)));
        assertInstanceOf(BalancedRouteStrategy.class, selector.select(request(STANDARD, 6000, 60)));
    }

    @Test
    void urgentIsNotOverriddenByEcoPreferredFlag() {
        DeliveryRequest urgentEco = new DeliveryRequest(1, URGENT, 6000, 60, true);
        DeliveryRequest standardEco = new DeliveryRequest(2, STANDARD, 6000, 60, true);

        assertInstanceOf(FastestRouteStrategy.class, selector.select(urgentEco));
        assertInstanceOf(EcoFriendlyRouteStrategy.class, selector.select(standardEco));
    }

    @Test
    void customRuleCanBeAddedWithoutModifyingSelector() {
        StrategySelector custom = new StrategySelector(new BalancedRouteStrategy())
                .addRule(new SelectionRule("big budget",
                        r -> r.maxBudget() >= 10000, new PremiumRouteStrategy()));

        assertInstanceOf(PremiumRouteStrategy.class, custom.select(request(STANDARD, 20000, 60)));
        // правило не подошло, срабатывает стратегия по умолчанию
        assertInstanceOf(BalancedRouteStrategy.class, custom.select(request(STANDARD, 100, 60)));
    }

    @Test
    void firstMatchingRuleWins() {
        StrategySelector ordered = new StrategySelector(new BalancedRouteStrategy())
                .addRule(new SelectionRule("first", r -> true, new FastestRouteStrategy()))
                .addRule(new SelectionRule("second", r -> true, new CheapestRouteStrategy()));

        assertInstanceOf(FastestRouteStrategy.class, ordered.select(request(STANDARD, 5000, 60)));
    }

    @Test
    void nullRequestIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> selector.select(null));
    }

    @Test
    void selectorAndPlannerChooseDifferentRoutesForDifferentRequests() {
        List<Route> routes = DemoData.routes();
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy());

        DeliveryRequest urgent = new DeliveryRequest(201, URGENT, 6000, 60, false);
        DeliveryRequest economy = new DeliveryRequest(202, ECONOMY, 3000, 70, false);
        DeliveryRequest eco = new DeliveryRequest(203, ECO, 6000, 60, false);
        DeliveryRequest standard = new DeliveryRequest(204, STANDARD, 6000, 60, false);

        assertEquals("B", winner(planner, urgent, routes));
        assertEquals("D", winner(planner, economy, routes));
        assertEquals("E", winner(planner, eco, routes));
        assertEquals("H", winner(planner, standard, routes));
    }

    private String winner(RoutePlanner planner, DeliveryRequest request, List<Route> routes) {
        planner.setStrategy(selector.select(request));
        return planner.plan(routes, request).selectedRoute().id();
    }
}
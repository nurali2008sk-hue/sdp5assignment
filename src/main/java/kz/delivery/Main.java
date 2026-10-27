package kz.delivery;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.selector.StrategySelector;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();

        runtimeSwitchingDemo(routes);
        automaticSelectionDemo(routes);
    }

    private static void runtimeSwitchingDemo(List<Route> routes) {
        DeliveryRequest request = new DeliveryRequest(101, DeliveryPriority.STANDARD, 6000, 60, false);
        System.out.println("=== RUNTIME STRATEGY SWITCHING (request #" + request.id() + ") ===\n");

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new CheapestRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new BalancedRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());
    }

    private static void automaticSelectionDemo(List<Route> routes) {
        System.out.println("=== AUTOMATIC STRATEGY SELECTION ===\n");

        StrategySelector selector = StrategySelector.withDefaultRules();
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy());

        List<DeliveryRequest> requests = List.of(
                new DeliveryRequest(201, DeliveryPriority.URGENT, 6000, 60, false),
                new DeliveryRequest(202, DeliveryPriority.ECONOMY, 3000, 70, false),
                new DeliveryRequest(203, DeliveryPriority.ECO, 6000, 60, false),
                new DeliveryRequest(204, DeliveryPriority.STANDARD, 6000, 60, false)
        );

        for (DeliveryRequest request : requests) {
            planner.setStrategy(selector.select(request));
            System.out.println("Request #" + request.id() + " (" + request.priority() + ")");
            System.out.println(planner.plan(routes, request).explain());
        }
    }
}

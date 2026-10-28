package kz.delivery;

import kz.delivery.comparison.StrategyComparator;
import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.exception.NoSuitableRouteException;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.selector.StrategySelector;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.PremiumRouteStrategy;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();

        runtimeSwitchingDemo(routes);
        automaticSelectionDemo(routes);
        comparisonDemo(routes);
        premiumStrategyDemo(routes);
        failureScenariosDemo(routes);
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

    private static void comparisonDemo(List<Route> routes) {
        System.out.println("=== STRATEGY COMPARISON MODE ===\n");

        DeliveryRequest request = new DeliveryRequest(105, DeliveryPriority.STANDARD, 6000, 60, false);
        System.out.println(StrategyComparator.withDefaultStrategies().report(routes, request));
    }


    private static void premiumStrategyDemo(List<Route> routes) {
        System.out.println("=== FIFTH STRATEGY: PREMIUM ===\n");

        StrategyComparator comparator = new StrategyComparator(List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(),
                new PremiumRouteStrategy()
        ));

        DeliveryRequest request = new DeliveryRequest(106, DeliveryPriority.STANDARD, 6000, 60, false);
        System.out.println(comparator.report(routes, request));
    }

    private static void failureScenariosDemo(List<Route> routes) {
        System.out.println("=== FAILURE SCENARIOS ===\n");
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        attempt("1. Empty route list",
                () -> planner.plan(List.of(),
                        new DeliveryRequest(301, DeliveryPriority.STANDARD, 6000, 60, false)));

        attempt("2. No route fits the budget (100)",
                () -> planner.plan(routes,
                        new DeliveryRequest(302, DeliveryPriority.URGENT, 100, 60, false)));

        attempt("3. Balanced weights sum to 0.9",
                () -> new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.1));

        attempt("4. Negative route cost",
                () -> new Route("X", routes.get(0).origin(), routes.get(0).destination(),
                        10, 20, -5, 1.0, 0, true));

        attempt("5. Missing delivery priority",
                () -> new DeliveryRequest(303, null, 6000, 60, false));
    }

    private static void attempt(String title, Runnable action) {
        System.out.println(title);
        try {
            action.run();
            System.out.println("  (no error, unexpected!)");
        } catch (NoSuitableRouteException e) {
            System.out.println("  NoSuitableRouteException: " + e.getMessage());
            e.getRejectedRoutes().forEach((id, why) ->
                    System.out.println("    Route " + id + ": " + why));
        } catch (IllegalArgumentException e) {
            System.out.println("  IllegalArgumentException: " + e.getMessage());
        }
        System.out.println();
    }
}
package kz.delivery;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;
import kz.delivery.planner.RoutePlanner;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();
        DeliveryRequest request = new DeliveryRequest(101, DeliveryPriority.STANDARD, 6000, 60, false);

        System.out.println("=== RUNTIME STRATEGY SWITCHING (request #" + request.id() + ") ===\n");

        // Один и тот же planner, алгоритм меняется на лету
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new CheapestRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());

        planner.setStrategy(new BalancedRouteStrategy());
        System.out.println(planner.plan(routes, request).explain());
    }
}

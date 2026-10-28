package kz.delivery.strategy;

import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Route;

import java.util.List;


public interface RouteStrategy {

    /**
     * @throws kz.delivery.exception.NoSuitableRouteException
     */
    RouteDecision selectRoute(List<Route> routes, DeliveryRequest request);

    String getStrategyName();
}

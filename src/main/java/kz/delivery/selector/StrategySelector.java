package kz.delivery.selector;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.strategy.BalancedRouteStrategy;
import kz.delivery.strategy.CheapestRouteStrategy;
import kz.delivery.strategy.EcoFriendlyRouteStrategy;
import kz.delivery.strategy.FastestRouteStrategy;
import kz.delivery.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class StrategySelector {

    private final List<SelectionRule> rules = new ArrayList<>();
    private final RouteStrategy defaultStrategy;

    public StrategySelector(RouteStrategy defaultStrategy) {
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "Default strategy must not be null");
    }


    public StrategySelector addRule(SelectionRule rule) {
        rules.add(Objects.requireNonNull(rule, "Rule must not be null"));
        return this;
    }

    public RouteStrategy select(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request must not be null");
        }
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .findFirst()
                .map(SelectionRule::strategy)
                .orElse(defaultStrategy);
    }


    public static StrategySelector withDefaultRules() {
        return new StrategySelector(new BalancedRouteStrategy())
                .addRule(SelectionRule.forPriority(DeliveryPriority.URGENT, new FastestRouteStrategy()))
                .addRule(new SelectionRule("ecoPreferred == true",
                        DeliveryRequest::ecoPreferred, new EcoFriendlyRouteStrategy()))
                .addRule(SelectionRule.forPriority(DeliveryPriority.ECO, new EcoFriendlyRouteStrategy()))
                .addRule(SelectionRule.forPriority(DeliveryPriority.ECONOMY, new CheapestRouteStrategy()))
                .addRule(SelectionRule.forPriority(DeliveryPriority.STANDARD, new BalancedRouteStrategy()));
    }
}

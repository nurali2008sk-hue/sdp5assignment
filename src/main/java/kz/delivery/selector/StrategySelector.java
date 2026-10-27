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

/**
 * Решает, КАКУЮ стратегию применить к запросу.
 * Правила проверяются по порядку добавления, побеждает первое подходящее.
 * Если ни одно не подошло, используется стратегия по умолчанию.
 */
public class StrategySelector {

    private final List<SelectionRule> rules = new ArrayList<>();
    private final RouteStrategy defaultStrategy;

    public StrategySelector(RouteStrategy defaultStrategy) {
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "Default strategy must not be null");
    }

    /** Добавляет правило в конец списка (порядок = приоритет). */
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

    /**
     * Набор правил по умолчанию:
     * URGENT -> Fastest; ecoPreferred -> Eco; ECO -> Eco; ECONOMY -> Cheapest; иначе Balanced.
     * URGENT проверяется первым, чтобы срочная доставка не подменялась эко-предпочтением.
     */
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

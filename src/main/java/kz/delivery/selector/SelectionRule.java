package kz.delivery.selector;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.strategy.RouteStrategy;

import java.util.function.Predicate;

/** Бизнес-правило: если запрос удовлетворяет условию, используем эту стратегию. */
public record SelectionRule(
        String description,
        Predicate<DeliveryRequest> condition,
        RouteStrategy strategy
) {

    public SelectionRule {
        if (description == null || condition == null || strategy == null) {
            throw new IllegalArgumentException("Rule description, condition and strategy are required");
        }
    }

    public boolean matches(DeliveryRequest request) {
        return condition.test(request);
    }

    /** Типовое правило: приоритет запроса, затем стратегия. */
    public static SelectionRule forPriority(DeliveryPriority priority, RouteStrategy strategy) {
        return new SelectionRule(
                "priority == " + priority,
                request -> request.priority() == priority,
                strategy);
    }
}

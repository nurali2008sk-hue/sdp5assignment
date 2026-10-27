package kz.delivery.domain;

public record DeliveryRequest(
        int id,
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        double maxCarbonEmission,   // лимит выбросов, кг CO2
        boolean ecoPreferred
) {

    public DeliveryRequest {
        if (priority == null) {
            throw new IllegalArgumentException("Delivery priority is required");
        }
        if (maxBudget <= 0) {
            throw new IllegalArgumentException("Max budget must be positive: " + maxBudget);
        }
        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException("Max delivery minutes must be positive: " + maxDeliveryMinutes);
        }
        if (maxCarbonEmission <= 0) {
            throw new IllegalArgumentException("Max carbon emission must be positive: " + maxCarbonEmission);
        }
    }

    /** Удобный конструктор: без лимита выбросов. */
    public DeliveryRequest(int id, DeliveryPriority priority, double maxBudget,
                           int maxDeliveryMinutes, boolean ecoPreferred) {
        this(id, priority, maxBudget, maxDeliveryMinutes, Double.MAX_VALUE, ecoPreferred);
    }
}

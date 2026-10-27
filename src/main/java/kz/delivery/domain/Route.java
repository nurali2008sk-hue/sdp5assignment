package kz.delivery.domain;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,      // кг CO2
        int trafficDelayMinutes,    // штраф за пробки, прибавляется ко времени
        boolean available            // false = маршрут закрыт или недоступен
) {

    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route id must not be blank");
        }
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Route must have origin and destination");
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance must not be negative: " + distanceKm);
        }
        if (estimatedMinutes < 0) {
            throw new IllegalArgumentException("Estimated minutes must not be negative: " + estimatedMinutes);
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost must not be negative: " + cost);
        }
        if (carbonEmission < 0) {
            throw new IllegalArgumentException("Carbon emission must not be negative: " + carbonEmission);
        }
        if (trafficDelayMinutes < 0) {
            throw new IllegalArgumentException("Traffic delay must not be negative: " + trafficDelayMinutes);
        }
    }

    /** Реальное время в пути с учётом пробок. */
    public int totalMinutes() {
        return estimatedMinutes + trafficDelayMinutes;
    }
}
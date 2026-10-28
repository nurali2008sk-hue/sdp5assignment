package kz.delivery;

import kz.delivery.domain.DeliveryPriority;
import kz.delivery.domain.DeliveryRequest;
import kz.delivery.domain.Location;
import kz.delivery.domain.Route;

public final class TestData {

    public static final Location FROM = new Location("From", 43.0, 76.0);
    public static final Location TO = new Location("To", 43.1, 76.1);

    private TestData() {
    }

    public static Route route(String id, double km, int minutes, double cost,
                              double co2, int traffic, boolean available) {
        return new Route(id, FROM, TO, km, minutes, cost, co2, traffic, available);
    }

    public static DeliveryRequest request(DeliveryPriority priority, double budget, int maxMinutes) {
        return new DeliveryRequest(1, priority, budget, maxMinutes, false);
    }
}
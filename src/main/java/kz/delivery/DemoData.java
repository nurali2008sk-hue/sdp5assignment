package kz.delivery;

import kz.delivery.domain.Location;
import kz.delivery.domain.Route;

import java.util.List;

public final class DemoData {

    private static final Location WAREHOUSE = new Location("Central Warehouse", 43.2220, 76.8512);
    private static final Location CUSTOMER = new Location("Customer, Dostyk Plaza", 43.2338, 76.9570);

    private DemoData() {
    }

    public static List<Route> routes() {
        return List.of(
                //    id   from       to        km    min  cost   co2  traffic available
                route("A", 12.0, 30, 3200, 2.4, 6, true),
                route("B", 9.5, 22, 4800, 3.1, 5, true),    // быстрый, дорогой
                route("C", 15.0, 45, 2200, 1.9, 10, true),
                route("D", 18.0, 55, 1500, 2.8, 3, true),   // самый дешёвый, медленный
                route("E", 11.0, 48, 2600, 0.9, 2, true),   // самый экологичный
                route("F", 8.0, 15, 7500, 4.5, 0, true),    // самый быстрый, премиум-цена
                route("G", 10.0, 20, 3000, 2.0, 4, false),  // выглядит отлично, но недоступен
                route("H", 13.5, 33, 3000, 1.2, 5, true)    // хороший компромисс
        );
    }

    private static Route route(String id, double km, int minutes, double cost,
                               double co2, int traffic, boolean available) {
        return new Route(id, WAREHOUSE, CUSTOMER, km, minutes, cost, co2, traffic, available);
    }
}
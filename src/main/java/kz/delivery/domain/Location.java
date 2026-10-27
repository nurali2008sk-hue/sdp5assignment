package kz.delivery.domain;

public record Location(String name, double latitude, double longitude) {

    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be blank");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be in [-90, 90]: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be in [-180, 180]: " + longitude);
        }
    }
}

package com.casrusil.siierpai.modules.energy.domain.model;

import java.util.Objects;

/**
 * Value Object: Ubicación geográfica de una estación de carga.
 * Incluye coordenadas y dirección física.
 */
public final class StationLocation {

    private final Double latitude;
    private final Double longitude;
    private final String address;
    private final String city;
    private final String country;

    private StationLocation(Double latitude, Double longitude, String address, String city, String country) {
        validateCoordinates(latitude, longitude);
        validateNotBlank(address, "La dirección");
        validateNotBlank(city, "La ciudad");
        validateNotBlank(country, "El país");

        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address.trim();
        this.city = city.trim();
        this.country = country.trim();
    }

    public static StationLocation of(Double latitude, Double longitude, String address, String city, String country) {
        return new StationLocation(latitude, longitude, address, city, country);
    }

    /**
     * Calcula la distancia aproximada en kilómetros a otra ubicación usando la
     * fórmula de Haversine.
     */
    public double distanceToKm(StationLocation other) {
        final int EARTH_RADIUS_KM = 6371;

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private void validateCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser nulas");
        }
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " no puede estar vacío");
        }
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StationLocation that = (StationLocation) o;
        return Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude) &&
                Objects.equals(address, that.address) &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address, city, country);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s [%.6f, %.6f]", address, city, country, latitude, longitude);
    }
}

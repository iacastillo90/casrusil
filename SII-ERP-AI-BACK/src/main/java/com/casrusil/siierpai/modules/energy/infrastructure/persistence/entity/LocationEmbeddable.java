package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Embeddable JPA: Ubicación geográfica.
 * Mapea el Value Object StationLocation del dominio.
 */
@Embeddable
public class LocationEmbeddable {

    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String country;

    protected LocationEmbeddable() {
        // Constructor vacío requerido por JPA
    }

    public LocationEmbeddable(Double latitude, Double longitude, String address, String city, String country) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.country = country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LocationEmbeddable that = (LocationEmbeddable) o;
        return Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address);
    }
}

package com.example.emergencyapi.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConvertToCoordinatesApiResponse(
        String words,
        Coordinates coordinates,
        String country,
        ErrorDetails error
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Coordinates(double lat, double lng) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetails(String code, String message) {
    }
}

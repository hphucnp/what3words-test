package com.example.emergencyapi.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConvertTo3waApiResponse(
        String words,
        String country,
        ErrorDetails error
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetails(String code, String message) {
    }
}

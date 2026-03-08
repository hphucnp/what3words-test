package com.example.emergencyapi.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutosuggestApiResponse(List<SuggestionItem> suggestions, ErrorDetails error) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SuggestionItem(String country, String nearestPlace, String words) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetails(String code, String message) {
    }
}

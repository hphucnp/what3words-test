package com.example.emergencyapi.client;

import com.example.emergencyapi.config.What3WordsProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class What3WordsClient {

    private final RestTemplate restTemplate;
    private final What3WordsProperties properties;

    public What3WordsClient(RestTemplate restTemplate, What3WordsProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public ConvertTo3waApiResponse convertTo3wa(double lat, double lng, String language) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/convert-to-3wa")
                .queryParam("coordinates", lat + "," + lng)
                .queryParam("key", properties.getKey())
                .queryParamIfPresent("language", java.util.Optional.ofNullable(language))
                .toUriString();
        return get(url, ConvertTo3waApiResponse.class);
    }

    public ConvertToCoordinatesApiResponse convertToCoordinates(String words) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/convert-to-coordinates")
                .queryParam("words", words)
                .queryParam("key", properties.getKey())
                .toUriString();
        return get(url, ConvertToCoordinatesApiResponse.class);
    }

    public AutosuggestApiResponse autosuggest(String input, String language, String clipToCountry, int nResults) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/autosuggest")
                .queryParam("input", input)
                .queryParam("key", properties.getKey())
                .queryParamIfPresent("language", java.util.Optional.ofNullable(language))
                .queryParamIfPresent("clip-to-country", java.util.Optional.ofNullable(clipToCountry))
                .queryParam("n-results", nResults)
                .toUriString();
        return get(url, AutosuggestApiResponse.class);
    }

    private <T> T get(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (RestClientException e) {
            throw new What3WordsClientException("Failed to call what3words API", e);
        }
    }

    public static class What3WordsClientException extends RuntimeException {
        public What3WordsClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

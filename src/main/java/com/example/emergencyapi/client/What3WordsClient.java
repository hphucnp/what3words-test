package com.example.emergencyapi.client;

import com.example.emergencyapi.config.What3WordsProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class What3WordsClient {

    private final RestTemplate restTemplate;
    private final What3WordsProperties properties;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitOpenedAtEpochMillis = -1L;

    public What3WordsClient(RestTemplate restTemplate, What3WordsProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public ConvertTo3waApiResponse convertTo3wa(double lat, double lng, String language) {
        String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/convert-to-3wa")
                .queryParam("coordinates", lat + "," + lng)
                .queryParam("key", properties.getKey())
                .queryParamIfPresent("language", java.util.Optional.ofNullable(language))
                .toUriString();
        return get(url, ConvertTo3waApiResponse.class);
    }

    public ConvertToCoordinatesApiResponse convertToCoordinates(String words) {
        String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/convert-to-coordinates")
                .queryParam("words", words)
                .queryParam("key", properties.getKey())
                .toUriString();
        return get(url, ConvertToCoordinatesApiResponse.class);
    }

    public AutosuggestApiResponse autosuggest(String input, String language, String clipToCountry, int nResults) {
        String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/autosuggest")
                .queryParam("input", input)
                .queryParam("key", properties.getKey())
                .queryParamIfPresent("language", java.util.Optional.ofNullable(language))
                .queryParamIfPresent("clip-to-country", java.util.Optional.ofNullable(clipToCountry))
                .queryParam("n-results", nResults)
                .toUriString();
        return get(url, AutosuggestApiResponse.class);
    }

    private <T> T get(String url, Class<T> responseType) {
        if (isCircuitOpen()) {
            throw new What3WordsClientException("what3words circuit breaker is open", null);
        }

        RestClientException lastError = null;
        int maxAttempts = Math.max(1, properties.getRetryMaxAttempts());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T response = restTemplate.getForObject(url, responseType);
                markSuccess();
                return response;
            } catch (RestClientException ex) {
                lastError = ex;
                if (!isRetryable(ex) || attempt == maxAttempts) {
                    break;
                }
                backoff();
            }
        }

        markFailure();
        throw new What3WordsClientException("Failed to call what3words API", lastError);
    }

    private boolean isRetryable(RestClientException ex) {
        if (ex instanceof HttpStatusCodeException statusCodeException) {
            return statusCodeException.getStatusCode().is5xxServerError()
                    || statusCodeException.getStatusCode().value() == 429;
        }
        return true;
    }

    private void backoff() {
        long sleepMillis = Math.max(0L, properties.getRetryBackoffMillis());
        if (sleepMillis == 0L) {
            return;
        }
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isCircuitOpen() {
        long openedAt = circuitOpenedAtEpochMillis;
        if (openedAt < 0) {
            return false;
        }

        if ((System.currentTimeMillis() - openedAt) >= properties.getCircuitBreakerOpenMillis()) {
            circuitOpenedAtEpochMillis = -1L;
            consecutiveFailures.set(0);
            return false;
        }
        return true;
    }

    private void markSuccess() {
        consecutiveFailures.set(0);
        circuitOpenedAtEpochMillis = -1L;
    }

    private void markFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= Math.max(1, properties.getCircuitBreakerFailureThreshold())) {
            circuitOpenedAtEpochMillis = System.currentTimeMillis();
        }
    }

    public static class What3WordsClientException extends RuntimeException {
        public What3WordsClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

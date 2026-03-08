package com.example.emergencyapi.service;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.emergencyapi.client.AutosuggestApiResponse;
import com.example.emergencyapi.client.ConvertTo3waApiResponse;
import com.example.emergencyapi.client.ConvertToCoordinatesApiResponse;
import com.example.emergencyapi.client.What3WordsClient;
import com.example.emergencyapi.client.What3WordsClient.What3WordsClientException;
import com.example.emergencyapi.dto.CoordinatesResponse;
import com.example.emergencyapi.dto.Suggestion;
import com.example.emergencyapi.exception.BadRequestException;
import com.example.emergencyapi.exception.NotRecognizedWithSuggestionsException;

@Service
public class EmergencyService {

    private static final Pattern THREE_WORD_FORMAT = Pattern.compile("^[^.\\s]+\\.[^.\\s]+\\.[^.\\s]+$");

    private final What3WordsClient what3WordsClient;

    public EmergencyService(What3WordsClient what3WordsClient) {
        this.what3WordsClient = what3WordsClient;
    }

    public String coordinatesTo3wa(double lat, double lng) {
        ConvertTo3waApiResponse response = what3WordsClient.convertTo3wa(lat, lng, null);
        if (response == null || response.words() == null || response.words().isBlank()) {
            throw new BadRequestException("Coordinates supplied do not convert to a 3wa");
        }
        return response.words();
    }

    public CoordinatesResponse threeWaToCoordinates(String words) {
        validateThreeWordFormatOrSuggest(words);

        ConvertToCoordinatesApiResponse response = safeConvertToCoordinates(words);
        if (response == null || response.coordinates() == null) {
            throwNotRecognizedWithSuggestions(words);
        }

        if (!"GB".equalsIgnoreCase(response.country())) {
            throwNotRecognizedWithSuggestions(words);
        }

        return new CoordinatesResponse(response.coordinates().lat(), response.coordinates().lng());
    }

    public String languageConvert(String words, String targetLanguage) {
        validateThreeWordFormatOrSuggest(words);

        ConvertToCoordinatesApiResponse coordinatesResponse = safeConvertToCoordinates(words);
        if (coordinatesResponse == null || coordinatesResponse.coordinates() == null) {
            throwNotRecognizedWithSuggestions(words);
        }
        if (!"GB".equalsIgnoreCase(coordinatesResponse.country())) {
            throwNotRecognizedWithSuggestions(words);
        }

        double lat = coordinatesResponse.coordinates().lat();
        double lng = coordinatesResponse.coordinates().lng();
        ConvertTo3waApiResponse convertedResponse = what3WordsClient.convertTo3wa(lat, lng, targetLanguage);

        if (convertedResponse == null || convertedResponse.words() == null || convertedResponse.words().isBlank()) {
            throw new BadRequestException("Unable to convert 3wa to target language: " + targetLanguage);
        }
        return convertedResponse.words();
    }

    private void validateThreeWordFormatOrSuggest(String words) {
        if (words == null || !THREE_WORD_FORMAT.matcher(words.trim()).matches()) {
            List<Suggestion> suggestions = buildUkSuggestions(words);
            if (!suggestions.isEmpty()) {
                throw new NotRecognizedWithSuggestionsException("3wa not recognised: " + words, suggestions);
            }
            throw new BadRequestException("3wa address supplied has invalid format");
        }
    }

    private void throwNotRecognizedWithSuggestions(String words) {
        List<Suggestion> suggestions = buildUkSuggestions(words);
        throw new NotRecognizedWithSuggestionsException("3wa not recognised: " + words, suggestions);
    }

    private List<Suggestion> buildUkSuggestions(String words) {
        AutosuggestApiResponse suggestionsResponse;
        try {
            suggestionsResponse = what3WordsClient.autosuggest(words, null, "GB", 3);
        } catch (What3WordsClientException e) {
            return Collections.emptyList();
        }

        if (suggestionsResponse == null || suggestionsResponse.suggestions() == null) {
            return Collections.emptyList();
        }

        return suggestionsResponse.suggestions().stream()
                .filter(item -> "GB".equalsIgnoreCase(item.country()))
                .limit(3)
                .map(item -> new Suggestion(item.country(), item.nearestPlace(), item.words()))
                .toList();
    }

    private ConvertToCoordinatesApiResponse safeConvertToCoordinates(String words) {
        try {
            return what3WordsClient.convertToCoordinates(words);
        } catch (What3WordsClientException e) {
            return null;
        }
    }
}

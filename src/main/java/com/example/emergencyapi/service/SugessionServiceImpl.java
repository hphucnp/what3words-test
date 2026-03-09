package com.example.emergencyapi.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.emergencyapi.client.AutosuggestApiResponse;
import com.example.emergencyapi.client.What3WordsClient;
import com.example.emergencyapi.client.What3WordsClient.What3WordsClientException;
import com.example.emergencyapi.dto.Suggestion;

@Service
public class SugessionServiceImpl implements SugessionService {

    private final What3WordsClient what3WordsClient;

    public SugessionServiceImpl(What3WordsClient what3WordsClient) {
        this.what3WordsClient = what3WordsClient;
    }

    @Override
    public List<Suggestion> buildUkSuggestions(String words) {
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
}

package com.example.emergencyapi.exception;

import com.example.emergencyapi.dto.Suggestion;

import java.util.List;

public class NotRecognizedWithSuggestionsException extends RuntimeException {

    private final List<Suggestion> suggestions;

    public NotRecognizedWithSuggestionsException(String message, List<Suggestion> suggestions) {
        super(message);
        this.suggestions = suggestions;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
}

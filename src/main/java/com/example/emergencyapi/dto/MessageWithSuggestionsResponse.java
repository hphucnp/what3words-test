package com.example.emergencyapi.dto;

import java.util.List;

public record MessageWithSuggestionsResponse(String message, List<Suggestion> suggestions) {
}

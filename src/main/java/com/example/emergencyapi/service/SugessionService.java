package com.example.emergencyapi.service;

import java.util.List;

import com.example.emergencyapi.dto.Suggestion;

public interface SugessionService {

    List<Suggestion> buildUkSuggestions(String words);
}

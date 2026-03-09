package com.example.emergencyapi.service;

import com.example.emergencyapi.dto.CoordinatesResponse;

public interface EmergencyService {

    String coordinatesTo3wa(double lat, double lng);

    CoordinatesResponse threeWaToCoordinates(String words);

    String languageConvert(String words, String targetLanguage);
}

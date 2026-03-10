package com.example.emergencyapi.controller;

import com.example.emergencyapi.dto.CoordinatesResponse;
import com.example.emergencyapi.dto.ThreeWordAddressResponse;
import com.example.emergencyapi.exception.BadRequestException;
import com.example.emergencyapi.service.EmergencyService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/emergencyapi")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @GetMapping("/coord-to-3wa")
    public ThreeWordAddressResponse convertCoordinatesToThreeWords(
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lng
    ) {
        double parsedLat = parseCoordinate(lat);
        double parsedLng = parseCoordinate(lng);
        return new ThreeWordAddressResponse(emergencyService.coordinatesTo3wa(parsedLat, parsedLng));
    }

    private double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Coordinates supplied do not convert to a 3wa");
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Coordinates supplied do not convert to a 3wa");
        }
    }

    @GetMapping("/3wa-to-coord")
    public CoordinatesResponse convertThreeWordsToCoordinates(
            @RequestParam("3wa") @NotBlank String threeWordAddress
    ) {
        return emergencyService.threeWaToCoordinates(threeWordAddress);
    }

    @GetMapping("/language-convert")
    public ThreeWordAddressResponse convertLanguage(
            @RequestParam("3wa") @NotBlank String threeWordAddress,
            @RequestParam("target_language") @NotBlank String targetLanguage
    ) {
        return new ThreeWordAddressResponse(emergencyService.languageConvert(threeWordAddress, targetLanguage));
    }
}

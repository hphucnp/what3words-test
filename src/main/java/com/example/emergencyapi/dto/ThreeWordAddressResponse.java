package com.example.emergencyapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThreeWordAddressResponse(@JsonProperty("3wa") String threeWordAddress) {
}

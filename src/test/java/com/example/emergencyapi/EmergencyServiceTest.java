package com.example.emergencyapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.emergencyapi.client.ConvertTo3waApiResponse;
import com.example.emergencyapi.client.ConvertToCoordinatesApiResponse;
import com.example.emergencyapi.client.What3WordsClient;
import com.example.emergencyapi.client.What3WordsClient.What3WordsClientException;
import com.example.emergencyapi.dto.Suggestion;
import com.example.emergencyapi.exception.BadRequestException;
import com.example.emergencyapi.exception.NotRecognizedWithSuggestionsException;
import com.example.emergencyapi.service.EmergencyService;
import com.example.emergencyapi.service.EmergencyServiceImpl;
import com.example.emergencyapi.service.SugessionService;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

        @Mock
        private What3WordsClient what3WordsClient;

        @Mock
        private SugessionService sugessionService;

        private EmergencyService emergencyService;

        @BeforeEach
        void setUp() {
                emergencyService = new EmergencyServiceImpl(what3WordsClient, sugessionService);
        }

        @Test
        void coordinatesTo3waSuccess() {
                when(what3WordsClient.convertTo3wa(51.508341, -0.125499, null))
                                .thenReturn(new ConvertTo3waApiResponse("daring.lion.race", "GB", null));

                String result = emergencyService.coordinatesTo3wa(51.508341, -0.125499);

                assertEquals("daring.lion.race", result);
        }

        @Test
        void threeWaToCoordinatesInvalidFormatThrows() {
                when(sugessionService.buildUkSuggestions("daring.lion"))
                                .thenReturn(List.of());

                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> emergencyService.threeWaToCoordinates("daring.lion"));

                assertEquals("3wa address supplied has invalid format", exception.getMessage());
        }

        @Test
        void threeWaToCoordinatesInvalidFormatReturnsSuggestionsWhenAvailable() {
                when(sugessionService.buildUkSuggestions("filled.count.snapz"))
                                .thenReturn(List.of(
                                                new Suggestion("GB", "Bishops Cleeve, Gloucestershire",
                                                                "filled.count.snaps"),
                                                new Suggestion("GB", "Bayswater, London", "filled.count.soap"),
                                                new Suggestion("GB", "Wednesfield, West Midlands",
                                                                "filled.count.slap")));

                NotRecognizedWithSuggestionsException exception = assertThrows(
                                NotRecognizedWithSuggestionsException.class,
                                () -> emergencyService.threeWaToCoordinates("filled.count.snapz"));

                assertEquals("3wa not recognised: filled.count.snapz", exception.getMessage());
                assertEquals(3, exception.getSuggestions().size());
        }

        @Test
        void threeWaToCoordinatesOutsideUkReturnsSuggestions() {
                when(what3WordsClient.convertToCoordinates("index.home.raft"))
                                .thenReturn(new ConvertToCoordinatesApiResponse(
                                                "index.home.raft",
                                                new ConvertToCoordinatesApiResponse.Coordinates(40.7128, -74.0060),
                                                "US",
                                                null));

                when(sugessionService.buildUkSuggestions("index.home.raft"))
                                .thenReturn(List.of(
                                                new Suggestion("GB", "Bayswater, London", "index.home.graft"),
                                                new Suggestion("GB", "Reading, Berkshire", "index.home.craft"),
                                                new Suggestion("GB", "York, North Yorkshire", "index.home.draft")));

                NotRecognizedWithSuggestionsException exception = assertThrows(
                                NotRecognizedWithSuggestionsException.class,
                                () -> emergencyService.threeWaToCoordinates("index.home.raft"));

                assertEquals("3wa not recognised: index.home.raft", exception.getMessage());
                assertEquals(3, exception.getSuggestions().size());
        }

        @Test
        void languageConvertReturnsTranslatedThreeWordAddress() {
                when(what3WordsClient.convertToCoordinates("daring.lion.race"))
                                .thenReturn(new ConvertToCoordinatesApiResponse(
                                                "daring.lion.race",
                                                new ConvertToCoordinatesApiResponse.Coordinates(51.508341, -0.125499),
                                                "GB",
                                                null));

                when(what3WordsClient.convertTo3wa(51.508341, -0.125499, "cy"))
                                .thenReturn(new ConvertTo3waApiResponse("sychach.parciau.lwmpyn", "GB", null));

                String result = emergencyService.languageConvert("daring.lion.race", "cy");

                assertEquals("sychach.parciau.lwmpyn", result);
        }

        @Test
        void languageConvertUnknownReturnsSuggestionsWhenCoordinateLookupFails() {
                when(what3WordsClient.convertToCoordinates("filled.count.snapz"))
                                .thenThrow(new What3WordsClientException("Failed to call what3words API",
                                                new RuntimeException("4xx")));

                when(sugessionService.buildUkSuggestions("filled.count.snapz"))
                                .thenReturn(List.of(
                                                new Suggestion("GB", "Bishops Cleeve, Gloucestershire",
                                                                "filled.count.snaps"),
                                                new Suggestion("GB", "Bayswater, London", "filled.count.soap"),
                                                new Suggestion("GB", "Wednesfield, West Midlands",
                                                                "filled.count.slap")));

                NotRecognizedWithSuggestionsException exception = assertThrows(
                                NotRecognizedWithSuggestionsException.class,
                                () -> emergencyService.languageConvert("filled.count.snapz", "cy"));

                assertEquals("3wa not recognised: filled.count.snapz", exception.getMessage());
                assertEquals(3, exception.getSuggestions().size());
        }
}

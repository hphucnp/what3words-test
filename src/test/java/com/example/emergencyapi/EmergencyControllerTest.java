package com.example.emergencyapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.emergencyapi.controller.GlobalExceptionHandler;
import com.example.emergencyapi.dto.CoordinatesResponse;
import com.example.emergencyapi.exception.BadRequestException;
import com.example.emergencyapi.exception.NotRecognizedWithSuggestionsException;
import com.example.emergencyapi.service.EmergencyService;

@WebMvcTest
@Import(GlobalExceptionHandler.class)
class EmergencyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private EmergencyService emergencyService;

        @Test
        void coordTo3waReturnsThreeWordAddress() throws Exception {
                Mockito.when(emergencyService.coordinatesTo3wa(51.508341, -0.125499)).thenReturn("daring.lion.race");

                mockMvc.perform(get("/emergencyapi/coord-to-3wa")
                                .param("lat", "51.508341")
                                .param("lng", "-0.125499")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.3wa").value("daring.lion.race"));
        }

        @Test
        void coordTo3waInvalidCoordinatesReturnsMessage() throws Exception {
                mockMvc.perform(get("/emergencyapi/coord-to-3wa")
                                .param("lat", "")
                                .param("lng", "-0.125499")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Coordinates supplied do not convert to a 3wa"));
        }

        @Test
        void threeWaToCoordReturnsCoordinates() throws Exception {
                Mockito.when(emergencyService.threeWaToCoordinates("daring.lion.race"))
                                .thenReturn(new CoordinatesResponse(51.508341, -0.125499));

                mockMvc.perform(get("/emergencyapi/3wa-to-coord")
                                .param("3wa", "daring.lion.race")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lat").value(51.508341))
                                .andExpect(jsonPath("$.lng").value(-0.125499));
        }

        @Test
        void threeWaToCoordInvalidFormatReturnsMessage() throws Exception {
                Mockito.when(emergencyService.threeWaToCoordinates("daring.lion"))
                                .thenThrow(new BadRequestException("3wa address supplied has invalid format"));

                mockMvc.perform(get("/emergencyapi/3wa-to-coord")
                                .param("3wa", "daring.lion")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("3wa address supplied has invalid format"));
        }

        @Test
        void threeWaToCoordUnknownReturnsSuggestions() throws Exception {
                Mockito.when(emergencyService.threeWaToCoordinates("filled.count.snap"))
                                .thenThrow(new NotRecognizedWithSuggestionsException(
                                                "3wa not recognised: filled.count.snap",
                                                List.of(
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Bishops Cleeve, Gloucestershire",
                                                                                "filled.count.snaps"),
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Bayswater, London",
                                                                                "filled.count.soap"),
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Wednesfield, West Midlands",
                                                                                "filled.count.slap"))));

                mockMvc.perform(get("/emergencyapi/3wa-to-coord")
                                .param("3wa", "filled.count.snap")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("3wa not recognised: filled.count.snap"))
                                .andExpect(jsonPath("$.suggestions[0].country").value("GB"))
                                .andExpect(jsonPath("$.suggestions[0].words").value("filled.count.snaps"))
                                .andExpect(jsonPath("$.suggestions.length()").value(3));
        }

        @Test
        void languageConvertReturnsConvertedAddress() throws Exception {
                Mockito.when(emergencyService.languageConvert("daring.lion.race", "cy"))
                                .thenReturn("sychach.parciau.lwmpyn");

                mockMvc.perform(get("/emergencyapi/language-convert")
                                .param("3wa", "daring.lion.race")
                                .param("target_language", "cy")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.3wa").value("sychach.parciau.lwmpyn"));
        }

        @Test
        void languageConvertUnknownReturnsSuggestions() throws Exception {
                Mockito.when(emergencyService.languageConvert("filled.count.snapz", "cy"))
                                .thenThrow(new NotRecognizedWithSuggestionsException(
                                                "3wa not recognised: filled.count.snapz",
                                                List.of(
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Bishops Cleeve, Gloucestershire",
                                                                                "filled.count.snaps"),
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Bayswater, London",
                                                                                "filled.count.soap"),
                                                                new com.example.emergencyapi.dto.Suggestion("GB",
                                                                                "Wednesfield, West Midlands",
                                                                                "filled.count.slap"))));

                mockMvc.perform(get("/emergencyapi/language-convert")
                                .param("3wa", "filled.count.snapz")
                                .param("target_language", "cy")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("3wa not recognised: filled.count.snapz"))
                                .andExpect(jsonPath("$.suggestions[0].country").value("GB"))
                                .andExpect(jsonPath("$.suggestions[0].words").value("filled.count.snaps"))
                                .andExpect(jsonPath("$.suggestions.length()").value(3));
        }
}

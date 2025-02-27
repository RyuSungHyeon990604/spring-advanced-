package org.example.expert.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(WeatherClient.class)
class WeatherClientTest {

    @Autowired
    WeatherClient weatherClient;
    @Autowired
    private MockRestServiceServer mockServer;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getWeather_오늘날씨데이터를가져온다() throws JsonProcessingException {
        //given
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd"));

        WeatherDto w1 = new WeatherDto("02-25", "bad");
        WeatherDto w2 = new WeatherDto("02-26", "good");
        WeatherDto w3 = new WeatherDto(today, "today weather");
        WeatherDto[] weatherDtos = new WeatherDto[]{w1, w2, w3};
        String jsonResponse = objectMapper.writeValueAsString(weatherDtos);

        mockServer.expect(requestTo("https://f-api.github.io/f-api/weather.json"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        //when
        String todayWeather = weatherClient.getTodayWeather();

        //then
        assertEquals(todayWeather, "today weather");

    }

    @Test
    void getWeather_오늘날씨데이터를가져올때200이외응답발생시ServerException발생() throws JsonProcessingException {
        //given
        mockServer.expect(requestTo("https://f-api.github.io/f-api/weather.json"))
                .andRespond(withNoContent());

        //when & then
        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    void getWeather_빈날씨데이터를가져왔을때ServerException발생() throws JsonProcessingException {
        //given
        WeatherDto[] weatherDtos = new WeatherDto[]{};
        String jsonResponse = objectMapper.writeValueAsString(weatherDtos);

        mockServer.expect(requestTo("https://f-api.github.io/f-api/weather.json"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        //when & then
        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    void getWeather_가져온날씨데이터가null일때ServerException발생() throws JsonProcessingException {
        //given
        String jsonResponse = objectMapper.writeValueAsString(null);

        mockServer.expect(requestTo("https://f-api.github.io/f-api/weather.json"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        //when & then
        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    void getWeather_가져온날씨중오늘의날씨데이터가존재하지않을때() throws JsonProcessingException {
        //given
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd"));

        WeatherDto w1 = new WeatherDto("02-25", "bad");
        WeatherDto w2 = new WeatherDto("02-26", "good");
        WeatherDto w3 = new WeatherDto("01-01", "weather");
        WeatherDto[] weatherDtos = new WeatherDto[]{w1, w2, w3};
        String jsonResponse = objectMapper.writeValueAsString(weatherDtos);

        mockServer.expect(requestTo("https://f-api.github.io/f-api/weather.json"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        //when & then
        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());

    }
}
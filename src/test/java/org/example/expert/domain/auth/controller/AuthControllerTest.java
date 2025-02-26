package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService authService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void signUp_회원가입성공하면토큰반환() throws Exception {
        //given
        SignupRequest signupRequest = new SignupRequest("a@a.com","Passwd","user");

        String token = "token";
        SignupResponse signupResponse = new SignupResponse(token);

        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);
        //when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest))
        );
        //then

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value(token));
    }

    @ParameterizedTest
    @CsvSource({
        "wrongEmail, Password, user",
            ",Password,user",
            "a@a.com,,user",
            "a@a.com,Password,",
    })
    void signUp_잘못된형식의정보로회원가입을할수없다(String email,String password, String role) throws Exception {
        //given
        SignupRequest signupRequest = new SignupRequest(email,password,role);

        String token = "token";
        SignupResponse signupResponse = new SignupResponse(token);

        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);
        //when
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
        );
        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void signin_로그인성공하면토큰을반환한다() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "Password");
        SigninResponse signinResponse = new SigninResponse("token");

        given(authService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("token"));
    }

    @Test
    void signin_AuthException발생시401반환() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "wrongPassword");

        given(authService.signin(any(SigninRequest.class)))
                .willThrow(AuthException.class);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest)));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void signin_InvalidRequestException발생시400반환() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "Password");

        given(authService.signin(any(SigninRequest.class)))
                .willThrow(InvalidRequestException.class);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }


}
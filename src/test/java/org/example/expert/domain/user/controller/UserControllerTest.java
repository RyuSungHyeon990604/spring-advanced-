package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void getUser_유저정보를가져온다() throws Exception {
        //given
        long userId = 1L;
        String userEmail = "user@example.com";
        UserResponse userResponse = new UserResponse(userId, userEmail);

        given(userService.getUser(userId)).willReturn(userResponse);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId));
        //then
        resultActions.andDo(print())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(userEmail));
    }

    @Test
    void getUser_userId에해당하는유가없다면400에러발생() throws Exception {
        //given
        long userId = 1L;
        String userEmail = "user@example.com";

        given(userService.getUser(userId)).willThrow(InvalidRequestException.class);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId));
        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void changePassword_비밀번호를변경한다() throws Exception {
        //given
        long userId = 1L;

        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("oldPassword", "newPassword1");
        doNothing().when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userChangePasswordRequest))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "000",
            "lowercase",
            "AAAA",
            "AaAaAaAaAaAa",
            "Abcd123"
    })
    void changePassword_새로운비밀번호가조건에맞지않을때400발생(String newPassword) throws Exception {
        //given
        long userId = 1L;

        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("oldPassword", newPassword);
        doNothing().when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userChangePasswordRequest))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_로직수행중InvalidRequestException발생했을때400응답반환() throws Exception {
        //given
        long userId = 1L;

        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("oldPassword", "newPassword1");
        doThrow(InvalidRequestException.class).when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userChangePasswordRequest))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }
}
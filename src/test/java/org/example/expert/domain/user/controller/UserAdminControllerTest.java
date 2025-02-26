package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserAdminService userAdminService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void changeUserRole_유저의권한을변경한다() throws Exception {
        //given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("admin");
        doNothing().when(userAdminService).changeUserRole(anyLong(), any(UserRoleChangeRequest.class));
        //when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest))
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void changeUserRole_InvalidRequestException발생했을때400응답반환() throws Exception {
        //given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("admin");
        doThrow(InvalidRequestException.class).when(userAdminService).changeUserRole(anyLong(), any(UserRoleChangeRequest.class));
        //when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest))
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

}
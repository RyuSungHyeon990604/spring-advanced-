package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ManagerService managerService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void saveManager_담당자를지정한다() throws Exception {
        //given
        long userId = 1L;

        long managerUserId = 2L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        long managerId = 1L;
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(managerId, new UserResponse(managerUserId, "b@a,com"));

        long todoId = 1L;
        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class))).willReturn(managerSaveResponse);

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos/{todoId}/managers", todoId)
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest))
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(managerId))
                .andExpect(jsonPath("$.user.id").value(managerUserId));
    }

    @Test
    void saveManager_담당자를지정중InvalidRequestException발생시400응답반환() throws Exception {
        //given
        long userId = 1L;

        long managerUserId = 2L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        long todoId = 1L;
        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class))).willThrow(InvalidRequestException.class);

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos/{todoId}/managers", todoId)
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest))
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void getMembers_일정의담당자목록을조회한다() throws Exception {
        //given
        long todoId = 1L;

        ManagerResponse managerResponse1 = new ManagerResponse(1L, new UserResponse(1L, "b@a.com"));
        ManagerResponse managerResponse2 = new ManagerResponse(2L, new UserResponse(2L, "c@a.com"));
        ManagerResponse managerResponse3 = new ManagerResponse(3L, new UserResponse(3L, "d@a.com"));
        List<ManagerResponse> managerResponseList = List.of(managerResponse1, managerResponse2, managerResponse3);

        given(managerService.getManagers(todoId)).willReturn(managerResponseList);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos/{todoId}/managers", todoId)
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].user.id").value(1L))
                .andExpect(jsonPath("$[0].user.email").value("b@a.com"))
                .andExpect(jsonPath("$[2].id").value(3L));
    }

    @Test
    void getMembers_조회하는도중InvalidRequestException이발생했을때400응답반환() throws Exception {
        //given
        long todoId = 1L;

        given(managerService.getManagers(todoId)).willThrow(InvalidRequestException.class);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos/{todoId}/managers", todoId)
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteManager_일정에서담당자를제거한다() throws Exception {
        //given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 2L;

        doNothing().when(managerService).deleteManager(anyLong(), anyLong(), anyLong());
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/todos/{todoId}/managers/{managerId}",todoId, managerId)
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );
        //then

        resultActions.andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void deleteManager_일정에서담당자를제거하는중에InvalidRequestException이발생하면400응답반환() throws Exception {
        //given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 2L;

        doThrow(InvalidRequestException.class).when(managerService).deleteManager(anyLong(), anyLong(), anyLong());
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/todos/{todoId}/managers/{managerId}",todoId, managerId)
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );
        //then

        resultActions.andDo(print())
                .andExpect(status().isBadRequest());

    }

}


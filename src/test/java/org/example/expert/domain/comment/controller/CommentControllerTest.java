package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void saveComment_댓글을저장한다() throws Exception {
        //given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");

        long userId = 1L;
        String userEmail = "a@a.com";
        UserResponse userResponse = new UserResponse(userId, userEmail);

        long commentId = 1L;
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(commentId, "contents", userResponse);


        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class))).willReturn(commentSaveResponse);

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos/{todoId}/comments",todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.contents").value("contents"))
                .andExpect(jsonPath("$.user.id").value(userId));
    }

    @Test
    void saveComment_댓글을저장하는도중InvalidRequestException발생시BadRequest반환() throws Exception {
        //given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");

        long userId = 1L;

        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class))).willThrow(InvalidRequestException.class);

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos/{todoId}/comments", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveComment_contents가공백일때400반환() throws Exception {
        //given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("");

        long userId = 1L;
        String userEmail = "a@a.com";
        UserResponse userResponse = new UserResponse(userId, userEmail);

        long commentId = 1L;
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(commentId, "contents", userResponse);


        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class))).willReturn(commentSaveResponse);

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos/{todoId}/comments",todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", userId)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_댓글목록을가져온다() throws Exception {
        //given
        long todoId = 1L;
        CommentResponse commentResponse1 = new CommentResponse(1L, "contents", new UserResponse(1L, "a@a.com"));
        CommentResponse commentResponse2 = new CommentResponse(2L, "contents", new UserResponse(1L, "a@a.com"));
        List<CommentResponse> commentResponseList = List.of(commentResponse1, commentResponse2);

        given(commentService.getComments(todoId)).willReturn(commentResponseList);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos/{todoId}/comments", todoId)
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

}
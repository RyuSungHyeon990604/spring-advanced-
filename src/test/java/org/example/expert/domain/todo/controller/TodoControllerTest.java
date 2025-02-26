package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
class TodoControllerTest {
    @MockBean
    private TodoService todoService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void saveTodo_일정을등록한다() throws Exception {
        //given
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");

        UserResponse userResponse = new UserResponse(1L, "a@a.com");
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L, todoSaveRequest.getTitle(), todoSaveRequest.getContents(), "weather", userResponse);

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(todoSaveResponse);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveRequest))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(todoSaveRequest.getTitle()))
                .andExpect(jsonPath("$.user.id").value(userResponse.getId()));
    }

    @Test
    void saveTodo_일정내용또는제목이비어있으면400응답반환() throws Exception {
        //given
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("", "");

        UserResponse userResponse = new UserResponse(1L, "a@a.com");
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L, todoSaveRequest.getTitle(), todoSaveRequest.getContents(), "weather", userResponse);

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(todoSaveResponse);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveRequest))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "a@a.com")
                        .requestAttr("userRole", "user")
        );
        //then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTodos_일정리스트를반환한다() throws Exception {
        //given
        UserResponse userResponse = new UserResponse(1L, "a@a.com");

        int page = 1;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        LocalDateTime now = LocalDateTime.now();
        TodoResponse todoResponse1 = new TodoResponse(1L, "title1", "contents1", "weather1", userResponse, now, now);
        TodoResponse todoResponse2 = new TodoResponse(2L, "title2", "contents2", "weather2", userResponse, now, now);
        TodoResponse todoResponse3 = new TodoResponse(3L, "title3", "contents3", "weather3", userResponse, now, now);
        List<TodoResponse> todoResponseList = List.of(todoResponse1, todoResponse2, todoResponse3);

        Page<TodoResponse> todoResponsePage = new PageImpl<>(todoResponseList, pageRequest, todoResponseList.size());

        given(todoService.getTodos(page, size)).willReturn(todoResponsePage);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].user.id").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[2].id").value(3L));
    }

    @Test
    void getTodos_페이지번호와사이즈가양수가아닐때400응답반환() throws Exception {

        //when
        //jakarta.validation.ConstraintViolationException 프록시객체 예외감지 못함?
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(0))
        );

        //then
        resultActions.andDo(print())
                .andExpect(status().is4xxClientError());

    }

    @Test
    void getTodo_일정을조회한다() throws Exception {
        //given
        long todoId = 1L;
        TodoResponse todoResponse = new TodoResponse(1L, "title", "contents", "weather", null, LocalDateTime.now(), LocalDateTime.now());
        given(todoService.getTodo(todoId)).willReturn(todoResponse);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos/{todoId}", todoId)
        );
        //then

        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contents").value("contents"));
    }

    @Test
    void getTodo_일정을조회중InvalidRequestException발생시400응답반환() throws Exception {
        //given
        long todoId = 1L;
        given(todoService.getTodo(todoId)).willThrow(InvalidRequestException.class);
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/todos/{todoId}", todoId)
        );
        //then

        resultActions.andDo(print())
                .andExpect(status().isBadRequest());
    }
}
package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    private Todo todo;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("email@a.com","pass", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        todo = new Todo("title", "contents", "good", user);
        ReflectionTestUtils.setField(todo, "id", 1L);
    }

    @Test
    @DisplayName("saveTodo() 정상등록 테스트")
    void saveTodo_정상등록(){
        //saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest)

        //given
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title","contents");
        String weather = "good";

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        //when
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

        //then
        assertEquals(todoSaveResponse.getId(),todo.getId());
        assertEquals(todoSaveResponse.getTitle(),todo.getTitle());
        assertEquals(todoSaveResponse.getContents(),todo.getContents());

        //save가 실제로 1번 호출됐는지 확인
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void saveTodo_날씨를가져오는도중ServerException이발생했다면오류반환(){
        //given
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title","contents");

        given(weatherClient.getTodayWeather()).willThrow(ServerException.class);

        //when & then

        assertThrows(ServerException.class, ()->todoService.saveTodo(authUser, todoSaveRequest));


    }

    @Test
    void getTodos_정상조회(){
        //given
        int page = 1;
        int size = 1;

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(new PageImpl<>(List.of(todo)));
        //when
        Page<TodoResponse> todos = todoService.getTodos(page, size);
        //then
        assertEquals(todos.getTotalElements(), 1);
        assertEquals(todos.getTotalPages(), 1);
        assertEquals(todos.getContent().get(0).getId(),todo.getId());
        assertEquals(todos.getContent().get(0).getUser().getId(),todo.getUser().getId());

    }

    @Test
    void getTodos_정상조회_빈리스트(){
        //given
        int page = 1;
        int size = 1;

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));
        //when
        Page<TodoResponse> todos = todoService.getTodos(page, size);
        //then
        assertEquals(todos.getTotalElements(), 0);
        assertEquals(todos.getSize(),0);
    }

    @Test
    void getTodo_정상조회(){
        //given
        long todoId = 1;

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        //when
        TodoResponse todoResponse = todoService.getTodo(todoId);

        //then
        assertEquals(todoResponse.getId(),todo.getId());
        assertEquals(todoResponse.getUser().getId(),todo.getUser().getId());
    }

    @Test
    void getTodo_todoId의_todo가_존재하지_않을때_예외발생(){
        //given
        long todoId = 1;

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
            todoService.getTodo(todoId);
        });
        //then
        assertEquals("Todo not found",invalidRequestException.getMessage());
    }
}
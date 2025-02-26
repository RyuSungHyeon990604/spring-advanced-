package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long userId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void todo의_작성자가아닌user가_담당자를_지정하려_할_때_예외가_발생() {
        // given
        long userId = 1L;
        long writerId = 3L;
        long todoId = 1L;
        long managerUserId = 2L;

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

        User writer = new User();
        ReflectionTestUtils.setField(writer, "id", writerId);

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", writer);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @Disabled("일정 작성자가 본인을 담당자로 등록할 수 없는 로직이 변경되어 해당 검증이 필요 없어졌음. : b279f796")
    void saveManager_일정작성자가_자신을_담당자로_등록하려할때_예외발생() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        long managerUserId = 1L;

        User managerUser = new User();
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    void saveManager_등록하려는_담당자가_이미_존재할때_예외발생() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        long managerUserId = 2L;

        User managerUser = new User();
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);


        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.existsByUserIdAndTodoId(anyLong(),anyLong())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("이미 등록된 담당자 입니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        User user = createUser(1L, "user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId, "Title", "Contents", "Sunny", user);

        Manager mockManager = createManager(1L, todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void 담당자가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = createUser(managerUserId, "b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void deleteManager_정상삭제(){

        //given
        long userId = 1L;
        User user = createUser(userId,"user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId, "Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerUserId)).willReturn(Optional.of(manager));
        //when
        managerService.deleteManager(userId, todoId, managerUserId);

        //then
        verify(managerRepository,times(1)).delete(manager);
    }

    @Test
    void deleteManager_user가_존재하지_않을때_예외발생(){
        //given
        long userId = 1L;
        User user = createUser(userId, "user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId, "Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.empty());
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(userId, todoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        verify(todoRepository,times(0)).findById(todoId);
        assertEquals("User not found", invalidRequestException.getMessage());
    }

    @Test
    void deleteManager_todo가_존재하지_않을때_예외발생(){
        //given
        long userId = 1L;
        User user = createUser(userId,"user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId,"Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(userId, todoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        assertEquals("Todo not found", invalidRequestException.getMessage());
    }

    @Test
    void deleteManager_todo의_user가_null일때(){
        //given
        long userId = 1L;
        User user = createUser(userId,"user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId, "Test Title", "Test Contents", "Sunny", null);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(userId, todoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", invalidRequestException.getMessage());
    }

    @Test
    void deleteManager_todo를_작성하지않은_user가_담당자를_삭제하려고할때_예외발생(){
        //given
        long userId = 1L;
        User user = createUser(userId,"user1@example.com", "password", UserRole.USER);

        long anotherUserId = 2L;
        User anotherUser = createUser(anotherUserId,"another@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId,"Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(anotherUserId)).willReturn(Optional.of(anotherUser));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(anotherUserId, todoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", invalidRequestException.getMessage());
    }

    @Test
    void deleteManager_managerId인_담당자가_없을때_예외발생(){
        //given
        long userId = 1L;
        User user = createUser(userId, "user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId,"Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerUserId)).willReturn(Optional.empty());
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(userId, todoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        assertEquals("Manager not found", invalidRequestException.getMessage());
    }

    @Test
    void deleteManager_todo에_등록된_담당자가_아닐때_예외발생(){
        //given
        long userId = 1L;
        User user = createUser(userId,"user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = createTodo(todoId,"Test Title", "Test Contents", "Sunny", user);

        long anotherTodoId = 2L;
        Todo anotherTodo = createTodo(anotherTodoId,"Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        Manager manager = createManager(managerUserId, user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(anotherTodoId)).willReturn(Optional.of(anotherTodo));
        given(managerRepository.findById(managerUserId)).willReturn(Optional.of(manager));
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(userId, anotherTodoId, managerUserId));
        verify(managerRepository,times(0)).delete(manager);
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", invalidRequestException.getMessage());
    }

    User createUser(long userId, String email,String password, UserRole role) {
        User user =  new User(email,password,role);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    Todo createTodo(long todoId, String title, String contents,String weather ,User user) {
        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        return todo;
    }

    Manager createManager(long managerId, User user, Todo todo) {
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        return manager;
    }
}

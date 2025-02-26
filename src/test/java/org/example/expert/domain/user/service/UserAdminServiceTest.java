package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void changeUserRole_유저의권한을변경한다(){
        //given
        long userId = 1L;
        User user = createUser(userId, UserRole.USER);
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest(UserRole.ADMIN.name());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        userAdminService.changeUserRole(userId, userRoleChangeRequest);

        //then
        assertEquals(UserRole.of(userRoleChangeRequest.getRole()), user.getUserRole());
    }

    @Test
    void changeUserRole_유저가존재하지않는다면권한을변경할수없다(){
        //given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("admin");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when & then
        assertThrows(InvalidRequestException.class,
                () -> userAdminService.changeUserRole(userId, userRoleChangeRequest),
                "User not found");
    }

    @Test
    void changeUserRole_기존과동일한권한이라면변경할수없다(){
        //given
        long userId = 1L;
        User user = createUser(userId, UserRole.USER);
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest(user.getUserRole().name());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when & then
        assertThrows(InvalidRequestException.class,
                () -> userAdminService.changeUserRole(userId, userRoleChangeRequest),
                "기존과 동일한 권한으로 변경할수없습니다.");
    }

    User createUser(long id, UserRole userRole){
        User user = new User("email","password", userRole);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
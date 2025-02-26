package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    @BeforeEach
    void setUp() {
        Long id = 1L;
        user = new User("email@a.com","oldPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
    }

    @Test
    void getUser_정상조회(){
        //given
        long userId = user.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        //when
        UserResponse userResponse = userService.getUser(userId);
        //then
        assertEquals(user.getEmail(), userResponse.getEmail());
        assertEquals(user.getId(), userResponse.getId());
    }

    @Test
    void getUser_user가_존재하지않을때(){
        //given
        long userId = 2L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        //when & then
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
        assertEquals(invalidRequestException.getMessage(), "User not found");
    }

    @Test
    void changePassword_정상변경(){
        //given
        long userId = user.getId();
        String oldPassword = user.getPassword();
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(userChangePasswordRequest.getNewPassword())).willReturn(encodedPassword);
        //when
        userService.changePassword(userId, userChangePasswordRequest);
        //then
        verify(passwordEncoder,times(1)).encode(userChangePasswordRequest.getNewPassword());
        assertEquals(user.getPassword(), encodedPassword);
    }

    @Test
    void changePassword_userId의_user를_찾을_수_없을때예_외발생(){
        //given
        long userId = user.getId();
        String oldPassword = user.getPassword();
        String newPassword = "newPassword";
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, userChangePasswordRequest),
                "User not found");

    }

    @Test
    void changePassword_올바른_비밀번호를_입력하지_않았을때_예외발생(){
        //given
        long userId = user.getId();
        String oldPassword = user.getPassword();
        String newPassword = "newPassword";
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
        //when
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(false);
        //then

        assertThrows(InvalidRequestException.class,
                ()->userService.changePassword(userId, userChangePasswordRequest),
                "잘못된 비밀번호입니다.");

    }

    @Test
    void changePassword_기존과_동일한_비밀번호로_변경할때_예외발생(){
//given
        long userId = user.getId();
        String oldPassword = user.getPassword();
        String newPassword = "newPassword";
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
        //when
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())).willReturn(true);
        //then

        assertThrows(InvalidRequestException.class,
                ()->userService.changePassword(userId, userChangePasswordRequest),
                "새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

}
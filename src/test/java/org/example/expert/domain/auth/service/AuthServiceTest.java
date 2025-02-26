package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_회원가입을한다(){
        //given
        long userId = 1L;
        SignupRequest signupRequest = new SignupRequest("a@a.com", "Password","user");
        User user = createUser(userId, signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getUserRole());

        String encodedPassword = "encodedPassword";
        String token = "Bearer token";

        given(userRepository.existsByEmail(user.getEmail())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(passwordEncoder.encode(user.getPassword())).willReturn(encodedPassword);
        given(jwtUtil.createToken(user.getId(),user.getEmail(), user.getUserRole())).willReturn(token);
        //when
        SignupResponse signup = authService.signup(signupRequest);

        //then
        assertEquals(signup.getBearerToken(), token);
    }

    @Test
    void signup_동일한이메일은회원가입을할수없다(){
        //given
        long userId = 1L;
        SignupRequest signupRequest = new SignupRequest("a@a.com", "Password","user");
        User user = createUser(userId, signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getUserRole());

        given(userRepository.existsByEmail(user.getEmail())).willReturn(true);
        //when & then
        assertThrows(InvalidRequestException.class,
                ()-> authService.signup(signupRequest),
                "이미 존재하는 이메일입니다.");

    }

    @Test
    void signin_로그인을한다(){
        //given
        long userId = 1L;
        String email = "a@a.com";
        String password = "Password";
        User user = createUser(userId, email, password, UserRole.USER.name());
        SigninRequest signinRequest = new SigninRequest(email, password);
        String token = "Bearer token";

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(),user.getEmail(), user.getUserRole())).willReturn(token);
        //when
        SigninResponse signin = authService.signin(signinRequest);

        //then
        assertEquals(signin.getBearerToken(), token);
    }

    @Test
    void signin_가입하지않은이메일로는로그인을할수없다(){
        //given
        String email = "a@a.com";
        String password = "Password";
        SigninRequest signinRequest = new SigninRequest(email, password);


        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        //when & then
        assertThrows(InvalidRequestException.class,
                ()->authService.signin(signinRequest),
                "가입되지 않은 유저입니다.");

    }

    @Test
    void signin_잘못된비밀번호로는로그인을할수없다(){
        //given
        long userId = 1L;
        String email = "a@a.com";
        String password = "Password";
        User user = createUser(userId, email, password, UserRole.USER.name());
        SigninRequest signinRequest = new SigninRequest(email, "Wrong Password");

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);
        //when & then
        assertThrows(AuthException.class,
                ()->authService.signin(signinRequest),
                "잘못된 비밀번호입니다.") ;
    }

    User createUser(long userId, String email, String password, String role){
        User user = new User(email, password, UserRole.of(role));
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }
}
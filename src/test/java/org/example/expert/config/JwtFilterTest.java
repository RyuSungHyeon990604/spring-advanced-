package org.example.expert.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    Claims claims;

    @InjectMocks
    JwtFilter jwtFilter;

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    FilterChain chain;

    String header = "Bearer token";
    String token = "token";


    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @Test
    void auth로시작하는endpoint시바로doFilter() throws ServletException, IOException {
        //given
        request.setRequestURI("/auth");
        //when
        jwtFilter.doFilter(request, response, chain);
        //then
        verify(chain, times(1)).doFilter(request, response);
    }


    @Test
    void request에Authorization헤더가존재하지않으면400에러반환() throws Exception {
        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void 토큰검증중에SecurityException이발생했을때401에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(SecurityException.class);

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    @Test
    void 토큰검증중에MalformedJwtException이발생했을때401에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(MalformedJwtException.class);

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    @Test
    void 토큰검증중에ExpiredJwtException이발생했을때401에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(ExpiredJwtException.class);

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    @Test
    void 토큰검증중에UnsupportedJwtException이발생했을때400에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(UnsupportedJwtException.class);

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    @Test
    void 토큰검증중에Exception이발생했을때400에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(new RuntimeException("another exception"));//checked exception 던질수없음

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void 토큰검증후null반환할때400에러반환() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willReturn(null);

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void 유저권한USER일때필터로직정상수행() throws ServletException, IOException {
        //given
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willReturn(claims);

        given(claims.get("userRole", String.class)).willReturn("USER");
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("a@a.com");
        given(claims.get("userRole")).willReturn("USER");

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        verify(chain,times(1)).doFilter(request, response);
    }
    @Test
    void 유저권한USER일때ADMIN작업을요청했을때403에러반환() throws ServletException, IOException {
        //given
        request.setRequestURI("/admin");
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willReturn(claims);

        given(claims.get("userRole", String.class)).willReturn("USER");
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("a@a.com");
        given(claims.get("userRole")).willReturn("USER");

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        verify(chain,times(0)).doFilter(request, response);
    }

    @Test
    void 유저권한ADMIN일때ADMIN작업을요청했을때정상수행() throws ServletException, IOException {
        //given
        request.setRequestURI("/admin");
        request.addHeader("Authorization", header);
        given(jwtUtil.substringToken(header)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willReturn(claims);

        given(claims.get("userRole", String.class)).willReturn("ADMIN");
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("a@a.com");
        given(claims.get("userRole")).willReturn("ADMIN");

        //when
        jwtFilter.doFilter(request,response,chain);

        //then
        verify(chain,times(1)).doFilter(request, response);
    }

}
package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class AdminInterceptor implements HandlerInterceptor {

    //메서드 수행전에
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("request : {}:{}", request.getMethod(), request.getRequestURI());
        log.info("User Id : {}", request.getAttribute("userId"));
        log.info("User Role : {}", request.getAttribute("userRole"));
        return true;
    }

    //처리도중 예외발생하면 수행X 즉, 컨트롤러가 정상적으로 클라이언트에게 return 했을때
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("처리완료");
    }

    //클라이언트가 응답을 받았을때, 즉 정상, 예외 모든 경우의 응답을 받았을때
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("response : {}:{}", request.getMethod(), request.getRequestURI());
    }
}

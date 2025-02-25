package org.example.expert.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();


    //@Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    @Around("execution(* org.example.expert.domain.*.*.*AdminController.*(..))")
    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        // body 가져오기
        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;

        log.info("요청 시간 : {}", LocalDateTime.now());
        log.info("User Id : {}, User Role : {}", request.getAttribute("userId"),  request.getAttribute("userRole"));
        log.info("METHOD : {}", request.getMethod());
        log.info("URL : {}", request.getRequestURI());
        log.info("REQUEST BODY : {}", new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));

        Object proceed = joinPoint.proceed();
        log.info("RESPONSE BODY : {}", objectMapper.writeValueAsString(proceed));
        return proceed;
    }
}

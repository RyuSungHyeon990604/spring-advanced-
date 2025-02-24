package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContext;

import java.util.Objects;

@Aspect
@Slf4j
@Component
public class LoggingAspect {
    //@Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    @Around("execution(* org.example.expert.domain.*.*.*AdminController.*(..))")
    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("@Around");
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        try {
            //preHandle
            log.info("request : {}:{}", request.getMethod(), request.getRequestURI());
            log.info("User Id : {}", request.getAttribute("userId"));
            log.info("User Role : {}", request.getAttribute("userRole"));
            Object proceed = joinPoint.proceed();
            //afterCompletion
            log.info("처리완료");
            return proceed;
        }finally {
            //afterCompletion
            log.info("response : {}:{}", request.getMethod(), request.getRequestURI());
        }
    }

}

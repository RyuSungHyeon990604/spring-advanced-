package org.example.expert.domain.comment.proxy;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@NoArgsConstructor
public class LoggingController implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        log.info("start Method: " + method.getName());
        Object result = proxy.invokeSuper(obj, args);
        log.info("end Method: " + method.getName());
        return result;
    }
}

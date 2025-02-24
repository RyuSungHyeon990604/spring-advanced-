package org.example.expert.domain.comment.config;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.controller.CommentAdminController;
import org.example.expert.domain.comment.proxy.LoggingProxy;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoggingControllerConfig {
    private final CommentAdminService commentAdminService;

    @Bean
    public CommentAdminController commentAdminController() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CommentAdminController.class);
        enhancer.setCallback(new LoggingProxy());
        return (CommentAdminController) enhancer.create(
                new Class[]{CommentAdminService.class},
                new Object[]{commentAdminService}
        );
    }


}

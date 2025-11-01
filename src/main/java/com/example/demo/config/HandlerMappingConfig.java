package com.example.demo.config;

import com.example.demo.handler.HttpRequestHandlerImpl;
import com.example.demo.constants.ApplicationConstants;
import jakarta.validation.Validator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HandlerMappingConfig {

    private final Repositories repositories;
    private final Validator validator;
    private final static String baseRequestPackagePropertyKey = "dynamic.endpoint.request.base-package";
    private final static String baseDtoPackagePropertyKey = "dynamic.endpoint.dto.base-package";

    public HandlerMappingConfig(ApplicationContext context, Validator validator) {
        this.repositories = new Repositories(context);
        this.validator = validator;
    }

    @Bean
    public HandlerMapping createHandlerMapping(ApplicationContext context) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        Map<String, Object> urlMap = new HashMap<>();

        Environment environment = context.getEnvironment();
        repositories.forEach(repoInfo -> {
            HttpRequestHandlerImpl handler = new HttpRequestHandlerImpl(
                    repoInfo,
                    (JpaRepository) repositories.getRepositoryFor(repoInfo).get(),
                    validator,
                    environment.getProperty(baseRequestPackagePropertyKey),
                    environment.getProperty(baseDtoPackagePropertyKey)
            );

            urlMap.put(ApplicationConstants.SLASH + repoInfo.getSimpleName().toLowerCase() + ApplicationConstants.URL_ENTITY_NAME_POSTFIX, handler);
        });

        mapping.setUrlMap(urlMap);
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return mapping;
    }
}

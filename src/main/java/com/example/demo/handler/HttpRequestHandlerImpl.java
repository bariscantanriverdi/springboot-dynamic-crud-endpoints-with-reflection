package com.example.demo.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.HttpRequestHandler;
import jakarta.validation.Validator;

import java.io.IOException;

import com.example.demo.constants.HttpConstants;

public class HttpRequestHandlerImpl implements HttpRequestHandler {

    private final Class<?> entityClass;
    private final JpaRepository repository;
    private final Validator validator;
    private final String baseRequestPackage;
    private final String baseDtoPackage;

    public HttpRequestHandlerImpl(Class<?> entityClass, JpaRepository repository, Validator validator, String baseRequestPackage, String baseDtoPackage) {
        this.entityClass = entityClass;
        this.repository = repository;
        this.validator = validator;
        this.baseRequestPackage = baseRequestPackage;
        this.baseDtoPackage = baseDtoPackage;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            IHandler handler = switch (request.getMethod()) {
                case HttpConstants.GET -> new GetHandler(
                        entityClass,
                        repository,
                        baseDtoPackage);

                case HttpConstants.POST -> new PostHandler(
                        entityClass,
                        repository,
                        validator,
                        baseRequestPackage,
                        baseDtoPackage);
                case HttpConstants.PUT -> new PutHandler(
                        entityClass,
                        repository,
                        validator,
                        baseRequestPackage,
                        baseDtoPackage);

                case HttpConstants.DELETE -> new DeleteHandler(repository);
                default -> null;
            };

            if (handler == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            handler.execute(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}

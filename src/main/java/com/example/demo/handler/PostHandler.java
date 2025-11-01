package com.example.demo.handler;

import com.example.demo.mapper.Mapper;
import com.example.demo.constants.ApplicationConstants;
import com.example.demo.constants.HttpConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PostHandler implements IHandler {

    private final Class<?> entityClass;
    private final JpaRepository repository;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final String baseRequestPackage;
    private final String baseDtoPackage;

    public PostHandler(Class<?> entityClass, JpaRepository repository, Validator validator, String baseRequestPackage, String baseDtoPackage) {
        this.entityClass = entityClass;
        this.repository = repository;
        this.validator = validator;
        this.baseRequestPackage = baseRequestPackage;
        this.baseDtoPackage = baseDtoPackage;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object requestObject = objectMapper.readValue(request.getInputStream(), getRequestClass(entityClass));
        if (requestObject == null) {
            return;
        }

        if (!ValidateRequest(requestObject, response)) {
            return;
        }

        Object entity = Mapper.map(requestObject, entityClass);
        repository.save(entity);

        Object responseDto = Mapper.map(entity, getDtoClass(entityClass));
        SetResponse(response, responseDto);
    }

    @Override
    public String getHttpMethod() {
        return HttpConstants.POST;
    }

    private void SetResponse(HttpServletResponse response, Object responseDto) throws Exception {
        response.setContentType(HttpConstants.CONTENT_TYPE);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    private boolean ValidateRequest(Object requestObject, HttpServletResponse response) throws IOException {
        Set<ConstraintViolation<Object>> violations = validator.validate(requestObject);
        if (!violations.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));
            response.setContentType(HttpConstants.CONTENT_TYPE);
            response.getWriter().write(objectMapper.writeValueAsString(errors));
            return false;
        }

        return true;
    }

    private Class<?> getRequestClass(Class<?> entityClass) throws ClassNotFoundException {
        return Class.forName(baseRequestPackage + ApplicationConstants.DOT + entityClass.getSimpleName().toLowerCase() + ApplicationConstants.DOT + ApplicationConstants.POST_FILE_PREFIX + entityClass.getSimpleName() + ApplicationConstants.REQUEST_POSTFIX);
    }

    private Class<?> getDtoClass(Class<?> entityClass) throws ClassNotFoundException {
        return Class.forName(baseDtoPackage + ApplicationConstants.DOT + entityClass.getSimpleName().toLowerCase() + ApplicationConstants.DOT + ApplicationConstants.POST_FILE_PREFIX + entityClass.getSimpleName() + ApplicationConstants.DTO_POSTFIX);
    }
}

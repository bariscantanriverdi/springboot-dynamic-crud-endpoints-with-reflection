package com.example.demo.handler;

import ch.qos.logback.core.util.StringUtil;
import com.example.demo.mapper.Mapper;
import com.example.demo.constants.ApplicationConstants;
import com.example.demo.constants.HttpConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PutHandler implements IHandler {

    private final Class<?> entityClass;
    private final JpaRepository repository;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final String baseRequestPackage;
    private final String baseDtoPackage;

    public PutHandler(Class<?> entityClass, JpaRepository repository, Validator validator, String baseRequestPackage, String baseDtoPackage) {
        this.entityClass = entityClass;
        this.repository = repository;
        this.validator = validator;
        this.baseRequestPackage = baseRequestPackage;
        this.baseDtoPackage = baseDtoPackage;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object requestObject = getRequestObject(request);
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
        return HttpConstants.PUT;
    }

    private void SetResponse(HttpServletResponse response, Object responseDto) throws Exception {
        response.setContentType(HttpConstants.CONTENT_TYPE);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    private Object getRequestObject(HttpServletRequest request) throws IOException, ClassNotFoundException {
        Object requestObject = objectMapper.readValue(request.getInputStream(), getRequestClass(entityClass));
        if (requestObject == null) {
            return null;
        }

        String id = extractIdentifier(request);
        if (id != null) {
            setId(requestObject, id);
        }

        return requestObject;
    }

    public static void setId(Object request, String id) {
        try {
            Field idField = request.getClass().getDeclaredField(ApplicationConstants.IDENTIFIER_FIELD);
            idField.setAccessible(true);
            idField.set(request, convertStringToFieldType(id, idField.getType()));
        } catch (Exception _) {
        }
    }

    private static Object convertStringToFieldType(String value, Class<?> fieldType) {
        if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            return Long.valueOf(value);
        } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (fieldType.equals(String.class)) {
            return value;
        } else if (fieldType.equals(UUID.class)) {
            return UUID.fromString(value);
        }
        throw new IllegalArgumentException(fieldType.getSimpleName());
    }

    private boolean ValidateRequest(Object requestObject, HttpServletResponse response) throws IOException {
        Set<ConstraintViolation<Object>> violations = validator.validate(requestObject);
        if (!violations.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage));

            response.setContentType(HttpConstants.CONTENT_TYPE);
            response.getWriter().write(objectMapper.writeValueAsString(errors));

            return false;
        }

        return true;
    }

    private Class<?> getRequestClass(Class<?> entityClass) throws ClassNotFoundException {
        return Class.forName(baseRequestPackage + ApplicationConstants.DOT + entityClass.getSimpleName().toLowerCase() + ApplicationConstants.DOT + ApplicationConstants.PUT_FILE_PREFIX + entityClass.getSimpleName() + ApplicationConstants.REQUEST_POSTFIX);
    }

    private Class<?> getDtoClass(Class<?> entityClass) throws ClassNotFoundException {
        return Class.forName(baseDtoPackage + ApplicationConstants.DOT + entityClass.getSimpleName().toLowerCase() + ApplicationConstants.DOT + ApplicationConstants.PUT_FILE_PREFIX + entityClass.getSimpleName() + ApplicationConstants.DTO_POSTFIX);
    }

    private String extractIdentifier(HttpServletRequest request) {
        String id = request.getParameter(ApplicationConstants.IDENTIFIER_FIELD);
        if (!StringUtil.isNullOrEmpty(id)) {
            return id;
        }

        String[] paths = request.getRequestURI().split(ApplicationConstants.SLASH);
        if (paths.length > HttpConstants.URL_PATH_LENGTH_THRESHOLD) {
            return paths[HttpConstants.URL_PATH_LENGTH_THRESHOLD];
        }

        return null;
    }
}

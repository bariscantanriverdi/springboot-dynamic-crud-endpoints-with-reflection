package com.example.demo.handler;

import ch.qos.logback.core.util.StringUtil;
import com.example.demo.mapper.Mapper;
import com.example.demo.constants.ApplicationConstants;
import com.example.demo.constants.HttpConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public class GetHandler implements IHandler {
    private final Class<?> entityClass;
    private final JpaRepository repository;
    private final ObjectMapper objectMapper;
    private final String baseDtoPackage;
    private static final int defaultPage = 0;
    private static final int defaultPageSize = 20;

    public GetHandler(Class<?> entityClass, JpaRepository repository, String baseDtoPackage) {
        this.entityClass = entityClass;
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.baseDtoPackage = baseDtoPackage;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = extractIdentifier(request);
        if (id != null) {
            Object entity = repository.findById(id).orElseThrow();
            SetResponse(response, Mapper.map(entity, getDtoClass(entityClass)));
            return;
        }

        Pageable pageable = PageRequest.of(getPage(request), getPageSize(request));
        Page<Object> entities = repository.findAll(pageable);
        SetResponse(response, Mapper.mapList(entities.getContent(), getDtoClass(entityClass)));
    }

    private int getPage(HttpServletRequest request){
        String page = request.getParameter(ApplicationConstants.PAGINATION_PAGE_KEYWORD);
        return page != null ? Integer.parseInt(page) : defaultPage;
    }

    private int getPageSize(HttpServletRequest request){
        String pageSize = request.getParameter(ApplicationConstants.PAGINATION_PAGE_SIZE_KEYWORD);
        return pageSize !=null ? Integer.parseInt(pageSize) : defaultPageSize;
    }

    @Override
    public String getHttpMethod() {
        return HttpConstants.GET;
    }

    private void SetResponse(HttpServletResponse response, Object responseDto) throws Exception {
        response.setContentType(HttpConstants.CONTENT_TYPE);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
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

    private Class<?> getDtoClass(Class<?> entityClass) throws ClassNotFoundException {
        return Class.forName(baseDtoPackage + ApplicationConstants.DOT + entityClass.getSimpleName().toLowerCase() + ApplicationConstants.DOT + ApplicationConstants.GET_FILE_PREFIX + entityClass.getSimpleName() + ApplicationConstants.DTO_POSTFIX);
    }

}

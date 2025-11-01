package com.example.demo.handler;

import ch.qos.logback.core.util.StringUtil;
import com.example.demo.constants.ApplicationConstants;
import com.example.demo.constants.HttpConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;


public class DeleteHandler implements IHandler {
    private final JpaRepository repository;

    public DeleteHandler(JpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = extractIdentifier(request);
        if (StringUtil.isNullOrEmpty(id)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ApplicationConstants.NOT_EMPTY_IDENTIFIER);
            return;
        }

        repository.deleteById(id);

        SetResponse(response);
    }

    @Override
    public String getHttpMethod() {
        return HttpConstants.DELETE;
    }

    private void SetResponse(HttpServletResponse response) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
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

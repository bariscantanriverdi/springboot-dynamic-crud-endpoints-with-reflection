package com.example.demo.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IHandler {
    void execute(HttpServletRequest request, HttpServletResponse response) throws Exception;
    String getHttpMethod();
}

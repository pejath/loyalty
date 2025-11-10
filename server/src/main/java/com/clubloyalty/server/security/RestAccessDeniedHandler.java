package com.clubloyalty.server.security;

import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {
    public void handle(HttpServletRequest r, HttpServletResponse s, org.springframework.security.access.AccessDeniedException e) throws IOException {
        s.setStatus(403);
        s.getWriter().write("Forbidden");
    }
}

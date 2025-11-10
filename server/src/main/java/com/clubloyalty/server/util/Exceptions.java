package com.clubloyalty.server.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class Exceptions {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        Map<String, Object> b = new HashMap<>();
        b.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(b);
    }
}

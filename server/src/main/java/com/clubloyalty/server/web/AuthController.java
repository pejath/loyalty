package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.AuthDtos.*;
import com.clubloyalty.server.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService a) {
        this.auth = a;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Validated LoginRequest r) {
        return ResponseEntity.ok(new TokenResponse(auth.login(r.username, r.password)));
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> register(@RequestBody @Validated RegisterRequest r) {
        auth.register(r);
        return ResponseEntity.ok(new SimpleResponse("OK"));
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication authentication) {
        return auth.profile(authentication.getName());
    }
}

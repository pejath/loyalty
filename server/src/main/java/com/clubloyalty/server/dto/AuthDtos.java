package com.clubloyalty.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTOs for authentication & registration
 */
public class AuthDtos {

    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;
        @NotBlank
        @Size(min = 4, max = 100)
        public String password;
        @NotBlank
        public String fullName;
        /**
         * Может совпадать с username, если используется телефон
         */
        @NotBlank
        public String phone;
    }

    public static class TokenResponse {
        public String token;

        public TokenResponse(String t) {
            this.token = t;
        }
    }

    public static class SimpleResponse {
        public String status;

        public SimpleResponse(String s) {
            this.status = s;
        }
    }

    public static class ProfileResponse {
        public String username;
        public java.util.List<String> roles;
        public Long memberId;

        public ProfileResponse(String username, java.util.List<String> roles, Long memberId) {
            this.username = username;
            this.roles = roles;
            this.memberId = memberId;
        }
    }
}

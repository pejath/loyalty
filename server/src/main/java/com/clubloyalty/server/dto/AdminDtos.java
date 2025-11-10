package com.clubloyalty.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class AdminDtos {
    public static class UserSummary {
        public Long id;
        public String username;
        public boolean enabled;
        public List<String> roles;
        public Long memberId;
    }

    public static class UserCreateRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        @NotEmpty
        public List<String> roles;
        public Boolean enabled;
        public String fullName;
        public String phone;
        public Long tierId;
    }

    public static class UserUpdateRequest {
        public Boolean enabled;
        public List<String> roles;
    }

    public static class PasswordResetRequest {
        @NotBlank
        public String password;
    }
}

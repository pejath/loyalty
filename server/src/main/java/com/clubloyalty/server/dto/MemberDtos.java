package com.clubloyalty.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MemberDtos {
    public static class MemberDto {
        public Long id;
        public String fullName;
        public String phone;
        public String tier;
        public Long tierId;
        public long points;
        public Long userId;
        public String username;
    }

    public static class MemberUpdateRequest {
        @NotBlank
        public String fullName;
        @NotBlank
        public String phone;
        @NotNull
        public Long tierId;
    }

    public static class PointsAdjustRequest {
        @NotNull
        public Long delta;
    }
}

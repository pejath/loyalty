package com.clubloyalty.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class RuleDtos {
    public static class RuleApplyDto {
        @NotBlank
        public String eventType;
        @NotNull
        public Long memberId;
        public Map<String, Object> context;
    }
}

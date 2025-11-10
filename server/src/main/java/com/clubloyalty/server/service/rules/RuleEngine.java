package com.clubloyalty.server.service.rules;

import java.util.Map;

public interface RuleEngine {
    long calculatePoints(String eventType, Map<String, Object> ctx);
}
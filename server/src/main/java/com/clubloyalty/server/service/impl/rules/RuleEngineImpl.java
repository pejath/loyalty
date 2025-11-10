package com.clubloyalty.server.service.impl.rules;

import com.clubloyalty.server.service.rules.RuleEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class RuleEngineImpl implements RuleEngine {
    private final Map<String, Function<Map<String, Object>, Long>> strategies = new HashMap<>();

    public RuleEngineImpl() {
        strategies.put("SESSION_TIME", ctx -> (Long) ctx.getOrDefault("minutes", 0L));
        strategies.put("PURCHASE_AMOUNT", ctx -> Math.round(((Number) ctx.getOrDefault("amount", 0)).doubleValue()));
        strategies.put("HAPPY_HOUR", ctx -> Math.round(((Number) ctx.getOrDefault("amount", 0)).doubleValue() * 2));
    }

    public long calculatePoints(String eventType, Map<String, Object> ctx) {
        return strategies.getOrDefault(eventType, c -> 0L).apply(ctx);
    }
}

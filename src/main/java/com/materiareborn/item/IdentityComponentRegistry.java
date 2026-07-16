package com.materiareborn.item;

import com.materiareborn.api.item.IdentityComponentRule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class IdentityComponentRegistry {
    private final Map<String, IdentityComponentRule> rules = new LinkedHashMap<>();

    public void register(IdentityComponentRule rule) {
        rules.put(rule.componentId(), rule);
    }

    public Optional<IdentityComponentRule> find(String componentId) {
        return Optional.ofNullable(rules.get(componentId));
    }

    public Map<String, IdentityComponentRule> snapshot() {
        return Collections.unmodifiableMap(rules);
    }
}

package com.materiareborn.api.item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record ComponentFingerprint(Map<String, String> components) {
    public static final ComponentFingerprint EMPTY = new ComponentFingerprint(Map.of());

    public ComponentFingerprint {
        Objects.requireNonNull(components, "components");
        TreeMap<String, String> sorted = new TreeMap<>();
        components.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Component id cannot be blank.");
            }
            sorted.put(key, Objects.requireNonNull(value, "component value"));
        });
        components = Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }

    public static ComponentFingerprint empty() {
        return EMPTY;
    }

    public static ComponentFingerprint of(Map<String, String> components) {
        return components.isEmpty() ? EMPTY : new ComponentFingerprint(components);
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    public String canonicalForm() {
        if (components.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        components.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append(';');
            }
            builder.append(key).append('=').append(value);
        });
        return builder.toString();
    }
}

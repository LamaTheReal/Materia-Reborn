package com.materiareborn.api.item;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record ItemKey(String namespace, String path) {
    private static final Pattern VALID_PART = Pattern.compile("[a-z0-9_.-]+");

    public ItemKey {
        namespace = normalize(namespace, "namespace");
        path = normalize(path, "path");
    }

    public static ItemKey of(String namespace, String path) {
        return new ItemKey(namespace, path);
    }

    public static ItemKey parse(String value) {
        Objects.requireNonNull(value, "value");
        int separator = value.indexOf(':');
        if (separator < 1 || separator == value.length() - 1) {
            throw new IllegalArgumentException("Item key must use namespace:path format.");
        }
        return new ItemKey(value.substring(0, separator), value.substring(separator + 1));
    }

    public String asString() {
        return namespace + ":" + path;
    }

    private static String normalize(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.toLowerCase(Locale.ROOT);
        if (!VALID_PART.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid item key " + name + ": " + value);
        }
        return normalized;
    }

    @Override
    public String toString() {
        return asString();
    }
}

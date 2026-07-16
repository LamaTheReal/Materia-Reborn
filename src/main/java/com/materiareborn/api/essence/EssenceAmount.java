package com.materiareborn.api.essence;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public record EssenceAmount(BigDecimal value) implements Comparable<EssenceAmount> {
    public static final EssenceAmount ZERO = new EssenceAmount(BigDecimal.ZERO);
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public EssenceAmount {
        Objects.requireNonNull(value, "value");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Essence amount cannot be negative.");
        }
        value = value.stripTrailingZeros();
    }

    public static EssenceAmount of(long value) {
        return new EssenceAmount(BigDecimal.valueOf(value));
    }

    public static EssenceAmount decimal(String value) {
        return new EssenceAmount(new BigDecimal(value));
    }

    public EssenceAmount plus(EssenceAmount other) {
        return new EssenceAmount(value.add(other.value, MATH_CONTEXT));
    }

    public EssenceAmount minus(EssenceAmount other) {
        BigDecimal result = value.subtract(other.value, MATH_CONTEXT);
        if (result.signum() < 0) {
            throw new IllegalArgumentException("Essence amount cannot become negative.");
        }
        return new EssenceAmount(result);
    }

    public EssenceAmount multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        if (multiplier.signum() < 0) {
            throw new IllegalArgumentException("Essence multiplier cannot be negative.");
        }
        return new EssenceAmount(value.multiply(multiplier, MATH_CONTEXT));
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    @Override
    public int compareTo(EssenceAmount other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}

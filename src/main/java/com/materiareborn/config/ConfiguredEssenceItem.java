package com.materiareborn.config;

public record ConfiguredEssenceItem(
        String resourcePath,
        int tableLevel,
        String category,
        String id,
        String tier,
        boolean enabled,
        int analysis,
        long baseValue,
        long sellValue,
        long purchaseCost
) {
    public ConfiguredEssenceItem withValues(
            boolean newEnabled,
            int newAnalysis,
            long newBaseValue,
            long newSellValue,
            long newPurchaseCost
    ) {
        return new ConfiguredEssenceItem(
                resourcePath,
                tableLevel,
                category,
                id,
                tier,
                newEnabled,
                newAnalysis,
                newBaseValue,
                newSellValue,
                newPurchaseCost
        );
    }
}

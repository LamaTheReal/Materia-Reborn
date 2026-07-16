package com.materiareborn.api.mapping;

@FunctionalInterface
public interface EssenceMappingSource {
    void collect(EssenceMappingCollector collector);
}

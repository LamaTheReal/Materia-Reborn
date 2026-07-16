package com.materiareborn.api.item;

@FunctionalInterface
public interface ItemIdentityResolver<T> {
    MateriaItemIdentity resolve(T value);
}

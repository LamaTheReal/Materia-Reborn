package com.materiareborn.api.table;

public enum TableTier {
    PRIMITIVE("primitive", 1, "Primitive Table"),
    ALCHEMICAL("alchemical", 2, "Alchemical Table"),
    ADVANCED("advanced", 3, "Advanced Table"),
    ARCANE("arcane", 4, "Arcane Table"),
    NEXUS("nexus", 5, "Materia Nexus");

    private final String id;
    private final int rank;
    private final String displayName;

    TableTier(String id, int rank, String displayName) {
        this.id = id;
        this.rank = rank;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public int rank() {
        return rank;
    }

    public String displayName() {
        return displayName;
    }

    public boolean supports(TableTier requiredTier) {
        return rank >= requiredTier.rank;
    }
}

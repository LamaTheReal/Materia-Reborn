package com.materiareborn.menu;

public enum MateriaTableTab {
    ESSENCE(0, "container.materia_reborn.materia_table.essence"),
    BACKPACK(1, "container.materia_reborn.materia_table.backpack"),
    FURNACE(2, "container.materia_reborn.materia_table.furnace");

    private static final MateriaTableTab[] VALUES = values();

    private final int id;
    private final String translationKey;

    MateriaTableTab(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public int id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public int requiredTableTier() {
        return id + 1;
    }

    public MateriaTableTab next() {
        return VALUES[(id + 1) % VALUES.length];
    }

    public MateriaTableTab previous() {
        return VALUES[(id + VALUES.length - 1) % VALUES.length];
    }

    public static int count() {
        return VALUES.length;
    }

    public static MateriaTableTab byId(int id) {
        for (MateriaTableTab tab : VALUES) {
            if (tab.id == id) {
                return tab;
            }
        }
        return ESSENCE;
    }
}

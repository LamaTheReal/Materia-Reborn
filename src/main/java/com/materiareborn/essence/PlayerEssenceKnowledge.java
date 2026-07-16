package com.materiareborn.essence;

import com.materiareborn.core.ModConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerEssenceKnowledge {
    private static final String KNOWLEDGE_TAG = ModConstants.MOD_ID + ".essence_knowledge";
    private static final String ANALYSIS_TAG = "Analysis";
    private static final String UNLOCKED_TAG = "Unlocked";
    private static final String SELECTED_ITEM_TAG = "SelectedItem";
    private static final String AUTO_SELL_TAG = "AutoSell";

    private PlayerEssenceKnowledge() {
    }

    public static int analysisProgress(Player player, EssenceItemDefinition definition) {
        int saved = analysisData(player).getInt(definition.catalogId());
        return Math.max(0, Math.min(definition.requiredAnalysis(), saved));
    }

    public static int addAnalysis(Player player, EssenceItemDefinition definition, int amount) {
        if (amount <= 0) {
            return analysisProgress(player, definition);
        }
        CompoundTag knowledge = knowledge(player);
        CompoundTag analysis = knowledge.getCompound(ANALYSIS_TAG);
        int next = Math.min(
                definition.requiredAnalysis(),
                analysisProgress(player, definition) + amount
        );
        analysis.putInt(definition.catalogId(), next);
        knowledge.put(ANALYSIS_TAG, analysis);
        return next;
    }

    public static boolean isAnalysisComplete(Player player, EssenceItemDefinition definition) {
        return analysisProgress(player, definition) >= definition.requiredAnalysis();
    }

    public static boolean isUnlocked(Player player, EssenceItemDefinition definition) {
        return knowledge(player).getCompound(UNLOCKED_TAG).getBoolean(definition.catalogId());
    }

    public static boolean autoSellEnabled(Player player) {
        return knowledge(player).getBoolean(AUTO_SELL_TAG);
    }

    public static boolean toggleAutoSell(Player player) {
        CompoundTag knowledge = knowledge(player);
        boolean enabled = !knowledge.getBoolean(AUTO_SELL_TAG);
        knowledge.putBoolean(AUTO_SELL_TAG, enabled);
        return enabled;
    }

    public static int unlockedWord(Player player, int firstIndex, int endIndex) {
        CompoundTag unlocked = knowledge(player).getCompound(UNLOCKED_TAG);
        int bits = 0;
        int end = Math.min(endIndex, EssenceItemCatalog.size());
        for (int index = Math.max(0, firstIndex); index < end; index++) {
            if (unlocked.getBoolean(EssenceItemCatalog.get(index).catalogId())) {
                bits |= 1 << (index - firstIndex);
            }
        }
        return bits;
    }

    public static boolean unlock(Player player, EssenceItemDefinition definition) {
        if (!isAnalysisComplete(player, definition) || isUnlocked(player, definition)) {
            return false;
        }
        CompoundTag knowledge = knowledge(player);
        CompoundTag unlocked = knowledge.getCompound(UNLOCKED_TAG);
        unlocked.putBoolean(definition.catalogId(), true);
        knowledge.put(UNLOCKED_TAG, unlocked);
        return true;
    }

    public static boolean removeUnlock(Player player, EssenceItemDefinition definition) {
        if (!isUnlocked(player, definition)) {
            return false;
        }
        CompoundTag knowledge = knowledge(player);
        CompoundTag unlocked = knowledge.getCompound(UNLOCKED_TAG);
        unlocked.remove(definition.catalogId());
        knowledge.put(UNLOCKED_TAG, unlocked);
        return true;
    }

    public static int selectedItemIndex(Player player) {
        CompoundTag knowledge = knowledge(player);
        if (!knowledge.contains(SELECTED_ITEM_TAG)) {
            return -1;
        }
        return EssenceItemCatalog.indexOf(knowledge.getString(SELECTED_ITEM_TAG));
    }

    public static void setSelectedItem(Player player, int catalogIndex) {
        CompoundTag knowledge = knowledge(player);
        if (catalogIndex < 0 || catalogIndex >= EssenceItemCatalog.size()) {
            knowledge.remove(SELECTED_ITEM_TAG);
            return;
        }
        knowledge.putString(SELECTED_ITEM_TAG, EssenceItemCatalog.get(catalogIndex).catalogId());
    }

    public static void unlockAll(Player player) {
        CompoundTag knowledge = knowledge(player);
        CompoundTag analysis = knowledge.getCompound(ANALYSIS_TAG);
        CompoundTag unlocked = knowledge.getCompound(UNLOCKED_TAG);
        for (EssenceItemDefinition definition : EssenceItemCatalog.definitions()) {
            analysis.putInt(definition.catalogId(), definition.requiredAnalysis());
            unlocked.putBoolean(definition.catalogId(), true);
        }
        knowledge.put(ANALYSIS_TAG, analysis);
        knowledge.put(UNLOCKED_TAG, unlocked);
    }
    public static void reset(Player player) {
        persisted(player).remove(KNOWLEDGE_TAG);
    }

    private static CompoundTag analysisData(Player player) {
        return knowledge(player).getCompound(ANALYSIS_TAG);
    }

    private static CompoundTag knowledge(Player player) {
        CompoundTag persisted = persisted(player);
        CompoundTag knowledge = persisted.getCompound(KNOWLEDGE_TAG);
        persisted.put(KNOWLEDGE_TAG, knowledge);
        return knowledge;
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return persisted;
    }
}
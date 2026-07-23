package com.materiareborn.command;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.config.ConfiguredEssenceItem;
import com.materiareborn.config.EssenceItemConfigFiles;
import com.materiareborn.config.MateriaConfig;
import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceItemDefinition;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.essence.PlayerEssenceKnowledge;
import com.materiareborn.essence.PlayerEssenceSessionHistory;
import com.materiareborn.menu.MateriaTableMenu;
import com.materiareborn.progression.PlayerMateriaProgress;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import java.util.Locale;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MateriaCommands {
    private MateriaCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher(), event.getBuildContext(), "materia");
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            String name
    ) {
        dispatcher.register(Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource())))
                .then(Commands.literal("getinfo")
                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                .executes(context -> getEssenceItemInfo(
                                        context.getSource(),
                                        ItemArgument.getItem(context, "item").getItem()
                                ))))
                .then(Commands.literal("unlock")
                        .then(Commands.literal("all")
                                .executes(context -> unlockAll(context.getSource())))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("item", ItemArgument.item(buildContext))
                                        .executes(context -> setItemUnlock(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                BuiltInRegistries.ITEM.getKey(
                                                        ItemArgument.getItem(context, "item").getItem()
                                                ),
                                                true
                                        )))))
                .then(Commands.literal("lock")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("item", ItemArgument.item(buildContext))
                                        .executes(context -> setItemUnlock(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                BuiltInRegistries.ITEM.getKey(
                                                        ItemArgument.getItem(context, "item").getItem()
                                                ),
                                                false
                                        )))))
                .then(Commands.literal("essence")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> getEssence(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))
                        .then(Commands.literal("give")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                                .executes(context -> modifyTargetEssence(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        EssenceOperation.GIVE,
                                                        LongArgumentType.getLong(context, "amount")
                                                )))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                                .executes(context -> modifyTargetEssence(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        EssenceOperation.REMOVE,
                                                        LongArgumentType.getLong(context, "amount")
                                                )))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                                .executes(context -> modifyTargetEssence(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        EssenceOperation.SET,
                                                        LongArgumentType.getLong(context, "amount")
                                                )))))));
    }

    private static int getEssenceItemInfo(CommandSourceStack source, Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        for (ConfiguredEssenceItem configured : EssenceItemConfigFiles.loadEditableItems()) {
            if (itemId.toString().equals(configured.id())) {
                sendEssenceItemInfo(
                        source,
                        itemId,
                        configured.tier(),
                        configured.tableLevel(),
                        configured.category(),
                        fileName(configured.resourcePath()),
                        configured.enabled(),
                        configured.analysis(),
                        configured.baseValue(),
                        configured.sellValue(),
                        configured.purchaseCost()
                );
                return 1;
            }
        }
        source.sendFailure(Component.literal("No Essence information found for " + itemId + "."));
        return 0;
    }

    private static void sendEssenceItemInfo(
            CommandSourceStack source,
            ResourceLocation itemId,
            String tier,
            int tableLevel,
            String category,
            String file,
            boolean enabled,
            int analysis,
            long base,
            long sell,
            long buy
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        "Essence info for " + itemId
                                + "\nTier: " + tier + " (Table " + tableLevel + ")"
                                + "\nDefault: " + formatAmount(base)
                                + "\nSell: " + formatAmount(sell)
                                + "\nBuy: " + formatAmount(buy)
                                + "\nAnalyze: " + formatAmount(analysis)
                                + "\nEnabled: " + (enabled && base > 0L && sell > 0L && buy > 0L ? "yes" : "no")
                                + "\nCategory: " + category
                                + "\nSource: " + file
                ),
                false
        );
    }

    private static String fileName(String resourcePath) {
        int separator = Math.max(resourcePath.lastIndexOf('/'), resourcePath.lastIndexOf('\\'));
        return separator < 0 ? resourcePath : resourcePath.substring(separator + 1);
    }

    private static int unlockAll(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerEssence.set(player, MateriaConfig.maxEssence());
        PlayerEssenceKnowledge.unlockAll(player);
        PlayerMateriaProgress.unlockAll(player);
        MateriaTableBlockEntity table = findTable(player);
        if (table != null) {
            table.applyPlayerProgress(player);
        }
        PlayerEssence.syncOpenMenu(player);
        source.sendSuccess(() -> Component.literal("All configured Materia progression unlocked."), true);
        return 1;
    }

    private static int reset(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerEssence.set(player, 0);
        PlayerEssenceKnowledge.reset(player);
        PlayerEssenceSessionHistory.clear(player);
        PlayerMateriaProgress.reset(player);
        MateriaTableBlockEntity table = findTable(player);
        if (table != null) {
            table.resetProgression();
        }
        PlayerEssence.syncOpenMenu(player);
        source.sendSuccess(() -> Component.literal("Materia reset complete."), true);
        return 1;
    }

    private static int getEssence(CommandSourceStack source, ServerPlayer target) {
        String playerName = target.getGameProfile().getName();
        String balance = formatAmount(PlayerEssence.get(target));
        source.sendSuccess(
                () -> Component.literal(playerName + " has " + balance + " Essence."),
                true
        );
        return 1;
    }

    private static int modifyTargetEssence(
            CommandSourceStack source,
            ServerPlayer target,
            EssenceOperation operation,
            long requestedAmount
    ) {
        long previousBalance = PlayerEssence.get(target);
        long newBalance = switch (operation) {
            case GIVE -> PlayerEssence.add(target, requestedAmount);
            case REMOVE -> PlayerEssence.remove(target, requestedAmount);
            case SET -> PlayerEssence.set(target, requestedAmount);
        };
        long changedAmount = switch (operation) {
            case GIVE -> newBalance - previousBalance;
            case REMOVE -> previousBalance - newBalance;
            case SET -> 0L;
        };
        String playerName = target.getGameProfile().getName();
        String resultLine = switch (operation) {
            case GIVE -> "Added " + formatAmount(changedAmount) + " Essence to " + playerName + ".";
            case REMOVE -> "Removed " + formatAmount(changedAmount) + " Essence from " + playerName + ".";
            case SET -> "Set " + playerName + "'s Essence to " + formatAmount(newBalance) + ".";
        };

        PlayerEssence.syncOpenMenu(target);
        source.sendSuccess(
                () -> Component.literal(
                        resultLine
                                + "\nNew balance: "
                                + formatAmount(newBalance)
                                + " Essence."
                ),
                true
        );
        return 1;
    }

    private static int setItemUnlock(
            CommandSourceStack source,
            ServerPlayer target,
            ResourceLocation itemId,
            boolean unlock
    ) {
        int catalogIndex = EssenceItemCatalog.indexOf(itemId.toString());
        if (catalogIndex < 0) {
            source.sendFailure(Component.literal(
                    "Item is not configured for Materia: " + itemId
            ));
            return 0;
        }

        EssenceItemDefinition definition = EssenceItemCatalog.get(catalogIndex);
        if (unlock) {
            PlayerEssenceKnowledge.grantUnlock(target, definition);
        } else {
            PlayerEssenceKnowledge.lockAndResetAnalysis(target, definition);
        }
        PlayerEssence.syncOpenMenu(target);

        String playerName = target.getGameProfile().getName();
        String action = unlock ? "Unlocked " : "Locked ";
        source.sendSuccess(
                () -> Component.literal(action + itemId + " for " + playerName + "."),
                true
        );
        return 1;
    }

    private static String formatAmount(long amount) {
        return String.format(Locale.ROOT, "%,d", Math.max(0L, amount));
    }

    private static MateriaTableBlockEntity findTable(ServerPlayer player) {
        if (player.containerMenu instanceof MateriaTableMenu menu && menu.tableBlockEntity() != null) {
            return menu.tableBlockEntity();
        }

        HitResult hit = player.pick(8.0D, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit) {
            BlockEntity blockEntity = player.level().getBlockEntity(blockHit.getBlockPos());
            if (blockEntity instanceof MateriaTableBlockEntity table) {
                return table;
            }
        }
        return null;
    }

    private enum EssenceOperation {
        GIVE,
        REMOVE,
        SET
    }
}

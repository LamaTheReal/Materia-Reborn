package com.materiareborn.command;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.config.MateriaConfig;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.essence.PlayerEssenceKnowledge;
import com.materiareborn.menu.MateriaTableMenu;
import com.materiareborn.progression.PlayerMateriaProgress;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MateriaCommands {
    private MateriaCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher(), "materia");
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource())))
                .then(Commands.literal("unlock")
                        .then(Commands.literal("all")
                                .executes(context -> unlockAll(context.getSource()))))
                .then(Commands.literal("essence")
                        .then(Commands.literal("give")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(context -> essence(
                                                context.getSource(),
                                                EssenceOperation.GIVE,
                                                LongArgumentType.getLong(context, "amount")
                                        ))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(context -> essence(
                                                context.getSource(),
                                                EssenceOperation.REMOVE,
                                                LongArgumentType.getLong(context, "amount")
                                        ))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(context -> essence(
                                                context.getSource(),
                                                EssenceOperation.SET,
                                                LongArgumentType.getLong(context, "amount")
                                        ))))));
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
    private static int reset(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerEssence.set(player, 0);
        PlayerEssenceKnowledge.reset(player);
        PlayerMateriaProgress.reset(player);
        MateriaTableBlockEntity table = findTable(player);
        if (table != null) {
            table.resetProgression();
        }
        PlayerEssence.syncOpenMenu(player);
        source.sendSuccess(() -> Component.literal("Materia reset complete."), true);
        return 1;
    }

    private static int essence(CommandSourceStack source, EssenceOperation operation, long amount)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        long value = switch (operation) {
            case GIVE -> PlayerEssence.add(player, amount);
            case REMOVE -> PlayerEssence.remove(player, amount);
            case SET -> PlayerEssence.set(player, amount);
        };
        PlayerEssence.syncOpenMenu(player);
        source.sendSuccess(() -> Component.literal("Materia essence: " + value), true);
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
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

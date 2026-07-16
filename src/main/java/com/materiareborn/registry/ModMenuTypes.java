package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import com.materiareborn.menu.MateriaTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ModConstants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MateriaTableMenu>> MATERIA_TABLE =
            MENU_TYPES.register("materia_table", () -> IMenuTypeExtension.create(MateriaTableMenu::fromNetwork));

    private ModMenuTypes() {
    }
}

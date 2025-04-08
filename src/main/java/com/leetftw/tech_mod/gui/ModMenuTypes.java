package com.leetftw.tech_mod.gui;

import com.leetftw.tech_mod.LeetTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes
{
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, LeetTechMod.MOD_ID);

    public static final Supplier<MenuType<GemRefineryMenu>> GEM_REFINERY_MENU = MENUS.register("gem_refinery_menu",
            resourceLocation -> IMenuTypeExtension.create(GemRefineryMenu::new));

    public static final Supplier<MenuType<CrystallizerMenu>> CRYSTALLIZER_MENU = MENUS.register("crystallizer_menu",
            resourceLocation -> IMenuTypeExtension.create(CrystallizerMenu::new));

    public static final Supplier<MenuType<CrystalInjectorMenu>> CRYSTAL_INJECTOR_MENU = MENUS.register("crystal_injector_menu",
            resourceLocation -> IMenuTypeExtension.create(CrystalInjectorMenu::new));

    public static final Supplier<MenuType<QuarryMenu>> QUARRY_MENU = MENUS.register("quarry_menu",
            resourceLocation -> IMenuTypeExtension.create(QuarryMenu::new));

    public static void register(IEventBus bus)
    {
        MENUS.register(bus);
    }
}

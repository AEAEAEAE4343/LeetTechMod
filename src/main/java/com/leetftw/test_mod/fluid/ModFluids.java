package com.leetftw.test_mod.fluid;

import com.leetftw.test_mod.LeetTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids
{
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, LeetTechMod.MOD_ID);

    public static final DeferredHolder<FluidType, LiquidAestheticFluidType> LIQUID_AESTHETIC_TYPE = FLUID_TYPES.register("liquid_aesthetic",
            () -> new LiquidAestheticFluidType(FluidType.Properties.create()));

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, LeetTechMod.MOD_ID);

    public static final DeferredHolder<Fluid, LiquidAestheticFluid.Source> LIQUID_AESTHETIC =
            FLUIDS.register("liquid_aesthetic", LiquidAestheticFluid.Source::new);
    public static final DeferredHolder<Fluid, LiquidAestheticFluid.Flowing> LIQUID_AESTHETIC_FLOWING =
            FLUIDS.register("liquid_aesthetic_flowing", LiquidAestheticFluid.Flowing::new);

    public static void register(IEventBus bus)
    {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }
}

package com.leetftw.tech_mod.item;

import com.leetftw.tech_mod.LeetTechMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LeetTechMod.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> ENERGY_STORED = REGISTRAR.registerComponentType(
            "energy_stored",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(Codec.INT)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ByteBufCodecs.INT)
    );
    public static final Supplier<DataComponentType<Integer>> ENERGY_CAPACITY = REGISTRAR.registerComponentType(
            "energy_capacity",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(Codec.INT)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ByteBufCodecs.INT)
    );
    public static final Supplier<DataComponentType<ResourceLocation>> MACHINE_UPGRADE = REGISTRAR.registerComponentType(
            "machine_upgrade",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(ResourceLocation.CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
    );
}

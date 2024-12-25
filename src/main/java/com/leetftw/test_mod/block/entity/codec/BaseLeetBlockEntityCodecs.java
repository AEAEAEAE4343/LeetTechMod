package com.leetftw.test_mod.block.entity.codec;

import com.leetftw.test_mod.LeetTechMod;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BaseLeetBlockEntityCodecs
{
    public static final Codec<Integer> ENERGY_CODEC = Codec.INT;
    public static final StreamCodec<ByteBuf, Integer> ENERGY_STREAM_CODEC = ByteBufCodecs.INT;

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LeetTechMod.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> ENERGY_STORED = REGISTRAR.registerComponentType(
            "energy_stored",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(ENERGY_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ENERGY_STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<Integer>> ENERGY_CAPACITY = REGISTRAR.registerComponentType(
            "energy_capacity",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(ENERGY_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ENERGY_STREAM_CODEC)
    );
}

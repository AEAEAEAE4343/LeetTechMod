package com.leetftw.tech_mod.datagen.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class ClientItemDefinition
{
    public static final Codec<ClientItemDefinition> CLIENT_ITEM_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                ClientItemModelDefinition.MODEL_CODEC.fieldOf("model").forGetter(ClientItemDefinition::model)
        ).apply(instance, ClientItemDefinition::new)
    );

    private final ClientItemModelDefinition m;

    public ClientItemDefinition(ClientItemModelDefinition model)
    {
        m = model;
    }

    public static ClientItemDefinition fromResourceLocation(ResourceLocation location, String path)
    {
        return new ClientItemDefinition(ClientItemModelDefinition.fromResourceLocation(location, path));
    }

    public ClientItemModelDefinition model() { return m; }
}

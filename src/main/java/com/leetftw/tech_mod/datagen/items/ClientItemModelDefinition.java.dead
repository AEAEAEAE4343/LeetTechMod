package com.leetftw.tech_mod.datagen.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class ClientItemModelDefinition
{
    public static final Codec<ClientItemModelDefinition> MODEL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("type").forGetter(ClientItemModelDefinition::type),
                    Codec.STRING.fieldOf("model").forGetter(ClientItemModelDefinition::model)
            ).apply(instance, ClientItemModelDefinition::new)
    );

    private final String t;
    private final String m;

    public ClientItemModelDefinition(String type, String model)
    {
        t = type;
        m = model;
    }

    public static ClientItemModelDefinition fromResourceLocation(ResourceLocation location, String path)
    {
        return new ClientItemModelDefinition("minecraft:model",
                path == null ? location.withPrefix("item/").toString()
                        : location.withPrefix(path + "/").toString());
    }

    public String type() { return t; }
    public String model() { return m; }
}

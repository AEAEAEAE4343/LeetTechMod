package com.leetftw.tech_mod.client.render.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineUpgradeItemModel implements ItemModel
{
    Map<String, BakedModel> modelMap;

    public MachineUpgradeItemModel(Map<String, BakedModel> models)
    {
        super();
        this.modelMap = models;
    }

    private BakedModel getOverlayModel(ItemStack stack)
    {
        return modelMap.getOrDefault(stack.get(ModDataComponents.MACHINE_UPGRADE).toString(), modelMap.get(MachineUpgrade.BLANK_KEY.toString()));
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed)
    {
        ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.STANDARD;
        BakedModel model = getOverlayModel(stack);

        ItemStackRenderState.LayerRenderState layerState = state.newLayer();
        layerState.setupBlockModel(model, RenderType.CUTOUT);
    }

    public record Unbaked(ResourceLocation model) implements ItemModel.Unbaked {
        static ItemModelGenerator modelGenerator = new ItemModelGenerator();

        // The map codec to register
        public static final MapCodec<MachineUpgradeItemModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(ResourceLocation.CODEC.fieldOf("model").forGetter(MachineUpgradeItemModel.Unbaked::model))
                        .apply(instance, MachineUpgradeItemModel.Unbaked::new)
        );

        @Override
        public ItemModel bake(ItemModel.BakingContext context)
        {
            BakedModel baked = context.bake(this.model);
            Map<String, BakedModel> modelMap = new HashMap<String, BakedModel>();
            modelMap.put("leet_tech:base", baked);

            ResourceLocation atlasLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png");

            // TEMPORARY WORKAROUND
            // MINECRAFT 1.21.5 WILL LIKELY HAVE A WAY OF DOING
            // THIS NATIVELY WITHOUT REFLECTION !!!
            List<ResourceLocation> textures;
            try
            {
                // Texture atlas isn't finalized so we need to find the unfinished atlas through reflection
                Field this0_field = FieldUtils.getAllFields(context.blockModelBaker().sprites().getClass())[1];
                this0_field.setAccessible(true);
                Object this0 = this0_field.get(context.blockModelBaker().sprites());

                Field atlasPreperations_field = FieldUtils.getAllFields(this0.getClass())[0];
                atlasPreperations_field.setAccessible(true);
                HashMap<ResourceLocation, AtlasSet.StitchResult> atlastPreperations = (HashMap<ResourceLocation, AtlasSet.StitchResult>) atlasPreperations_field.get(this0);
                AtlasSet.StitchResult blockAtlas = atlastPreperations.get(atlasLocation);

                Field preparations_field = FieldUtils.getAllFields(blockAtlas.getClass())[1];
                preparations_field.setAccessible(true);
                SpriteLoader.Preparations preperations = (SpriteLoader.Preparations) preparations_field.get(blockAtlas);
                preperations.regions();

                textures = preperations.regions().keySet().stream()
                        .filter(loc -> loc.getPath().startsWith("item/leet_tech_machine_upgrade_")).toList();
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Leet knew this was going to happen yet did it anyway: " + e.toString());
            }

            context.blockModelBaker().sprites().get(new Material(atlasLocation, ResourceLocation.fromNamespaceAndPath("leet_tech", "item/leet_tech_machine_upgrade_speed_tier_1")));

            for (ResourceLocation texture : textures)
            {
                String id = texture.getPath().replace("item/leet_tech_machine_upgrade_", "");

                JsonObject textureMap = new JsonObject();
                textureMap.add("layer0", new JsonPrimitive("leet_tech:item/machine_upgrade_base"));
                textureMap.add("layer1", new JsonPrimitive(texture.toString()));
                TextureSlots.Resolver resolver = new TextureSlots.Resolver();
                resolver.addFirst(TextureSlots.parseTextureMap(textureMap, atlasLocation));
                TextureSlots slots = resolver.resolve(() -> "LEETTECH MACHINE UPGRADE ITEM MODEL");

                ModelState modelState = new SimpleModelState(Transformation.identity());
                BakedModel model = modelGenerator.bake(slots, context.blockModelBaker(), modelState, false, false, ItemTransforms.NO_TRANSFORMS);

                modelMap.put(texture.getNamespace() + ":" + id, model);
            }

            return new MachineUpgradeItemModel(modelMap);
        }

        @Override
        public MapCodec<MachineUpgradeItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.resolve(this.model);
        }
    }
}

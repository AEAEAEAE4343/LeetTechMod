package com.leetftw.tech_mod.client.render.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        BakedModel model = getOverlayModel(stack);
        ItemStackRenderState.LayerRenderState layerState = state.newLayer();
        if (stack.hasFoil()) layerState.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        layerState.setupBlockModel(model, RenderType.CUTOUT);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        static final ItemModelGenerator modelGenerator = new ItemModelGenerator();
        static final ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "item/machine_upgrade");
        static final ResourceLocation itemGenerated = ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated");
        static final ResourceLocation atlasLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png");
        static final Unbaked instance = new Unbaked();

        // The map codec to register
        public static final MapCodec<MachineUpgradeItemModel.Unbaked> MAP_CODEC = MapCodec.unit(instance);

        private Tuple<String, BakedModel> createModelFromTexture(ResourceLocation textureLocation, ModelBaker baker, ItemTransforms transforms)
        {
            String id = textureLocation.getNamespace() + ":" + textureLocation.getPath().replace("item/leet_tech_machine_upgrade_", "");

            JsonObject textureMap = new JsonObject();
            textureMap.add("layer0", new JsonPrimitive(baseTexture.toString()));
            if (!textureLocation.equals(baseTexture)) textureMap.add("layer1", new JsonPrimitive(textureLocation.toString()));
            TextureSlots.Resolver resolver = new TextureSlots.Resolver();
            resolver.addFirst(TextureSlots.parseTextureMap(textureMap, atlasLocation));
            TextureSlots slots = resolver.resolve(() -> "LEETTECH MACHINE UPGRADE ITEM MODEL: " + id);
            ModelState modelState = new SimpleModelState(Transformation.identity());
            return new Tuple<>(id, modelGenerator.bake(slots, baker, modelState, false, false, transforms));
        }

        private boolean isUpgradeTexture(ResourceLocation loc)
        {
            return loc.getPath().startsWith("item/leet_tech_machine_upgrade_");
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context)
        {
            BakedModel baked = context.bake(itemGenerated);
            ItemTransforms transforms = baked.getTransforms();

            Map<String, BakedModel> modelMap = new HashMap<String, BakedModel>();
            modelMap.put(MachineUpgrade.BLANK_KEY.toString(), createModelFromTexture(baseTexture, context.blockModelBaker(), transforms).getB());

            // TEMPORARY WORKAROUND
            // MINECRAFT 1.21.5 WILL LIKELY HAVE A WAY OF DOING
            // THIS NATIVELY WITHOUT REFLECTION !!!
            List<ResourceLocation> textures = new ArrayList<>();
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

                preperations.regions().keySet().stream()
                        .filter(this::isUpgradeTexture)
                        .forEach(textures::add);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Leet knew this was going to happen yet did it anyway: " + e.toString());
            }

            for (ResourceLocation texture : textures)
            {
                Tuple<String, BakedModel> modelTuple = createModelFromTexture(texture, context.blockModelBaker(), transforms);
                modelMap.put(modelTuple.getA(), modelTuple.getB());
            }

            return new MachineUpgradeItemModel(modelMap);
        }

        @Override
        public MapCodec<MachineUpgradeItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.resolve(itemGenerated);
        }
    }
}

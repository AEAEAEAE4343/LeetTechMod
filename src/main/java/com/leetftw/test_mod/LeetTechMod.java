package com.leetftw.test_mod;

import com.leetftw.test_mod.block.ModBlocks;
import com.leetftw.test_mod.block.entity.ModBlockEntities;
import com.leetftw.test_mod.block.entity.codec.BaseLeetBlockEntityCodecs;
import com.leetftw.test_mod.fluid.ModFluids;
import com.leetftw.test_mod.item.ModCreativeTabs;
import com.leetftw.test_mod.item.ModItems;
import com.leetftw.test_mod.client.gui.CrystallizerScreen;
import com.leetftw.test_mod.client.gui.GemRefineryScreen;
import com.leetftw.test_mod.gui.ModMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LeetTechMod.MOD_ID)
public class LeetTechMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "leet_tech";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public LeetTechMod(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register block entity components
        BaseLeetBlockEntityCodecs.REGISTRAR.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ModItems.AESTHETIC_DUST);
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            event.accept(ModBlocks.AESTHETIC_BLOCK_ITEM);
            event.accept(ModBlocks.BUDDING_AESTHETIC_BLOCK_ITEM);
            event.accept(ModBlocks.SMALL_AESTHETIC_BUD_ITEM);
            event.accept(ModBlocks.MEDIUM_AESTHETIC_BUD_ITEM);
            event.accept(ModBlocks.LARGE_AESTHETIC_BUD_ITEM);
            event.accept(ModBlocks.AESTHETIC_ClUSTER_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event)
        {
            event.registerFluidType(new IClientFluidTypeExtensions()
            {
                private static final ResourceLocation
                        STILL = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/liquid_aesthetic"),
                        FLOWING = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/liquid_aesthetic_flowing");

                @Override
                public @NotNull ResourceLocation getStillTexture()
                {
                    return STILL;
                }

                @Override
                public @NotNull ResourceLocation getFlowingTexture()
                {
                    return FLOWING;
                }
            }, ModFluids.LIQUID_AESTHETIC_TYPE.get());
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event)
        {
            LOGGER.info("Registering menu for Gem Refinery");
            event.register(ModMenuTypes.GEM_REFINERY_MENU.get(), GemRefineryScreen::new);

            LOGGER.info("Registering menu for Crystallizer");
            event.register(ModMenuTypes.CRYSTALLIZER_MENU.get(), CrystallizerScreen::new);
        }
    }
}

package com.github.sajmon.labyrythm;

import org.slf4j.Logger;

import com.github.sajmon.labyrythm.item.ModCreativeTabs;
import com.mojang.logging.LogUtils;
import com.github.sajmon.labyrythm.item.ModItems;
import com.github.sajmon.labyrythm.potion.ModPotions;
import com.github.sajmon.labyrythm.structures.ModStructures;
import com.github.sajmon.labyrythm.structures.pieces.ModStructurePieces;
import com.github.sajmon.labyrythm.entity.ModEntityTypes;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;
import com.github.sajmon.labyrythm.entity.ModActivities;
import com.github.sajmon.labyrythm.event.ModEvents;
import com.github.sajmon.labyrythm.brewing.ModBrewingRecipes;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Labyrythm.MOD_ID)
public class Labyrythm
{
    public static final String MOD_ID = "labyrythm";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Labyrythm(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ModEvents.class);
        
        ModEntityTypes.register(modEventBus);
        ModActivities.register(modEventBus);
        
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModPotions.register(modEventBus); // Register potions
        
        ModStructures.register(modEventBus);
        ModStructurePieces.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            // Register brewing recipes
            ModBrewingRecipes.register();
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.MINOTAUR.get(), MinotaurEntity.createAttributes().build());
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }
    }
}

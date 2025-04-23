package com.github.sajmon.labyrythm.client;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.client.model.MinotaurModel;
import com.github.sajmon.labyrythm.client.model.ModModelLayers;
import com.github.sajmon.labyrythm.client.renderer.MinotaurRenderer;
import com.github.sajmon.labyrythm.entity.ModEntityTypes;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Labyrythm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistration {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.MINOTAUR.get(), MinotaurRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.MINOTAUR, () -> MinotaurModel.createBodyLayer());
    }
}

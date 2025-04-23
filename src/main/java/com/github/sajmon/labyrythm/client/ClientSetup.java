package com.github.sajmon.labyrythm.client;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.client.animation.AnimationLoader;
import com.github.sajmon.labyrythm.client.animation.ResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = Labyrythm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AnimationLoader.loadAnimations(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "animations/minotaur.animation.json"));
        });
    }
    
    @SubscribeEvent
    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceReloadListener());
    }
}

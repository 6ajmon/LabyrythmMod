package com.github.sajmon.labyrythm.client.animation;

import com.github.sajmon.labyrythm.Labyrythm;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ResourceReloadListener extends SimplePreparableReloadListener<Void> {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
        AnimationLoader.reset();
        AnimationLoader.loadAnimations(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "animations/minotaur.animation.json"));
    }
}

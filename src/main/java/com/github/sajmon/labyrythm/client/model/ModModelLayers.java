package com.github.sajmon.labyrythm.client.model;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation MINOTAUR = 
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur"), "main");
}
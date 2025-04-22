package com.github.sajmon.labyrythm.client.renderer;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.client.model.MinotaurModel;
import com.github.sajmon.labyrythm.client.model.ModModelLayers;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MinotaurRenderer extends MobRenderer<MinotaurEntity, MinotaurModel<MinotaurEntity>> {
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "textures/entity/minotaur.png");

    public MinotaurRenderer(EntityRendererProvider.Context context) {
        super(context, new MinotaurModel<>(context.bakeLayer(ModModelLayers.MINOTAUR)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MinotaurEntity entity) {
        return TEXTURE;
    }
}
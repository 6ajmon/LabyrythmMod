package com.github.sajmon.labyrythm.client.renderer;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.client.model.MinotaurModel;
import com.github.sajmon.labyrythm.client.model.ModModelLayers;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

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

    @Override
    public void render(MinotaurEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource buffer, int packedLight) {
        // Get the animation controller if you're using GeckoLib, or however you're handling animations
        String animState = entity.getAnimationState();
        
        // Play the appropriate animation based on state
        // This code will vary based on your animation system, but conceptually:
        switch (animState) {
            case "idle":
                // Play idle animation
                break;
            case "walk":
                // Play walk animation
                break;
            case "run":
                // Play run animation
                break;
            case "attack":
                // Play attack animation
                break;
        }
        
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }
}
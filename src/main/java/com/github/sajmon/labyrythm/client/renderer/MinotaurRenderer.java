package com.github.sajmon.labyrythm.client.renderer;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.client.model.MinotaurModel;
import com.github.sajmon.labyrythm.client.model.ModModelLayers;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class MinotaurRenderer extends MobRenderer<MinotaurEntity, MinotaurModel<MinotaurEntity>> {
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "textures/entity/minotaur.png");

    public MinotaurRenderer(EntityRendererProvider.Context context) {
        super(context, new MinotaurModel<>(context.bakeLayer(ModModelLayers.MINOTAUR)), 0.5F);
        
        // Explicitly cast to the correct type to ensure item rendering works
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(MinotaurEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MinotaurEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        
        // No need to manually adjust animation state here - entity tick handles it
        
        // Render the entity
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        
        matrixStack.popPose();
    }
}
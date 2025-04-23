package com.github.sajmon.labyrythm.client.model;

import com.github.sajmon.labyrythm.entity.MinotaurEntity;
import com.github.sajmon.labyrythm.client.animation.AnimationLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class MinotaurModel<T extends MinotaurEntity> extends EntityModel<T> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public MinotaurModel(ModelPart root) {
        this.root = root;
        // Use the exact names from your JSON file
        this.head = root.getChild("Head");
        this.body = root.getChild("Body");
        this.leftArm = root.getChild("Left Arm");
        this.rightArm = root.getChild("Right Arm");
        this.leftLeg = root.getChild("Left Leg");
        this.rightLeg = root.getChild("Right Leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // HEAD
        PartDefinition head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), 
                PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Hat layer - same dimensions as head with deformation
        head.addOrReplaceChild("Hat Layer", CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        // BODY
        PartDefinition body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create()
                .texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), 
                PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Body layer - same dimensions as body with deformation
        body.addOrReplaceChild("Body Layer", CubeListBuilder.create()
                .texOffs(16, 32)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        // RIGHT ARM
        PartDefinition rightArm = partdefinition.addOrReplaceChild("Right Arm", CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), 
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        
        // Right arm layer - same dimensions with deformation
        rightArm.addOrReplaceChild("Right Arm Layer", CubeListBuilder.create()
                .texOffs(40, 32)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        // LEFT ARM
        PartDefinition leftArm = partdefinition.addOrReplaceChild("Left Arm", CubeListBuilder.create()
                .texOffs(32, 48)
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), 
                PartPose.offset(5.0F, 2.0F, 0.0F));
        
        // Left arm layer - same dimensions with deformation
        leftArm.addOrReplaceChild("Left Arm Layer", CubeListBuilder.create()
                .texOffs(48, 48)
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        // LEFT LEG
        PartDefinition leftLeg = partdefinition.addOrReplaceChild("Left Leg", CubeListBuilder.create()
                .texOffs(16, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), 
                PartPose.offset(2.0F, 12.0F, 0.0F));
        
        // Left leg layer - same dimensions with deformation
        leftLeg.addOrReplaceChild("Left Leg Layer", CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        // RIGHT LEG
        PartDefinition rightLeg = partdefinition.addOrReplaceChild("Right Leg", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), 
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        
        // Right leg layer - same dimensions with deformation
        rightLeg.addOrReplaceChild("Right Leg Layer", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, 
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset model parts
        this.head.xRot = 0;
        this.head.yRot = 0;
        this.head.zRot = 0;
        this.rightArm.xRot = 0;
        this.rightArm.yRot = 0;
        this.rightArm.zRot = 0;
        this.leftArm.xRot = 0;
        this.leftArm.yRot = 0;
        this.leftArm.zRot = 0;
        this.rightLeg.xRot = 0;
        this.rightLeg.yRot = 0;
        this.rightLeg.zRot = 0;
        this.leftLeg.xRot = 0;
        this.leftLeg.yRot = 0;
        this.leftLeg.zRot = 0;
        
        String animState = entity.getAnimationState();
        
        this.head.xRot += headPitch * ((float)Math.PI / 180F);
        this.head.yRot += netHeadYaw * ((float)Math.PI / 180F);
        
        switch (animState) {
            case "idle":
                applyIdleAnimation(ageInTicks);
                break;
            case "walk":
                applyWalkAnimation(limbSwing, limbSwingAmount, ageInTicks);
                break;
            case "run":
                applyRunAnimation(limbSwing, limbSwingAmount, ageInTicks);
                break;
            case "attack":
                applyRunAnimation(limbSwing, limbSwingAmount, ageInTicks);
                break;
            default:
                applyIdleAnimation(ageInTicks);
                break;
        }
    }

    private void applyIdleAnimation(float ageInTicks) {
        float animLength = AnimationLoader.getAnimationLength("idle");
        float animTime = (ageInTicks % (animLength * 20)) / (animLength * 20);
        
        float[] headRotation = AnimationLoader.getInterpolatedRotation("idle", "Head", animTime);
        this.head.xRot += headRotation[0] * ((float)Math.PI / 180F);
        this.head.yRot += headRotation[1] * ((float)Math.PI / 180F);
        this.head.zRot += headRotation[2] * ((float)Math.PI / 180F);
        
        float[] rightArmRotation = AnimationLoader.getInterpolatedRotation("idle", "Right Arm", animTime);
        this.rightArm.xRot += rightArmRotation[0] * ((float)Math.PI / 180F);
        this.rightArm.yRot += rightArmRotation[1] * ((float)Math.PI / 180F);
        this.rightArm.zRot += rightArmRotation[2] * ((float)Math.PI / 180F);
        
        float[] leftArmRotation = AnimationLoader.getInterpolatedRotation("idle", "Left Arm", animTime);
        this.leftArm.xRot += leftArmRotation[0] * ((float)Math.PI / 180F);
        this.leftArm.yRot += leftArmRotation[1] * ((float)Math.PI / 180F);
        this.leftArm.zRot += leftArmRotation[2] * ((float)Math.PI / 180F);
    }

    private void applyWalkAnimation(float limbSwing, float limbSwingAmount, float ageInTicks) {
        float animationSpeed = 0.2F;
        float phase = limbSwing * animationSpeed;
        
        this.rightArm.xRot += Mth.cos(phase * (float)Math.PI + (float)Math.PI) * 10.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.leftArm.xRot += Mth.cos(phase * (float)Math.PI) * 10.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.rightLeg.xRot += Mth.cos(phase * (float)Math.PI) * 25.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.leftLeg.xRot += Mth.cos(phase * (float)Math.PI + (float)Math.PI) * 25.0F * (float)Math.PI / 180F * limbSwingAmount;
    }

    private void applyRunAnimation(float limbSwing, float limbSwingAmount, float ageInTicks) {
        float animationSpeed = 0.1F;
        float phase = limbSwing * animationSpeed;
        
        this.rightArm.xRot = -105.0F * (float)Math.PI / 180F;
        this.rightArm.xRot += Mth.cos(phase * (float)Math.PI * 2) * 20.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.leftArm.xRot += Mth.cos(phase * (float)Math.PI * 2) * 40.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.rightLeg.xRot += Mth.cos(phase * (float)Math.PI * 2) * 65.0F * (float)Math.PI / 180F * limbSwingAmount;
        this.leftLeg.xRot += Mth.cos(phase * (float)Math.PI * 2 + (float)Math.PI) * 65.0F * (float)Math.PI / 180F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, 
                               int packedLight, int packedOverlay, int packedColor) {
        // Render base model
        head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }

    // axe handling
    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        boolean rightArm = arm == HumanoidArm.RIGHT;
        ModelPart armPart = rightArm ? this.rightArm : this.leftArm;
        
        armPart.translateAndRotate(poseStack);
        
    }
}
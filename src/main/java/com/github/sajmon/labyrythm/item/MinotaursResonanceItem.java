package com.github.sajmon.labyrythm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MinotaursResonanceItem extends AxeItem {
    // Constants for dash mechanics
    private static final int MAX_USE_DURATION = 72000;
    private static final int MIN_CHARGE_TICKS = 60;
    private static final int FULL_CHARGE_TICKS = 60;
    private static final int DASH_COOLDOWN_TICKS = 240;
    private static final double DASH_FORCE = 3.0D;
    private static final double DASH_ATTACK_RANGE = 6.0D;
    
    public MinotaursResonanceItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, true));
        
        // Call the parent method to ensure normal axe behavior
        return super.hurtEnemy(stack, target, attacker);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Check if player is on cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        
        // Start using the item (charging)
        player.startUsingItem(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.5F, 0.8F);
        return InteractionResultHolder.consume(itemstack);
    }
    
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!(entity instanceof Player)) return;
        
        int chargeTicks = getUseDuration(stack) - remainingUseTicks;
        
        // Play charging sounds at intervals
        if (chargeTicks % 5 == 0 && chargeTicks > 0) {
            float pitch = 0.8F + (chargeTicks / (float)FULL_CHARGE_TICKS) * 0.4F;
            pitch = Mth.clamp(pitch, 0.8F, 1.2F);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                    SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.5F, pitch);
        }
        
        // Visual feedback for charging
        if (level.isClientSide && chargeTicks > MIN_CHARGE_TICKS) {
            // Add client-side particles at regular intervals
            if (chargeTicks % 2 == 0) {
                double x = entity.getX() + (Math.random() - 0.5) * 0.5;
                double y = entity.getY() + entity.getEyeHeight() - 0.2 + (Math.random() - 0.5) * 0.5;
                double z = entity.getZ() + (Math.random() - 0.5) * 0.5;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP, 
                        x, y, z, 0, 0, 0);
            }
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        
        int chargeTicks = getUseDuration(stack) - timeLeft;
        
        if (chargeTicks < MIN_CHARGE_TICKS) {
            return;
        }
        
        // Calculate dash power based on charge time
        float chargeRatio = Math.min(1.0F, (float) chargeTicks / FULL_CHARGE_TICKS);
        double dashForce = DASH_FORCE * chargeRatio;
        
        // Execute the dash
        performDash(level, player, dashForce);
        
        // Set cooldown
        player.getCooldowns().addCooldown(this, DASH_COOLDOWN_TICKS);
        
        // Play dash sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 
                0.5F, 0.8F + chargeRatio * 0.4F);
        
        // Add usage statistic
        player.awardStat(Stats.ITEM_USED.get(this));
    }
    
    private void performDash(Level level, Player player, double dashForce) {
        // Get the direction the player is looking
        Vec3 lookVec = player.getLookAngle();
        
        // Apply momentum to the player
        player.setDeltaMovement(lookVec.x * dashForce, 
                                lookVec.y * dashForce * 0.5,
                                lookVec.z * dashForce);
        
        // Disable fall damage for a brief period
        player.fallDistance = 0;
        
        // Attack entities in front of the player
        AABB attackBox = player.getBoundingBox().inflate(DASH_ATTACK_RANGE)
                .move(lookVec.scale(DASH_ATTACK_RANGE * 0.5));
        
        List<Entity> entities = level.getEntities(player, attackBox, 
                entity -> entity instanceof LivingEntity && entity != player);
        
        if (!entities.isEmpty()) {
            // Get the closest entity
            Entity target = entities.get(0);
            if (entities.size() > 1) {
                target = entities.stream()
                        .min((e1, e2) -> Double.compare(
                                e1.distanceToSqr(player), 
                                e2.distanceToSqr(player)))
                        .orElse(entities.get(0));
            }
            
            // Attack the entity
            if (target instanceof LivingEntity livingTarget) {
                // Deal damage with increased knockback
                player.attack(livingTarget);
                
                // Apply darkness effect
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, true));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1, false, true));
                
                // Apply knockback
                double knockbackStrength = 1.0;
                Vec3 knockbackDir = target.position().subtract(player.position()).normalize();
                target.push(knockbackDir.x * knockbackStrength, 0.3, knockbackDir.z * knockbackStrength);
            }
        }
    }
    
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_DURATION;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE; // Changed from UseAnim.SPEAR to UseAnim.NONE
    }
    
    // Make sure we don't prevent using the item by adding this method
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }
    
    // Ensure UseAnimation is properly shown
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return stack;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Add custom tooltips after
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip")
                   .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip2")
                   .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click and hold to charge a dash attack")
                   .withStyle(ChatFormatting.DARK_AQUA));
    }
    
    // The item needs to be marked as usable in combat
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    // For debugging
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (isSelected && entity instanceof Player player && level.isClientSide() && player.getUseItem() == stack) {
            int chargeTicks = player.getUseItemRemainingTicks();
        }
    }
}

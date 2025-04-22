package com.github.sajmon.labyrythm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class MinotaurEntity extends Monster implements NeutralMob {
    // Range for detecting sounds
    private static final int SOUND_DETECTION_RANGE = 16;
    // Cooldown for sound detection (in ticks)
    private int soundDetectionCooldown = 0;
    // Last position where sound was detected
    private BlockPos lastSoundPosition = null;
    // Required by NeutralMob interface
    private int angerTime;
    private UUID angerTarget;

    public MinotaurEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        // Set XP reward
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MinotaurSoundDetectionGoal(this));
        this.goalSelector.addGoal(3, new MinotaurMoveToSoundGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8D));
        
        // The minotaur is blind, so we don't add LookAtPlayerGoal
        
        // Target anything that hurts it
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Target players if they make sound
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, 
                (livingEntity) -> this.canDetectEntity(livingEntity)));
        // Also target iron golems if they make sound
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, 10, true, false, 
                (livingEntity) -> this.canDetectEntity(livingEntity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    // Method to check if the minotaur can detect an entity based on sound
    private boolean canDetectEntity(LivingEntity target) {
        // Entities are only detected if they recently moved or are attacking
        return target.getSpeed() > 0.1D || 
               target.isUsingItem() || 
               target.hurtMarked;
    }

    // Handle sound detection logic
    public void onSoundHeard(BlockPos soundPos) {
        if (this.soundDetectionCooldown <= 0) {
            this.lastSoundPosition = soundPos;
            this.soundDetectionCooldown = 40; // 2 seconds cooldown
            
            // Become angry
            if (this.getTarget() == null) {
                // Look for an entity near the sound source
                this.alertEntitiesNearSound(soundPos);
            }
        }
    }

    private void alertEntitiesNearSound(BlockPos soundPos) {
        AABB searchBox = new AABB(soundPos).inflate(5.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, 
                (entity) -> entity != this && !(entity instanceof MinotaurEntity) && entity.isAlive());
        
        if (!entities.isEmpty()) {
            LivingEntity target = entities.get(this.random.nextInt(entities.size()));
            this.setTarget(target);
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        if (this.soundDetectionCooldown > 0) {
            this.soundDetectionCooldown--;
        }
    }

    // Override sounds
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
    }

    public SoundEvent getAttackSound() {
        return SoundEvents.COW_AMBIENT; // Or COW_HURT for more aggressive sound
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(40);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.angerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setPersistentAngerTarget(UUID target) {
        this.angerTarget = target;
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.angerTarget;
    }

    @Override
    public boolean isAngryAt(LivingEntity entity) {
        // The minotaur is angry when it has a target or when it detected a sound recently
        return this.getTarget() != null || this.lastSoundPosition != null;
    }

    // Custom goal for sound detection
    private static class MinotaurSoundDetectionGoal extends Goal {
        private final MinotaurEntity minotaur;
        private int soundCheckCooldown = 0;

        public MinotaurSoundDetectionGoal(MinotaurEntity minotaur) {
            this.minotaur = minotaur;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true; // Always active
        }

        @Override
        public void tick() {
            if (soundCheckCooldown <= 0) {
                Level level = this.minotaur.level();
                
                // Check for nearby entities making noise
                AABB searchBox = this.minotaur.getBoundingBox().inflate(SOUND_DETECTION_RANGE);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox, 
                        (entity) -> entity != this.minotaur && this.minotaur.canDetectEntity(entity));
                
                for (LivingEntity entity : entities) {
                    if (this.minotaur.canDetectEntity(entity)) {
                        this.minotaur.onSoundHeard(entity.blockPosition());
                        break;
                    }
                }
                
                soundCheckCooldown = 10; // Check every half second
            } else {
                soundCheckCooldown--;
            }
        }
    }

    // Custom goal to move toward sound
    private static class MinotaurMoveToSoundGoal extends Goal {
        private final MinotaurEntity minotaur;
        private int pathfindDelay = 0;

        public MinotaurMoveToSoundGoal(MinotaurEntity minotaur) {
            this.minotaur = minotaur;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.minotaur.lastSoundPosition != null && 
                  this.minotaur.getTarget() == null;
        }

        @Override
        public void start() {
            this.pathfindDelay = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.minotaur.lastSoundPosition != null && 
                  this.minotaur.getTarget() == null;
        }

        @Override
        public void tick() {
            if (this.minotaur.lastSoundPosition != null) {
                if (pathfindDelay <= 0) {
                    this.minotaur.getNavigation().moveTo(
                        this.minotaur.lastSoundPosition.getX(),
                        this.minotaur.lastSoundPosition.getY(),
                        this.minotaur.lastSoundPosition.getZ(),
                        1.0D
                    );
                    pathfindDelay = 10; // Update path every half second
                } else {
                    pathfindDelay--;
                }
                
                // If we reached the sound position, clear it
                Vec3 targetPos = Vec3.atCenterOf(this.minotaur.lastSoundPosition);
                if (this.minotaur.position().distanceToSqr(targetPos) < 4.0) {
                    this.minotaur.lastSoundPosition = null;
                }
            }
        }
    }
}
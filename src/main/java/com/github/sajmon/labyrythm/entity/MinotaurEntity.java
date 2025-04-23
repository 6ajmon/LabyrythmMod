package com.github.sajmon.labyrythm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public class MinotaurEntity extends Monster implements NeutralMob, VibrationSystem {
    // Entity data
    private static final EntityDataAccessor<Integer> ANGER_LEVEL = SynchedEntityData.defineId(MinotaurEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_DASHING = SynchedEntityData.defineId(MinotaurEntity.class, EntityDataSerializers.BOOLEAN);
    
    private static final int VIBRATION_DETECTION_RANGE = 48;
    
    // Vibration system components
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    private final VibrationSystem.User vibrationUser = new MinotaurVibrationUser(this);
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    
    // NeutralMob components
    private int angerTime;
    private UUID angerTarget;
    
    // Animation states
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    public final AnimationState listenAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    
    // Dash attack properties
    private static final int DASH_COOLDOWN = 60; // 3 seconds
    private static final int DASH_DURATION = 20; // 1 second
    private static final double DASH_SPEED = 1.5;
    private int dashCooldownTicks = 0;
    private int dashDurationTicks = 0;
    
    // Target tracking
    private final Map<UUID, Boolean> hasHeardEntity = new HashMap<>();
    private BlockPos lastSoundPosition = null;
    private int soundTrackingTicks = 0;
    private static final int MAX_TRACKING_TICKS = 200; // 10 seconds

    // Vibration detection memory
    private boolean targetDetectedByVibration = false;
    private int vibrationDetectionCooldown = 0;
    private static final int VIBRATION_MEMORY_DURATION = 400; // 20 seconds

    public MinotaurEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
    }

    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER_LEVEL, 0);
        builder.define(IS_DASHING, false);
    }

    @Override
    protected void registerGoals() {
        // Goals will be handled by Brain system
    }

    @Override
    protected Brain<?> makeBrain(com.mojang.serialization.Dynamic<?> dynamic) {
        return MinotaurAi.makeBrain(this, dynamic);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<MinotaurEntity> getBrain() {
        return (Brain<MinotaurEntity>) super.getBrain();
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            listenerConsumer.accept(this.dynamicGameEventListener, serverLevel);
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        Level level = this.level();
        
        // Add this near the beginning of your tick method
        if (!level.isClientSide() && tickCount % 100 == 0) {
            // If we're not doing anything interesting, force patrol behavior
            if (this.getTarget() == null && this.lastSoundPosition == null && 
                !this.getNavigation().isInProgress()) {
                
                // Generate random patrol point
                RandomSource random = this.getRandom();
                int x = Mth.randomBetweenInclusive(random, -16, 16);
                int y = Mth.randomBetweenInclusive(random, -2, 2);
                int z = Mth.randomBetweenInclusive(random, -16, 16);
                
                BlockPos targetPos = this.blockPosition().offset(x, y, z);
                
                // Force navigation to this point
                this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                System.out.println("Forcing patrol to: " + targetPos.toShortString());
            }
        }
        
        
        // Update vibration detection memory
        if (vibrationDetectionCooldown > 0) {
            vibrationDetectionCooldown--;
            if (vibrationDetectionCooldown <= 0) {
                targetDetectedByVibration = false;
                // If the chase was based on vibration and memory faded, stop chasing
                if (this.getTarget() != null && !this.isWithinMeleeAttackRange(this.getTarget())) {
                    this.setTarget(null);
                    this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    System.out.println("Lost vibration detection memory, stopping chase");
                }
            }
        }
        
        // *** Add this new section for forcing path creation ***
        if (!level.isClientSide()) {
            // If we have a WALK_TARGET but no path, force path creation
            Brain<?> brain = this.getBrain();
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            
            if (walkTarget.isPresent() && !this.getNavigation().isInProgress()) {
                WalkTarget target = walkTarget.get();
                BlockPos targetPos = BlockPos.containing(target.getTarget().currentPosition());
                
                // Force direct navigation
                boolean success = this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                System.out.println("Forcing navigation to " + targetPos.toShortString() + ", success: " + success);
                
                // If navigation fails, try investigating potential issues
                if (!success) {
                    Path path = this.getNavigation().createPath(targetPos, 0);
                    if (path == null) {
                        System.out.println("Failed to create path to " + targetPos.toShortString());
                    } else {
                        System.out.println("Path exists but navigation failed, path length: " + path.getNodeCount());
                    }
                }
            }
        }
        // *** End of new section ***
        
        // Debug movement state
        if (!level.isClientSide() && tickCount % 20 == 0) {
            BlockPos pos = this.blockPosition();
            System.out.println("Minotaur at " + pos.toShortString() + 
                ", has path: " + (this.getNavigation().getPath() != null) + 
                ", isInProgress: " + this.getNavigation().isInProgress() +
                ", hasTarget: " + (this.getTarget() != null) +
                ", hasSound: " + (this.lastSoundPosition != null));
            
            // Add this to check attack cooldown
            Brain<?> brain = this.getBrain();
            Optional<Boolean> cooldown = brain.getMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
            System.out.println("Attack cooldown active: " + cooldown.isPresent());
            
            // Force reset cooldown if entity has been idle too long with target
            if (cooldown.isPresent() && this.getTarget() != null && 
                !this.getNavigation().isInProgress() && tickCount % 60 == 0) {
                System.out.println("Forcing cooldown reset");
                brain.eraseMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
            }
        }
        
        if (level instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        // Handle dash cooldown
        if (dashCooldownTicks > 0) {
            dashCooldownTicks--;
        }
        
        // Handle dash duration
        if (this.isDashing()) {
            dashDurationTicks--;
            if (dashDurationTicks <= 0) {
                this.setDashing(false);
            }
            
            // Damage entities in path during dash
            if (!level.isClientSide()) {
                this.performDashAttack();
            }
        }
        
        // Handle sound tracking
        if (lastSoundPosition != null && soundTrackingTicks > 0) {
            soundTrackingTicks--;
            if (soundTrackingTicks <= 0) {
                lastSoundPosition = null;
            }
        }
        
        // Animation states
        if (level.isClientSide()) {
            if (this.isDashing()) {
                dashAnimationState.startIfStopped(this.tickCount);
            } else {
                dashAnimationState.stop();
                
                if (this.getNavigation().isInProgress()) {
                    walkAnimationState.startIfStopped(this.tickCount);
                    idleAnimationState.stop();
                } else {
                    walkAnimationState.stop();
                    idleAnimationState.startIfStopped(this.tickCount);
                }
            }
        }
        
        // Add this near the end of your tick method
        // Emergency direct attack if brain-based one isn't working
        if (!level.isClientSide() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distSq = this.distanceToSqr(target);
            
            if (distSq <= 6.0 && tickCount % 20 == 0) {
                System.out.println("Emergency direct attack!");
                this.doHurtTarget(target);
            }
        }
        
        // Add this near the beginning of your tick method, after all your other code
        
        // Modify movement speed based on activity
        if (!level.isClientSide()) {
            Optional<Activity> currentActivity = this.getBrain().getActiveNonCoreActivity();
            
            if (currentActivity.isPresent()) {
                Activity activity = currentActivity.get();
                
                // Speed multipliers
                float speedMultiplier = 1.0f;
                
                if (activity == ModActivities.CHASE.get()) {
                    // Run very fast when chasing
                    speedMultiplier = 1.8f;
                } else if (activity == ModActivities.PATROL.get() || 
                          activity == ModActivities.INVESTIGATE.get()) {
                    // Move faster than normal when patrolling or investigating
                    speedMultiplier = 1.3f;
                }
                
                // Apply speed multiplier by setting attribute modifier
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D * speedMultiplier);
            }
        }
        
        // Rest of your existing tick method...
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.getProfiler().push("minotaurBrain");
        this.getBrain().tick(serverLevel, this);
        serverLevel.getProfiler().pop();
        
        // Check for persistent anger targets when not already pursuing something
        if (this.getTarget() == null && this.getRemainingPersistentAngerTime() > 0) {
            // Try to find the entity that angered us
            if (this.getPersistentAngerTarget() != null) {
                Entity entity = ((ServerLevel)this.level()).getEntity(this.getPersistentAngerTarget());
                if (entity instanceof LivingEntity livingEntity && this.canTargetEntity(entity)) {
                    this.setTarget(livingEntity);
                    this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
                    this.targetDetectedByVibration = true;
                }
            }
        }
        
        MinotaurAi.updateActivity(this);
        
        // Update angerTime
        if (this.angerTime > 0) {
            this.angerTime--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        
        if (result && source.getEntity() instanceof LivingEntity attacker) {
            // Make sure we can target the entity that hurt us
            if (this.canTargetEntity(attacker)) {
                // Set the attacker as our target
                this.setTarget(attacker);
                this.targetDetectedByVibration = true; // Treat it like detection to enable chase
                this.vibrationDetectionCooldown = VIBRATION_MEMORY_DURATION;
                
                // Set the attack target directly in the brain
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);
                
                // Remember the attacker in case we lose sight of them
                this.setPersistentAngerTarget(attacker.getUUID());
                this.setRemainingPersistentAngerTime(400); // 20 seconds of anger
                
                System.out.println("Minotaur targeting " + attacker.getName().getString() + " after being attacked");
            }
        }
        
        return result;
    }

    // Vibration response methods
    public void onHeardVibration(Entity source, BlockPos soundPos) {
        if (source instanceof Player player && this.canTargetEntity(player)) {
            this.setTarget(player);
            this.setLastSoundPosition(soundPos);
            this.hasHeardEntity.put(source.getUUID(), true);
            
            // Mark that this target was detected by vibration
            this.targetDetectedByVibration = true;
            this.vibrationDetectionCooldown = VIBRATION_MEMORY_DURATION;
            
            // Set the attack target directly in the brain
            this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
            
            // Debug
            System.out.println("Detected player through vibration!");
        } else if (soundPos != null) {
            // For null source (player-made block sounds)
            this.setLastSoundPosition(soundPos);
            MinotaurAi.updateActivity(this);
            System.out.println("Heard vibration at " + soundPos.toShortString() + " - investigating");
        }
    }

    // Target methods
    public boolean canTargetEntity(@Nullable Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return this.level() == entity.level() && 
                   EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && 
                   !this.isAlliedTo(entity) && 
                   livingEntity.getType() != EntityType.ARMOR_STAND && 
                   !livingEntity.isInvulnerable() && 
                   !livingEntity.isDeadOrDying();
        }
        return false;
    }

    // Dash attack methods
    public boolean canDash() {
        return dashCooldownTicks <= 0;
    }
    
    public void startDash(Vec3 direction) {
        if (!this.canDash()) return;
        
        this.setDashing(true);
        this.dashDurationTicks = DASH_DURATION;
        this.dashCooldownTicks = DASH_COOLDOWN;
        
        // Set velocity in dash direction
        Vec3 normalizedDir = direction.normalize();
        this.setDeltaMovement(normalizedDir.x * DASH_SPEED, 
                             this.onGround() ? 0.1 : this.getDeltaMovement().y, 
                             normalizedDir.z * DASH_SPEED);
        
        // Play sound
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 0.5F);
    }
    
    public void startDashToTarget() {
        // Set dash state to true
        this.setDashing(true);
        
        // Get target position
        LivingEntity target = this.getTarget();
        if (target != null) {
            // Calculate direction vector to target
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            
            // Normalize direction vector
            double length = Math.sqrt(dx * dx + dz * dz);
            dx = dx / length;
            dz = dz / length;
            
            double dashForce = 2.5;
            
            // Apply dash momentum
            this.setDeltaMovement(dx * dashForce, 0.1, dz * dashForce);
            
            this.dashDurationTicks = 20;  // Slightly longer dash duration
            
            this.dashCooldownTicks = 240;
            
            // Play dash sound
            this.playSound(SoundEvents.PHANTOM_SWOOP, 2.0F, 1.0F);
        }
    }
    
    private void performDashAttack() {
        // Calculate AABB for damage area (in front of minotaur in movement direction)
        Vec3 direction = this.getDeltaMovement().normalize();
        AABB attackBox = this.getBoundingBox().inflate(1.0);
        
        // Damage all entities in the area
        for (Entity entity : this.level().getEntities(this, attackBox)) {
            if (entity != this && entity instanceof LivingEntity livingEntity && this.canTargetEntity(entity)) {
                entity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                
                // Knockback effect
                double knockbackStrength = this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                if (knockbackStrength > 0) {
                    Vec3 knockbackDir = entity.position().subtract(this.position()).normalize();
                    entity.push(knockbackDir.x * knockbackStrength, 0.3, knockbackDir.z * knockbackStrength);
                }
            }
        }
    }

    // Getters/Setters
    public boolean isDashing() {
        return this.entityData.get(IS_DASHING);
    }
    
    public void setDashing(boolean dashing) {
        this.entityData.set(IS_DASHING, dashing);
    }
    
    public void setLastSoundPosition(BlockPos pos) {
        this.lastSoundPosition = pos;
        this.soundTrackingTicks = MAX_TRACKING_TICKS;
    }
    
    public BlockPos getLastSoundPosition() {
        return this.lastSoundPosition;
    }
    
    public boolean hasHeardEntity(Entity entity) {
        return entity != null && this.hasHeardEntity.getOrDefault(entity.getUUID(), false);
    }

    public boolean wasTargetDetectedByVibration() {
        return targetDetectedByVibration;
    }

    // Helper method to check if within melee range
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return this.distanceToSqr(target) <= 6.0;
    }

    // Navigation methods
    @Override
    protected PathNavigation createNavigation(Level level) {
        // Custom navigation that can climb walls and jump high
        return new MinotaurNavigation(this, level);
    }
    
    // Used to make minotaur jump between floors (up to 7 blocks high)
    public void jumpToPosition(BlockPos targetPos) {
        if (!this.onGround()) return;
        
        double heightDifference = targetPos.getY() - this.getY();
        if (heightDifference > 0) {
            // Calculate needed velocity for the jump
            // v = sqrt(2 * g * h), where g is gravity and h is height
            double jumpVelocity = Math.sqrt(2 * 0.08 * heightDifference) * 1.2; // 0.08 is MC gravity, 1.2 is a buffer
            
            // Cap jump velocity at a reasonable value
            jumpVelocity = Math.min(jumpVelocity, 1.8);
            
            this.setDeltaMovement(this.getDeltaMovement().x, jumpVelocity, this.getDeltaMovement().z);
            this.hasImpulse = true;
        }
    }
    
    // Fall damage immunity
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // Immune to fall damage
    }

    // NeutralMob methods
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

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.JUMP_STRENGTH, 2.0D);
    }
    
    // Updated VibrationUser class for Minotaur
    private static class MinotaurVibrationUser implements VibrationSystem.User {
        private final MinotaurEntity minotaur;
        
        public MinotaurVibrationUser(MinotaurEntity minotaur) {
            this.minotaur = minotaur;
        }
        
        public int getVibrationCooldownTicks() {
            return 20; // 1 second cooldown between detecting vibrations
        }
        
        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event, GameEvent.Context context) {
            // Only accept vibrations from players or null sources (some block interactions)
            Entity source = context.sourceEntity();
            
            // Check if the source is a player
            return source == null || source instanceof Player;
        }
        
        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event, 
                                      @Nullable Entity entity, @Nullable Entity projectileOwner, float distance) {
            // Determine the source (use projectile owner if available)
            Entity source = projectileOwner != null ? projectileOwner : entity;
            
            // Only process vibrations from players
            if (source instanceof Player || source == null) {
                this.minotaur.onHeardVibration(source, pos);
                
                if (source instanceof Player player && this.minotaur.canTargetEntity(player)) {
                    this.minotaur.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
                } else {
                    // For player-made sounds where the player isn't directly detected
                    this.minotaur.getBrain().setMemoryWithExpiry(MemoryModuleType.WALK_TARGET, 
                                                               new WalkTarget(pos, 1.0F, 1), 
                                                               200);
                }
            }
        }
        
        @Override
        public PositionSource getPositionSource() {
            // Use Minecraft's built-in EntityPositionSource instead of custom one
            return new net.minecraft.world.level.gameevent.EntityPositionSource(this.minotaur, this.minotaur.getEyeHeight());
        }
        
        @Override
        public int getListenerRadius() {
            return VIBRATION_DETECTION_RANGE; // Now uses the configurable constant
        }
        
        public VibrationSystem getVibrationSystem() {
            return this.minotaur;
        }
    }
    
    // Custom Navigation for Minotaur
    private static class MinotaurNavigation extends WallClimberNavigation {
        private final MinotaurEntity minotaur;
        
        public MinotaurNavigation(MinotaurEntity minotaur, Level level) {
            super(minotaur, level);
            this.minotaur = minotaur;
            this.maxDistanceToWaypoint = 2.0F; // More forgiving with reaching waypoints
        }
        
        @Override
        public void tick() {
            super.tick();
            
            // Check if we need to perform a big jump to reach a target
            if (this.path != null && !this.path.isDone()) {
                int pathIndex = Math.min(this.path.getNextNodeIndex() + 3, this.path.getNodeCount() - 1);
                if (pathIndex >= 0) {
                    BlockPos nextPos = BlockPos.containing(this.path.getNode(pathIndex).x, 
                                                         this.path.getNode(pathIndex).y, 
                                                         this.path.getNode(pathIndex).z);
                    
                    double heightDifference = nextPos.getY() - this.minotaur.getY();
                    if (heightDifference > 1.5 && heightDifference < 8) {
                        // We need to jump to a higher level
                        this.minotaur.jumpToPosition(nextPos);
                    }
                }
            }
        }
    }

    /**
     * Gets the current animation state as a string for the renderer to use
     */
    public String getAnimationState() {
        if (this.isDashing()) {
            return "run";
        } else if (this.isAggressive() || this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceToSqr(target) <= 4.0) {
                return "attack";
            } else {
                return "run";
            }
        } else if (this.getNavigation().isInProgress()) {
            return "walk";
        } else if (this.lastSoundPosition != null) {
            return "listen"; // Changed from "idle" to "listen"
        } else {
            return "idle";
        }
    }
}
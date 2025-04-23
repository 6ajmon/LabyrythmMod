package com.github.sajmon.labyrythm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public class MinotaurEntity extends Monster implements NeutralMob, VibrationSystem {
    private static final EntityDataAccessor<Integer> ANGER_LEVEL = SynchedEntityData.defineId(MinotaurEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_DASHING = SynchedEntityData.defineId(MinotaurEntity.class, EntityDataSerializers.BOOLEAN);

    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    public static final float PATROL_SPEED_MULTIPLIER = 1.5f;
    public static final float INVESTIGATE_SPEED_MULTIPLIER = 1.5f;
    public static final float CHASE_SPEED_MULTIPLIER = 1.625f;
    public static final float PATROL_WALK_SPEED = 1.3F;
    public static final float INVESTIGATE_WALK_SPEED = 1.3F;
    public static final float CHASE_WALK_SPEED = 1.6F;

    public static final double DASH_FORCE = 2.5D;
    public static final int DASH_DURATION_TICKS = 20;
    public static final int DASH_COOLDOWN_TICKS = 240;

    public static final int VIBRATION_DETECTION_RANGE = 32;
    private static final int MAX_TRACKING_TICKS = 200;
    private static final int VIBRATION_MEMORY_DURATION = 400;

    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    private final VibrationSystem.User vibrationUser = new MinotaurVibrationUser(this);
    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();

    private int angerTime;
    private UUID angerTarget;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    public final AnimationState listenAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int dashCooldownTicks = 0;
    private int dashDurationTicks = 0;

    private final Map<UUID, Boolean> hasHeardEntity = new HashMap<>();
    private BlockPos lastSoundPosition = null;
    private int soundTrackingTicks = 0;

    private boolean targetDetectedByVibration = false;
    private int vibrationDetectionCooldown = 0;

    private static final Map<String, Set<UUID>> DEFEATED_MINOTAURS = new HashMap<>();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.labyrythm.minotaur.boss_name"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public MinotaurEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;

        this.bossEvent.setDarkenScreen(false); // Disable screen darkening effect
        this.bossEvent.setCreateWorldFog(false); // Disable the fog effect
    }

    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER_LEVEL, 0);
        builder.define(IS_DASHING, false);
    }

    @Override
    protected void registerGoals() {
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

        if (!level.isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (!level.isClientSide() && this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
            this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            this.targetDetectedByVibration = false;
            this.lastSoundPosition = null;

            this.getBrain().setActiveActivityIfPossible(ModActivities.PATROL.get());

            RandomSource random = this.getRandom();
            int x = Mth.randomBetweenInclusive(random, -16, 16);
            int y = Mth.randomBetweenInclusive(random, -2, 2);
            int z = Mth.randomBetweenInclusive(random, -16, 16);

            BlockPos targetPos = this.blockPosition().offset(x, y, z);

            this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
        }

        if (!level.isClientSide() && tickCount % 80 == 0) {
            if (this.getTarget() == null && this.lastSoundPosition == null &&
                !this.getNavigation().isInProgress()) {

                RandomSource random = this.getRandom();
                int x = Mth.randomBetweenInclusive(random, -20, 20);
                int y = Mth.randomBetweenInclusive(random, -2, 2);
                int z = Mth.randomBetweenInclusive(random, -20, 20);

                BlockPos targetPos = this.blockPosition().offset(x, y, z);

                this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
            }
        }

        if (vibrationDetectionCooldown > 0) {
            vibrationDetectionCooldown--;
            if (vibrationDetectionCooldown <= 0) {
                targetDetectedByVibration = false;
                if (this.getTarget() != null && !this.isWithinMeleeAttackRange(this.getTarget())) {
                    this.setTarget(null);
                    this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
            }
        }

        if (!level.isClientSide()) {
            Brain<?> brain = this.getBrain();
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);

            if (walkTarget.isPresent() && !this.getNavigation().isInProgress()) {
                WalkTarget target = walkTarget.get();
                BlockPos targetPos = BlockPos.containing(target.getTarget().currentPosition());

                boolean success = this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);

                if (!success) {
                    Path path = this.getNavigation().createPath(targetPos, 0);
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        if (dashCooldownTicks > 0) {
            dashCooldownTicks--;
        }

        if (this.isDashing()) {
            dashDurationTicks--;
            if (dashDurationTicks <= 0) {
                this.setDashing(false);
            }

            if (!level.isClientSide()) {
                this.performDashAttack();
            }
        }

        if (lastSoundPosition != null && soundTrackingTicks > 0) {
            soundTrackingTicks--;
            if (soundTrackingTicks <= 0) {
                lastSoundPosition = null;
            }
        }

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

        if (!level.isClientSide() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distSq = this.distanceToSqr(target);

            if (distSq <= 6.0 && tickCount % 20 == 0) {
                this.doHurtTarget(target);
            }
        }

        if (!level.isClientSide()) {
            Optional<Activity> currentActivity = this.getBrain().getActiveNonCoreActivity();

            if (currentActivity.isPresent()) {
                Activity activity = currentActivity.get();

                float speedMultiplier = PATROL_SPEED_MULTIPLIER;

                if (activity == ModActivities.CHASE.get()) {
                    speedMultiplier = CHASE_SPEED_MULTIPLIER;
                } else if (activity == ModActivities.INVESTIGATE.get()) {
                    speedMultiplier = INVESTIGATE_SPEED_MULTIPLIER;
                }

                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(BASE_MOVEMENT_SPEED * speedMultiplier);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.getBrain().tick(serverLevel, this);

        if (this.getTarget() == null && this.getRemainingPersistentAngerTime() > 0) {
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

        if (this.angerTime > 0) {
            this.angerTime--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        if (result && source.getEntity() instanceof LivingEntity attacker) {
            if (this.canTargetEntity(attacker)) {
                this.setTarget(attacker);
                this.targetDetectedByVibration = true;
                this.vibrationDetectionCooldown = VIBRATION_MEMORY_DURATION;

                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);

                this.setPersistentAngerTarget(attacker.getUUID());
                this.setRemainingPersistentAngerTime(400);
            }
        }

        return result;
    }

    public void onHeardVibration(Entity source, BlockPos soundPos) {
        if (source instanceof Player player && this.canTargetEntity(player)) {
            this.setTarget(player);
            this.setLastSoundPosition(soundPos);
            this.hasHeardEntity.put(source.getUUID(), true);

            this.targetDetectedByVibration = true;
            this.vibrationDetectionCooldown = VIBRATION_MEMORY_DURATION;

            this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
        } else if (soundPos != null) {
            this.setLastSoundPosition(soundPos);
            MinotaurAi.updateActivity(this);
        }
    }

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

    public boolean canDash() {
        return dashCooldownTicks <= 0;
    }

    public void startDash(Vec3 direction) {
        if (!this.canDash()) return;

        this.setDashing(true);
        this.dashDurationTicks = DASH_DURATION_TICKS;
        this.dashCooldownTicks = DASH_COOLDOWN_TICKS;

        Vec3 normalizedDir = direction.normalize();
        this.setDeltaMovement(normalizedDir.x * DASH_FORCE,
                             this.onGround() ? 0.1 : this.getDeltaMovement().y,
                             normalizedDir.z * DASH_FORCE);

        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 0.5F);
    }

    public void startDashToTarget() {
        this.setDashing(true);

        LivingEntity target = this.getTarget();
        if (target != null) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();

            double length = Math.sqrt(dx * dx + dz * dz);
            dx = dx / length;
            dz = dz / length;

            this.setDeltaMovement(dx * DASH_FORCE, 0.1, dz * DASH_FORCE);

            this.dashDurationTicks = DASH_DURATION_TICKS;
            this.dashCooldownTicks = DASH_COOLDOWN_TICKS;

            this.playSound(SoundEvents.PHANTOM_SWOOP, 2.0F, 1.0F);
        }
    }

    private void performDashAttack() {
        Vec3 direction = this.getDeltaMovement().normalize();
        AABB attackBox = this.getBoundingBox().inflate(1.0);

        for (Entity entity : this.level().getEntities(this, attackBox)) {
            if (entity != this && entity instanceof LivingEntity livingEntity && this.canTargetEntity(entity)) {
                entity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

                double knockbackStrength = this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                if (knockbackStrength > 0) {
                    Vec3 knockbackDir = entity.position().subtract(this.position()).normalize();
                    entity.push(knockbackDir.x * knockbackStrength, 0.3, knockbackDir.z * knockbackStrength);
                }
            }
        }
    }

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

    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return this.distanceToSqr(target) <= 6.0;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new MinotaurNavigation(this, level);
    }

    public void jumpToPosition(BlockPos targetPos) {
        if (!this.onGround()) return;

        double heightDifference = targetPos.getY() - this.getY();
        if (heightDifference > 0) {
            double jumpVelocity = Math.sqrt(2 * 0.08 * heightDifference) * 1.2;

            jumpVelocity = Math.min(jumpVelocity, 1.8);

            this.setDeltaMovement(this.getDeltaMovement().x, jumpVelocity, this.getDeltaMovement().z);
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.FOLLOW_RANGE, VIBRATION_DETECTION_RANGE)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.SCALE, 1.24D)
                .add(Attributes.JUMP_STRENGTH, 2.0D);
    }

    private static class MinotaurVibrationUser implements VibrationSystem.User {
        private final MinotaurEntity minotaur;

        public MinotaurVibrationUser(MinotaurEntity minotaur) {
            this.minotaur = minotaur;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event, GameEvent.Context context) {
            Entity source = context.sourceEntity();
            return source == null || source instanceof Player;
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event,
                                      @Nullable Entity entity, @Nullable Entity projectileOwner, float distance) {
            Entity source = projectileOwner != null ? projectileOwner : entity;

            if (source instanceof Player || source == null) {
                this.minotaur.onHeardVibration(source, pos);

                if (source instanceof Player player && this.minotaur.canTargetEntity(player)) {
                    this.minotaur.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
                } else {
                    this.minotaur.getBrain().setMemoryWithExpiry(MemoryModuleType.WALK_TARGET,
                                                               new WalkTarget(pos, INVESTIGATE_WALK_SPEED, 1),
                                                               200);
                }
            }
        }

        @Override
        public PositionSource getPositionSource() {
            return new net.minecraft.world.level.gameevent.EntityPositionSource(this.minotaur, this.minotaur.getEyeHeight());
        }

        @Override
        public int getListenerRadius() {
            return VIBRATION_DETECTION_RANGE;
        }
    }

    private static class MinotaurNavigation extends WallClimberNavigation {
        private final MinotaurEntity minotaur;

        public MinotaurNavigation(MinotaurEntity minotaur, Level level) {
            super(minotaur, level);
            this.minotaur = minotaur;
            this.maxDistanceToWaypoint = 2.0F;
        }

        @Override
        public void tick() {
            super.tick();

            if (this.path != null && !this.path.isDone()) {
                int pathIndex = Math.min(this.path.getNextNodeIndex() + 3, this.path.getNodeCount() - 1);
                if (pathIndex >= 0) {
                    BlockPos nextPos = BlockPos.containing(this.path.getNode(pathIndex).x,
                                                         this.path.getNode(pathIndex).y,
                                                         this.path.getNode(pathIndex).z);

                    double heightDifference = nextPos.getY() - this.minotaur.getY();
                    if (heightDifference > 1.5 && heightDifference < 8) {
                        this.minotaur.jumpToPosition(nextPos);
                    }
                }
            }
        }
    }

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
            return "listen";
        } else {
            return "idle";
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            String dimensionKey = this.level().dimension().location().toString();
            DEFEATED_MINOTAURS.computeIfAbsent(dimensionKey, k -> new HashSet<>()).add(this.getUUID());

            this.level().playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.HOSTILE, 1.0F, 1.0F);

            this.bossEvent.setVisible(false);
        }

        super.die(damageSource);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public static boolean isMinotaurDefeatedInDimension(String dimensionKey) {
        Set<UUID> defeated = DEFEATED_MINOTAURS.get(dimensionKey);
        return defeated != null && !defeated.isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.labyrythm.minotaur.boss_name");
    }
}
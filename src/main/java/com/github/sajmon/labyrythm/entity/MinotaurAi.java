package com.github.sajmon.labyrythm.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.schedule.Activity;


import java.util.List;
import java.util.Optional;

public class MinotaurAi {
    private static final float PATROL_WALK_SPEED = MinotaurEntity.PATROL_WALK_SPEED;
    private static final float INVESTIGATE_WALK_SPEED = MinotaurEntity.INVESTIGATE_WALK_SPEED;
    private static final float CHASE_WALK_SPEED = MinotaurEntity.CHASE_WALK_SPEED;

    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
            MemoryModuleType.PATH,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.HEARD_BELL_TIME,
            MemoryModuleType.ATTACK_COOLING_DOWN
    );
    
    protected static final Activity IDLE = Activity.IDLE;
    protected static final Activity PATROL = ModActivities.PATROL.get();
    protected static final Activity INVESTIGATE = ModActivities.INVESTIGATE.get();
    protected static final Activity CHASE = ModActivities.CHASE.get();
    protected static final Activity ATTACK = ModActivities.ATTACK.get();
    
    public static Brain<?> makeBrain(MinotaurEntity minotaur, Dynamic<?> dynamic) {
        Brain.Provider<MinotaurEntity> provider = Brain.provider(MEMORY_TYPES, ImmutableList.of());
        Brain<MinotaurEntity> brain = provider.makeBrain(dynamic);
        
        initIdleActivity(brain);
        initPatrolActivity(brain);
        initInvestigateActivity(brain);
        initChaseActivity(brain);
        initAttackActivity(brain);
        
        brain.setDefaultActivity(PATROL);
        brain.useDefaultActivity();
        
        return brain;
    }
    
    public static void updateActivity(MinotaurEntity minotaur) {
        Brain<MinotaurEntity> brain = minotaur.getBrain();
        
        Optional<Activity> currentActivityOpt = brain.getActiveNonCoreActivity();
        Activity currentActivity = currentActivityOpt.orElse(null);
        
        if (currentActivity == null) {
            List<Activity> possibleActivities = List.of(ATTACK, CHASE, INVESTIGATE, PATROL, IDLE);
            for (Activity activity : possibleActivities) {
                if (brain.isActive(activity)) {
                    currentActivity = activity;
                    break;
                }
            }
        }
        
        if (hasAttackTarget(minotaur)) {
            LivingEntity target = minotaur.getTarget();
            assert target != null;
            double distanceSq = minotaur.distanceToSqr(target);
            
            if (minotaur.wasTargetDetectedByVibration()) {
                if (distanceSq <= 6.0) {
                    brain.setActiveActivityIfPossible(ATTACK);
                } else {
                    brain.setActiveActivityIfPossible(CHASE);
                }
            } else {
                minotaur.setTarget(null);
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.setActiveActivityIfPossible(PATROL);
            }
        } else if (minotaur.getLastSoundPosition() != null) {
            brain.setActiveActivityIfPossible(INVESTIGATE);
            
            BlockPos soundPos = minotaur.getLastSoundPosition();
            WalkTarget target = new WalkTarget(soundPos, 1.2F, 1);
            
            brain.setMemory(MemoryModuleType.WALK_TARGET, target);
        } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.setActiveActivityIfPossible(PATROL);
            brain.setDefaultActivity(PATROL);
        }
    }
    
    private static boolean hasAttackTarget(MinotaurEntity minotaur) {
        return minotaur.getTarget() != null && minotaur.getTarget().isAlive();
    }
    
    private static void initIdleActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(IDLE, ImmutableList.of(
            Pair.of(0, new RunOne<>(ImmutableList.of(
                Pair.of(SetLookAndInteract.create(EntityType.PLAYER, 6), 1),
                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                Pair.of(new DoNothing(10, 20), 1)
                )))
        ));
    }
    
    private static void initPatrolActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(PATROL, ImmutableList.of(
            Pair.of(0, new RunOne<>(ImmutableList.of(
                Pair.of(RandomStroll.swim(1.2F), 3),
                Pair.of(RandomStroll.stroll(1.3F), 6),
                Pair.of(createRandomPatrolGoal(), 12),       
                Pair.of(new DoNothing(5, 15), 1)             
            )))
        ));
    }
    
    private static void initInvestigateActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(INVESTIGATE, ImmutableList.of(
            Pair.of(0, createInvestigateSoundGoal()), 
            Pair.of(1, new MoveToTargetSink(20, 40)),
            Pair.of(2, SetWalkTargetFromLookTarget.create(INVESTIGATE_WALK_SPEED, 2))
        ));
    }
    
    private static void initChaseActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(CHASE, ImmutableList.of(
            Pair.of(0, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(CHASE_WALK_SPEED)),
            Pair.of(1, createDashAttackGoal())
        ));
    }
    
    private static void initAttackActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(ATTACK, ImmutableList.of(
            Pair.of(0, createMeleeAttackGoal())
        ));
    }
    
    private static BehaviorControl<MinotaurEntity> createRandomPatrolGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET)
            ).apply(instance, (walkTarget) -> {
                return (level, entity, gameTime) -> {
                    RandomSource random = entity.getRandom();
                    
                    int x = Mth.randomBetweenInclusive(random, -20, 20);
                    int y = Mth.randomBetweenInclusive(random, -2, 2);
                    int z = Mth.randomBetweenInclusive(random, -20, 20);
                    
                    BlockPos targetPos = entity.blockPosition().offset(x, y, z);
                    
                    WalkTarget target = new WalkTarget(targetPos, PATROL_WALK_SPEED, 1);
                    
                    entity.getBrain().setMemoryWithExpiry(MemoryModuleType.WALK_TARGET, target, 30);
                    
                    return true;
                };
            });
        });
    }
    
    private static BehaviorControl<MinotaurEntity> createInvestigateSoundGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.absent(MemoryModuleType.WALK_TARGET)
            ).apply(instance, (walkTarget) -> {
                return (level, entity, gameTime) -> {

                    BlockPos soundPos = entity.getLastSoundPosition();
                    if (soundPos == null) {
                        return false;
                    }
                    
                    WalkTarget target = new WalkTarget(soundPos, 1.2F, 1);
                    entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, target);
                    
                    return true;
                };
            });
        });
    }
    
    private static BehaviorControl<MinotaurEntity> createDashAttackGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
            ).apply(instance, (attackTarget, attackCoolingDown) -> {
                return (level, entity, gameTime) -> {

                    LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                    
                    if (entity.isDashing()) {
                        return true;
                    }
                    
                    if (!entity.canDash()) {
                        return false;
                    }
                    
                    double distance = entity.distanceTo(target);
                    
                    if (distance >= 5.0 && distance <= 60.0) {
                        entity.startDashToTarget();
                        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 200L);
                        return true;
                    }
                    
                    return false;
                };
            });
        });
    }
    
    private static BehaviorControl<MinotaurEntity> createMeleeAttackGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
            ).apply(instance, (attackTarget, attackCoolingDown) -> {
                return (level, entity, gameTime) -> {
                    LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                    
                    double distSq = entity.distanceToSqr(target);
                    
                    if (distSq <= 6.0) {
                        entity.doHurtTarget(target);
                        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 20L);
                        return true;
                    }
                    
                    return false;
                };
            });
        });
    }
}
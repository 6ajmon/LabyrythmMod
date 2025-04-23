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
    // Import speed constants from MinotaurEntity - add reference to them here
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
            MemoryModuleType.ATTACK_COOLING_DOWN  // Add this line
    );
    
    // Use the pre-registered activities instead of registering them here
    protected static final Activity IDLE = Activity.IDLE;
    protected static final Activity PATROL = ModActivities.PATROL.get();
    protected static final Activity INVESTIGATE = ModActivities.INVESTIGATE.get();
    protected static final Activity CHASE = ModActivities.CHASE.get();
    protected static final Activity ATTACK = ModActivities.ATTACK.get();
    
    public static Brain<?> makeBrain(MinotaurEntity minotaur, Dynamic<?> dynamic) {
        Brain.Provider<MinotaurEntity> provider = Brain.provider(MEMORY_TYPES, ImmutableList.of());
        Brain<MinotaurEntity> brain = provider.makeBrain(dynamic);
        
        // Register activities
        initIdleActivity(brain);
        initPatrolActivity(brain);
        initInvestigateActivity(brain);
        initChaseActivity(brain);
        initAttackActivity(brain);
        
        // Set default activity
        brain.setDefaultActivity(PATROL);
        brain.useDefaultActivity();
        
        return brain;
    }
    
    // Update the active activity based on conditions
    public static void updateActivity(MinotaurEntity minotaur) {
        Brain<MinotaurEntity> brain = minotaur.getBrain();
        
        Optional<Activity> currentActivityOpt = brain.getActiveNonCoreActivity();
        Activity currentActivity = currentActivityOpt.orElse(null);
        
        // If no non-core activity is present, check each possible activity manually
        if (currentActivity == null) {
            // Check activities in priority order
            List<Activity> possibleActivities = List.of(ATTACK, CHASE, INVESTIGATE, PATROL, IDLE);
            for (Activity activity : possibleActivities) {
                if (brain.isActive(activity)) {
                    currentActivity = activity;
                    break;
                }
            }
        }
        
        System.out.println("Current activity: " + currentActivity);
        
        // Choose activity based on conditions
        if (hasAttackTarget(minotaur)) {
            LivingEntity target = minotaur.getTarget();
            assert target != null;
            double distanceSq = minotaur.distanceToSqr(target);
            
            // Only chase if the target was detected through vibrations
            if (minotaur.wasTargetDetectedByVibration()) {
                // If within attack range, attack
                if (distanceSq <= 6.0) {
                    System.out.println("SWITCHING TO ATTACK ACTIVITY");
                    brain.setActiveActivityIfPossible(ATTACK);
                } else {
                    // Otherwise chase
                    System.out.println("SWITCHING TO CHASE ACTIVITY");
                    brain.setActiveActivityIfPossible(CHASE);
                }
            } else {
                // If target wasn't detected by vibration, don't chase - go back to patrol
                System.out.println("Target not detected by vibration, ignoring");
                minotaur.setTarget(null);
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.setActiveActivityIfPossible(PATROL);
            }
        } else if (minotaur.getLastSoundPosition() != null) {
            // Investigate sounds even if no target - this is fine for any vibration
            brain.setActiveActivityIfPossible(INVESTIGATE);
            
            BlockPos soundPos = minotaur.getLastSoundPosition();
            WalkTarget target = new WalkTarget(soundPos, 1.2F, 1);
            
            brain.setMemory(MemoryModuleType.WALK_TARGET, target);
            System.out.println("Setting investigate walk target to: " + soundPos.toShortString());
        } else {
            // IMPORTANT: Call setActiveActivityIfPossible AND setDefaultActivity
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.setActiveActivityIfPossible(PATROL);
            brain.setDefaultActivity(PATROL);
            
            // Force patrol activity selection
            System.out.println("Setting PATROL as active activity");
        }
    }
    
    private static boolean hasAttackTarget(MinotaurEntity minotaur) {
        return minotaur.getTarget() != null && minotaur.getTarget().isAlive();
    }
    
    // Initialize activities
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
                // REDUCED movement speeds
                Pair.of(RandomStroll.swim(1.2F), 3),          // REDUCED from 1.5F
                Pair.of(RandomStroll.stroll(1.3F), 6),        // REDUCED from 1.7F
                Pair.of(createRandomPatrolGoal(), 12),       
                Pair.of(new DoNothing(5, 15), 1)             
            )))
        ));
    }
    
    private static void initInvestigateActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(INVESTIGATE, ImmutableList.of(
            Pair.of(0, createInvestigateSoundGoal()), 
            Pair.of(1, new MoveToTargetSink(20, 40)),
            // Use the investigate walk speed constant
            Pair.of(2, SetWalkTargetFromLookTarget.create(INVESTIGATE_WALK_SPEED, 2))
        ));
    }
    
    private static void initChaseActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(CHASE, ImmutableList.of(
            // Use the chase walk speed constant
            Pair.of(0, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(CHASE_WALK_SPEED)),
            Pair.of(1, createDashAttackGoal())
        ));
    }
    
    private static void initAttackActivity(Brain<MinotaurEntity> brain) {
        brain.addActivity(ATTACK, ImmutableList.of(
            Pair.of(0, createMeleeAttackGoal())
        ));
    }
    
    // Custom behavior methods
    
    // Random patrol within the labyrinth
    private static BehaviorControl<MinotaurEntity> createRandomPatrolGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET)
            ).apply(instance, (walkTarget) -> {
                return (level, entity, gameTime) -> {
                    RandomSource random = entity.getRandom();
                    
                    // More reasonable patrol range (not so far)
                    int x = Mth.randomBetweenInclusive(random, -20, 20);  // INCREASED from -16,16
                    int y = Mth.randomBetweenInclusive(random, -2, 2);
                    int z = Mth.randomBetweenInclusive(random, -20, 20);  // INCREASED from -16,16
                    
                    BlockPos targetPos = entity.blockPosition().offset(x, y, z);
                    
                    // Use the constant for walk speed
                    WalkTarget target = new WalkTarget(targetPos, PATROL_WALK_SPEED, 1);
                    
                    // DECREASED expiry from 40 to 30 ticks - change patrol points more often
                    entity.getBrain().setMemoryWithExpiry(MemoryModuleType.WALK_TARGET, target, 30);
                    
                    // Debug info
                    if (random.nextInt(10) == 0) { // Reduce debug spam
                        System.out.println("Minotaur patrolling to: " + targetPos.toShortString());
                    }
                    return true;
                };
            });
        });
    }
    
    // Investigate a sound
    private static BehaviorControl<MinotaurEntity> createInvestigateSoundGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                // Change from registered to absent - we want this to execute when there is NO walk target
                instance.absent(MemoryModuleType.WALK_TARGET)
            ).apply(instance, (walkTarget) -> {
                return (level, entity, gameTime) -> {

                    BlockPos soundPos = entity.getLastSoundPosition();
                    if (soundPos == null) {
                        return false;
                    }
                    
                    // IMPORTANT: Only set the brain's WALK_TARGET, don't use direct navigation
                    WalkTarget target = new WalkTarget(soundPos, 1.2F, 1);
                    entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, target);
                    
                    // Debug output
                    System.out.println("Setting walk target to sound at: " + soundPos.toShortString());
                    
                    return true;
                };
            });
        });
    }
    
    // Dash attack behavior
    private static BehaviorControl<MinotaurEntity> createDashAttackGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
            ).apply(instance, (attackTarget, attackCoolingDown) -> {
                return (level, entity, gameTime) -> {

                    // Get target directly from the brain
                    LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                    
                    // Check if we're already dashing
                    if (entity.isDashing()) {
                        return true;
                    }
                    
                    // Check if dash is on cooldown
                    if (!entity.canDash()) {
                        return false;
                    }
                    
                    // Check distance to target
                    double distance = entity.distanceTo(target);
                    
                    // Debug
                    System.out.println("Dash check - distance: " + distance + ", can dash: " + entity.canDash());
                    
                    // CHANGED: Increased max dash distance to 60 (was 40)
                    if (distance >= 5.0 && distance <= 60.0) {
                        // Start dash attack
                        System.out.println("Starting dash attack!");
                        entity.startDashToTarget();
                        // CHANGED: Increased cooldown from 60L to 200L (10 seconds)
                        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 200L);
                        return true;
                    }
                    
                    return false;
                };
            });
        });
    }
    
    // Melee attack behavior
    private static BehaviorControl<MinotaurEntity> createMeleeAttackGoal() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
            ).apply(instance, (attackTarget, attackCoolingDown) -> {
                return (level, entity, gameTime) -> {
                    // Get target from brain directly
                    LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                    
                    // Debug with more info
                    double distSq = entity.distanceToSqr(target);
                    System.out.println("Attack check - distance: " + distSq + 
                                      ", entity pos: " + entity.blockPosition() + 
                                      ", target pos: " + target.blockPosition());
                    
                    // CHANGED: Increased attack range to 6.0 (squared)
                    if (distSq <= 6.0) {
                        System.out.println("PERFORMING ATTACK!");
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
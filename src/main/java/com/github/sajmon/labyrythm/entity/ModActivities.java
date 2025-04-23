package com.github.sajmon.labyrythm.entity;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModActivities {
    public static final DeferredRegister<Activity> ACTIVITIES = 
            DeferredRegister.create(BuiltInRegistries.ACTIVITY, Labyrythm.MOD_ID);
    
    // Register activities using the DeferredRegister
    public static final Supplier<Activity> PATROL = ACTIVITIES.register(
            "minotaur_patrol", () -> new Activity("minotaur_patrol"));
    
    public static final Supplier<Activity> INVESTIGATE = ACTIVITIES.register(
            "minotaur_investigate", () -> new Activity("minotaur_investigate"));
    
    public static final Supplier<Activity> CHASE = ACTIVITIES.register(
            "minotaur_chase", () -> new Activity("minotaur_chase"));
    
    public static final Supplier<Activity> ATTACK = ACTIVITIES.register(
            "minotaur_attack", () -> new Activity("minotaur_attack"));
            
    public static void register(IEventBus modEventBus) {
        ACTIVITIES.register(modEventBus);
    }
}
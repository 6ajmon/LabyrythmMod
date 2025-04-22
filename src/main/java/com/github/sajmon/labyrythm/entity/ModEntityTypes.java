package com.github.sajmon.labyrythm.entity;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;


public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(Registries.ENTITY_TYPE, Labyrythm.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MinotaurEntity>> MINOTAUR = 
            ENTITY_TYPES.register("minotaur", 
                    () -> EntityType.Builder.of(MinotaurEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.4F) // Slightly larger than a zombie
                    .clientTrackingRange(16)
                    .build(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
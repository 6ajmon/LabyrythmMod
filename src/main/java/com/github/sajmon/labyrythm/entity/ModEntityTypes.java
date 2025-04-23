package com.github.sajmon.labyrythm.entity;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(Registries.ENTITY_TYPE, Labyrythm.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MinotaurEntity>> MINOTAUR = 
            ENTITY_TYPES.register("minotaur", 
                    () -> EntityType.Builder.of(MinotaurEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 2.7F)
                            .fireImmune()
                            .build(Labyrythm.MOD_ID + ":minotaur"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
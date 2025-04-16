package com.github.sajmon.labyrythm.structures;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Labyrythm.MOD_ID);

    public static final ResourceKey<Structure> MINOTAURS_LABYRINTH_KEY =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaurs_labyrinth"));

    public static final DeferredHolder<StructureType<?>, StructureType<MinotaursLabyrinth>> MINOTAURS_LABYRINTH =
            STRUCTURE_TYPES.register("minotaurs_labyrinth", () -> () -> MinotaursLabyrinth.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
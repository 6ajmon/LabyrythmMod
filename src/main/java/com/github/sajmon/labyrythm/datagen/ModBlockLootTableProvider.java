package com.github.sajmon.labyrythm.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // TODO: Implement block loot tables
        // Example: dropSelf(ModBlocks.YOUR_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // TODO: Return an iterable of your mod's blocks
        // Example: return ModBlocks.BLOCKS.getEntries().stream().map(Supplier::get)::iterator;
        return Collections.emptyList();
    }
}

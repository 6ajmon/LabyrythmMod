package com.github.sajmon.labyrythm.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        // TODO: Implement data maps if needed
        // Example: builder(YourDataMaps.YOUR_MAP).add(YourRegistries.YOUR_ENTRY, YourData.INSTANCE, false);
    }
}

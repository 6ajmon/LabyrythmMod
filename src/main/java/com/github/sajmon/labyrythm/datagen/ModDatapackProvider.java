package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {
    // Define your RegistrySetBuilder here if needed
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();
        // .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
        // .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
        // .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
        // .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Labyrythm.MOD_ID));
    }
}

package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Labyrythm.MOD_ID);
    }

    @Override
    protected void start() {
        // TODO: Implement global loot modifiers
        // Example: add("your_modifier_name", new YourLootModifier(
        //     new LootItemCondition[] { ... }, YourItem.get()
        // ));
    }
}

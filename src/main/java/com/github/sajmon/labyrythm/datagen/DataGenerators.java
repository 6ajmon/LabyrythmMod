package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Labyrythm.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server providers
        // Combined loot table provider for both block and chest loot tables
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output -> 
            new LootTableProvider(output, Collections.emptySet(),
                List.of(
                    new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                    new LootTableProvider.SubProviderEntry(ModChestLootTableProvider::new, LootContextParamSets.CHEST)
                ), 
                lookupProvider));
        
        // Add recipe provider - Use explicit cast
        generator.addProvider(event.includeServer(), (DataProvider.Factory<ModRecipeProvider>) output -> 
            new ModRecipeProvider(output, lookupProvider));

        // Create block tags provider
        BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        
        // Add block tags provider - Use explicit cast
        generator.addProvider(event.includeServer(), (DataProvider.Factory<BlockTagsProvider>) output -> blockTagsProvider);
        
        // Add item tags provider - Use explicit cast  
        generator.addProvider(event.includeServer(), (DataProvider.Factory<ModItemTagProvider>) output -> 
            new ModItemTagProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        // Add data map provider - Use explicit cast
        generator.addProvider(event.includeServer(), (DataProvider.Factory<ModDataMapProvider>) output -> 
            new ModDataMapProvider(output, lookupProvider));
        
        // Add datapack provider - Use explicit cast
        generator.addProvider(event.includeServer(), (DataProvider.Factory<ModDatapackProvider>) output -> 
            new ModDatapackProvider(output, lookupProvider));
        
        // Add global loot modifier provider - Use explicit cast
        generator.addProvider(event.includeServer(), (DataProvider.Factory<ModGlobalLootModifierProvider>) output -> 
            new ModGlobalLootModifierProvider(output, lookupProvider));

        // Client providers
        // Add item model provider - Use explicit cast
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModItemModelProvider>) output -> 
            new ModItemModelProvider(output, existingFileHelper));
        
        // Add block state provider - Use explicit cast
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModBlockStateProvider>) output -> 
            new ModBlockStateProvider(output, existingFileHelper));
    }
}

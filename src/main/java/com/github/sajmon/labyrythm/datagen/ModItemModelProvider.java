package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Labyrythm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // TODO: Register item models
        // Example: simpleItem(ModItems.YOUR_ITEM);
        // You might need to manually handle existing items if they don't follow the simple pattern
        // simpleItem(ModItems.MINOTAURS_RESONANCE); // Assuming handheld
        // simpleItem(ModItems.SCULK_HORN);
        // simpleItem(ModItems.SCULK_UPGRADE);
    }

    private ItemModelBuilder simpleItem(DeferredHolder<Item, ? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "item/" + item.getId().getPath()));
    }

    // Add other helper methods like handheldItem if needed
    private ItemModelBuilder handheldItem(DeferredHolder<Item, ? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "item/" + item.getId().getPath()));
    }
}

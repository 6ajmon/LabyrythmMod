package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Sculk Horn to Sculk Upgrade recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCULK_UPGRADE.get())
                .pattern("DHD")
                .pattern("DDD")
                .pattern("DDD")
                .define('D', Items.DIAMOND)
                .define('H', ModItems.SCULK_HORN.get())
                .unlockedBy("has_sculk_horn", has(ModItems.SCULK_HORN.get()))
                .save(recipeOutput);

        // Sculk Upgrade duplication recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCULK_UPGRADE.get(), 2)
                .pattern("DUD")
                .pattern("DDD")
                .pattern("DDD")
                .define('D', Items.DIAMOND)
                .define('U', ModItems.SCULK_UPGRADE.get())
                .unlockedBy("has_sculk_upgrade", has(ModItems.SCULK_UPGRADE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "sculk_upgrade_duplication"));

        // Smithing recipe for Minotaur's Resonance
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SCULK_UPGRADE.get()),  // template
                Ingredient.of(Items.DIAMOND_AXE),             // base (Changed from ItemTags.AXES)
                Ingredient.of(Items.ECHO_SHARD),              // addition
                RecipeCategory.COMBAT,
                ModItems.MINOTAURS_RESONANCE.get())
                .unlocks("has_sculk_upgrade", has(ModItems.SCULK_UPGRADE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaurs_resonance"));
    }
}

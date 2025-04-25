package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class ModChestLootTableProvider implements LootTableSubProvider {
    // Add this constructor to handle the lookup provider
    public ModChestLootTableProvider(HolderLookup.Provider registries) {
        // The HolderLookup.Provider parameter is needed for the factory method,
        // but we don't need to use it in this class
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        // Tier 1 - Basic loot (worst)
        biConsumer.accept(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_1")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 5))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.COAL)
                                        .setWeight(20)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.BREAD)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.IRON_PICKAXE)
                                        .setWeight(5))
                                .add(LootItem.lootTableItem(Items.IRON_SWORD)
                                        .setWeight(5)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .setWeight(1)
                                        .setQuality(2))
                                .add(LootItem.lootTableItem(Items.EMERALD)
                                        .setWeight(3)
                                        .setQuality(1)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.IRON_HELMET)
                                        .setWeight(5))));

        // Tier 2 - Medium loot
        biConsumer.accept(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_2")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(4, 6))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT)
                                        .setWeight(20)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.APPLE)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                        .setWeight(5)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 2))
                                .add(LootItem.lootTableItem(Items.IRON_SWORD)
                                        .setWeight(10)
                                        .apply(EnchantRandomlyFunction.randomEnchantment()))
                                .add(LootItem.lootTableItem(Items.IRON_HELMET)
                                        .setWeight(10)
                                        .apply(EnchantRandomlyFunction.randomEnchantment()))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .setWeight(5)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))));

        // Tier 3 - Best loot
        biConsumer.accept(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_3")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(4, 7))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))))
                                .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                        .setWeight(10))
                                .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE)
                                        .setWeight(2)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(2, 4))
                                .add(LootItem.lootTableItem(Items.ECHO_SHARD)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.SCULK)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.SCULK_SENSOR)
                                        .setWeight(5))
                                .add(LootItem.lootTableItem(Items.SCULK_CATALYST)
                                        .setWeight(2)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 3))
                                .add(LootItem.lootTableItem(Items.DIAMOND_SWORD)
                                        .setWeight(5)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))
                                .add(LootItem.lootTableItem(Items.DIAMOND_HELMET)
                                        .setWeight(5)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))
                                .add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)
                                        .setWeight(5)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))
                                .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                        .setWeight(10)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))));
    }
}

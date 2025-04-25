package com.github.sajmon.labyrythm.datagen;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
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
                                .setRolls(UniformGenerator.between(4, 6))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.COAL)
                                        .setWeight(20)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.COBWEB)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                .add(LootItem.lootTableItem(Items.BLACK_CANDLE)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.SCULK)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.SCULK_SENSOR)
                                        .setWeight(3))
                                .add(LootItem.lootTableItem(Items.NOTE_BLOCK)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.SOUL_LANTERN)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))


                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(2, 4))
                                .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.SOUL_TORCH)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))))
                                .add(LootItem.lootTableItem(Items.LEAD)
                                        .setWeight(8))
                                .add(LootItem.lootTableItem(Items.NAME_TAG)
                                        .setWeight(5))
                                .add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR)
                                        .setWeight(3))
                                .add(LootItem.lootTableItem(Items.SADDLE)
                                        .setWeight(2)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .setWeight(3)
                                        .setQuality(2))
                                .add(LootItem.lootTableItem(Items.EMERALD)
                                        .setWeight(5)
                                        .setQuality(1)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))));

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
                                        .setWeight(5))
                                .add(LootItem.lootTableItem(Items.COBWEB)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.BLACK_CANDLE)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.SOUL_TORCH)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))))
                                .add(LootItem.lootTableItem(Items.LEAD)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.NAME_TAG)
                                        .setWeight(8))
                                .add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR)
                                        .setWeight(4))
                                .add(LootItem.lootTableItem(Items.SADDLE)
                                        .setWeight(3))
                                .add(LootItem.lootTableItem(Items.DISC_FRAGMENT_5)
                                        .setWeight(6)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))))


                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .setWeight(5)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))));

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
                                .add(LootItem.lootTableItem(Items.COBWEB)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))))
                                .add(LootItem.lootTableItem(Items.BLACK_CANDLE)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                                .add(LootItem.lootTableItem(Items.NOTE_BLOCK)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.SOUL_LANTERN)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))))


                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 5))
                                .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))))
                                .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE)
                                        .setWeight(15)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                .add(LootItem.lootTableItem(Items.SOUL_TORCH)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))))
                                .add(LootItem.lootTableItem(Items.LEAD)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.NAME_TAG)
                                        .setWeight(10)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR)
                                        .setWeight(5))
                                .add(LootItem.lootTableItem(Items.SADDLE)
                                        .setWeight(6))
                                .add(LootItem.lootTableItem(Items.DISC_FRAGMENT_5)
                                        .setWeight(12)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5)))))


                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.ECHO_SHARD)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))))


                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(2, 4))
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
                                .add(LootItem.lootTableItem(Items.DIAMOND_HOE)
                                        .setWeight(4)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))
                                .add(LootItem.lootTableItem(Items.DIAMOND_BOOTS)
                                        .setWeight(5)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30))))
                                .add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
                                        .setWeight(5)
                                        .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30)))))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE)
                                        .setWeight(1))
                                .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                        .setWeight(4)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.MUSIC_DISC_5)
                                        .setWeight(1)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                        .setWeight(10))
                                .add(LootItem.lootTableItem(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                                        .setWeight(10))
                                .add(LootItem.lootTableItem(ModItems.SCULK_UPGRADE.get())
                                        .setWeight(5))));

    }
}

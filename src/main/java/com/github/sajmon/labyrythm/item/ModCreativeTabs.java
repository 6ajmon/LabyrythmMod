package com.github.sajmon.labyrythm.item;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.potion.ModPotions;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Labyrythm.MOD_ID);

    public static final CreativeModeTab.Builder LABYRYTHM_TAB_BUILDER = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Labyrythm.MOD_ID + ".main_tab"))
            .icon(() -> new ItemStack(ModItems.MINOTAURS_RESONANCE.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.MINOTAURS_RESONANCE.get());
                output.accept(ModItems.SCULK_HORN.get());
                output.accept(ModItems.SCULK_UPGRADE.get());
                
                // Add potions to creative tab
                output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.DARKNESS));
                output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LONG_DARKNESS));
                output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LINGERING_DARKNESS));
                
                // Add splash potions
                output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.DARKNESS));
                output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LONG_DARKNESS));
            });

    public static final Supplier<CreativeModeTab> LABYRYTHM_TAB =
            CREATIVE_MODE_TABS.register("labyrythm_tab", () -> LABYRYTHM_TAB_BUILDER.build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

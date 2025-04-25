package com.github.sajmon.labyrythm.item;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Labyrythm.MOD_ID);


    public static final DeferredItem<MinotaursResonanceItem> MINOTAURS_RESONANCE = ITEMS.register("minotaurs_resonance",
            () -> new MinotaursResonanceItem(
                    ModTiers.SCULK,
                    new Item.Properties()
                    .rarity(Rarity.RARE)
                    .attributes(AxeItem.createAttributes(ModTiers.SCULK, 4, -3.0f))
            ));
            
    public static final DeferredItem<SculkHornItem> SCULK_HORN = ITEMS.register("sculk_horn",
            () -> new SculkHornItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

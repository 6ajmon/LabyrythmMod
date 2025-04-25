package com.github.sajmon.labyrythm.brewing;

import com.github.sajmon.labyrythm.item.ModItems;
import com.github.sajmon.labyrythm.potion.ModPotions;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public class ModBrewingRecipes {
    
    public static void register() {
        NeoForge.EVENT_BUS.register(ModBrewingRecipes.class);
    }
    
    @net.neoforged.bus.api.SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        // Use the PotionBrewing.Builder from the event
        var builder = event.getBuilder();
        
        // Base darkness potion from awkward potion + sculk horn
        builder.addMix(Potions.AWKWARD, ModItems.SCULK_HORN.get(), ModPotions.DARKNESS);
        
        // Alternative recipe with echo shard
        builder.addMix(Potions.AWKWARD, Items.ECHO_SHARD, ModPotions.DARKNESS);
        
        // Long duration variant
        builder.addMix(ModPotions.DARKNESS, Items.REDSTONE, ModPotions.LONG_DARKNESS);
        
        // Strong variant
        builder.addMix(ModPotions.DARKNESS, Items.GLOWSTONE_DUST, ModPotions.STRONG_DARKNESS);
    }
}

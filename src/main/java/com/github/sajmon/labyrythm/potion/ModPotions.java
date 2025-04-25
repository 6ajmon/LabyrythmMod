package com.github.sajmon.labyrythm.potion;

import com.github.sajmon.labyrythm.Labyrythm;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = 
            DeferredRegister.create(Registries.POTION, Labyrythm.MOD_ID);
    
    // Regular darkness potion (3 minutes duration)
    public static final DeferredHolder<Potion, Potion> DARKNESS = POTIONS.register("darkness",
            () -> new Potion(new MobEffectInstance(MobEffects.DARKNESS, 3600)));
    
    // Long darkness potion (8 minutes duration)
    public static final DeferredHolder<Potion, Potion> LONG_DARKNESS = POTIONS.register("long_darkness",
            () -> new Potion(new MobEffectInstance(MobEffects.DARKNESS, 9600)));
    
    // Lingering darkness potion (45 seconds but with area of effect)
    public static final DeferredHolder<Potion, Potion> LINGERING_DARKNESS = POTIONS.register("lingering_darkness",
            () -> new Potion(new MobEffectInstance(MobEffects.DARKNESS, 900)));
    
    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}

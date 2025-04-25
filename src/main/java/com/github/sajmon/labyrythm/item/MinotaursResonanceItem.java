package com.github.sajmon.labyrythm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import java.util.List;

public class MinotaursResonanceItem extends AxeItem {
    
    public MinotaursResonanceItem(Tier tier, Properties properties) {
        // Pass Iron Axe values to parent class: 6.0f attack damage, -3.1f attack speed (same as vanilla Iron Axe)
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply darkness effect (6 seconds = 120 ticks)
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, true));
        
        // Call the parent method to ensure normal axe behavior
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        
        // Add custom tooltips after
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip")
                   .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip2")
                   .withStyle(ChatFormatting.GRAY));
    }
}

package com.github.sajmon.labyrythm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class MinotaursResonanceItem extends AxeItem {
    
    public MinotaursResonanceItem(Tier tier, Properties properties) {
        // Pass Iron Axe values to parent class: 6.0f attack damage, -3.1f attack speed (same as vanilla Iron Axe)
        super(tier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Add the standard damage tooltips first (like vanilla weapons)
        tooltip.add((Component.empty()));
        
        // Add custom tooltips after
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip")
                   .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.labyrythm.minotaurs_resonance.tooltip2")
                   .withStyle(ChatFormatting.GRAY));
    }
}

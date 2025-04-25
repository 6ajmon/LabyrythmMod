package com.github.sajmon.labyrythm.item;

import com.github.sajmon.labyrythm.entity.ModEntityTypes;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public class SculkHornItem extends Item {
    private static final int COOLDOWN_TICKS = 600;
    
    public SculkHornItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        
        // Check if the block is a Sculk Catalyst
        if (level.getBlockState(pos).getBlock() instanceof SculkCatalystBlock) {
            if (!level.isClientSide) {
                // Create and spawn a minotaur
                MinotaurEntity minotaur = ModEntityTypes.MINOTAUR.get().create(level);
                if (minotaur != null) {
                    // Position the minotaur on top of the catalyst
                    minotaur.moveTo(
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            context.getRotation(), 0);
                    
                    // Set minotaur to full health
                    minotaur.setHealth(minotaur.getMaxHealth());
                    
                    // Add minotaur to the world
                    level.addFreshEntity(minotaur);
                    
                    // Play summoning sounds
                    level.playSound(null, pos, SoundEvents.WARDEN_EMERGE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F);
                    
                    // Emit game event for particles and other effects
                    level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
                    
                    // Consume the item if not in creative mode
                    if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                        context.getItemInHand().shrink(1);
                        context.getPlayer().getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                    }
                }
            }
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Use on a Sculk Catalyst to summon a Minotaur")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}

package com.github.sajmon.labyrythm.event;

import com.github.sajmon.labyrythm.Labyrythm;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid = Labyrythm.MOD_ID)
public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.BreakSpeed event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof ServerPlayer player && !player.level().isClientSide()) {
            try {
                LabyrinthEffectsManager.checkPlayerInLabyrinth(player, player.serverLevel());
            } catch (Exception e) {
                // Silently fail
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            LabyrinthEffectsManager.onPlayerLogout(player);
        }
    }
}
package com.github.sajmon.labyrythm.event;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int tickCounter = 0; // Now static
    
    // Track player position every tick to check if they're in a labyrinth
    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.BreakSpeed event) { // Now static
        Entity entity = event.getEntity();
        
        // Log only once every 100 ticks to avoid spam
        tickCounter++;
        boolean shouldLog = tickCounter % 100 == 0;
        
        if (entity instanceof ServerPlayer player && !player.level().isClientSide()) {
            if (shouldLog) {
                LOGGER.info("Checking if player {} is in labyrinth at position {}", 
                    player.getName().getString(), player.blockPosition());
            }
            
            try {
                LabyrinthEffectsManager.checkPlayerInLabyrinth(player, player.serverLevel());
                if (shouldLog) {
                    LOGGER.info("Successfully checked labyrinth for player {}", player.getName().getString());
                }
            } catch (Exception e) {
                LOGGER.error("Error checking if player is in labyrinth: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Clean up when players log out
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) { // Now static
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            LOGGER.info("Player {} logged out, cleaning up labyrinth effect tracking", player.getName().getString());
            LabyrinthEffectsManager.onPlayerLogout(player);
        }
    }
}
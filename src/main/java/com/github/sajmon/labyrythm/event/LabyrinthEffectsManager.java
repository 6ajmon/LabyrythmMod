package com.github.sajmon.labyrythm.event;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.structures.ModStructures;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LabyrinthEffectsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Boolean> playerInLabyrinth = new HashMap<>();
    private static final ResourceLocation LABYRINTH_ID = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaurs_labyrinth");
    
    // Mining fatigue effect parameters
    private static final int MINING_FATIGUE_LEVEL = 2;
    private static final int EFFECT_DURATION = 300;
    private static final boolean SHOW_PARTICLES = false;
    private static final boolean AMBIENT_EFFECT = true;
    
    // Debug counter to reduce log spam
    private static int debugCounter = 0;
    
    /**
     * Check if a player is inside a labyrinth structure and apply effects
     */
    public static void checkPlayerInLabyrinth(ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();
        UUID playerId = player.getUUID();
        
        // Log only periodically to reduce spam
        boolean shouldLog = (++debugCounter % 200) == 0;
        
        if (shouldLog) {
            LOGGER.info("[Labyrinth] Checking player {} at position {}", player.getName().getString(), playerPos);
        }
        
        // Get all labyrinth structures within range
        boolean isInLabyrinth = false;
        try {
            isInLabyrinth = isPositionInLabyrinth(playerPos, level);
            if (shouldLog) {
                LOGGER.info("[Labyrinth] Player is in labyrinth: {}", isInLabyrinth);
            }
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Error checking if position is in labyrinth: {}", e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Handle the player's state change
        Boolean wasInLabyrinth = playerInLabyrinth.get(playerId);
        
        if (isInLabyrinth) {
            if (wasInLabyrinth == null || !wasInLabyrinth) {
                // Player just entered the labyrinth
                LOGGER.info("[Labyrinth] Player {} entered the labyrinth at {}", player.getName().getString(), playerPos);
                onPlayerEnterLabyrinth(player);
                playerInLabyrinth.put(playerId, true);
            } else {
                // Player is still in the labyrinth
                if (shouldLog) {
                    LOGGER.debug("[Labyrinth] Player {} still in labyrinth", player.getName().getString());
                }
                refreshLabyrinthEffects(player);
            }
        } else if (wasInLabyrinth != null && wasInLabyrinth) {
            // Player just left the labyrinth
            LOGGER.info("[Labyrinth] Player {} exited the labyrinth", player.getName().getString());
            onPlayerExitLabyrinth(player);
            playerInLabyrinth.put(playerId, false);
        }
    }
    
    /**
     * Check if a position is inside any labyrinth structure
     */
    private static boolean isPositionInLabyrinth(BlockPos pos, ServerLevel level) {
        // Log structure registry key
        LOGGER.debug("[Labyrinth] Structure Key: {}", ModStructures.MINOTAURS_LABYRINTH_KEY);
        
        try {
            // Get the structure key
            ResourceKey<Structure> labyrinthKey = ModStructures.MINOTAURS_LABYRINTH_KEY;
            
            // Log structure registry access
            LOGGER.debug("[Labyrinth] Registry access: {}", level.registryAccess());
            
            // Get the structure registry
            var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            LOGGER.debug("[Labyrinth] Structure Registry: {}", structureRegistry);
            
            // Get the structure holder
            var structureHolder = structureRegistry.getHolder(labyrinthKey);
            
            if (structureHolder.isEmpty()) {
                LOGGER.warn("[Labyrinth] Could not find structure holder for key: {}", labyrinthKey);
                return false;
            }
            
            LOGGER.debug("[Labyrinth] Structure Holder: {}", structureHolder);
            
            // Get the actual structure
            Structure structure = structureHolder.get().value();
            
            if (structure == null) {
                LOGGER.warn("[Labyrinth] Structure is null for key: {}", labyrinthKey);
                return false;
            }
            
            LOGGER.debug("[Labyrinth] Structure: {}", structure);
            
            // Check if position is inside this structure
            var structureStart = level.structureManager().getStructureAt(pos, structure);
            LOGGER.debug("[Labyrinth] Structure Start: {}, Valid: {}", structureStart, structureStart.isValid());
            
            return structureStart.isValid();
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Exception in isPositionInLabyrinth: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Called when a player enters the labyrinth
     */
    private static void onPlayerEnterLabyrinth(Player player) {
        try {
            // Add mining fatigue effect
            MobEffectInstance effect = createMiningFatigueEffect();
            player.addEffect(effect);
            LOGGER.info("[Labyrinth] Applied mining fatigue effect to player {} (Level: {}, Duration: {})", 
                player.getName().getString(), effect.getAmplifier(), effect.getDuration());
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Error applying mining fatigue: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Refresh the effects on a player in the labyrinth
     */
    private static void refreshLabyrinthEffects(Player player) {
        // Keep refreshing the mining fatigue to maintain it while inside
        if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.addEffect(createMiningFatigueEffect());
            LOGGER.debug("[Labyrinth] Refreshed mining fatigue effect for player {}", player.getName().getString());
        }
    }
    
    /**
     * Called when a player exits the labyrinth
     */
    private static void onPlayerExitLabyrinth(Player player) {
        // Remove the mining fatigue effect when exiting
        boolean hadEffect = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        LOGGER.info("[Labyrinth] Removed mining fatigue effect from player {} (had effect: {})", 
            player.getName().getString(), hadEffect);
    }
    
    /**
     * Create the mining fatigue effect instance
     */
    private static MobEffectInstance createMiningFatigueEffect() {
        return new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                EFFECT_DURATION,
                MINING_FATIGUE_LEVEL,
                AMBIENT_EFFECT,
                SHOW_PARTICLES
        );
    }
    
    /**
     * Clear player tracking when they disconnect
     */
    public static void onPlayerLogout(Player player) {
        playerInLabyrinth.remove(player.getUUID());
        LOGGER.info("[Labyrinth] Removed player {} from tracking on logout", player.getName().getString());
    }
}
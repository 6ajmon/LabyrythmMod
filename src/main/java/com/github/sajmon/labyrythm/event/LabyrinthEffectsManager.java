package com.github.sajmon.labyrythm.event;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;
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
    
    private static final int MINING_FATIGUE_LEVEL = 2;
    private static final int EFFECT_DURATION = 300;
    private static final boolean SHOW_PARTICLES = false;
    private static final boolean AMBIENT_EFFECT = true;
    
    private static int debugCounter = 0;
    
    public static void checkPlayerInLabyrinth(ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();
        UUID playerId = player.getUUID();
        
        boolean shouldLog = (++debugCounter % 200) == 0;
        
        boolean isInLabyrinth = false;
        try {
            isInLabyrinth = isPositionInLabyrinth(playerPos, level);
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Error checking if position is in labyrinth: {}", e.getMessage());
            e.printStackTrace();
            return;
        }
        
        Boolean wasInLabyrinth = playerInLabyrinth.get(playerId);
        
        if (isInLabyrinth) {
            if (wasInLabyrinth == null || !wasInLabyrinth) {
                LOGGER.info("[Labyrinth] Player {} entered the labyrinth at {}", player.getName().getString(), playerPos);
                onPlayerEnterLabyrinth(player);
                playerInLabyrinth.put(playerId, true);
            } else {
                refreshLabyrinthEffects(player);
            }
        } else if (wasInLabyrinth != null && wasInLabyrinth) {
            LOGGER.info("[Labyrinth] Player {} exited the labyrinth", player.getName().getString());
            onPlayerExitLabyrinth(player);
            playerInLabyrinth.put(playerId, false);
        }
    }
    
    private static boolean isPositionInLabyrinth(BlockPos pos, ServerLevel level) {
        try {
            ResourceKey<Structure> labyrinthKey = ModStructures.MINOTAURS_LABYRINTH_KEY;
            
            var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            
            var structureHolder = structureRegistry.getHolder(labyrinthKey);
            
            if (structureHolder.isEmpty()) {
                LOGGER.warn("[Labyrinth] Could not find structure holder for key: {}", labyrinthKey);
                return false;
            }
            
            Structure structure = structureHolder.get().value();
            
            if (structure == null) {
                LOGGER.warn("[Labyrinth] Structure is null for key: {}", labyrinthKey);
                return false;
            }
            
            var structureStart = level.structureManager().getStructureAt(pos, structure);
            
            return structureStart.isValid();
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Exception in isPositionInLabyrinth: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static void onPlayerEnterLabyrinth(Player player) {
        try {
            // Check if Minotaur has been defeated in this dimension
            String dimensionKey = player.level().dimension().location().toString();
            
            if (MinotaurEntity.isMinotaurDefeatedInDimension(dimensionKey)) {
                LOGGER.info("[Labyrinth] Minotaur has been defeated in dimension {}, not applying mining fatigue to player {}", 
                        dimensionKey, player.getName().getString());
                return;
            }
            
            MobEffectInstance effect = createMiningFatigueEffect();
            player.addEffect(effect);
            LOGGER.info("[Labyrinth] Applied mining fatigue effect to player {}", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("[Labyrinth] Error applying mining fatigue: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void refreshLabyrinthEffects(Player player) {
        // Check if Minotaur has been defeated before refreshing
        String dimensionKey = player.level().dimension().location().toString();
        
        if (MinotaurEntity.isMinotaurDefeatedInDimension(dimensionKey)) {
            // If Minotaur is defeated, remove mining fatigue if it exists
            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
                LOGGER.info("[Labyrinth] Removed mining fatigue from player {} because Minotaur has been defeated", 
                        player.getName().getString());
            }
            return;
        }
        
        if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.addEffect(createMiningFatigueEffect());
        }
    }
    
    private static void onPlayerExitLabyrinth(Player player) {
        boolean hadEffect = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        LOGGER.info("[Labyrinth] Removed mining fatigue effect from player {}", player.getName().getString());
    }
    
    private static MobEffectInstance createMiningFatigueEffect() {
        return new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                EFFECT_DURATION,
                MINING_FATIGUE_LEVEL,
                AMBIENT_EFFECT,
                SHOW_PARTICLES
        );
    }
    
    public static void onPlayerLogout(Player player) {
        playerInLabyrinth.remove(player.getUUID());
        LOGGER.info("[Labyrinth] Removed player {} from tracking on logout", player.getName().getString());
    }
}
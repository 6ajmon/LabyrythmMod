package com.github.sajmon.labyrythm.event;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;
import com.github.sajmon.labyrythm.structures.ModStructures;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

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
    
    public static void checkPlayerInLabyrinth(ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();
        UUID playerId = player.getUUID();
        
        boolean isInLabyrinth = false;
        try {
            isInLabyrinth = isPositionInLabyrinth(playerPos, level);
        } catch (Exception e) {
            return;
        }
        
        Boolean wasInLabyrinth = playerInLabyrinth.get(playerId);
        
        if (isInLabyrinth) {
            if (wasInLabyrinth == null || !wasInLabyrinth) {
                onPlayerEnterLabyrinth(player);
                playerInLabyrinth.put(playerId, true);
            } else {
                refreshLabyrinthEffects(player);
            }
        } else if (wasInLabyrinth != null && wasInLabyrinth) {
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
                return false;
            }
            
            Structure structure = structureHolder.get().value();
            
            if (structure == null) {
                return false;
            }
            
            var structureStart = level.structureManager().getStructureAt(pos, structure);
            
            return structureStart.isValid();
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void onPlayerEnterLabyrinth(Player player) {
        try {
            String dimensionKey = player.level().dimension().location().toString();
            
            if (MinotaurEntity.isMinotaurDefeatedInDimension(dimensionKey)) {
                return;
            }
            
            MobEffectInstance effect = createMiningFatigueEffect();
            player.addEffect(effect);
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    private static void refreshLabyrinthEffects(Player player) {
        String dimensionKey = player.level().dimension().location().toString();
        
        if (MinotaurEntity.isMinotaurDefeatedInDimension(dimensionKey)) {
            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
            }
            return;
        }
        
        if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.addEffect(createMiningFatigueEffect());
        }
    }
    
    private static void onPlayerExitLabyrinth(Player player) {
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
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
    }
}
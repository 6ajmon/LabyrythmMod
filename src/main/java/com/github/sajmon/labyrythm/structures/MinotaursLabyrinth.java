package com.github.sajmon.labyrythm.structures;

import com.github.sajmon.labyrythm.structures.pieces.MinotaursLabyrinthPieces;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.core.HolderSet;


public class MinotaursLabyrinth extends LabyrythmStructure {
    public static final MapCodec<MinotaursLabyrinth> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    Biome.LIST_CODEC.fieldOf("allowed_biomes").forGetter(structure -> structure.allowedBiomes),
                    Codec.BOOL.fieldOf("check_height").forGetter(structure -> structure.doCheckHeight),
                    Codec.BOOL.fieldOf("avoid_water").forGetter(structure -> structure.doAvoidWater),
                    Codec.BOOL.fieldOf("avoid_structures").forGetter(structure -> structure.doAvoidStructures),
                    Codec.INT.fieldOf("size").orElse(5).forGetter(structure -> structure.size)
            ).apply(instance, MinotaursLabyrinth::new)
    );

    private final HolderSet<Biome> allowedBiomes;
    private final boolean doCheckHeight;
    private final boolean doAvoidWater;
    private final boolean doAvoidStructures;
    private final int size;

    public MinotaursLabyrinth(StructureSettings settings, HolderSet<Biome> allowedBiomes,
                              boolean doCheckHeight, boolean doAvoidWater,
                              boolean doAvoidStructures, int size) {
        super(settings, allowedBiomes, doCheckHeight, doAvoidWater, doAvoidStructures);
        this.allowedBiomes = allowedBiomes;
        this.doCheckHeight = doCheckHeight;
        this.doAvoidWater = doAvoidWater;
        this.doAvoidStructures = doAvoidStructures;
        this.size = size;
    }

    @Override
    public void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos centerPos = new BlockPos(
                (chunkPos.x << 4) + 7,
                0,
                (chunkPos.z << 4) + 7
        );

        // Get the minimum and maximum height for the current world
        int minY = context.heightAccessor().getMinBuildHeight();
        int maxY = context.heightAccessor().getMaxBuildHeight();
        
        // Calculate a depth that's approximately 20-40% up from the bottom of the world
        // This places it in deep underground but not at bedrock level
        int range = maxY - minY;
        int undergroundHeight = minY + 10 + context.random().nextInt(Math.max(1, (int)(range * 0.2)));
        
        centerPos = new BlockPos(centerPos.getX(), undergroundHeight, centerPos.getZ());

        RandomSource random = context.random();
        Rotation rotation = Rotation.getRandom(random);

        MinotaursLabyrinthPieces.addPieces(
                builder,
                centerPos,
                rotation,
                random,
                this.size,
                context.structureTemplateManager()
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MINOTAURS_LABYRINTH.get();
    }
}
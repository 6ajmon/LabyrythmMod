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
import net.minecraft.world.level.levelgen.Heightmap;


public class MinotaursLabyrinth extends LabyrythmStructure {
    // Define codec for serialization with our specific parameters
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

    // Private fields specific to MinotaursLabyrinth
    private final HolderSet<Biome> allowedBiomes;
    private final boolean doCheckHeight;
    private final boolean doAvoidWater;
    private final boolean doAvoidStructures;
    private final int size; // Controls the size/complexity of the labyrinth

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
        // Get chunk position and create a BlockPos at the center
        ChunkPos chunkPos = context.chunkPos();
        BlockPos centerPos = new BlockPos(
                (chunkPos.x << 4) + 7,
                0, // Height will be determined later
                (chunkPos.z << 4) + 7
        );

        // Get surface height at the center position
        int surfaceHeight = context.chunkGenerator().getBaseHeight(
                centerPos.getX(),
                centerPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,  // Changed from int to Heightmap.Types
                context.heightAccessor(),
                context.randomState()
        );

        // Update centerPos with the correct height
        centerPos = new BlockPos(centerPos.getX(), surfaceHeight, centerPos.getZ());

        // Get random source from context
        RandomSource random = context.random();

        // Choose a random rotation for the structure
        Rotation rotation = Rotation.getRandom(random);

        // Create the main entrance piece and pass the template manager
        MinotaursLabyrinthPieces.addPieces(
                builder,
                centerPos,
                rotation,
                random,
                this.size,
                context.structureTemplateManager() // Pass the template manager from the context
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MINOTAURS_LABYRINTH.get();
    }
}
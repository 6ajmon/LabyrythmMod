package com.github.sajmon.labyrythm.structures;


import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public abstract class LabyrythmStructure extends Structure {
    private HolderSet<Biome> allowedBiomes;
    private boolean doCheckHeight;
    private boolean doAvoidWater;
    private boolean doAvoidStructures;

    public LabyrythmStructure(StructureSettings settings, HolderSet<Biome> allowedBiomes, boolean doCheckHeight, boolean doAvoidWater, boolean doAvoidStructures) {
        super(settings);
        this.allowedBiomes = allowedBiomes;
        this.doCheckHeight = doCheckHeight;
        this.doAvoidWater = doAvoidWater;
        this.doAvoidStructures = doAvoidStructures;
    }

    public LabyrythmStructure(StructureSettings settings, HolderSet<Biome> allowedBiomes) {
        this(settings, allowedBiomes, true, true, true);
    }

    public LabyrythmStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if(this.checkLocation(context)) {
            // Use onTopOfChunkCenter instead of the non-existent generalizeHeight
            // The actual height calculation will be done in generatePieces
            return Structure.onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, (builder) -> {
                this.generatePieces(builder, context);
            });
        }
        return Optional.empty();
    }

    public void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {

    }

    public boolean checkLocation(GenerationContext context) {
        return this.checkLocation(context, allowedBiomes, doCheckHeight, doAvoidWater, doAvoidStructures);
    }

    protected boolean checkLocation(GenerationContext context, HolderSet<Biome> allowedBiomes, boolean checkHeight, boolean avoidWater, boolean avoidStructures) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos centerOfChunk = new BlockPos((chunkPos.x << 4) + 7, 0, (chunkPos.z << 4) + 7);

        if (avoidWater) {
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            LevelHeightAccessor heightLimitView = context.heightAccessor();
            int centerHeight = chunkGenerator.getBaseHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, context.randomState());
            NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), heightLimitView, context.randomState());
            BlockState topBlock = columnOfBlocks.getBlock(centerHeight);
            if (!topBlock.getFluidState().isEmpty()) return false;
        }

        return true;
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
    }
}

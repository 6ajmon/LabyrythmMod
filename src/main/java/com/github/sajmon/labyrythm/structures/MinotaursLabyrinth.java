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

        int surfaceHeight = context.chunkGenerator().getBaseHeight(
                centerPos.getX(),
                centerPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        centerPos = new BlockPos(centerPos.getX(), surfaceHeight, centerPos.getZ());

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
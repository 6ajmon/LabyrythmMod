package com.github.sajmon.labyrythm.structures.pieces;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

public class MinotaursLabyrinthPieces {
    private static final ResourceLocation ENTRANCE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_entrance");
    private static final ResourceLocation STRAIGHT = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_straight");
    private static final ResourceLocation CORNER = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_corner");
    private static final ResourceLocation TSHAPE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_tshape");
    private static final ResourceLocation END = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end");
    private static final ResourceLocation CROSS = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_cross");
    private static final ResourceLocation END_HATCH = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_hatch");
    private static final ResourceLocation END_CHEST = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_chest");
    private static final ResourceLocation BOSS_ROOM = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_boss_room");

    private static final int PIECE_SIZE = 7;
    
    private static final Map<ResourceLocation, Set<Direction>> PIECE_CONNECTIONS = new HashMap<>();
    
    static {
        PIECE_CONNECTIONS.put(ENTRANCE, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        PIECE_CONNECTIONS.put(STRAIGHT, EnumSet.of(Direction.EAST, Direction.WEST));
        PIECE_CONNECTIONS.put(CORNER, EnumSet.of(Direction.EAST, Direction.NORTH));
        PIECE_CONNECTIONS.put(TSHAPE, EnumSet.of(Direction.EAST, Direction.NORTH, Direction.SOUTH));
        PIECE_CONNECTIONS.put(END, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(CROSS, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        PIECE_CONNECTIONS.put(END_HATCH, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(END_CHEST, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(BOSS_ROOM, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
    }

    public static void addPieces(StructurePiecesBuilder builder, BlockPos centerPos, Rotation initialRotation,
                                 RandomSource random, int configSize, StructureTemplateManager templateManager) {
        int levels = 2 + random.nextInt(3);
        
        int mazeSize = Math.max(9, (int)Math.sqrt(configSize));
        if (mazeSize % 2 == 0) mazeSize++;
        
        boolean bossRoomPlaced = false;
        GridPos bossRoomPosition = null;
        
        for (int level = 0; level < levels; level++) {
            int yOffset = -level * 7;
            
            MazeCell[][] mazeGrid = new MazeCell[mazeSize][mazeSize];
            for (int z = 0; z < mazeSize; z++) {
                for (int x = 0; x < mazeSize; x++) {
                    mazeGrid[z][x] = new MazeCell();
                }
            }
            
            int centerX = mazeSize / 2;
            int centerZ = mazeSize / 2;
            
            BlockPos levelCenterPos = new BlockPos(
                centerPos.getX(),
                centerPos.getY() + yOffset,
                centerPos.getZ()
            );
            
            Set<GridPos> visited = new HashSet<>();
            Map<GridPos, PieceInfo> pieceInfoMap = new HashMap<>();
            
            GridPos entrancePos;
            
            if (level == 0) {
                int minPos = 2;
                int maxPos = mazeSize - 3;
                
                int entranceX = centerX + random.nextIntBetweenInclusive(-1, 1);
                int entranceZ = centerZ + random.nextIntBetweenInclusive(-1, 1); 
                
                entranceX = Math.max(minPos, Math.min(maxPos, entranceX));
                entranceZ = Math.max(minPos, Math.min(maxPos, entranceZ));
                
                entrancePos = new GridPos(entranceX, entranceZ);
                
                visited.add(entrancePos);
                pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
            } else {
                GridPos hatchPos = level > 0 ? levelConnections.get(level-1) : null;
                
                if (hatchPos != null) {
                    entrancePos = hatchPos;
                    visited.add(entrancePos);
                    pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
                } else {
                    int minPos = 2;
                    int maxPos = mazeSize - 3;
                    int entranceX = random.nextIntBetweenInclusive(minPos, maxPos);
                    int entranceZ = random.nextIntBetweenInclusive(minPos, maxPos);
                    
                    entrancePos = new GridPos(entranceX, entranceZ);
                    visited.add(entrancePos);
                    pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
                }
            }
            
            Stack<GridPos> stack = new Stack<>();
            stack.push(entrancePos);
            
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            
            while (!stack.isEmpty()) {
                GridPos current = stack.peek();
                
                List<Direction> validDirections = new ArrayList<>();
                for (Direction dir : directions) {
                    GridPos neighbor = current.offset(dir);
                    if (isValidPosition(neighbor, mazeSize) && !visited.contains(neighbor)) {
                        validDirections.add(dir);
                    }
                }
                
                if (!validDirections.isEmpty()) {
                    Direction chosenDir = validDirections.get(random.nextInt(validDirections.size()));
                    GridPos next = current.offset(chosenDir);
                    
                    connectCells(pieceInfoMap, current, next, chosenDir, random);
                    
                    visited.add(next);
                    stack.push(next);
                } else {
                    stack.pop();
                }
            }
            
            if (level == levels - 1 && !bossRoomPlaced) {
                List<GridPos> suitablePositions = new ArrayList<>();
                
                int minInterior = 1;
                int maxInterior = mazeSize - 2;
                
                for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                    GridPos pos = entry.getKey();
                    ResourceLocation pieceType = entry.getValue().pieceType;
                    
                    if (pos.x > minInterior && pos.x < maxInterior && 
                        pos.z > minInterior && pos.z < maxInterior) {
                        
                        if (pieceType == CROSS || pieceType == TSHAPE) {
                            suitablePositions.add(pos);
                            suitablePositions.add(pos);
                        } else if (pieceType == STRAIGHT) {
                            suitablePositions.add(pos);
                        }
                    }
                }
                
                if (!suitablePositions.isEmpty()) {
                    GridPos bossPos = suitablePositions.get(random.nextInt(suitablePositions.size()));
                    PieceInfo originalInfo = pieceInfoMap.get(bossPos);
                    pieceInfoMap.put(bossPos, new PieceInfo(BOSS_ROOM, originalInfo.rotation, level));
                    bossRoomPlaced = true;
                    bossRoomPosition = bossPos;
                } else {
                    List<GridPos> interiorPositions = new ArrayList<>();
                    
                    for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                        GridPos pos = entry.getKey();
                        if (pos.x > minInterior && pos.x < maxInterior && 
                            pos.z > minInterior && pos.z < maxInterior) {
                            interiorPositions.add(pos);
                        }
                    }
                    
                    if (!interiorPositions.isEmpty()) {
                        GridPos bossPos = interiorPositions.get(random.nextInt(interiorPositions.size()));
                        PieceInfo originalInfo = pieceInfoMap.get(bossPos);
                        pieceInfoMap.put(bossPos, new PieceInfo(BOSS_ROOM, originalInfo.rotation, level));
                        bossRoomPlaced = true;
                        bossRoomPosition = bossPos;
                    } else {
                        for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                            GridPos pos = entry.getKey();
                            if (pos.x > 0 && pos.x < mazeSize - 1 && 
                                pos.z > 0 && pos.z < mazeSize - 1) {
                                PieceInfo originalInfo = pieceInfoMap.get(pos);
                                pieceInfoMap.put(pos, new PieceInfo(BOSS_ROOM, originalInfo.rotation, level));
                                bossRoomPlaced = true;
                                bossRoomPosition = pos;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (level < levels - 1) {
                List<GridPos> interiorEndPieces = new ArrayList<>();
                List<GridPos> fallbackEndPieces = new ArrayList<>();
                
                int minInterior = 2;
                int maxInterior = mazeSize - 3;
                
                for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                    if (entry.getValue().pieceType == END) {
                        GridPos pos = entry.getKey();
                        
                        if (pos.x >= minInterior && pos.x <= maxInterior && 
                            pos.z >= minInterior && pos.z <= maxInterior) {
                            interiorEndPieces.add(pos);
                        } else if (pos.x > 0 && pos.x < mazeSize - 1 && 
                                  pos.z > 0 && pos.z < mazeSize - 1) {
                            fallbackEndPieces.add(pos);
                        }
                    }
                }
                
                GridPos hatchPos = null;
                
                if (!interiorEndPieces.isEmpty()) {
                    hatchPos = interiorEndPieces.get(random.nextInt(interiorEndPieces.size()));
                } else if (!fallbackEndPieces.isEmpty()) {
                    hatchPos = fallbackEndPieces.get(random.nextInt(fallbackEndPieces.size()));
                } else if (!pieceInfoMap.isEmpty()) {
                    List<GridPos> allEndPieces = new ArrayList<>();
                    for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                        if (entry.getValue().pieceType == END) {
                            allEndPieces.add(entry.getKey());
                        }
                    }
                    
                    if (!allEndPieces.isEmpty()) {
                        hatchPos = allEndPieces.get(random.nextInt(allEndPieces.size()));
                    }
                }
                
                if (hatchPos != null) {
                    PieceInfo endInfo = pieceInfoMap.get(hatchPos);
                    
                    pieceInfoMap.put(hatchPos, new PieceInfo(END_HATCH, endInfo.rotation));
                    
                    levelConnections.put(level, hatchPos);
                    
                    List<GridPos> endPieces = new ArrayList<>();
                    for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                        if (entry.getValue().pieceType == END && !entry.getKey().equals(hatchPos)) {
                            endPieces.add(entry.getKey());
                        }
                    }
                    
                    for (GridPos endPos : endPieces) {
                        if (random.nextFloat() < 0.25f) {
                            pieceInfoMap.put(endPos, new PieceInfo(END_CHEST, pieceInfoMap.get(endPos).rotation, level));
                        }
                    }
                }
            } else {
                List<GridPos> endPieces = new ArrayList<>();
                for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                    if (entry.getValue().pieceType == END) {
                        endPieces.add(entry.getKey());
                    }
                }
                
                for (GridPos endPos : endPieces) {
                    if (random.nextFloat() < 0.5f) {
                        pieceInfoMap.put(endPos, new PieceInfo(END_CHEST, pieceInfoMap.get(endPos).rotation, level));
                    }
                }
            }
            
            for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                GridPos gridPos = entry.getKey();
                PieceInfo pieceInfo = entry.getValue();
                
                BlockPos piecePos = new BlockPos(
                    levelCenterPos.getX() + ((gridPos.x - centerX) * PIECE_SIZE),
                    levelCenterPos.getY(),
                    levelCenterPos.getZ() + ((gridPos.z - centerZ) * PIECE_SIZE)
                );
                
                LabyrinthPiece piece = new LabyrinthPiece(
                    templateManager,
                    pieceInfo.pieceType,
                    piecePos,
                    pieceInfo.rotation,
                     0,
                    level
                );
                
                builder.addPiece(piece);
            }
        }
    }

    private static final Map<Integer, GridPos> levelConnections = new HashMap<>();

    private static boolean isValidPosition(GridPos pos, int mazeSize) {
        return pos.x >= 0 && pos.x < mazeSize && pos.z >= 0 && pos.z < mazeSize;
    }
    
    private static void connectCells(Map<GridPos, PieceInfo> pieceInfoMap, GridPos current, GridPos next, Direction dir, RandomSource random) {
        PieceInfo currentInfo = pieceInfoMap.getOrDefault(current, null);
        Set<Direction> currentConnections = new HashSet<>();
        
        if (currentInfo != null) {
            currentConnections = getRotatedConnections(PIECE_CONNECTIONS.get(currentInfo.pieceType), currentInfo.rotation);
        }
        
        currentConnections.add(dir);
        
        if (currentInfo == null || currentInfo.pieceType != ENTRANCE) {
            PieceInfo updatedInfo = determinePieceTypeAndRotation(currentConnections);
            pieceInfoMap.put(current, updatedInfo);
        }
        
        Set<Direction> nextConnections = EnumSet.of(dir.getOpposite());
        
        PieceInfo nextInfo = determinePieceTypeAndRotation(nextConnections);
        pieceInfoMap.put(next, nextInfo);
    }
    
    private static Set<Direction> getRotatedConnections(Set<Direction> connections, Rotation rotation) {
        Set<Direction> rotated = EnumSet.noneOf(Direction.class);
        for (Direction dir : connections) {
            rotated.add(rotation.rotate(dir));
        }
        return rotated;
    }
    
    private static PieceInfo determinePieceTypeAndRotation(Set<Direction> connections) {
        int connectionCount = connections.size();
        ResourceLocation pieceType;
        Rotation rotation = Rotation.NONE;
        
        switch (connectionCount) {
            case 4:
                pieceType = CROSS;
                break;
                
            case 3:
                pieceType = TSHAPE;
                
                if (connections.containsAll(EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST))) {
                    rotation = Rotation.COUNTERCLOCKWISE_90;
                } else if (connections.containsAll(EnumSet.of(Direction.SOUTH, Direction.EAST, Direction.WEST))) {
                    rotation = Rotation.CLOCKWISE_90;
                } else if (connections.containsAll(EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST))) {
                    rotation = Rotation.CLOCKWISE_180;
                }
                break;
                
            case 2:
                if (connections.containsAll(EnumSet.of(Direction.EAST, Direction.WEST)) ||
                    connections.containsAll(EnumSet.of(Direction.NORTH, Direction.SOUTH))) {
                    pieceType = STRAIGHT;
                    
                    if (connections.contains(Direction.NORTH)) {
                        rotation = Rotation.CLOCKWISE_90;
                    }
                } else {
                    pieceType = CORNER;
                    
                    if (connections.containsAll(EnumSet.of(Direction.EAST, Direction.SOUTH))) {
                        rotation = Rotation.CLOCKWISE_90;
                    } else if (connections.containsAll(EnumSet.of(Direction.SOUTH, Direction.WEST))) {
                        rotation = Rotation.CLOCKWISE_180;
                    } else if (connections.containsAll(EnumSet.of(Direction.WEST, Direction.NORTH))) {
                        rotation = Rotation.COUNTERCLOCKWISE_90;
                    }
                }
                break;
                
            case 1:
            default:
                pieceType = END;
                Direction dir = connections.iterator().next();
                
                if (dir == Direction.SOUTH) {
                    rotation = Rotation.CLOCKWISE_90;
                } else if (dir == Direction.WEST) {
                    rotation = Rotation.CLOCKWISE_180;
                } else if (dir == Direction.NORTH) {
                    rotation = Rotation.COUNTERCLOCKWISE_90;
                }
                break;
        }
        
        return new PieceInfo(pieceType, rotation);
    }
    
    private static class GridPos {
        final int x;
        final int z;
        
        GridPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        GridPos offset(Direction dir) {
            return new GridPos(x + dir.getStepX(), z + dir.getStepZ());
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridPos gridPos = (GridPos) o;
            return x == gridPos.x && z == gridPos.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
    
    private static class MazeCell {
        private final Set<Direction> connections = EnumSet.noneOf(Direction.class);
        
        public void addConnection(Direction dir) {
            connections.add(dir);
        }
        
        public Set<Direction> getConnections() {
            return connections;
        }
    }
    
    private static class PieceInfo {
        final ResourceLocation pieceType;
        final Rotation rotation;
        final int level;
        
        PieceInfo(ResourceLocation pieceType, Rotation rotation) {
            this(pieceType, rotation, 0);
        }
        
        PieceInfo(ResourceLocation pieceType, Rotation rotation, int level) {
            this.pieceType = pieceType;
            this.rotation = rotation;
            this.level = level;
        }
    }

    public static class LabyrinthPiece extends TemplateStructurePiece {
        private final int level;

        public LabyrinthPiece(StructureTemplateManager manager, ResourceLocation location,
                              BlockPos pos, Rotation rotation, int yOffset, int level) {
            super(ModStructurePieces.MINOTAUR_LABYRINTH_PIECE.get(), 0, manager, location,
                    location.toString() + "_level_" + level, makeSettings(rotation), pos.offset(0, yOffset, 0));
            this.level = level;
        }

        public LabyrinthPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.MINOTAUR_LABYRINTH_PIECE.get(), tag, context.structureTemplateManager(),
                    (location) -> makeSettings(Rotation.valueOf(tag.getString("Rotation"))));
            this.level = tag.getInt("Level");
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation) {
            return new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setMirror(Mirror.NONE)
                    .setRotationPivot(new BlockPos(3, 0, 3))
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox box) {
            if (marker.startsWith("Chest")) {
                Rotation pieceRotation = this.placeSettings.getRotation();
                
                Direction chestFacing = Direction.EAST;
                
                if (pieceRotation == Rotation.CLOCKWISE_90) {
                    chestFacing = Direction.SOUTH;
                } else if (pieceRotation == Rotation.CLOCKWISE_180) {
                    chestFacing = Direction.WEST;
                } else if (pieceRotation == Rotation.COUNTERCLOCKWISE_90) {
                    chestFacing = Direction.NORTH;
                }
                
                level.setBlock(pos, Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, chestFacing), 2);
                
                BlockEntity blockEntity = level.getBlockEntity(pos);
                
                if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                    int structureLevel = this.level;
                    
                    ResourceLocation lootTableLocation;
                    if (structureLevel <= 0) {
                        lootTableLocation = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_basic");
                    } else if (structureLevel == 1) {
                        lootTableLocation = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_uncommon");
                    } else if (structureLevel == 2) {
                        lootTableLocation = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_rare");
                    } else {
                        lootTableLocation = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "chests/labyrinth_valuable");
                    }
                    
                    ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);
                    
                    chestBlockEntity.setLootTable(lootTableKey, random.nextLong());
                }
            }
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", this.placeSettings.getRotation().name());
            tag.putInt("Level", this.level);
        }
    }
}
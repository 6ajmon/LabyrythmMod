package com.github.sajmon.labyrythm.structures.pieces;

import com.github.sajmon.labyrythm.Labyrythm;
import com.github.sajmon.labyrythm.entity.MinotaurEntity;
import com.github.sajmon.labyrythm.entity.ModEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.*;

public class MinotaursLabyrinthPieces {
    private static final ResourceLocation ENTRANCE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_entrance");
    private static final ResourceLocation STRAIGHT = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_straight");
    private static final ResourceLocation CORNER = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_corner");
    private static final ResourceLocation TSHAPE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_tshape");
    private static final ResourceLocation END = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end");
    private static final ResourceLocation CROSS = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_cross");
    private static final ResourceLocation END_HATCH = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_hatch");
    private static final ResourceLocation BOSS_ROOM = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_boss_room");
    private static final ResourceLocation END_CHEST_1 = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_chest_1");
    private static final ResourceLocation END_CHEST_2 = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_chest_2");
    private static final ResourceLocation END_CHEST_3 = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_chest_3");
    
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
        PIECE_CONNECTIONS.put(BOSS_ROOM, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        PIECE_CONNECTIONS.put(END_CHEST_1, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(END_CHEST_2, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(END_CHEST_3, EnumSet.of(Direction.EAST));
    }

    public static void addPieces(StructurePiecesBuilder builder, BlockPos centerPos, Rotation initialRotation,
                                 RandomSource random, int configSize, StructureTemplateManager templateManager) {
        int levels = 2 + random.nextInt(3);
        
        int mazeSize = Math.max(5, (int)Math.sqrt(configSize));
        if (mazeSize % 2 == 0) mazeSize++;
        
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
                entrancePos = new GridPos(centerX, centerZ);
                visited.add(entrancePos);
                pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
            } else {
                GridPos hatchPos = level > 0 ? levelConnections.get(level-1) : null;
                
                if (hatchPos != null) {
                    entrancePos = hatchPos;
                    visited.add(entrancePos);
                    pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
                } else {
                    entrancePos = new GridPos(centerX, centerZ);
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
            
            if (level < levels - 1) {
                List<GridPos> interiorEndPieces = new ArrayList<>();
                List<GridPos> edgeEndPieces = new ArrayList<>();
                
                for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                    if (entry.getValue().pieceType == END) {
                        GridPos pos = entry.getKey();
                        if (isEdgePosition(pos, mazeSize)) {
                            edgeEndPieces.add(pos);
                        } else {
                            interiorEndPieces.add(pos);
                        }
                    }
                }
                
                GridPos hatchPos = null;
                
                if (!interiorEndPieces.isEmpty()) {
                    hatchPos = interiorEndPieces.get(random.nextInt(interiorEndPieces.size()));
                } else if (!edgeEndPieces.isEmpty()) {
                    hatchPos = edgeEndPieces.get(random.nextInt(edgeEndPieces.size()));
                }
                
                if (hatchPos != null) {
                    PieceInfo endInfo = pieceInfoMap.get(hatchPos);
                    
                    pieceInfoMap.put(hatchPos, new PieceInfo(END_HATCH, endInfo.rotation));
                    
                    levelConnections.put(level, hatchPos);
                }
            }
            
            float chestChance = (level == levels - 1) ? 0.5f : 0.25f;
            
            for (Map.Entry<GridPos, PieceInfo> entry : new HashMap<>(pieceInfoMap).entrySet()) {
                if (entry.getValue().pieceType == END) {
                    if (random.nextFloat() < chestChance) {
                        PieceInfo endInfo = entry.getValue();
                        
                        ResourceLocation chestType = selectChestType(level, levels, random);
                        
                        pieceInfoMap.put(entry.getKey(), new PieceInfo(chestType, endInfo.rotation));
                    }
                }
            }
            
            if (level == levels - 1) {
                GridPos centerGridPos = new GridPos(centerX, centerZ);
                
                if (pieceInfoMap.containsKey(centerGridPos)) {
                    PieceInfo existingPiece = pieceInfoMap.get(centerGridPos);
                    Set<Direction> connections = getRotatedConnections(
                        PIECE_CONNECTIONS.get(existingPiece.pieceType), 
                        existingPiece.rotation
                    );
                    
                    pieceInfoMap.put(centerGridPos, new PieceInfo(BOSS_ROOM, existingPiece.rotation));
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
                    0
                );
                
                builder.addPiece(piece);
            }
        }
    }

    private static final Map<Integer, GridPos> levelConnections = new HashMap<>();

    private static boolean isValidPosition(GridPos pos, int mazeSize) {
        return pos.x >= 0 && pos.x < mazeSize && pos.z >= 0 && pos.z < mazeSize;
    }
    
    private static boolean isEdgePosition(GridPos pos, int mazeSize) {
        return pos.x == 0 || pos.x == mazeSize - 1 || pos.z == 0 || pos.z == mazeSize - 1;
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
    
    private static ResourceLocation selectChestType(int currentLevel, int totalLevels, RandomSource random) {
        float roll = random.nextFloat();
        
        float depthFactor = (float)currentLevel / (totalLevels - 1);
        
        if (currentLevel == 0) {
            if (roll < 0.80f) return END_CHEST_1;
            else if (roll < 0.95f) return END_CHEST_2;
            else return END_CHEST_3;
        } 
        else if (currentLevel == totalLevels - 1) {
            if (roll < 0.0f) return END_CHEST_1;
            else if (roll < 0.30f) return END_CHEST_2;
            else return END_CHEST_3;
        } 
        else {
            float chest1Chance = 0.45f - (0.45f * depthFactor * depthFactor);
            float chest2Chance = 0.40f + (0.15f * depthFactor);
            
            if (roll < chest1Chance) return END_CHEST_1;
            else if (roll < (chest1Chance + chest2Chance)) return END_CHEST_2;
            else return END_CHEST_3;
        }
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
        
        PieceInfo(ResourceLocation pieceType, Rotation rotation) {
            this.pieceType = pieceType;
            this.rotation = rotation;
        }
    }

    public static class LabyrinthPiece extends TemplateStructurePiece {

        public LabyrinthPiece(StructureTemplateManager manager, ResourceLocation location,
                              BlockPos pos, Rotation rotation, int yOffset) {
            super(ModStructurePieces.MINOTAUR_LABYRINTH_PIECE.get(), 0, manager, location,
                    location.toString(), makeSettings(rotation), pos.offset(0, yOffset, 0));
        }

        public LabyrinthPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.MINOTAUR_LABYRINTH_PIECE.get(), tag, context.structureTemplateManager(),
                    (location) -> makeSettings(Rotation.valueOf(tag.getString("Rotation"))));
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
            if (marker.equalsIgnoreCase("minotaur") || marker.toLowerCase().contains("minotaur")) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                
                try {
                    MinotaurEntity minotaur = ModEntityTypes.MINOTAUR.get().create(level.getLevel());
                    if (minotaur != null) {
                        minotaur.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                        boolean success = level.addFreshEntity(minotaur);
                        
                        if (success) {
                            minotaur.setHealth(minotaur.getMaxHealth());
                            minotaur.setPersistenceRequired();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", this.placeSettings.getRotation().name());
        }
    }
}
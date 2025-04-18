package com.github.sajmon.labyrythm.structures.pieces;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
    // Define the structure piece types based on your saved templates
    private static final ResourceLocation ENTRANCE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_entrance");
    private static final ResourceLocation STRAIGHT = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_straight");
    private static final ResourceLocation CORNER = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_corner");
    private static final ResourceLocation TSHAPE = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_tshape");
    private static final ResourceLocation END = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end");
    private static final ResourceLocation CROSS = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_cross");
    private static final ResourceLocation END_HATCH = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_end_hatch");
    private static final ResourceLocation BOSS_ROOM = ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "minotaur_labyrinth/ml_boss_room");
    

    // The standard size of a single piece
    private static final int PIECE_SIZE = 7;
    
    // Define connections for each piece type based on their default orientation
    private static final Map<ResourceLocation, Set<Direction>> PIECE_CONNECTIONS = new HashMap<>();
    
    static {
        // Define which directions each piece type connects to (without rotation)
        PIECE_CONNECTIONS.put(ENTRANCE, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        PIECE_CONNECTIONS.put(STRAIGHT, EnumSet.of(Direction.EAST, Direction.WEST));
        PIECE_CONNECTIONS.put(CORNER, EnumSet.of(Direction.EAST, Direction.NORTH));
        PIECE_CONNECTIONS.put(TSHAPE, EnumSet.of(Direction.EAST, Direction.NORTH, Direction.SOUTH));
        PIECE_CONNECTIONS.put(END, EnumSet.of(Direction.EAST));
        PIECE_CONNECTIONS.put(CROSS, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        PIECE_CONNECTIONS.put(END_HATCH, EnumSet.of(Direction.EAST)); // Tak samo jak END, ale z zejściem w dół
        PIECE_CONNECTIONS.put(BOSS_ROOM, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)); // Boss room connects in all directions
    }

    public static void addPieces(StructurePiecesBuilder builder, BlockPos centerPos, Rotation initialRotation,
                                 RandomSource random, int configSize, StructureTemplateManager templateManager) {
        // Generate a random number of levels between 1 and 4
        int levels = 2 + random.nextInt(3); // Random value from 1 to 4
        
        // Define maze size (adjust based on the configSize parameter)
        int mazeSize = Math.max(5, (int)Math.sqrt(configSize));
        if (mazeSize % 2 == 0) mazeSize++; // Ensure odd size for centered entrance
        
        System.out.println("Generating labyrinth with " + levels + " levels and maze size " + mazeSize + "x" + mazeSize);
        
        for (int level = 0; level < levels; level++) {
            // Offset Y position for different levels
            int yOffset = -level * 7; // Spacing between levels based on piece height
            
            // Create a 2D grid to represent our maze layout
            MazeCell[][] mazeGrid = new MazeCell[mazeSize][mazeSize];
            for (int z = 0; z < mazeSize; z++) {
                for (int x = 0; x < mazeSize; x++) {
                    mazeGrid[z][x] = new MazeCell();
                }
            }
            
            // Calculate center position for the entrance or for the position below the hatch
            int centerX = mazeSize / 2;
            int centerZ = mazeSize / 2;
            
            BlockPos levelCenterPos = new BlockPos(
                centerPos.getX(),
                centerPos.getY() + yOffset,
                centerPos.getZ()
            );
            
            // Generate the maze using randomized DFS
            Set<GridPos> visited = new HashSet<>();
            Map<GridPos, PieceInfo> pieceInfoMap = new HashMap<>();
            
            // Place the entrance based on level
            GridPos entrancePos;
            
            if (level == 0) {
                // For the first level, place entrance at the center
                entrancePos = new GridPos(centerX, centerZ);
                visited.add(entrancePos);
                pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
            } else {
                // For other levels, we'll place the entrance below the hatch from the previous level
                // The hatch position from previous level needs to be stored
                GridPos hatchPos = level > 0 ? levelConnections.get(level-1) : null;
                
                if (hatchPos != null) {
                    entrancePos = hatchPos;
                    visited.add(entrancePos);
                    pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
                } else {
                    // Fallback if no hatch position exists
                    entrancePos = new GridPos(centerX, centerZ);
                    visited.add(entrancePos);
                    pieceInfoMap.put(entrancePos, new PieceInfo(ENTRANCE, initialRotation));
                }
            }
            
            // Start DFS from the entrance
            Stack<GridPos> stack = new Stack<>();
            stack.push(entrancePos);
            
            // Four possible directions: North, East, South, West
            Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            
            while (!stack.isEmpty()) {
                GridPos current = stack.peek();
                
                // Get unvisited neighbors
                List<Direction> validDirections = new ArrayList<>();
                for (Direction dir : directions) {
                    GridPos neighbor = current.offset(dir);
                    if (isValidPosition(neighbor, mazeSize) && !visited.contains(neighbor)) {
                        validDirections.add(dir);
                    }
                }
                
                if (!validDirections.isEmpty()) {
                    // Choose a random direction
                    Direction chosenDir = validDirections.get(random.nextInt(validDirections.size()));
                    GridPos next = current.offset(chosenDir);
                    
                    // Connect current cell with the next cell
                    connectCells(pieceInfoMap, current, next, chosenDir, random);
                    
                    // Mark as visited and push to stack
                    visited.add(next);
                    stack.push(next);
                } else {
                    // Backtrack
                    stack.pop();
                }
            }
            
            // Add a hatch to the next level (except for the lowest level)
            if (level < levels - 1) {
                List<GridPos> interiorEndPieces = new ArrayList<>();
                List<GridPos> edgeEndPieces = new ArrayList<>();
                
                // Sort end pieces into interior and edge categories
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
                
                // Prioritize interior pieces, fall back to edge pieces if necessary
                if (!interiorEndPieces.isEmpty()) {
                    hatchPos = interiorEndPieces.get(random.nextInt(interiorEndPieces.size()));
                    System.out.println("Placed hatch at interior end piece");
                } else if (!edgeEndPieces.isEmpty()) {
                    hatchPos = edgeEndPieces.get(random.nextInt(edgeEndPieces.size()));
                    System.out.println("No interior end pieces available, placed hatch at edge end piece");
                }
                
                if (hatchPos != null) {
                    PieceInfo endInfo = pieceInfoMap.get(hatchPos);
                    
                    // Replace END with END_HATCH maintaining the rotation
                    pieceInfoMap.put(hatchPos, new PieceInfo(END_HATCH, endInfo.rotation));
                    
                    // Store hatch position for next level's entrance
                    levelConnections.put(level, hatchPos);
                }
            }
            
            // Replace center piece with boss room on the lowest level
            if (level == levels - 1) {
                GridPos centerGridPos = new GridPos(centerX, centerZ);
                
                // If there's a piece at the center, replace it with the boss room
                if (pieceInfoMap.containsKey(centerGridPos)) {
                    // Get connections from the existing piece to maintain the same pathways
                    PieceInfo existingPiece = pieceInfoMap.get(centerGridPos);
                    Set<Direction> connections = getRotatedConnections(
                        PIECE_CONNECTIONS.get(existingPiece.pieceType), 
                        existingPiece.rotation
                    );
                    
                    // Create boss room with same rotation to maintain connections
                    pieceInfoMap.put(centerGridPos, new PieceInfo(BOSS_ROOM, existingPiece.rotation));
                    
                    System.out.println("Placed boss room at center of lowest level (" + level + ")");
                }
            }
            
            // Now place all the pieces in the world based on our maze layout
            for (Map.Entry<GridPos, PieceInfo> entry : pieceInfoMap.entrySet()) {
                GridPos gridPos = entry.getKey();
                PieceInfo pieceInfo = entry.getValue();
                
                BlockPos piecePos = new BlockPos(
                    levelCenterPos.getX() + ((gridPos.x - centerX) * PIECE_SIZE),
                    levelCenterPos.getY(),
                    levelCenterPos.getZ() + ((gridPos.z - centerZ) * PIECE_SIZE)
                );
                
                System.out.println("Adding piece at: " + piecePos + " of type: " + pieceInfo.pieceType + 
                                  " with rotation: " + pieceInfo.rotation + 
                                  " for grid pos: " + gridPos.x + "," + gridPos.z +
                                  " on level: " + level);
                
                // Create and add the structure piece
                LabyrinthPiece piece = new LabyrinthPiece(
                    templateManager,
                    pieceInfo.pieceType,
                    piecePos,
                    pieceInfo.rotation,
                    0 // No additional Y offset, already included in piecePos
                );
                
                builder.addPiece(piece);
            }
        }
    }

    // Mapa przechowująca pozycje zejść między poziomami
    private static final Map<Integer, GridPos> levelConnections = new HashMap<>();

    // Helper method to check if a position is within the maze bounds
    private static boolean isValidPosition(GridPos pos, int mazeSize) {
        return pos.x >= 0 && pos.x < mazeSize && pos.z >= 0 && pos.z < mazeSize;
    }
    
    // Helper method to determine if a position is on the edge of the maze
    private static boolean isEdgePosition(GridPos pos, int mazeSize) {
        return pos.x == 0 || pos.x == mazeSize - 1 || pos.z == 0 || pos.z == mazeSize - 1;
    }
    
    // Helper method to connect two cells and assign appropriate piece types and rotations
    private static void connectCells(Map<GridPos, PieceInfo> pieceInfoMap, GridPos current, GridPos next, Direction dir, RandomSource random) {
        // Get or create the current cell's piece info
        PieceInfo currentInfo = pieceInfoMap.getOrDefault(current, null);
        Set<Direction> currentConnections = new HashSet<>();
        
        if (currentInfo != null) {
            // Get the actual connections after applying rotation
            currentConnections = getRotatedConnections(PIECE_CONNECTIONS.get(currentInfo.pieceType), currentInfo.rotation);
        }
        
        // Add the new connection direction
        currentConnections.add(dir);
        
        // Determine piece type and rotation for the current cell based on connections
        if (currentInfo == null || currentInfo.pieceType != ENTRANCE) {
            // Skip entrance, as it's already set
            PieceInfo updatedInfo = determinePieceTypeAndRotation(currentConnections);
            pieceInfoMap.put(current, updatedInfo);
        }
        
        // For the next cell, initially it just has one connection (opposite of dir)
        Set<Direction> nextConnections = EnumSet.of(dir.getOpposite());
        
        // Start with END piece for the next cell (we'll update it if more connections are added)
        PieceInfo nextInfo = determinePieceTypeAndRotation(nextConnections);
        pieceInfoMap.put(next, nextInfo);
    }
    
    // Apply rotation to a set of connections
    private static Set<Direction> getRotatedConnections(Set<Direction> connections, Rotation rotation) {
        Set<Direction> rotated = EnumSet.noneOf(Direction.class);
        for (Direction dir : connections) {
            rotated.add(rotation.rotate(dir));
        }
        return rotated;
    }
    
    // Determine the appropriate piece type and rotation based on connections
    private static PieceInfo determinePieceTypeAndRotation(Set<Direction> connections) {
        int connectionCount = connections.size();
        ResourceLocation pieceType;
        Rotation rotation = Rotation.NONE;
        
        switch (connectionCount) {
            case 4:
                // Four connections - cross piece (lub entrance jeśli jest na początku)
                pieceType = CROSS;
                break;
                
            case 3:
                // Three connections - T-shape
                pieceType = TSHAPE;
                
                // Determine rotation for T-shape, default connects EAST, NORTH, SOUTH
                if (connections.containsAll(EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST))) {
                    rotation = Rotation.COUNTERCLOCKWISE_90; // If North, East, West
                } else if (connections.containsAll(EnumSet.of(Direction.SOUTH, Direction.EAST, Direction.WEST))) {
                    rotation = Rotation.CLOCKWISE_90; // If South, East, West
                } else if (connections.containsAll(EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST))) {
                    rotation = Rotation.CLOCKWISE_180; // If North, South, West
                }
                // Default rotation for East, North, South
                break;
                
            case 2:
                // Check if opposite directions (straight) or adjacent (corner)
                if (connections.containsAll(EnumSet.of(Direction.EAST, Direction.WEST)) ||
                    connections.containsAll(EnumSet.of(Direction.NORTH, Direction.SOUTH))) {
                    // Straight piece
                    pieceType = STRAIGHT;
                    
                    // Rotation for North-South orientation
                    if (connections.contains(Direction.NORTH)) {
                        rotation = Rotation.CLOCKWISE_90;
                    }
                    // Default rotation for East-West
                } else {
                    // Corner piece
                    pieceType = CORNER;
                    
                    // Determine rotation, default connects EAST and NORTH
                    if (connections.containsAll(EnumSet.of(Direction.EAST, Direction.SOUTH))) {
                        rotation = Rotation.CLOCKWISE_90;
                    } else if (connections.containsAll(EnumSet.of(Direction.SOUTH, Direction.WEST))) {
                        rotation = Rotation.CLOCKWISE_180;
                    } else if (connections.containsAll(EnumSet.of(Direction.WEST, Direction.NORTH))) {
                        rotation = Rotation.COUNTERCLOCKWISE_90;
                    }
                    // Default rotation for East-North
                }
                break;
                
            case 1:
            default:
                // One connection - end piece
                pieceType = END;
                Direction dir = connections.iterator().next();
                
                // Determine rotation, default connects EAST
                if (dir == Direction.SOUTH) {
                    rotation = Rotation.CLOCKWISE_90;
                } else if (dir == Direction.WEST) {
                    rotation = Rotation.CLOCKWISE_180;
                } else if (dir == Direction.NORTH) {
                    rotation = Rotation.COUNTERCLOCKWISE_90;
                }
                // Default rotation for East
                break;
        }
        
        return new PieceInfo(pieceType, rotation);
    }
    
    // Simple class to represent a 2D grid position
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
    
    // Helper class to store maze cell information
    private static class MazeCell {
        private final Set<Direction> connections = EnumSet.noneOf(Direction.class);
        
        public void addConnection(Direction dir) {
            connections.add(dir);
        }
        
        public Set<Direction> getConnections() {
            return connections;
        }
    }
    
    // Class to hold piece type and rotation information
    private static class PieceInfo {
        final ResourceLocation pieceType;
        final Rotation rotation;
        
        PieceInfo(ResourceLocation pieceType, Rotation rotation) {
            this.pieceType = pieceType;
            this.rotation = rotation;
        }
    }

    // The actual structure piece class
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
                    .setRotationPivot(new BlockPos(3, 0, 3)) // Punkt obrotu w centrum elementu 7x7
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK); // Zmienione z STRUCTURE_AND_AIR na STRUCTURE_BLOCK_ONLY
        }

        @Override
        protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox box) {
            if (marker.startsWith("Chest")) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                // Set loot table
            } else if (marker.startsWith("Spawner")) {
                level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
                // Configure spawner
            }
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", this.placeSettings.getRotation().name());
        }
    }
}
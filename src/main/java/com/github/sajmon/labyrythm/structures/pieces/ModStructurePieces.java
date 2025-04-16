package com.github.sajmon.labyrythm.structures.pieces;

import com.github.sajmon.labyrythm.Labyrythm;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Labyrythm.MOD_ID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> MINOTAUR_LABYRINTH_PIECE =
            STRUCTURE_PIECE_TYPES.register("minotaur_labyrinth_piece",
                    () -> MinotaursLabyrinthPieces.LabyrinthPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PIECE_TYPES.register(eventBus);
    }
}
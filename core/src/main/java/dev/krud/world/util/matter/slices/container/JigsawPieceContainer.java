/**
 * KRUD World — World Generator
 * Copyright (C) 2026 Krud Studio
 *
 * Based on KrudWorld World Generator:
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 * https://github.com/VolmitSoftware/KrudWorld
 * License: GPL-3.0
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License.
 */

package dev.krud.world.util.matter.slices.container;

import dev.krud.world.engine.object.KrudWorldJigsawPiece;

public class JigsawPieceContainer extends RegistrantContainer<KrudWorldJigsawPiece> {
    public JigsawPieceContainer(String loadKey) {
        super(KrudWorldJigsawPiece.class, loadKey);
    }

    public static JigsawPieceContainer toContainer(KrudWorldJigsawPiece piece) {
        return new JigsawPieceContainer(piece.getLoadKey());
    }
}

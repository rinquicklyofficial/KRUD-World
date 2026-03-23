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

package dev.krud.world.engine.object;

import dev.krud.world.core.nms.datapack.IDataFixer;
import dev.krud.world.util.data.Varint;
import dev.krud.world.util.io.IO;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.*;

@Getter
@ToString
@Accessors(fluent = true, chain = true)
@EqualsAndHashCode
public final class KrudWorldDimensionType {
    @NonNull
    private final String key;
    @NonNull
    private final IDataFixer.Dimension base;
    @NonNull
    private final KrudWorldDimensionTypeOptions options;
    private final int logicalHeight;
    private final int height;
    private final int minY;

    public KrudWorldDimensionType(
            @NonNull IDataFixer.Dimension base,
            @NonNull KrudWorldDimensionTypeOptions options,
            int logicalHeight,
            int height,
            int minY
    ) {
        if (logicalHeight > height) throw new IllegalArgumentException("Logical height cannot be greater than height");
        if (logicalHeight < 0) throw new IllegalArgumentException("Logical height cannot be less than zero");
        if (height < 16 || height > 4064 ) throw new IllegalArgumentException("Height must be between 16 and 4064");
        if ((height & 15) != 0) throw new IllegalArgumentException("Height must be a multiple of 16");
        if (minY < -2032 || minY > 2031) throw new IllegalArgumentException("Min Y must be between -2032 and 2031");
        if ((minY & 15) != 0) throw new IllegalArgumentException("Min Y must be a multiple of 16");

        this.base = base;
        this.options = options;
        this.logicalHeight = logicalHeight;
        this.height = height;
        this.minY = minY;
        this.key = createKey();
    }

    public static KrudWorldDimensionType fromKey(String key) {
        var stream = new ByteArrayInputStream(IO.decode(key.replace(".", "=").toUpperCase()));
        try (var din = new DataInputStream(stream)) {
            return new KrudWorldDimensionType(
                    IDataFixer.Dimension.values()[din.readUnsignedByte()],
                    new KrudWorldDimensionTypeOptions().read(din),
                    Varint.readUnsignedVarInt(din),
                    Varint.readUnsignedVarInt(din),
                    Varint.readSignedVarInt(din)
            );
        } catch (IOException e) {
            throw new RuntimeException("This is impossible", e);
        }
    }

    public String toJson(IDataFixer fixer) {
        return fixer.createDimension(
                base,
                minY,
                height,
                logicalHeight,
                options.copy()
        ).toString(4);
    }

    private String createKey() {
        var stream = new ByteArrayOutputStream(41);
        try (var dos = new DataOutputStream(stream)) {
            dos.writeByte(base.ordinal());
            options.write(dos);
            Varint.writeUnsignedVarInt(logicalHeight, dos);
            Varint.writeUnsignedVarInt(height, dos);
            Varint.writeSignedVarInt(minY, dos);
        } catch (IOException e) {
            throw new RuntimeException("This is impossible", e);
        }

        return IO.encode(stream.toByteArray()).replace("=", ".").toLowerCase();
    }

    public KrudWorldDimensionTypeOptions options() {
        return options.copy();
    }
}

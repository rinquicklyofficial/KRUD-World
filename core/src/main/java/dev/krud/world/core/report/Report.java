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

package dev.krud.world.core.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Report {
    @Builder.Default
    private final ReportType type = ReportType.NOTICE;
    @Builder.Default
    private final String title = "Problem...";
    @Builder.Default
    private final String message = "No Message";
    @Builder.Default
    private final String suggestion = "No Suggestion";

    public String toString() {
        return type + ": " + title + ": " + message + ": Suggestion: " + suggestion;
    }
}

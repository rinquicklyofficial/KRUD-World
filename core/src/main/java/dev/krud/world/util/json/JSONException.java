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

package dev.krud.world.util.json;


/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * Constructs a new JSONException with the specified cause.
     *
     * @param cause The cause.
     */
    public JSONException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    /**
     * Returns the cause of this exception or null if the cause is nonexistent
     * or unknown.
     *
     * @return the cause of this exception or null if the cause is nonexistent
     * or unknown.
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }
}

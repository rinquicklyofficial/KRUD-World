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
 * The <code>JSONString</code> interface allows a <code>toJSONString()</code>
 * method so that a class can change the behavior of
 * <code>JSONObject.toString()</code>, <code>JSONArray.toString()</code>, and
 * <code>JSONWriter.value(</code>Object<code>)</code>. The
 * <code>toJSONString</code> method will be used instead of the default behavior
 * of using the Object's <code>toString()</code> method and quoting the result.
 */
public interface JSONString {
    /**
     * The <code>toJSONString</code> method allows a class to produce its own
     * JSON serialization.
     *
     * @return A strictly syntactically correct JSON text.
     */
    String toJSONString();
}

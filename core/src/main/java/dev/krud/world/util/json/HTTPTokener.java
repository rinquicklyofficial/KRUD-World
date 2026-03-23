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
 * The HTTPTokener extends the JSONTokener to provide additional methods for the
 * parsing of HTTP headers.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class HTTPTokener extends JSONTokener {

    /**
     * Construct an HTTPTokener from a string.
     *
     * @param string A source string.
     */
    public HTTPTokener(String string) {
        super(string);
    }

    /**
     * Get the next token or string. This is used in parsing HTTP headers.
     *
     * @return A String.
     */
    public String nextToken() throws JSONException {
        char c;
        char q;
        StringBuilder sb = new StringBuilder();
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if (c == '"' || c == '\'') {
            q = c;
            for (; ; ) {
                c = next();
                if (c < ' ') {
                    throw syntaxError("Unterminated string.");
                }
                if (c == q) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
        for (; ; ) {
            if (c == 0 || Character.isWhitespace(c)) {
                return sb.toString();
            }
            sb.append(c);
            c = next();
        }
    }
}

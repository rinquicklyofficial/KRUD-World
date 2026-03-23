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

package dev.krud.world.util.decree.annotations;

import dev.krud.world.util.decree.DecreeOrigin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Decree {

    String DEFAULT_DESCRIPTION = "No Description Provided";

    /**
     * The name of this command, which is the Method's name by default
     */
    String name() default "";

    /**
     * Only allow if studio mode is enabled
     *
     * @return defaults to false
     */
    boolean studio() default false;

    /**
     * If the node's functions MUST be run in sync, set this to true.<br>
     * Defaults to false
     */
    boolean sync() default false;

    /**
     * The description of this command.<br>
     * Is {@link #DEFAULT_DESCRIPTION} by default
     */
    String description() default DEFAULT_DESCRIPTION;

    /**
     * The origin this command must come from.<br>
     * Must be elements of the {@link DecreeOrigin} enum<br>
     * By default, is {@link DecreeOrigin#BOTH}, meaning both console & player can send the command
     */
    DecreeOrigin origin() default DecreeOrigin.BOTH;

    /**
     * The aliases of this parameter (instead of just the {@link #name() name} (if specified) or Method Name (name of
     * method))<br>
     * Can be initialized as just a string (ex. "alias") or as an array (ex. {"alias1", "alias2"})<br>
     * If someone uses /plugin foo and you specify alias="f" here, /plugin f will do the exact same.
     */
    String[] aliases() default "";
}

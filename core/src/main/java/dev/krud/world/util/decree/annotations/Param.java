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

import dev.krud.world.util.decree.DecreeParameterHandler;
import dev.krud.world.util.decree.specialhandlers.DummyHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String DEFAULT_DESCRIPTION = "No Description Provided";

    /**
     * The main name of this command.<br>
     * Required parameter.<br>
     * This is what is used in game, alongside any (if specified) {@link #aliases() aliases}
     */
    String name() default "";

    /**
     * The description of this parameter, used in help-popups in game.<br>
     * The default value is {@link #DEFAULT_DESCRIPTION}
     */
    String description() default DEFAULT_DESCRIPTION;

    /**
     * The default value for this argument.<br>
     * The entered string is parsed to the value similarly to how commandline-text would be.<br>
     * Which indicates the variable MUST be defined by the person running the command.<br>
     * If you define this, the variable automatically becomes non-required, but can still be set.
     */
    String defaultValue() default "";

    /**
     * The aliases of this parameter (instead of just the {@link #name() name} (if specified) or Method Name (name of
     * method))<br>
     * Can be initialized as just a string (ex. "alias") or as an array (ex. {"alias1", "alias2"})<br>
     * If someone uses /plugin foo bar=baz and you specify alias="b" here, /plugin foo b=baz will do the exact same.
     */
    String[] aliases() default "";

    /**
     * Attempts to dynamically pull context from the player, default data or something else for supported types
     */
    boolean contextual() default false;

    Class<? extends DecreeParameterHandler<?>> customHandler() default DummyHandler.class;
}

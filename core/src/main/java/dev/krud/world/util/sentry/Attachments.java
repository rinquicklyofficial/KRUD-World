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

package dev.krud.world.util.sentry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.krud.world.core.safeguard.KrudWorldSafeguard;
import dev.krud.world.util.collection.KMap;
import io.sentry.Attachment;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class Attachments {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Attachment PLUGINS = jsonProvider(Attachments::plugins, "plugins.json");
    public static final Attachment SAFEGUARD = jsonProvider(KrudWorldSafeguard::asAttachment, "safeguard.json");

    public static Attachment json(Object object, String name) {
        return new Attachment(GSON.toJson(object).getBytes(StandardCharsets.UTF_8), name, "application/json", "event.attachment", true);
    }

    public static Attachment jsonProvider(Callable<Object> object, String name) {
        return new Attachment(() -> GSON.toJson(object.call()).getBytes(StandardCharsets.UTF_8), name, "application/json", "event.attachment", true);
    }

    private static KMap<String, Object> plugins() {
        KMap<String, String> enabled = new KMap<>();
        KMap<String, String> disabled = new KMap<>();

        var pm = Bukkit.getPluginManager();
        for (var plugin : pm.getPlugins()) {
            if (plugin.isEnabled()) {
                enabled.put(plugin.getName(), plugin.getDescription().getVersion());
            } else {
                disabled.put(plugin.getName(), plugin.getDescription().getVersion());
            }
        }

        return new KMap<String, Object>()
                .qput("enabled", enabled)
                .qput("disabled", disabled);
    }
}

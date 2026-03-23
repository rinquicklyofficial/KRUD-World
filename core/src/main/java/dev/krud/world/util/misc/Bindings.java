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

package dev.krud.world.util.misc;

import com.google.gson.JsonSyntaxException;
import dev.krud.world.BuildConstants;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.KrudWorldSettings;
import dev.krud.world.core.nms.INMS;
import dev.krud.world.core.safeguard.KrudWorldSafeguard;
import dev.krud.world.core.tools.KrudWorldToolbelt;
import dev.krud.world.engine.platform.PlatformChunkGenerator;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.json.JSONException;
import dev.krud.world.util.reflect.ShadeFix;
import dev.krud.world.util.scheduling.J;
import dev.krud.world.util.sentry.Attachments;
import dev.krud.world.util.sentry.KrudWorldLogger;
import dev.krud.world.util.sentry.ServerID;
import io.sentry.Sentry;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Bindings {

    public static void capture(Throwable throwable) {
        Sentry.captureException(throwable);
    }

    public static void setupSentry() {
        var settings = KrudWorldSettings.get().getSentry();
        if (settings.disableAutoReporting || Sentry.isEnabled() || Boolean.getBoolean("iris.suppressReporting")) return;
        KrudWorld.info("Enabling Sentry for anonymous error reporting. You can disable this in the settings.");
        KrudWorld.info("Your server ID is: " + ServerID.ID);

        Sentry.init(options -> {
            options.setDsn("http://4cdbb9ac953306529947f4ca1e8e6b26@sentry.volmit.com:8080/2");
            if (settings.debug) {
                options.setLogger(new KrudWorldLogger());
                options.setDebug(true);
            }

            options.setAttachServerName(false);
            options.setEnableUncaughtExceptionHandler(false);
            options.setRelease(KrudWorld.instance.getDescription().getVersion());
            options.setEnvironment(BuildConstants.ENVIRONMENT);
            options.setBeforeSend((event, hint) -> {
                if (suppress(event.getThrowable())) return null;
                event.setTag("iris.safeguard", KrudWorldSafeguard.mode().getId());
                event.setTag("iris.nms", INMS.get().getClass().getCanonicalName());
                var context = KrudWorldContext.get();
                if (context != null) event.getContexts().set("engine", context.asContext());
                event.getContexts().set("safeguard", KrudWorldSafeguard.asContext());
                return event;
            });
        });
        Sentry.configureScope(scope -> {
            if (settings.includeServerId) scope.setUser(ServerID.asUser());
            scope.addAttachment(Attachments.PLUGINS);
            scope.addAttachment(Attachments.SAFEGUARD);
            scope.setTag("server", Bukkit.getVersion());
            scope.setTag("server.type", Bukkit.getName());
            scope.setTag("server.api", Bukkit.getBukkitVersion());
            scope.setTag("iris.commit", BuildConstants.COMMIT);
        });
    }

    private static boolean suppress(Throwable e) {
        return (e instanceof IllegalStateException ex && "zip file closed".equals(ex.getMessage()))
                || e instanceof JSONException
                || e instanceof JsonSyntaxException;
    }


    public static void setupBstats(KrudWorld plugin) {
        J.s(() -> {
            var metrics = new Metrics(plugin, 24220);
            metrics.addCustomChart(new SingleLineChart("custom_dimensions", () -> Bukkit.getWorlds()
                    .stream()
                    .filter(KrudWorldToolbelt::isKrudWorldWorld)
                    .mapToInt(w -> 1)
                    .sum()));

            metrics.addCustomChart(new DrilldownPie("used_packs", () -> Bukkit.getWorlds().stream()
                    .map(KrudWorldToolbelt::access)
                    .filter(Objects::nonNull)
                    .map(PlatformChunkGenerator::getEngine)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(engine -> engine.getDimension().getLoadKey(), engine -> {
                        var hash32 = engine.getHash32().getNow(null);
                        if (hash32 == null) return Map.of();
                        int version = engine.getDimension().getVersion();
                        String checksum = Long.toHexString(hash32);

                        return Map.of("v" + version + " (" + checksum + ")", 1);
                    }, (a, b) -> {
                        Map<String, Integer> merged = new HashMap<>(a);
                        b.forEach((k, v) -> merged.merge(k, v, Integer::sum));
                        return merged;
                    }))));
            metrics.addCustomChart(new DrilldownPie("environment", () -> Map.of(BuildConstants.ENVIRONMENT, Map.of(BuildConstants.COMMIT, 1))));

            plugin.postShutdown(metrics::shutdown);
        });
    }

    public static class Adventure {
        private final BukkitAudiences audiences;

        public Adventure(KrudWorld plugin) {
            ShadeFix.fix(ComponentSerializer.class);
            this.audiences = BukkitAudiences.create(plugin);
        }

        public Audience player(Player player) {
            return audiences.player(player);
        }

        public Audience sender(CommandSender sender) {
            return audiences.sender(sender);
        }
    }
}

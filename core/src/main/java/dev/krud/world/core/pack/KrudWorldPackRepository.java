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

package dev.krud.world.core.pack;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.service.StudioSVC;
import dev.krud.world.util.format.Form;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.scheduling.jobs.DownloadJob;
import dev.krud.world.util.scheduling.jobs.JobCollection;
import dev.krud.world.util.scheduling.jobs.SingleJob;
import lombok.Builder;
import lombok.Data;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

@Data
@Builder
public class KrudWorldPackRepository {
    @Builder.Default
    private String user = "KrudWorldDimensions";

    @Builder.Default
    private String repo = "overworld";

    @Builder.Default
    private String branch = "master";

    @Builder.Default
    private String tag = "";

    /**
     *
     */
    public static KrudWorldPackRepository from(String g) {
        // https://github.com/KrudWorldDimensions/overworld
        if (g.startsWith("https://github.com/")) {
            String sub = g.split("\\Qgithub.com/\\E")[1];
            KrudWorldPackRepository r = KrudWorldPackRepository.builder()
                    .user(sub.split("\\Q/\\E")[0])
                    .repo(sub.split("\\Q/\\E")[1]).build();

            if (g.contains("/tree/")) {
                r.setBranch(g.split("/tree/")[1]);
            }

            return r;
        } else if (g.contains("/")) {
            String[] f = g.split("\\Q/\\E");

            if (f.length == 1) {
                return from(g);
            } else if (f.length == 2) {
                return KrudWorldPackRepository.builder()
                        .user(f[0])
                        .repo(f[1])
                        .build();
            } else if (f.length >= 3) {
                KrudWorldPackRepository r = KrudWorldPackRepository.builder()
                        .user(f[0])
                        .repo(f[1])
                        .build();

                if (f[2].startsWith("#")) {
                    r.setTag(f[2].substring(1));
                } else {
                    r.setBranch(f[2]);
                }

                return r;
            }
        } else {
            return KrudWorldPackRepository.builder()
                    .user("KrudWorldDimensions")
                    .repo(g)
                    .branch(g.equals("overworld") ? "stable" : "master")
                    .build();
        }

        return null;
    }

    public String toURL() {
        if (!tag.trim().isEmpty()) {
            return "https://codeload.github.com/" + user + "/" + repo + "/zip/refs/tags/" + tag;
        }

        return "https://codeload.github.com/" + user + "/" + repo + "/zip/refs/heads/" + branch;
    }

    public void install(VolmitSender sender, Runnable whenComplete) throws MalformedURLException {
        File pack = KrudWorld.instance.getDataFolderNoCreate(StudioSVC.WORKSPACE_NAME, getRepo());

        if (!pack.exists()) {
            File dl = new File(KrudWorld.getTemp(), "dltk-" + UUID.randomUUID() + ".zip");
            File work = new File(KrudWorld.getTemp(), "extk-" + UUID.randomUUID());
            new JobCollection(Form.capitalize(getRepo()),
                    new DownloadJob(toURL(), pack),
                    new SingleJob("Extracting", () -> ZipUtil.unpack(dl, work)),
                    new SingleJob("Installing", () -> {
                        try {
                            FileUtils.copyDirectory(work.listFiles()[0], pack);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })).execute(sender, whenComplete);
        } else {
            sender.sendMessage("Pack already exists!");
        }
    }
}

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

package dev.krud.world.core.loader;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.scripting.environment.PackEnvironment;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.framework.Engine;
import dev.krud.world.engine.object.*;
import dev.krud.world.engine.object.annotations.Snippet;
import dev.krud.world.engine.object.matter.KrudWorldMatterObject;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.context.KrudWorldContext;
import dev.krud.world.util.format.C;
import dev.krud.world.util.mantle.flag.MantleFlagAdapter;
import dev.krud.world.util.mantle.flag.MantleFlag;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.parallel.BurstExecutor;
import dev.krud.world.util.parallel.MultiBurst;
import dev.krud.world.util.reflect.KeyedType;
import dev.krud.world.util.scheduling.ChronoLatch;
import dev.krud.world.util.scheduling.J;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Data
public class KrudWorldData implements ExclusionStrategy, TypeAdapterFactory {
    private static final KMap<File, KrudWorldData> dataLoaders = new KMap<>();
    private final File dataFolder;
    private final int id;
    private boolean closed = false;
    private PackEnvironment environment;
    private ResourceLoader<KrudWorldBiome> biomeLoader;
    private ResourceLoader<KrudWorldLootTable> lootLoader;
    private ResourceLoader<KrudWorldRegion> regionLoader;
    private ResourceLoader<KrudWorldDimension> dimensionLoader;
    private ResourceLoader<KrudWorldGenerator> generatorLoader;
    private ResourceLoader<KrudWorldJigsawPiece> jigsawPieceLoader;
    private ResourceLoader<KrudWorldJigsawPool> jigsawPoolLoader;
    private ResourceLoader<KrudWorldJigsawStructure> jigsawStructureLoader;
    private ResourceLoader<KrudWorldEntity> entityLoader;
    private ResourceLoader<KrudWorldMarker> markerLoader;
    private ResourceLoader<KrudWorldSpawner> spawnerLoader;
    private ResourceLoader<KrudWorldMod> modLoader;
    private ResourceLoader<KrudWorldBlockData> blockLoader;
    private ResourceLoader<KrudWorldExpression> expressionLoader;
    private ResourceLoader<KrudWorldObject> objectLoader;
    private ResourceLoader<KrudWorldMatterObject> matterLoader;
    private ResourceLoader<KrudWorldImage> imageLoader;
    private ResourceLoader<KrudWorldScript> scriptLoader;
    private ResourceLoader<KrudWorldCave> caveLoader;
    private ResourceLoader<KrudWorldRavine> ravineLoader;
    private ResourceLoader<KrudWorldMatterObject> matterObjectLoader;
    private KMap<String, KList<String>> possibleSnippets;
    private Gson gson;
    private Gson snippetLoader;
    private GsonBuilder builder;
    private KMap<Class<? extends KrudWorldRegistrant>, ResourceLoader<? extends KrudWorldRegistrant>> loaders = new KMap<>();
    private Engine engine;

    private KrudWorldData(File dataFolder) {
        this.engine = null;
        this.dataFolder = dataFolder;
        this.id = RNG.r.imax();
        hotloaded();
    }

    public static KrudWorldData get(File dataFolder) {
        return dataLoaders.computeIfAbsent(dataFolder, KrudWorldData::new);
    }

    public static Optional<KrudWorldData> getLoaded(File dataFolder) {
        return Optional.ofNullable(dataLoaders.get(dataFolder));
    }

    public static void dereference() {
        dataLoaders.values().forEach(KrudWorldData::cleanupEngine);
    }

    public static int cacheSize() {
        int m = 0;
        for (KrudWorldData i : dataLoaders.values()) {
            for (ResourceLoader<?> j : i.getLoaders().values()) {
                m += j.getLoadCache().getSize();
            }
        }

        return m;
    }

    private static void printData(ResourceLoader<?> rl) {
        KrudWorld.warn("  " + rl.getResourceTypeName() + " @ /" + rl.getFolderName() + ": Cache=" + rl.getLoadCache().getSize() + " Folders=" + rl.getFolders().size());
    }

    public static KrudWorldObject loadAnyObject(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldObject.class, key, nearest);
    }

    public static KrudWorldMatterObject loadAnyMatter(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldMatterObject.class, key, nearest);
    }

    public static KrudWorldBiome loadAnyBiome(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldBiome.class, key, nearest);
    }

    public static KrudWorldExpression loadAnyExpression(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldExpression.class, key, nearest);
    }

    public static KrudWorldMod loadAnyMod(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldMod.class, key, nearest);
    }

    public static KrudWorldJigsawPiece loadAnyJigsawPiece(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldJigsawPiece.class, key, nearest);
    }

    public static KrudWorldJigsawPool loadAnyJigsawPool(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldJigsawPool.class, key, nearest);
    }

    public static KrudWorldEntity loadAnyEntity(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldEntity.class, key, nearest);
    }

    public static KrudWorldLootTable loadAnyLootTable(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldLootTable.class, key, nearest);
    }

    public static KrudWorldBlockData loadAnyBlock(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldBlockData.class, key, nearest);
    }

    public static KrudWorldSpawner loadAnySpaner(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldSpawner.class, key, nearest);
    }

    public static KrudWorldScript loadAnyScript(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldScript.class, key, nearest);
    }

    public static KrudWorldRavine loadAnyRavine(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldRavine.class, key, nearest);
    }

    public static KrudWorldRegion loadAnyRegion(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldRegion.class, key, nearest);
    }

    public static KrudWorldMarker loadAnyMarker(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldMarker.class, key, nearest);
    }

    public static KrudWorldCave loadAnyCave(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldCave.class, key, nearest);
    }

    public static KrudWorldImage loadAnyImage(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldImage.class, key, nearest);
    }

    public static KrudWorldDimension loadAnyDimension(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldDimension.class, key, nearest);
    }

    public static KrudWorldJigsawStructure loadAnyJigsawStructure(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldJigsawStructure.class, key, nearest);
    }

    public static KrudWorldGenerator loadAnyGenerator(String key, @Nullable KrudWorldData nearest) {
        return loadAny(KrudWorldGenerator.class, key, nearest);
    }

    public static <T extends KrudWorldRegistrant> T loadAny(Class<T> type, String key, @Nullable KrudWorldData nearest) {
        try {
            if (nearest != null) {
                T t = nearest.load(type, key, false);
                if (t != null) {
                    return t;
                }
            }

            for (File i : Objects.requireNonNull(KrudWorld.instance.getDataFolder("packs").listFiles())) {
                if (i.isDirectory()) {
                    KrudWorldData dm = get(i);
                    if (dm == nearest) continue;
                    T t = dm.load(type, key, false);

                    if (t != null) {
                        return t;
                    }
                }
            }
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
        }

        return null;
    }

    public <T extends KrudWorldRegistrant> T load(Class<T> type, String key, boolean warn) {
        var loader = getLoader(type);
        if (loader == null) return null;
        return loader.load(key, warn);
    }

    @SuppressWarnings("unchecked")
    public <T extends KrudWorldRegistrant> ResourceLoader<T> getLoader(Class<T> type) {
        return (ResourceLoader<T>) loaders.get(type);
    }

    public ResourceLoader<?> getTypedLoaderFor(File f) {
        String[] k = f.getPath().split("\\Q" + File.separator + "\\E");

        for (String i : k) {
            for (ResourceLoader<?> j : loaders.values()) {
                if (j.getFolderName().equals(i)) {
                    return j;
                }
            }
        }

        return null;
    }

    public void cleanupEngine() {
        if (engine != null && engine.isClosed()) {
            engine = null;
            KrudWorld.debug("Dereferenced Data<Engine> " + getId() + " " + getDataFolder());
        }
    }

    public void preprocessObject(KrudWorldRegistrant t) {
        try {
            KrudWorldContext ctx = KrudWorldContext.get();
            Engine engine = this.engine;

            if (engine == null && ctx != null && ctx.getEngine() != null) {
                engine = ctx.getEngine();
            }

            if (engine == null && t.getPreprocessors().isNotEmpty()) {
                KrudWorld.error("Failed to preprocess object " + t.getLoadKey() + " because there is no engine context here. (See stack below)");
                try {
                    throw new RuntimeException();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }

            if (engine == null) return;
            var global = engine.getDimension().getPreProcessors(t.getFolderName());
            var local = t.getPreprocessors();
            if ((global != null && global.isNotEmpty()) || local.isNotEmpty()) {
                synchronized (this) {
                    if (global != null) {
                        for (String i : global) {
                            engine.getExecution().preprocessObject(i, t);
                            KrudWorld.debug("Loader<" + C.GREEN + t.getTypeName() + C.LIGHT_PURPLE + "> iprocess " + C.YELLOW + t.getLoadKey() + C.LIGHT_PURPLE + " in <rainbow>" + i);
                        }
                    }

                    for (String i : local) {
                        engine.getExecution().preprocessObject(i, t);
                        KrudWorld.debug("Loader<" + C.GREEN + t.getTypeName() + C.LIGHT_PURPLE + "> iprocess " + C.YELLOW + t.getLoadKey() + C.LIGHT_PURPLE + " in <rainbow>" + i);
                    }
                }
            }
        } catch (Throwable e) {
            KrudWorld.error("Failed to preprocess object!");
            e.printStackTrace();
        }
    }

    public void close() {
        closed = true;
        dump();
        dataLoaders.remove(dataFolder);
    }

    public KrudWorldData copy() {
        return KrudWorldData.get(dataFolder);
    }

    private <T extends KrudWorldRegistrant> ResourceLoader<T> registerLoader(Class<T> registrant) {
        try {
            KrudWorldRegistrant rr = registrant.getConstructor().newInstance();
            ResourceLoader<T> r = null;
            if (registrant.equals(KrudWorldObject.class)) {
                r = (ResourceLoader<T>) new ObjectResourceLoader(dataFolder, this, rr.getFolderName(),
                        rr.getTypeName());
            } else if (registrant.equals(KrudWorldMatterObject.class)) {
                r = (ResourceLoader<T>) new MatterObjectResourceLoader(dataFolder, this, rr.getFolderName(),
                        rr.getTypeName());
            } else if (registrant.equals(KrudWorldScript.class)) {
                r = (ResourceLoader<T>) new ScriptResourceLoader(dataFolder, this, rr.getFolderName(),
                        rr.getTypeName());
            } else if (registrant.equals(KrudWorldImage.class)) {
                r = (ResourceLoader<T>) new ImageResourceLoader(dataFolder, this, rr.getFolderName(),
                        rr.getTypeName());
            } else {
                J.attempt(() -> registrant.getConstructor().newInstance().registerTypeAdapters(builder));
                r = new ResourceLoader<>(dataFolder, this, rr.getFolderName(), rr.getTypeName(), registrant);
            }

            loaders.put(registrant, r);

            return r;
        } catch (Throwable e) {
            KrudWorld.reportError(e);
            e.printStackTrace();
            KrudWorld.error("Failed to create loader! " + registrant.getCanonicalName());
        }

        return null;
    }

    public synchronized void hotloaded() {
        closed = false;
        possibleSnippets = new KMap<>();
        builder = new GsonBuilder()
                .addDeserializationExclusionStrategy(this)
                .addSerializationExclusionStrategy(this)
                .setLenient()
                .registerTypeAdapterFactory(this)
                .registerTypeAdapter(MantleFlag.class, new MantleFlagAdapter())
                .setPrettyPrinting();
        loaders.clear();
        File packs = dataFolder;
        packs.mkdirs();
        this.lootLoader = registerLoader(KrudWorldLootTable.class);
        this.spawnerLoader = registerLoader(KrudWorldSpawner.class);
        this.entityLoader = registerLoader(KrudWorldEntity.class);
        this.regionLoader = registerLoader(KrudWorldRegion.class);
        this.biomeLoader = registerLoader(KrudWorldBiome.class);
        this.modLoader = registerLoader(KrudWorldMod.class);
        this.dimensionLoader = registerLoader(KrudWorldDimension.class);
        this.jigsawPoolLoader = registerLoader(KrudWorldJigsawPool.class);
        this.jigsawStructureLoader = registerLoader(KrudWorldJigsawStructure.class);
        this.jigsawPieceLoader = registerLoader(KrudWorldJigsawPiece.class);
        this.generatorLoader = registerLoader(KrudWorldGenerator.class);
        this.caveLoader = registerLoader(KrudWorldCave.class);
        this.markerLoader = registerLoader(KrudWorldMarker.class);
        this.ravineLoader = registerLoader(KrudWorldRavine.class);
        this.blockLoader = registerLoader(KrudWorldBlockData.class);
        this.expressionLoader = registerLoader(KrudWorldExpression.class);
        this.objectLoader = registerLoader(KrudWorldObject.class);
        this.imageLoader = registerLoader(KrudWorldImage.class);
        this.scriptLoader = registerLoader(KrudWorldScript.class);
        this.matterObjectLoader = registerLoader(KrudWorldMatterObject.class);
        this.environment = PackEnvironment.create(this);
        builder.registerTypeAdapterFactory(KeyedType::createTypeAdapter);

        gson = builder.create();
        dimensionLoader.streamAll()
                .map(KrudWorldDimension::getDataScripts)
                .flatMap(KList::stream)
                .forEach(environment::execute);

        if (engine != null) {
            engine.hotload();
        }
    }

    public void dump() {
        for (ResourceLoader<?> i : loaders.values()) {
            i.clearCache();
        }
    }

    public void clearLists() {
        for (ResourceLoader<?> i : loaders.values()) {
            i.clearList();
        }
        possibleSnippets.clear();
    }

    public Set<Class<?>> resolveSnippets() {
        var result = new HashSet<Class<?>>();
        var processed = new HashSet<Class<?>>();
        var excluder = gson.excluder();

        var queue = new LinkedList<Class<?>>(loaders.keySet());
        while (!queue.isEmpty()) {
            var type = queue.poll();
            if (excluder.excludeClass(type, false) || !processed.add(type))
                continue;
            if (type.isAnnotationPresent(Snippet.class))
                result.add(type);

            try {
                for (var field : type.getDeclaredFields()) {
                    if (excluder.excludeField(field, false))
                        continue;

                    queue.add(field.getType());
                }
            } catch (Throwable ignored) {
            }
        }

        return result;
    }

    public String toLoadKey(File f) {
        if (f.getPath().startsWith(getDataFolder().getPath())) {
            String[] full = f.getPath().split("\\Q" + File.separator + "\\E");
            String[] df = getDataFolder().getPath().split("\\Q" + File.separator + "\\E");
            StringBuilder g = new StringBuilder();
            boolean m = true;
            for (int i = 0; i < full.length; i++) {
                if (i >= df.length) {
                    if (m) {
                        m = false;
                        continue;
                    }

                    g.append("/").append(full[i]);
                }
            }

            return g.substring(1).split("\\Q.\\E")[0];
        } else {
            KrudWorld.error("Forign file from loader " + f.getPath() + " (loader realm: " + getDataFolder().getPath() + ")");
        }

        KrudWorld.error("Failed to load " + f.getPath() + " (loader realm: " + getDataFolder().getPath() + ")");

        return null;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> c) {
        if (c.equals(AtomicCache.class)) {
            return true;
        } else return c.equals(ChronoLatch.class);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!typeToken.getRawType().isAnnotationPresent(Snippet.class)) {
            return null;
        }

        String snippetType = typeToken.getRawType().getDeclaredAnnotation(Snippet.class).value();
        String snippedBase = "snippet/" + snippetType + "/";

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter jsonWriter, T t) throws IOException {
                gson.getDelegateAdapter(KrudWorldData.this, typeToken).write(jsonWriter, t);
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                TypeAdapter<T> adapter = gson.getDelegateAdapter(KrudWorldData.this, typeToken);

                if (reader.peek().equals(JsonToken.STRING)) {
                    String r = reader.nextString();
                    if (!r.startsWith("snippet/"))
                        return null;
                    if (!r.startsWith(snippedBase))
                        r = snippedBase + r.substring(8);

                    File f = new File(getDataFolder(), r + ".json");
                    if (f.exists()) {
                        try (JsonReader snippetReader = new JsonReader(new FileReader(f))){
                            return adapter.read(snippetReader);
                        } catch (Throwable e) {
                            KrudWorld.error("Couldn't read snippet " + r + " in " + reader.getPath() + " (" + e.getMessage() + ")");
                        }
                    } else {
                        KrudWorld.error("Couldn't find snippet " + r + " in " + reader.getPath());
                    }

                    return null;
                }

                try {
                    return adapter.read(reader);
                } catch (Throwable e) {
                    KrudWorld.error("Failed to read " + typeToken.getRawType().getCanonicalName() + "... faking objects a little to load the file at least.");
                    KrudWorld.reportError(e);
                    try {
                        return (T) typeToken.getRawType().getConstructor().newInstance();
                    } catch (Throwable ignored) {

                    }
                }
                return null;
            }
        };
    }

    public KList<String> getPossibleSnippets(String f) {
        return possibleSnippets.computeIfAbsent(f, (k) -> {
            KList<String> l = new KList<>();

            File snippetFolder = new File(getDataFolder(), "snippet/" + f);
            if (!snippetFolder.exists()) return l;

            String absPath = snippetFolder.getAbsolutePath();
            try (var stream = Files.walk(snippetFolder.toPath())) {
                stream.filter(Files::isRegularFile)
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .filter(s -> s.endsWith(".json"))
                        .map(s -> s.substring(absPath.length() + 1))
                        .map(s -> s.replace("\\", "/"))
                        .map(s -> s.split("\\Q.\\E")[0])
                        .forEach(s -> l.add("snippet/" + s));
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return l;
        });
    }

    public boolean isClosed() {
        return closed;
    }

    public void savePrefetch(Engine engine) {
        BurstExecutor b = MultiBurst.ioBurst.burst(loaders.size());

        for (ResourceLoader<?> i : loaders.values()) {
            b.queue(() -> {
                try {
                    i.saveFirstAccess(engine);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        b.complete();
        KrudWorld.info("Saved Prefetch Cache to speed up future world startups");
    }

    public void loadPrefetch(Engine engine) {
        BurstExecutor b = MultiBurst.ioBurst.burst(loaders.size());

        for (ResourceLoader<?> i : loaders.values()) {
            b.queue(() -> {
                try {
                    i.loadFirstAccess(engine);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        b.complete();
        KrudWorld.info("Loaded Prefetch Cache to reduce generation disk use.");
    }
}
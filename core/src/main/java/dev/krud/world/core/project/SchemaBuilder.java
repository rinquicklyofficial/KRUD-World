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

package dev.krud.world.core.project;

import dev.krud.world.KrudWorld;
import dev.krud.world.core.link.Identifier;
import dev.krud.world.core.link.data.DataType;
import dev.krud.world.core.loader.KrudWorldData;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.core.loader.ResourceLoader;
import dev.krud.world.core.service.ExternalDataSVC;
import dev.krud.world.engine.object.annotations.*;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.collection.KMap;
import dev.krud.world.util.data.B;
import dev.krud.world.util.json.JSONArray;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.reflect.KeyedType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SchemaBuilder {
    private static final String SYMBOL_LIMIT__N = "*";
    private static final String SYMBOL_TYPE__N = "";
    private static final JSONArray POTION_TYPES = getPotionTypes();
    private static final JSONArray ENCHANT_TYPES = getEnchantTypes();
    private static final JSONArray FONT_TYPES = new JSONArray(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private final KMap<String, JSONObject> definitions;
    private final Class<?> root;
    private final KList<String> warnings;
    private final KrudWorldData data;

    public SchemaBuilder(Class<?> root, KrudWorldData data) {
        this.data = data;
        warnings = new KList<>();
        this.definitions = new KMap<>();
        this.root = root;
    }

    private static JSONArray getPotionTypes() {
        JSONArray a = new JSONArray();

        for (PotionEffectType gg : PotionEffectType.values()) {
            a.put(gg.getName().toUpperCase().replaceAll("\\Q \\E", "_"));
        }

        return a;
    }

    private static JSONArray getEnchantTypes() {
        JSONArray array = new JSONArray();
        for (Enchantment e : Enchantment.values()) {
            array.put(e.getKey().getKey());
        }
        return array;
    }

    public JSONObject construct() {
        JSONObject schema = new JSONObject();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("$id", "https://volmit.com/iris-schema/" + root.getSimpleName().toLowerCase() + ".json");

        JSONObject props = buildProperties(root);

        for (String i : props.keySet()) {
            if (!schema.has(i)) {
                schema.put(i, props.get(i));
            }
        }

        JSONObject defs = new JSONObject();

        for (Map.Entry<String, JSONObject> entry : definitions.entrySet()) {
            defs.put(entry.getKey(), entry.getValue());
        }

        schema.put("definitions", defs);

        for (String i : warnings) {
            KrudWorld.warn(root.getSimpleName() + ": " + i);
        }

        return schema;
    }

    private JSONObject buildProperties(Class<?> c) {
        JSONObject o = new JSONObject();
        JSONObject properties = new JSONObject();
        String desc = getDescription(c);
        o.put("description", desc);
        o.put("x-intellij-html-description", desc.replace("\n", "<br>"));
        o.put("type", getType(c));
        JSONArray required = new JSONArray();
        JSONArray extended = new JSONArray();

        var parent = c.getSuperclass();
        while (parent != null && KrudWorldRegistrant.class.isAssignableFrom(parent)) {
            buildProperties(properties, required, extended, parent);
            parent = parent.getSuperclass();
        }

        buildProperties(properties, required, extended, c);

        if (required.length() > 0) {
            o.put("required", required);
        }
        if (extended.length() > 0) {
            o.put("allOf", extended);
        }

        o.put("properties", properties);


        return buildSnippet(o, c);
    }

    private void buildProperties(JSONObject properties, JSONArray required, JSONArray extended, Class<?> c) {
        for (Field k : c.getDeclaredFields()) {
            if (Modifier.isStatic(k.getModifiers()) || Modifier.isFinal(k.getModifiers()) || Modifier.isTransient(k.getModifiers())) {
                continue;
            }

            try {
                k.setAccessible(true);
            } catch (InaccessibleObjectException e) {
                continue;
            }

            JSONObject property = buildProperty(k, c);

            if (Boolean.TRUE == property.remove("!top")) {
                extended.put(property);
                continue;
            }

            if (Boolean.TRUE == property.remove("!required")) {
                required.put(k.getName());
            }

            properties.put(k.getName(), property);
        }
    }

    private JSONObject buildProperty(Field k, Class<?> cl) {
        JSONObject prop = new JSONObject();
        String type = getType(k.getType());
        KList<String> description = new KList<>();
        prop.put("!required", k.isAnnotationPresent(Required.class));
        prop.put("type", type);
        String fancyType = "Unknown Type";

        switch (type) {
            case "boolean" -> fancyType = "Boolean";
            case "integer" -> {
                fancyType = "Integer";
                if (k.isAnnotationPresent(MinNumber.class)) {
                    int min = (int) k.getDeclaredAnnotation(MinNumber.class).value();
                    prop.put("minimum", min);
                    description.add(SYMBOL_LIMIT__N + " Minimum allowed is " + min);
                }
                if (k.isAnnotationPresent(MaxNumber.class)) {
                    int max = (int) k.getDeclaredAnnotation(MaxNumber.class).value();
                    prop.put("maximum", max);
                    description.add(SYMBOL_LIMIT__N + " Maximum allowed is " + max);
                }
            }
            case "number" -> {
                fancyType = "Number";
                if (k.isAnnotationPresent(MinNumber.class)) {
                    double min = k.getDeclaredAnnotation(MinNumber.class).value();
                    prop.put("minimum", min);
                    description.add(SYMBOL_LIMIT__N + " Minimum allowed is " + min);
                }
                if (k.isAnnotationPresent(MaxNumber.class)) {
                    double max = k.getDeclaredAnnotation(MaxNumber.class).value();
                    prop.put("maximum", max);
                    description.add(SYMBOL_LIMIT__N + " Maximum allowed is " + max);
                }
            }
            case "string" -> {
                fancyType = "Text";
                if (k.isAnnotationPresent(MinNumber.class)) {
                    int min = (int) k.getDeclaredAnnotation(MinNumber.class).value();
                    prop.put("minLength", min);
                    description.add(SYMBOL_LIMIT__N + " Minimum Length allowed is " + min);
                }
                if (k.isAnnotationPresent(MaxNumber.class)) {
                    int max = (int) k.getDeclaredAnnotation(MaxNumber.class).value();
                    prop.put("maxLength", max);
                    description.add(SYMBOL_LIMIT__N + " Maximum Length allowed is " + max);
                }


                if (k.isAnnotationPresent(RegistryListResource.class)) {
                    RegistryListResource rr = k.getDeclaredAnnotation(RegistryListResource.class);
                    ResourceLoader<?> loader = data.getLoaders().get(rr.value());

                    if (loader != null) {
                        String key = "erz" + loader.getFolderName();

                        if (!definitions.containsKey(key)) {
                            JSONObject j = new JSONObject();
                            j.put("enum", new JSONArray(loader.getPossibleKeys()));
                            definitions.put(key, j);
                        }

                        fancyType = "KrudWorld " + loader.getResourceTypeName();
                        prop.put("$ref", "#/definitions/" + key);
                        description.add(SYMBOL_TYPE__N + "  Must be a valid " + loader.getFolderName() + " (use ctrl+space for auto complete!)");
                    } else {
                        KrudWorld.error("Cannot find Registry Loader for type " + rr.value() + " used in " + k.getDeclaringClass().getCanonicalName() + " in field " + k.getName());
                    }
                } else if (k.isAnnotationPresent(RegistryListBlockType.class)) {
                    String key = "enum-block-type";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        JSONArray ja = new JSONArray();

                        for (String i : data.getBlockLoader().getPossibleKeys()) {
                            ja.put(i);
                        }

                        for (String i : B.getBlockTypes()) {
                            ja.put(i);
                        }

                        j.put("enum", ja);
                        definitions.put(key, j);
                    }

                    fancyType = "Block Type";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Block Type (use ctrl+space for auto complete!)");

                } else if (k.isAnnotationPresent(RegistryListItemType.class)) {
                    String key = "enum-item-type";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        j.put("enum", B.getItemTypes());
                        definitions.put(key, j);
                    }

                    fancyType = "Item Type";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Item Type (use ctrl+space for auto complete!)");

                } else if (k.isAnnotationPresent(RegistryListSpecialEntity.class)) {
                    String key = "enum-reg-specialentity";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        KList<String> list = KrudWorld.service(ExternalDataSVC.class)
                                .getAllIdentifiers(DataType.ENTITY)
                                .stream()
                                .map(Identifier::toString)
                                .collect(KList.collector());
                        j.put("enum", list.toJSONStringArray());
                        definitions.put(key, j);
                    }

                    fancyType = "Custom Mob Type";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Custom Mob Type (use ctrl+space for auto complete!)");
                } else if (k.isAnnotationPresent(RegistryListFont.class)) {
                    String key = "enum-font";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        j.put("enum", FONT_TYPES);
                        definitions.put(key, j);
                    }

                    fancyType = "Font Family";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Font Family (use ctrl+space for auto complete!)");

                } else if (k.isAnnotationPresent(RegistryListEnchantment.class)) {
                    String key = "enum-enchantment";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        j.put("enum", ENCHANT_TYPES);
                        definitions.put(key, j);
                    }

                    fancyType = "Enchantment Type";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Enchantment Type (use ctrl+space for auto complete!)");
                } else if (k.isAnnotationPresent(RegistryListFunction.class)) {
                    var functionClass = k.getDeclaredAnnotation(RegistryListFunction.class).value();
                    try {
                        var instance = functionClass.getDeclaredConstructor().newInstance();
                        String key = instance.key();
                        fancyType = instance.fancyName();

                        if (!definitions.containsKey(key)) {
                            JSONObject j = new JSONObject();
                            j.put("enum", instance.apply(data));
                            definitions.put(key, j);
                        }

                        prop.put("$ref", "#/definitions/" + key);
                        description.add(SYMBOL_TYPE__N + "  Must be a valid " + fancyType + " (use ctrl+space for auto complete!)");
                    } catch (Throwable e) {
                        KrudWorld.error("Could not execute apply method in " + functionClass.getName());
                    }
                } else if (k.getType().equals(PotionEffectType.class)) {
                    String key = "enum-potion-effect-type";

                    if (!definitions.containsKey(key)) {
                        JSONObject j = new JSONObject();
                        j.put("enum", POTION_TYPES);
                        definitions.put(key, j);
                    }

                    fancyType = "Potion Effect Type";
                    prop.put("$ref", "#/definitions/" + key);
                    description.add(SYMBOL_TYPE__N + "  Must be a valid Potion Effect Type (use ctrl+space for auto complete!)");

                } else if (KeyedType.isKeyed(k.getType())) {
                    fancyType = addEnum(k.getType(), prop, description, KeyedType.values(k.getType()), Function.identity());
                } else if (k.getType().isEnum()) {
                    fancyType = addEnum(k.getType(), prop, description, k.getType().getEnumConstants(), o -> ((Enum<?>) o).name());
                }
            }
            case "object" -> {
                //TODO add back descriptions
                if (k.isAnnotationPresent(RegistryMapBlockState.class)) {
                    String blockType = k.getDeclaredAnnotation(RegistryMapBlockState.class).value();
                    fancyType = "Block State";
                    prop.put("!top", true);
                    JSONArray any = new JSONArray();
                    prop.put("anyOf", any);

                    B.getBlockStates().forEach((blocks, properties) -> {
                        if (blocks.isEmpty()) return;

                        String raw = blocks.getFirst().replace(':', '_');
                        String enumKey = "enum-block-state-" + raw;
                        String propertiesKey = "obj-block-state-" + raw;

                        any.put(new JSONObject()
                                .put("if", new JSONObject()
                                        .put("properties", new JSONObject()
                                                .put(blockType, new JSONObject()
                                                        .put("type", "string")
                                                        .put("$ref", "#/definitions/" + enumKey))))
                                .put("then", new JSONObject()
                                        .put("properties", new JSONObject()
                                                .put(k.getName(), new JSONObject()
                                                        .put("type", "object")
                                                        .put("$ref", "#/definitions/" + propertiesKey))))
                                .put("else", false));

                        if (!definitions.containsKey(enumKey)) {
                            JSONArray filters = new JSONArray();
                            blocks.forEach(filters::put);

                            definitions.put(enumKey, new JSONObject()
                                    .put("type", "string")
                                    .put("enum", filters));
                        }

                        if (!definitions.containsKey(propertiesKey)) {
                            JSONObject props = new JSONObject();
                            properties.forEach(property -> {
                                props.put(property.name(), property.buildJson());
                            });

                            definitions.put(propertiesKey, new JSONObject()
                                    .put("type", "object")
                                    .put("properties", props));
                        }
                    });
                } else {
                    fancyType = k.getType().getSimpleName().replaceAll("\\QKrudWorld\\E", "") + " (Object)";
                    String key = "obj-" + k.getType().getCanonicalName().replaceAll("\\Q.\\E", "-").toLowerCase();
                    if (!definitions.containsKey(key)) {
                        definitions.put(key, new JSONObject());
                        definitions.put(key, buildProperties(k.getType()));
                    }
                    prop.put("$ref", "#/definitions/" + key);
                }
            }
            case "array" -> {
                fancyType = "List of Something...?";
                ArrayType t = k.getDeclaredAnnotation(ArrayType.class);
                if (t != null) {
                    if (t.min() > 0) {
                        prop.put("minItems", t.min());
                        if (t.min() == 1) {
                            description.add(SYMBOL_LIMIT__N + " At least one entry must be defined, or just remove this list.");
                        } else {
                            description.add(SYMBOL_LIMIT__N + " Requires at least " + t.min() + " entries.");
                        }
                    }

                    String arrayType = getType(t.type());

                    switch (arrayType) {
                        case "integer" -> fancyType = "List of Integers";
                        case "number" -> fancyType = "List of Numbers";
                        case "object" -> {
                            fancyType = "List of " + t.type().getSimpleName().replaceAll("\\QKrudWorld\\E", "") + "s (Objects)";
                            String key = "obj-" + t.type().getCanonicalName().replaceAll("\\Q.\\E", "-").toLowerCase();
                            if (!definitions.containsKey(key)) {
                                definitions.put(key, new JSONObject());
                                definitions.put(key, buildProperties(t.type()));
                            }
                            JSONObject items = new JSONObject();
                            items.put("$ref", "#/definitions/" + key);
                            prop.put("items", items);
                        }
                        case "string" -> {
                            fancyType = "List of Text";

                            if (k.isAnnotationPresent(RegistryListResource.class)) {
                                RegistryListResource rr = k.getDeclaredAnnotation(RegistryListResource.class);
                                ResourceLoader<?> loader = data.getLoaders().get(rr.value());

                                if (loader != null) {
                                    fancyType = "List<" + loader.getResourceTypeName() + ">";
                                    String key = "erz" + loader.getFolderName();

                                    if (!definitions.containsKey(key)) {
                                        JSONObject j = new JSONObject();
                                        j.put("enum", new JSONArray(loader.getPossibleKeys()));
                                        definitions.put(key, j);
                                    }

                                    JSONObject items = new JSONObject();
                                    items.put("$ref", "#/definitions/" + key);
                                    prop.put("items", items);
                                    description.add(SYMBOL_TYPE__N + "  Must be a valid " + loader.getResourceTypeName() + " (use ctrl+space for auto complete!)");
                                } else {
                                    KrudWorld.error("Cannot find Registry Loader for type (list schema) " + rr.value() + " used in " + k.getDeclaringClass().getCanonicalName() + " in field " + k.getName());
                                }
                            } else if (k.isAnnotationPresent(RegistryListBlockType.class)) {
                                fancyType = "List of Block Types";
                                String key = "enum-block-type";

                                if (!definitions.containsKey(key)) {
                                    JSONObject j = new JSONObject();
                                    JSONArray ja = new JSONArray();

                                    for (String i : data.getBlockLoader().getPossibleKeys()) {
                                        ja.put(i);
                                    }

                                    for (String i : B.getBlockTypes()) {
                                        ja.put(i);
                                    }

                                    j.put("enum", ja);
                                    definitions.put(key, j);
                                }

                                JSONObject items = new JSONObject();
                                items.put("$ref", "#/definitions/" + key);
                                prop.put("items", items);
                                description.add(SYMBOL_TYPE__N + "  Must be a valid Block Type (use ctrl+space for auto complete!)");
                            } else if (k.isAnnotationPresent(RegistryListItemType.class)) {
                                fancyType = "List of Item Types";
                                String key = "enum-item-type";

                                if (!definitions.containsKey(key)) {
                                    JSONObject j = new JSONObject();
                                    j.put("enum", B.getItemTypes());
                                    definitions.put(key, j);
                                }

                                JSONObject items = new JSONObject();
                                items.put("$ref", "#/definitions/" + key);
                                prop.put("items", items);
                                description.add(SYMBOL_TYPE__N + "  Must be a valid Item Type (use ctrl+space for auto complete!)");
                            } else if (k.isAnnotationPresent(RegistryListFont.class)) {
                                String key = "enum-font";
                                fancyType = "List of Font Families";

                                if (!definitions.containsKey(key)) {
                                    JSONObject j = new JSONObject();
                                    j.put("enum", FONT_TYPES);
                                    definitions.put(key, j);
                                }

                                JSONObject items = new JSONObject();
                                items.put("$ref", "#/definitions/" + key);
                                prop.put("items", items);
                                description.add(SYMBOL_TYPE__N + "  Must be a valid Font Family (use ctrl+space for auto complete!)");
                            } else if (k.isAnnotationPresent(RegistryListEnchantment.class)) {
                                fancyType = "List of Enchantment Types";
                                String key = "enum-enchantment";

                                if (!definitions.containsKey(key)) {
                                    JSONObject j = new JSONObject();
                                    j.put("enum", ENCHANT_TYPES);
                                    definitions.put(key, j);
                                }

                                JSONObject items = new JSONObject();
                                items.put("$ref", "#/definitions/" + key);
                                prop.put("items", items);
                                description.add(SYMBOL_TYPE__N + "  Must be a valid Enchantment Type (use ctrl+space for auto complete!)");
                            } else if (k.isAnnotationPresent(RegistryListFunction.class)) {
                                var functionClass = k.getDeclaredAnnotation(RegistryListFunction.class).value();
                                try {
                                    var instance = functionClass.getDeclaredConstructor().newInstance();
                                    String key = instance.key();
                                    fancyType = instance.fancyName();

                                    if (!definitions.containsKey(key)) {
                                        JSONObject j = new JSONObject();
                                        j.put("enum", instance.apply(data));
                                        definitions.put(key, j);
                                    }

                                    JSONObject items = new JSONObject();
                                    items.put("$ref", "#/definitions/" + key);
                                    prop.put("items", items);
                                    description.add(SYMBOL_TYPE__N + "  Must be a valid " + fancyType + " (use ctrl+space for auto complete!)");
                                } catch (Throwable e) {
                                    KrudWorld.error("Could not execute apply method in " + functionClass.getName());
                                }
                            } else if (t.type().equals(PotionEffectType.class)) {
                                fancyType = "List of Potion Effect Types";
                                String key = "enum-potion-effect-type";

                                if (!definitions.containsKey(key)) {
                                    JSONObject j = new JSONObject();
                                    j.put("enum", POTION_TYPES);
                                    definitions.put(key, j);
                                }

                                JSONObject items = new JSONObject();
                                items.put("$ref", "#/definitions/" + key);
                                prop.put("items", items);
                                description.add(SYMBOL_TYPE__N + "  Must be a valid Potion Effect Type (use ctrl+space for auto complete!)");
                            } else if (KeyedType.isKeyed(t.type())) {
                                fancyType = addEnumList(prop, description, t, KeyedType.values(t.type()), Function.identity());
                            } else if (t.type().isEnum()) {
                                fancyType = addEnumList(prop, description, t, t.type().getEnumConstants(), o -> ((Enum<?>) o).name());
                            }
                        }
                    }
                } else {
                    warnings.add("Undefined array type for field " + k.getName() + " (" + k.getType().getSimpleName() + ") in class " + cl.getSimpleName());
                }
            }
            default ->
                    warnings.add("Unexpected Schema Type: " + type + " for field " + k.getName() + " (" + k.getType().getSimpleName() + ") in class " + cl.getSimpleName());
        }

        KList<String> d = new KList<>();
        d.add("<h>" + k.getName() + "</h>");
        d.add(getFieldDescription(k) + "<hr></hr>");
        d.add("<h>" + fancyType + "</h>");
        String typeDesc = getDescription(k.getType());
        boolean present = !typeDesc.isBlank();
        if (present) d.add(typeDesc);

        Snippet snippet = k.getType().getDeclaredAnnotation(Snippet.class);
        if (snippet == null) {
            ArrayType array = k.getType().getDeclaredAnnotation(ArrayType.class);
            if (array != null) {
                snippet = array.type().getDeclaredAnnotation(Snippet.class);
            }
        }

        if (snippet != null) {
            String sm = snippet.value();
            if (present) d.add("    ");
            d.add("You can instead specify \"snippet/" + sm + "/some-name.json\" to use a snippet file instead of specifying it here.");
            present = false;
        }

        try {
            k.setAccessible(true);
            Object value = k.get(cl.newInstance());

            if (value != null) {
                if (present) d.add("    ");
                if (value instanceof List) {
                    d.add(SYMBOL_LIMIT__N + " Default Value is an empty list");
                } else if (!k.getType().isPrimitive() && !(value instanceof Number) && !(value instanceof String) && !(value instanceof Enum<?>) && !KeyedType.isKeyed(k.getType())) {
                    d.add(SYMBOL_LIMIT__N + " Default Value is a default object (create this object to see default properties)");
                } else {
                    d.add(SYMBOL_LIMIT__N + " Default Value is " + value);
                }
            }
        } catch (Throwable ignored) {

        }

        description.forEach((g) -> d.add(g.trim()));
        String desc = d.toString("\n")
                .replace("<hr></hr>", "\n")
                .replace("<h>", "")
                .replace("</h>", "");
        String hDesc = d.toString("<br>");
        prop.put("type", type);
        prop.put("description", desc);
        prop.put("x-intellij-html-description", hDesc);
        return buildSnippet(prop, k.getType());
    }

    private JSONObject buildSnippet(JSONObject prop, Class<?> type) {
        Snippet snippet = type.getDeclaredAnnotation(Snippet.class);
        if (snippet == null) return prop;

        JSONObject anyOf = new JSONObject();
        JSONArray arr = new JSONArray();
        JSONObject str = new JSONObject();
        str.put("type", "string");
        String key = "enum-snippet-" + snippet.value();
        str.put("$ref", "#/definitions/" + key);

        if (!definitions.containsKey(key)) {
            JSONObject j = new JSONObject();
            JSONArray snl = new JSONArray();
            data.getPossibleSnippets(snippet.value()).forEach(snl::put);
            j.put("enum", snl);
            definitions.put(key, j);
        }

        arr.put(prop);
        arr.put(str);
        str.put("description", prop.getString("description"));
        str.put("x-intellij-html-description", prop.getString("x-intellij-html-description"));
        anyOf.put("anyOf", arr);
        anyOf.put("description", prop.getString("description"));
        anyOf.put("x-intellij-html-description", prop.getString("x-intellij-html-description"));
        anyOf.put("!required", type.isAnnotationPresent(Required.class));

        return anyOf;
    }

    @NotNull
    private <T> String addEnumList(JSONObject prop, KList<String> description, ArrayType t, T[] values, Function<T, String> function) {
        JSONObject items = new JSONObject();
        var s = addEnum(t.type(), items, description, values, function);
        prop.put("items", items);

        return "List of " + s + "s";
    }

    @NotNull
    private <T> String addEnum(Class<?> type, JSONObject prop, KList<String> description, T[] values, Function<T, String> function) {
        JSONArray a = new JSONArray();
        boolean advanced = type.isAnnotationPresent(Desc.class);
        for (T gg : values) {
            if (advanced) {
                try {
                    JSONObject j = new JSONObject();
                    String name = function.apply(gg);
                    j.put("const", name);
                    Desc dd = type.getField(name).getAnnotation(Desc.class);
                    String desc = dd == null ? ("No Description for " + name) : dd.value();
                    j.put("description", desc);
                    j.put("x-intellij-html-description", desc.replace("\n", "<br>"));
                    a.put(j);
                } catch (Throwable e) {
                    KrudWorld.reportError(e);
                    e.printStackTrace();
                }
            } else {
                a.put(function.apply(gg));
            }
        }

        String key = (advanced ? "oneof-" : "") + "enum-" + type.getCanonicalName().replaceAll("\\Q.\\E", "-").toLowerCase();

        if (!definitions.containsKey(key)) {
            JSONObject j = new JSONObject();
            j.put(advanced ? "oneOf" : "enum", a);
            definitions.put(key, j);
        }

        prop.put("$ref", "#/definitions/" + key);
        description.add(SYMBOL_TYPE__N + "  Must be a valid " + type.getSimpleName().replaceAll("\\QKrudWorld\\E", "") + " (use ctrl+space for auto complete!)");
        return type.getSimpleName().replaceAll("\\QKrudWorld\\E", "");
    }

    private String getType(Class<?> c) {
        if (c.equals(int.class) || c.equals(Integer.class) || c.equals(long.class) || c.equals(Long.class)) {
            return "integer";
        }

        if (c.equals(float.class) || c.equals(double.class) || c.equals(Float.class) || c.equals(Double.class)) {
            return "number";
        }

        if (c.equals(boolean.class) || c.equals(Boolean.class)) {
            return "boolean";
        }

        if (c.equals(String.class) || c.isEnum() || KeyedType.isKeyed(c)) {
            return "string";
        }

        if (c.equals(KList.class)) {
            return "array";
        }

        if (c.equals(KMap.class)) {
            return "object";
        }

        if (!c.isAnnotationPresent(Desc.class) && c.getCanonicalName().startsWith("dev.krud.world.")) {
            warnings.addIfMissing("Unsupported Type: " + c.getCanonicalName() + " Did you forget @Desc?");
        }

        return "object";
    }

    private String getFieldDescription(Field r) {

        if (r.isAnnotationPresent(Desc.class)) {
            return r.getDeclaredAnnotation(Desc.class).value();
        }

        // suppress warnings on bukkit classes
        if (r.getDeclaringClass().getName().startsWith("org.bukkit.")) {
            return "Bukkit package classes and enums have no descriptions";
        }

        warnings.addIfMissing("Missing @Desc on field " + r.getName() + " (" + r.getType() + ") in " + r.getDeclaringClass().getCanonicalName());
        return "No Field Description";
    }

    private String getDescription(Class<?> r) {
        if (r.isAnnotationPresent(Desc.class)) {
            return r.getDeclaredAnnotation(Desc.class).value();
        }

        if (!r.isPrimitive() && !r.equals(KList.class) && !r.equals(KMap.class) && r.getCanonicalName().startsWith("com.volmit.")) {
            warnings.addIfMissing("Missing @Desc on " + r.getSimpleName() + " in " + (r.getDeclaringClass() != null ? r.getDeclaringClass().getCanonicalName() : " NOSRC"));
        }
        return "";
    }
}

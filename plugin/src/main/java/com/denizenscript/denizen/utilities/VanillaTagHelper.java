package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VanillaTagHelper {

    public static HashMap<Material, HashSet<String>> tagsByMaterial = new HashMap<>();

    public static HashMap<String, HashSet<Material>> materialTagsByKey = new HashMap<>();

    public static HashMap<EntityType, HashSet<String>> tagsByEntity = new HashMap<>();

    public static HashMap<String, HashSet<EntityType>> entityTagsByKey = new HashMap<>();

    public static void addOrUpdateMaterialTag(Tag<Material> tag) {
        if (materialTagsByKey.containsKey(tag.getKey().getKey())) {
            updateMaterialTag(tag);
        }
        else {
            addMaterialTag(tag);
        }
    }

    public static void addOrUpdateEntityTag(Tag<EntityType> tag) {
        if (entityTagsByKey.containsKey(tag.getKey().getKey())) {
            updateEntityTag(tag);
        }
        else {
            addEntityTag(tag);
        }
    }

    static <T extends Keyed> void update(Tag<T> tag, HashMap<T, HashSet<String>> tagByObj, HashMap<String, HashSet<T>> objByTag) {
        String tagName = Utilities.namespacedKeyToString(tag.getKey());
        Set<T> objs = objByTag.get(tagName);
        if (objs == null) {
            return;
        }
        for (T obj : objs) {
            Set<String> tags = tagByObj.get(obj);
            if (tags.size() == 1) {
                tagByObj.remove(obj);
            }
            else {
                tags.remove(tagName);
            }
        }
        Set<T> newObjs = tag.getValues();
        for (T obj : newObjs) {
            tagByObj.computeIfAbsent(obj, k -> new HashSet<>()).add(tagName);
        }
        objs.clear();
        objs.addAll(newObjs);
    }

    public static void updateMaterialTag(Tag<Material> tag) {
        update(tag, tagsByMaterial, materialTagsByKey);
    }

    public static void updateEntityTag(Tag<EntityType> tag) {
        update(tag, tagsByEntity, entityTagsByKey);
    }

    static <T extends Keyed> void add(Tag<T> tag, HashMap<T, HashSet<String>> tagByObj, HashMap<String, HashSet<T>> objByTag) {
        String tagName = Utilities.namespacedKeyToString(tag.getKey());
        objByTag.computeIfAbsent(tagName, (k) -> new HashSet<>()).addAll(tag.getValues());
        for (T obj : tag.getValues()) {
            tagByObj.computeIfAbsent(obj, (k) -> new HashSet<>()).add(tagName);
        }
    }

    static void addMaterialTag(Tag<Material> tag) {
        add(tag, tagsByMaterial, materialTagsByKey);
    }

    static void addEntityTag(Tag<EntityType> tag) {
        add(tag, tagsByEntity, entityTagsByKey);
    }

    static {
        for (Tag<Material> tag : Bukkit.getTags("blocks", Material.class)) {
            addMaterialTag(tag);
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) { // Note: existed on prior versions, but was bugged
            for (Tag<Material> tag : Bukkit.getTags("fluids", Material.class)) {
                addMaterialTag(tag);
            }
            for (Tag<EntityType> tag : Bukkit.getTags("entity_types", EntityType.class)) {
                addEntityTag(tag);
            }
        }
        for (Tag<Material> tag : Bukkit.getTags("items", Material.class)) {
            addMaterialTag(tag);
        }
    }

    public static boolean isValidTagName(String name) {
        return name != null && !name.isEmpty() && NamespacedKey.fromString(name) != null;
    }
}

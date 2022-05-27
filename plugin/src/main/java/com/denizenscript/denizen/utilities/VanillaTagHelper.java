package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;

public class VanillaTagHelper {

    public static HashMap<Material, HashSet<String>> tagsByMaterial = new HashMap<>();

    public static HashMap<String, HashSet<Material>> materialTagsByKey = new HashMap<>();

    public static HashMap<EntityType, HashSet<String>> tagsByEntity = new HashMap<>();

    public static HashMap<String, HashSet<EntityType>> entityTagsByKey = new HashMap<>();

    static <T extends Keyed> void add(Tag<T> tag, HashMap<T, HashSet<String>> tagByObj, HashMap<String, HashSet<T>> objByTag) {
        objByTag.computeIfAbsent(tag.getKey().getKey(), (k) -> new HashSet<>()).addAll(tag.getValues());
        for (T obj : tag.getValues()) {
            tagByObj.computeIfAbsent(obj, (k) -> new HashSet<>()).add(tag.getKey().getKey());
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
}

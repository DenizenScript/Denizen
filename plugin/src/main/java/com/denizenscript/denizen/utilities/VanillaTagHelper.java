package com.denizenscript.denizen.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.HashMap;
import java.util.HashSet;

public class VanillaTagHelper {

    public static HashMap<Material, HashSet<String>> tagsByMaterial = new HashMap<>();

    public static HashMap<String, HashSet<Material>> tagsByKey = new HashMap<>();

    static void addTag(Tag<Material> tag) {
        tagsByKey.computeIfAbsent(tag.getKey().getKey(), (k) -> new HashSet<>()).addAll(tag.getValues());
        for (Material mat : tag.getValues()) {
            tagsByMaterial.computeIfAbsent(mat, (k) -> new HashSet<>()).add(tag.getKey().getKey());
        }
    }

    static {
        for (Tag<Material> tag : Bukkit.getTags("blocks", Material.class)) {
            addTag(tag);
        }
        /*for (Tag<Material> tag : Bukkit.getTags("fluids", Material.class)) {
            addTag(tag);
        }*/ // Temporarily exclude fluids due to spigot bug
        for (Tag<Material> tag : Bukkit.getTags("items", Material.class)) {
            addTag(tag);
        }
    }
}

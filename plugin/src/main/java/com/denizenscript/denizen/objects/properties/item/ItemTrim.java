package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class ItemTrim extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name trim
    // @input MapTag
    // @description
    // An armor item's trim.
    // Allowed keys: material, pattern.
    // Valid material values can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/trim/TrimMaterial.html>
    // Valid pattern values can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/trim/TrimPattern.html>
    // Valid values also include ones added by datapacks, plugins, etc. as a namespaced key.
    // For the mechanism, if an item already has a trim, you can omit either material or pattern to keep the original data while also changing the other option.
    // For example, if you only want to change the pattern and not the material, you can omit the material, and it will use the already existing material.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof ArmorMeta;
    }

    @Override
    public MapTag getPropertyValue() {
        ArmorTrim currentTrim = ((ArmorMeta) getItemMeta()).getTrim();
        if (currentTrim == null) {
            return null;
        }
        MapTag map = new MapTag();
        map.putObject("material", new ElementTag(Utilities.namespacedKeyToString(currentTrim.getMaterial().getKey()), true));
        map.putObject("pattern", new ElementTag(Utilities.namespacedKeyToString(currentTrim.getPattern().getKey()), true));
        return map;
    }

    @Override
    public boolean isDefaultValue(MapTag map) {
        return map.isEmpty();
    }

    @Override
    public String getPropertyId() {
        return "trim";
    }

    @Override
    public void setPropertyValue(MapTag map, Mechanism mechanism) {
        ElementTag mat = map.getElement("material");
        ElementTag pat = map.getElement("pattern");
        ArmorMeta meta = (ArmorMeta) getItemMeta();
        ArmorTrim currentTrim = meta.getTrim();
        if (mat == null && currentTrim == null) {
            mechanism.echoError("The armor piece must have a material already if you want to omit it!");
            return;
        }
        if (pat == null && currentTrim == null) {
            mechanism.echoError("The armor piece must have a pattern already if you want to omit it!");
            return;
        }
        TrimMaterial material = mat == null ? currentTrim.getMaterial() : Registry.TRIM_MATERIAL.get(Utilities.parseNamespacedKey(mat.asString()));
        TrimPattern pattern = pat == null ? currentTrim.getPattern() : Registry.TRIM_PATTERN.get(Utilities.parseNamespacedKey(pat.asString()));
        meta.setTrim(new ArmorTrim(material, pattern));
        setItemMeta(meta);
    }

    public static void register() {
        autoRegister("trim", ItemTrim.class, MapTag.class, false);
    }
}

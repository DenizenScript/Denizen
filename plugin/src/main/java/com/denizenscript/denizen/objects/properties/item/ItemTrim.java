package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
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
    // Controls the trim on armor.
    // Allowed keys: material, pattern.
    // Valid material inputs can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/trim/TrimMaterial.html>
    // Valid pattern inputs can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/trim/TrimPattern.html>
    // If an item already has a trim, you can omit either material or pattern to keep the original data while also changing the other option.
    // For example, if you only want to change the pattern and not the material, you can omit the material, and it will use the already existing material.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof ArmorMeta;
    }

    @Override
    public MapTag getPropertyValue() {
        ArmorMeta meta = (ArmorMeta) getItemMeta();
        MapTag map = new MapTag();
        if (meta.getTrim() == null) {
            return map;
        }
        map.putObject("material", new ElementTag(Utilities.namespacedKeyToString(meta.getTrim().getMaterial().getKey())));
        map.putObject("pattern", new ElementTag(Utilities.namespacedKeyToString(meta.getTrim().getPattern().getKey())));
        return map;
    }

    @Override
    public String getPropertyId() {
        return "trim";
    }

    @Override
    public void setPropertyValue(MapTag trim, Mechanism mechanism) {
        ArmorMeta meta = (ArmorMeta) getItemMeta();
        TrimMaterial material = NMSHandler.itemHelper.convertToTrimMaterial(trim.getElement("material"));
        if (material == null && meta.getTrim() == null) {
            mechanism.echoError("The armor piece needs to have a material already if you want to omit it!");
            return;
        }
        TrimPattern pattern = NMSHandler.itemHelper.convertToTrimPattern(trim.getElement("pattern"));
        if (pattern == null && meta.getTrim() == null) {
            mechanism.echoError("The armor piece needs to have a pattern already if you want to omit it!");
            return;
        }
        meta.setTrim(new ArmorTrim(material == null ? meta.getTrim().getMaterial() : material, pattern == null ? meta.getTrim().getPattern() : pattern));
        getItemStack().setItemMeta(meta);
    }

    public static void register() {
        autoRegister("trim", ItemTrim.class, MapTag.class, false);
    }
}

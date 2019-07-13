package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.MapMeta;

public class ItemMap implements Property {

    public static Material MAP_MATERIAL = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) ? Material.FILLED_MAP : Material.MAP;

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getItemStack().getType() == MAP_MATERIAL);
    }

    public static ItemMap getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemMap((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "map"
    };

    public static final String[] handledMechs = new String[] {
            "map"
    };


    private ItemMap(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.map>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.map
        // @description
        // Returns the ID number of the map item's map.
        // -->
        if (attribute.startsWith("map")) {
            if (!hasMapId()) {
                return null;
            }
            return new ElementTag(getMapId())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public boolean hasMapId() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            MapMeta map = (MapMeta) item.getItemStack().getItemMeta();
            return map.hasMapId();
        }
        return true;
    }

    public int getMapId() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            MapMeta map = (MapMeta) item.getItemStack().getItemMeta();
            if (!map.hasMapId()) {
                return 0;
            }
            return map.getMapId();
        }
        return item.getItemStack().getDurability();
    }

    public void setMapId(int id) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            MapMeta map = (MapMeta) item.getItemStack().getItemMeta();
            map.setMapId(id);
            item.getItemStack().setItemMeta(map);
        }
        item.getItemStack().setDurability((short) (id));
    }

    @Override
    public String getPropertyString() {
        if (!hasMapId()) {
            return null;
        }
        return String.valueOf(getMapId());
    }

    @Override
    public String getPropertyId() {
        return "map";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name map
        // @input Element(Number)
        // @description
        // Changes what map ID number a map item uses.
        // @tags
        // <ItemTag.map>
        // -->

        if (mechanism.matches("map") && mechanism.requireInteger()) {
            setMapId(mechanism.getValue().asInt());
        }
    }
}

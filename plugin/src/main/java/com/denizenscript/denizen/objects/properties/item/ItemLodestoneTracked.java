package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.CompassMeta;

public class ItemLodestoneTracked implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == Material.COMPASS);
    }

    public static ItemLodestoneTracked getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLodestoneTracked((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "lodestone_tracked"
    };

    public static final String[] handledMechs = new String[] {
            "lodestone_tracked"
    };

    public ItemLodestoneTracked(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.lodestone_tracked>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.lodestone_tracked
        // @description
        // Returns whether the compass will track a lodestone. If "true", the compass will only work if there's a lodestone at the target location.
        // See also <@link tag ItemTag.lodestone_location>
        // -->
        if (attribute.startsWith("lodestone_tracked")) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            return new ElementTag(meta.isLodestoneTracked()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        return meta.isLodestoneTracked() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "lodestone_tracked";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name lodestone_tracked
        // @input ElementTag(Boolean)
        // @description
        // Changes whether the compass will track a lodestone. If "true", the compass will only work if there's a lodestone at the target location.
        // See also <@link mechanism ItemTag.lodestone_location>
        // @tags
        // <ItemTag.lodestone_tracked>
        // -->
        if (mechanism.matches("lodestone_tracked") && mechanism.requireBoolean()) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            meta.setLodestoneTracked(mechanism.getValue().asBoolean());
            item.setItemMeta(meta);
        }
    }
}

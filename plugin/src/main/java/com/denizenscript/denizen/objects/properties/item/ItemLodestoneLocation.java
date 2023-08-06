package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.meta.CompassMeta;

public class ItemLodestoneLocation implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == Material.COMPASS);
    }

    public static ItemLodestoneLocation getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLodestoneLocation((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "lodestone_location"
    };

    public static final String[] handledMechs = new String[] {
            "lodestone_location"
    };

    public ItemLodestoneLocation(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.lodestone_location>
        // @returns LocationTag
        // @group properties
        // @mechanism ItemTag.lodestone_location
        // @description
        // Returns the lodestone location this compass is pointing at (if any).
        // See also <@link tag ItemTag.lodestone_tracked>
        // -->
        if (attribute.startsWith("lodestone_location")) {
            LocationTag target = getTarget();
            if (target == null) {
                return null;
            }
            return target.getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public LocationTag getTarget() {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        Location loc = meta.getLodestone();
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        return new LocationTag(meta.getLodestone());
    }

    @Override
    public String getPropertyString() {
        LocationTag target = getTarget();
        if (target == null) {
            return null;
        }
        return target.identify();
    }

    @Override
    public String getPropertyId() {
        return "lodestone_location";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name lodestone_location
        // @input LocationTag
        // @description
        // Changes the lodestone location this compass is pointing at.
        // See also <@link mechanism ItemTag.lodestone_tracked>
        // Give no input to unset.
        // @tags
        // <ItemTag.lodestone_location>
        // -->
        if (mechanism.matches("lodestone_location")) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            if (mechanism.hasValue() && mechanism.requireObject(LocationTag.class)) {
                LocationTag loc = mechanism.valueAsType(LocationTag.class).clone();
                if (loc.getWorldName() != null && loc.getWorld() == null) {
                    return; // This edge case is handled by RawNBT
                }
                meta.setLodestone(loc);
            }
            else {
                meta.setLodestone(null);
            }
            item.setItemMeta(meta);
        }
    }
}

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

public class ItemApple implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((((ItemTag) item).getItemStack().getType() == Material.GOLDEN_APPLE)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13) && ((ItemTag) item).getItemStack().getType() == Material.ENCHANTED_GOLDEN_APPLE));
    }

    public static ItemApple getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemApple((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "apple_enchanted"
    };

    public static final String[] handledMechs = new String[] {
            "apple_enchanted"
    };

    private ItemApple(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.apple_enchanted>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.apple_enchanted
        // @description
        // Returns whether a golden apple item is enchanted.
        // NOTE: In 1.13+, enchanted golden apples are now a separate Material type, making this tag no longer required.
        // -->
        if (attribute.startsWith("apple_enchanted")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                return new ElementTag(item.getItemStack().getType() == Material.ENCHANTED_GOLDEN_APPLE)
                        .getObjectAttribute(attribute.fulfill(1));
            }
            return new ElementTag(item.getItemStack().getDurability() == 1)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            return null;
        }
        if (item.getItemStack().getDurability() == 1) {
            return "true";
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "apple_enchanted";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name apple_enchanted
        // @input ElementTag(Boolean)
        // @description
        // Changes whether a golden apple is enchanted.
        // NOTE: In 1.13+, enchanted golden apples are now a separate Material type, making this mechanism no longer required.
        // @tags
        // <ItemTag.apple_enchanted>
        // -->
        if (mechanism.matches("apple_enchanted") && mechanism.requireBoolean()) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                item.getItemStack().setType(mechanism.getValue().asBoolean() ? Material.ENCHANTED_GOLDEN_APPLE : Material.GOLDEN_APPLE);
            }
            item.getItemStack().setDurability((short) (mechanism.getValue().asBoolean() ? 1 : 0));
        }
    }
}

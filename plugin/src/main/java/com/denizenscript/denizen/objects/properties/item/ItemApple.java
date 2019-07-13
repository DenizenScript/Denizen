package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;

public class ItemApple implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((((dItem) item).getItemStack().getType() == Material.GOLDEN_APPLE)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && ((dItem) item).getItemStack().getType() == Material.ENCHANTED_GOLDEN_APPLE));
    }

    public static ItemApple getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemApple((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "apple_enchanted"
    };

    public static final String[] handledMechs = new String[] {
            "apple_enchanted"
    };


    private ItemApple(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.apple_enchanted>
        // @returns Element(Boolean)
        // @group properties
        // @mechanism dItem.apple_enchanted
        // @description
        // Returns whether a golden apple item is enchanted.
        // NOTE: In 1.13+, enchanted golden apples are now a separate Material type, making this tag no longer required.
        // -->
        if (attribute.startsWith("apple_enchanted")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                return new Element(item.getItemStack().getType() == Material.ENCHANTED_GOLDEN_APPLE)
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(item.getItemStack().getDurability() == 1)
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
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
        // @object dItem
        // @name apple_enchanted
        // @input Element(Boolean)
        // @description
        // Changes whether a golden apple is enchanted.
        // NOTE: In 1.13+, enchanted golden apples are now a separate Material type, making this mechanism no longer required.
        // @tags
        // <i@item.apple_enchanted>
        // -->

        if (mechanism.matches("apple_enchanted") && mechanism.requireBoolean()) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                item.getItemStack().setType(mechanism.getValue().asBoolean() ? Material.ENCHANTED_GOLDEN_APPLE : Material.GOLDEN_APPLE);
            }
            item.getItemStack().setDurability((short) (mechanism.getValue().asBoolean() ? 1 : 0));
        }
    }
}

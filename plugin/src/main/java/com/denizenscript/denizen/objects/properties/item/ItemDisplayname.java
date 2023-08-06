package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDisplayname implements Property {

    public static boolean describes(ObjectTag item) {
        // Technically, all items can have a display name
        return item instanceof ItemTag;
    }

    public static ItemDisplayname getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemDisplayname((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "display", "has_display"
    };

    public static final String[] handledMechs = new String[] {
            "display", "display_name"
    };

    public ItemDisplayname(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    public boolean hasDisplayName() {
        return item.getItemMeta() != null && item.getItemMeta().hasDisplayName();
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.display>
        // @returns ElementTag
        // @mechanism ItemTag.display
        // @synonyms ItemTag.display_name
        // @group properties
        // @description
        // Returns the display name of the item, as set by plugin or an anvil.
        // -->
        if (attribute.startsWith("display")) {
            if (hasDisplayName()) {
                return new ElementTag(NMSHandler.itemHelper.getDisplayName(item), true)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ItemTag.has_display>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.display
        // @group properties
        // @description
        // Returns whether the item has a custom set display name.
        // -->
        if (attribute.startsWith("has_display")) {
            return new ElementTag(hasDisplayName())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        if (hasDisplayName()) {
            String res = NMSHandler.itemHelper.getDisplayName(item);
            if (res.isEmpty()) { // Special case: persist empty strings as a single empty color code so it's not ignored
                return ChatColor.WHITE.toString();
            }
            return res;
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "display";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name display
        // @input ElementTag
        // @synonyms ItemTag.display_name
        // @description
        // Changes the item's display name.
        // Give no input to remove the item's display name.
        // @tags
        // <ItemTag.display>
        // -->
        if (mechanism.matches("display")) {
            if (!mechanism.hasValue()) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(null);
                item.setItemMeta(meta);
            }
            else {
                NMSHandler.itemHelper.setDisplayName(item, mechanism.getValue().asString());
            }
        }
        else if (mechanism.matches("display_name")) {
            BukkitImplDeprecations.itemDisplayNameMechanism.warn(mechanism.context);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(mechanism.hasValue() ? CoreUtilities.clearNBSPs(EscapeTagUtil.unEscape(mechanism.getValue().asString())) : null);
            item.setItemMeta(meta);
        }
    }
}

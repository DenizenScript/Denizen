package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class InventoryTitle implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories could possibly have a title
        return inventory instanceof InventoryTag;
    }

    public static InventoryTitle getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryTitle((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
            "title"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    InventoryTag inventory;

    public InventoryTitle(InventoryTag inventory) {
        this.inventory = inventory;
    }

    public String getTitle() {
        if (inventory.getInventory() != null) {
            String title = NMSHandler.getInstance().getTitle(inventory.getInventory());
            if (title != null) {
                if (inventory.isUnique()) {
                    return title.substring(0, title.length() - InventoryTag.inventoryNameNotableRequired);
                }
                else if (!title.startsWith("container.")) {
                    return title;
                }
            }
        }
        return null;
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        // Only show a property string for titles that can actually change
        if (inventory.isUnique()
                || inventory.getIdType().equals("generic")
                || inventory.getIdType().equals("location")) {
            return getTitle();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "title";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <InventoryTag.title>
        // @returns ElementTag
        // @group properties
        // @mechanism InventoryTag.title
        // @description
        // Returns the title of the inventory.
        // -->
        PropertyParser.<InventoryTitle>registerTag("title", (attribute, inventory) -> {
            return new ElementTag(inventory.getTitle());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object InventoryTag
        // @name title
        // @input Element
        // @description
        // Sets the title of the inventory. (Only works for "generic" inventories.)
        // @tags
        // <InventoryTag.title>
        // -->
        if (mechanism.matches("title") && inventory.getIdType().equals("generic")) {
            inventory.setTitle(mechanism.getValue().asString());
        }

    }
}

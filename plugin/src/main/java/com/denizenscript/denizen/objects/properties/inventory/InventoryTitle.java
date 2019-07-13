package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class InventoryTitle implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories could possibly have a title
        return inventory instanceof dInventory;
    }

    public static InventoryTitle getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryTitle((dInventory) inventory);
    }

    public static final String[] handledTags = new String[] {
            "title"
    };

    public static final String[] handledMechs = new String[] {
            "title"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;

    public InventoryTitle(dInventory inventory) {
        this.inventory = inventory;
    }

    public String getTitle() {
        if (inventory.getInventory() != null) {
            String title = NMSHandler.getInstance().getTitle(inventory.getInventory());
            if (title != null) {
                if (inventory.isUnique()) {
                    return title.substring(0, title.length() - dInventory.inventoryNameNotableRequired);
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

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <in@inventory.title>
        // @returns ElementTag
        // @group properties
        // @mechanism dInventory.title
        // @description
        // Returns the title of the inventory.
        // -->
        if (attribute.startsWith("title")) {
            return new ElementTag(getTitle()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dInventory
        // @name title
        // @input Element
        // @description
        // Sets the title of the inventory. (Only works for "generic" inventories.)
        // @tags
        // <in@inventory.title>
        // -->
        if (mechanism.matches("title") && inventory.getIdType().equals("generic")) {
            inventory.setTitle(mechanism.getValue().asString());
        }

    }
}

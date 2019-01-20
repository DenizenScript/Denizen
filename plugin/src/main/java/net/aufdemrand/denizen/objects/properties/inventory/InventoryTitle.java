package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class InventoryTitle implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories could possibly have a title
        return inventory instanceof dInventory;
    }

    public static InventoryTitle getFrom(dObject inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryTitle((dInventory) inventory);
    }

    public static final String[] handledTags = new String[]{
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
            String title = inventory.getInventory().getTitle();
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
        // @returns Element
        // @group properties
        // @mechanism dInventory.title
        // @description
        // Returns the title of the inventory.
        // -->
        if (attribute.startsWith("title")) {
            return new Element(getTitle()).getAttribute(attribute.fulfill(1));
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

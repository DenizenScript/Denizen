package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptHelper;
import net.aufdemrand.denizen.tags.Attribute;

public class InventoryTitle implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories could possibly have a title
        return inventory instanceof dInventory;
    }

    public static InventoryTitle getFrom(dObject inventory) {
        if (!describes(inventory)) return null;
        return new InventoryTitle((dInventory) inventory);
    }


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
            if (title != null && !title.startsWith("container.")) {
                if (!inventory.isGettingSaveObj() && InventoryScriptHelper.notableInventories.containsKey(title))
                    return title.substring(0, title.length()-6);
                else
                    return title;
            }
        }
        return null;
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getTitle();
    }

    @Override
    public String getPropertyId() {
        return "title";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <in@inventory.title>
        // @returns Element
        // @group properties
        // @mechanism dInventory.title
        // @description
        // Returns the title of the inventory.
        // -->
        if (attribute.startsWith("title")) {
            String title = getTitle();
            if (title == null)
                return Element.NULL.getAttribute(attribute.fulfill(1));
            else
                return new Element(title).getAttribute(attribute.fulfill(1));
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

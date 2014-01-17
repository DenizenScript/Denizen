package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
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
        if (inventory.getInventory() != null)
            return inventory.getInventory().getTitle();
        else
            return null;
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (inventory.getIdType().equals("generic")
                && inventory.getIdHolder().equals("CHEST")
                && !getTitle().equals("Chest"))
            return getTitle();
        else
            return null;
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
        // TODO
    }
}

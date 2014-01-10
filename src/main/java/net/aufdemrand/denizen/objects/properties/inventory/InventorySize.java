package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;

public class InventorySize implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories should have a size
        return inventory instanceof dInventory;
    }

    public static InventorySize getFrom(dObject inventory) {
        if (!describes(inventory)) return null;
        return new InventorySize((dInventory) inventory);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;

    public InventorySize(dInventory inventory) {
        this.inventory = inventory;
    }

    public int getSize() {
        if (inventory.getInventory() == null)
            return 0;
        return inventory.getInventory().getSize();
    }

    public void setSize(int size) {
        inventory.setSize(size);
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (getSize() > 0 && inventory.getIdType().equals("generic")
                && inventory.getIdType().equals("CHEST"))
            return String.valueOf(getSize());
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "size";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <in@inventory.size>
        // @returns Element(Number)
        // @description
        // Return the number of slots in the inventory.
        // -->
        if (attribute.startsWith("size"))
            return new Element(getSize())
                    .getAttribute(attribute.fulfill(1));

        return null;

    }

}

package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class InventorySize implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories should have a size
        return inventory instanceof dInventory;
    }

    public static InventorySize getFrom(dObject inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventorySize((dInventory) inventory);
    }

    public static final String[] handledTags = new String[] {
            "size"
    };

    public static final String[] handledMechs = new String[] {
            "size"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;

    public InventorySize(dInventory inventory) {
        this.inventory = inventory;
    }

    public int getSize() {
        if (inventory.getInventory() == null) {
            return 0;
        }
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
                && inventory.getIdHolder().equals("CHEST")) {
            return String.valueOf(getSize());
        }
        else {
            return null;
        }
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

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <in@inventory.size>
        // @returns Element(Number)
        // @group properties
        // @mechanism dInventory.size
        // @description
        // Return the number of slots in the inventory.
        // -->
        if (attribute.startsWith("size")) {
            return new Element(getSize())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;

    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dInventory
        // @name size
        // @input Element(Number)
        // @description
        // Sets the size of the inventory. (Only works for "generic" chest inventories.)
        // @tags
        // <in@inventory.size>
        // -->
        if (mechanism.matches("size") && inventory.getIdType().equals("generic") && mechanism.requireInteger()) {
            setSize(mechanism.getValue().asInt());
        }

    }
}

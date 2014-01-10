package net.aufdemrand.denizen.objects.properties.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;

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
        if (size <= 0 || !inventory.getIdType().equals("generic"))
            return;
        else if (size%9 != 0)
            dB.echoError("InventorySize must be multiple of 9.");
        else {
            int oldSize = getSize();
            ItemStack[] oldContents;
            if (inventory.getInventory() != null) {
                oldContents = inventory.getInventory().getContents();
                if (oldSize > size) {
                    ItemStack[] newContents = new ItemStack[size];
                    for (int i = 0; i < size; i++)
                        newContents[i] = oldContents[i];
                    inventory.setInventory(Bukkit.getServer().createInventory(null, size));
                    inventory.setContents(newContents);
                }
                else {
                    inventory.setInventory(Bukkit.getServer().createInventory(null, size));
                    inventory.setContents(oldContents);
                }
            }
            else
                inventory.setInventory(Bukkit.getServer().createInventory(null, size));
        }
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (getSize() > 0 && inventory.getIdType().equals("generic"))
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

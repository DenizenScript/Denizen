package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;

public class InventoryContents implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories should have contents
        return inventory instanceof dInventory;
    }

    public static InventoryContents getFrom(dObject inventory) {
        if (!describes(inventory)) return null;
        return new InventoryContents((dInventory) inventory);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;

    public InventoryContents(dInventory inventory) {
        this.inventory = inventory;
    }

    public dList getContents(boolean simple) {
        if (inventory.getInventory() == null)
            return null;
        dList contents = new dList();
        boolean containsNonAir = false;
        for (ItemStack item : inventory.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                containsNonAir = true;
                if (simple)
                    contents.add(new dItem(item).identifySimple());
                else
                    contents.add(new dItem(item).identify());
            }
            else
                contents.add("i@air");
        }
        if (!containsNonAir)
            contents.clear();
        else
            contents = dList.valueOf(contents.identify().replaceAll("(\\|i@air)*$", ""));
        return contents;
    }

    public dList getContentsWithLore(String lore, boolean simple) {
        if (inventory.getInventory() == null)
            return null;
        dList contents = new dList();
        for (ItemStack item : inventory.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    for (String line : item.getItemMeta().getLore()) {
                        // Add the item to the list if it contains the lore specified in
                        // the context
                        if (ChatColor.stripColor(line).equalsIgnoreCase(lore)) {
                            if (simple)
                                contents.add(new dItem(item).identifySimple());
                            else
                                contents.add(new dItem(item).identify());
                            break;
                        }
                    }
                }
            }
        }
        return contents;
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!inventory.getIdType().equals("generic"))
            return null;
        dList contents = getContents(false);
        if (contents == null || contents.isEmpty())
            return null;
        else
            return contents.identify();
    }

    @Override
    public String getPropertyId() {
        return "contents";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <in@inventory.list_contents>
        // @returns dList(dItem)
        // @description
        // Returns a list of all items in the inventory.
        // -->
        if (attribute.startsWith("list_contents")) {
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <in@inventory.list_contents.simple>
            // @returns dList(dItem)
            // @description
            // Returns a list of all items in the inventory, without item properties.
            // -->
            if (attribute.startsWith("simple"))
                return getContents(true).getAttribute(attribute.fulfill(1));

            // <--[tag]
            // @attribute <in@inventory.list_contents.with_lore[<element>]>
            // @returns dList(dItem)
            // @description
            // Returns a list of all items in the inventory with the specified
            // lore. Color codes are ignored.
            // -->
            if (attribute.startsWith("with_lore")) {
                // Must specify lore to check
                if (!attribute.hasContext(1)) return Element.NULL.getAttribute(attribute.fulfill(1));
                String lore = attribute.getContext(1);
                attribute.fulfill(1);
                // <--[tag]
                // @attribute <in@inventory.list_contents.with_lore[<element>].simple>
                // @returns dList(dItem)
                // @description
                // Returns a list of all items in the inventory with the specified
                // lore, without item properties. Color codes are ignored.
                // -->
                if (attribute.startsWith("simple"))
                    return getContentsWithLore(lore, true)
                                .getAttribute(attribute.fulfill(1));

                return getContentsWithLore(lore, false)
                            .getAttribute(attribute);
            }

            return getContents(false).getAttribute(attribute);
        }

        return null;

    }

    @Override
    public void adjust(Mechanism mechanism) {
        // TODO
    }

}

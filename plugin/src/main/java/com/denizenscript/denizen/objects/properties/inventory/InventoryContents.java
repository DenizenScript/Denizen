package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InventoryContents implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories should have contents
        return inventory instanceof dInventory;
    }

    public static InventoryContents getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryContents((dInventory) inventory);
    }

    public static final String[] handledTags = new String[] {
            "list_contents"
    };

    public static final String[] handledMechs = new String[] {
            "contents"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;

    public InventoryContents(dInventory inventory) {
        this.inventory = inventory;
    }

    public ListTag getContents(int simpleOrFull) {
        if (inventory.getInventory() == null) {
            return null;
        }
        ListTag contents = new ListTag();
        boolean containsNonAir = false;
        for (ItemStack item : inventory.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                containsNonAir = true;
                if (simpleOrFull == 1) {
                    contents.add(new dItem(item).identifySimple());
                }
                else if (simpleOrFull == 2) {
                    contents.add(new dItem(item).getFullString());
                }
                else {
                    contents.add(new dItem(item).identify());
                }
            }
            else {
                contents.add("i@air");
            }
        }
        if (!containsNonAir) {
            contents.clear();
        }
        else {
            contents = ListTag.valueOf(contents.identify().replaceAll("(\\|i@air)*$", ""));
        }
        return contents;
    }

    public ListTag getContentsWithLore(String lore, boolean simple) {
        if (inventory.getInventory() == null) {
            return null;
        }
        ListTag contents = new ListTag();
        lore = ChatColor.stripColor(lore);
        for (ItemStack item : inventory.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    for (String line : item.getItemMeta().getLore()) {
                        // Add the item to the list if it contains the lore specified in
                        // the context
                        if (ChatColor.stripColor(line).equalsIgnoreCase(lore)) {
                            if (simple) {
                                contents.add(new dItem(item).identifySimple());
                            }
                            else {
                                contents.add(new dItem(item).identify());
                            }
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
        if (!inventory.getIdType().equals("generic") && !inventory.isUnique()) {
            return null;
        }
        ListTag contents = getContents(0);
        if (contents == null || contents.isEmpty()) {
            return null;
        }
        else {
            return contents.identify();
        }
    }

    @Override
    public String getPropertyId() {
        return "contents";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <in@inventory.list_contents>
        // @returns ListTag(dItem)
        // @group properties
        // @mechanism dInventory.contents
        // @description
        // Returns a list of all items in the inventory.
        // -->
        if (attribute.startsWith("list_contents")) {
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <in@inventory.list_contents.simple>
            // @returns ListTag(dItem)
            // @group properties
            // @mechanism dInventory.contents
            // @description
            // Returns a list of all items in the inventory, without item properties.
            // -->
            if (attribute.startsWith("simple")) {
                return getContents(1).getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <in@inventory.list_contents.full>
            // @returns ListTag(dItem)
            // @group properties
            // @mechanism dInventory.contents
            // @description
            // Returns a list of all items in the inventory, with the tag item.full used.
            // Irrelevant on modern (1.13+) servers.
            // -->
            if (attribute.startsWith("full")) {
                return getContents(2).getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <in@inventory.list_contents.with_lore[<element>]>
            // @returns ListTag(dItem)
            // @group properties
            // @mechanism dInventory.contents
            // @description
            // Returns a list of all items in the inventory with the specified
            // lore. Color codes are ignored.
            // -->
            if (attribute.startsWith("with_lore")) {
                // Must specify lore to check
                if (!attribute.hasContext(1)) {
                    return null;
                }
                String lore = attribute.getContext(1);
                attribute.fulfill(1);
                // <--[tag]
                // @attribute <in@inventory.list_contents.with_lore[<element>].simple>
                // @returns ListTag(dItem)
                // @group properties
                // @mechanism dInventory.contents
                // @description
                // Returns a list of all items in the inventory with the specified
                // lore, without item properties. Color codes are ignored.
                // -->
                if (attribute.startsWith("simple")) {
                    return getContentsWithLore(lore, true)
                            .getAttribute(attribute.fulfill(1));
                }

                return getContentsWithLore(lore, false)
                        .getAttribute(attribute);
            }

            return getContents(0).getAttribute(attribute);
        }

        return null;

    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dInventory
        // @name contents
        // @input ListTag(dItem)
        // @description
        // Sets the contents of the inventory.
        // @tags
        // <in@inventory.list_contents>
        // <in@inventory.list_contents.simple>
        // <in@inventory.list_contents.with_lore[<lore>]>
        // <in@inventory.list_contents.with_lore[<lore>].simple>
        // -->
        if (mechanism.matches("contents") && inventory.getIdType().equals("generic")) {
            inventory.setContents(mechanism.valueAsType(ListTag.class), mechanism.context);
        }

    }
}

package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InventoryContents implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories should have contents
        return inventory instanceof InventoryTag;
    }

    public static InventoryContents getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryContents((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
            "contents"
    };

    InventoryTag inventory;

    public InventoryContents(InventoryTag inventory) {
        this.inventory = inventory;
    }

    public ListTag getContents(boolean simple) {
        if (inventory.getInventory() == null) {
            return null;
        }
        int lastNonAir = -1;
        ListTag contents = new ListTag();
        for (ItemStack item : inventory.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                lastNonAir = contents.size();
                if (simple) {
                    contents.add(new ItemTag(item).identifySimple());
                }
                else {
                    contents.addObject(new ItemTag(item));
                }
            }
            else {
                contents.addObject(new ItemTag(Material.AIR));
            }
        }
        lastNonAir++;
        while (contents.size() > lastNonAir) {
            contents.remove(lastNonAir);
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
                                contents.add(new ItemTag(item).identifySimple());
                            }
                            else {
                                contents.addObject(new ItemTag(item));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return contents;
    }

    @Override
    public String getPropertyString() {
        if (!inventory.isGeneric() && !inventory.isSaving) {
            return null;
        }
        ListTag contents = getContents(false);
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

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.map_slots>
        // @returns MapTag
        // @group properties
        // @description
        // Returns a map of inventory slots to the items in those slots (excludes air).
        // -->
        PropertyParser.registerTag(InventoryContents.class, MapTag.class, "map_slots", (attribute, contents) -> {
            MapTag map = new MapTag();
            ItemStack[] items = contents.inventory.getContents();
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null || items[i].getType() == Material.AIR) {
                    continue;
                }
                map.putObject(String.valueOf(i + 1), new ItemTag(items[i]));
            }
            return map;
        });

        // <--[tag]
        // @attribute <InventoryTag.list_contents>
        // @returns ListTag(ItemTag)
        // @group properties
        // @mechanism InventoryTag.contents
        // @description
        // Returns a list of all items in the inventory.
        // -->
        PropertyParser.registerTag(InventoryContents.class, ListTag.class, "list_contents", (attribute, contents) -> {

            // <--[tag]
            // @attribute <InventoryTag.list_contents.simple>
            // @returns ListTag(ItemTag)
            // @group properties
            // @mechanism InventoryTag.contents
            // @description
            // Returns a list of all items in the inventory, without item properties.
            // -->
            if (attribute.startsWith("simple", 2)) {
                attribute.fulfill(1);
                return contents.getContents(true);
            }

            // <--[tag]
            // @attribute <InventoryTag.list_contents.with_lore[<element>]>
            // @returns ListTag(ItemTag)
            // @group properties
            // @mechanism InventoryTag.contents
            // @description
            // Returns a list of all items in the inventory with the specified
            // lore. Color codes are ignored.
            // -->
            if (attribute.startsWith("with_lore", 2)) {
                attribute.fulfill(1);
                // Must specify lore to check
                if (!attribute.hasParam()) {
                    return null;
                }
                String lore = attribute.getParam();
                attribute.fulfill(1);

                // <--[tag]
                // @attribute <InventoryTag.list_contents.with_lore[<element>].simple>
                // @returns ListTag(ItemTag)
                // @group properties
                // @mechanism InventoryTag.contents
                // @description
                // Returns a list of all items in the inventory with the specified
                // lore, without item properties. Color codes are ignored.
                // -->
                if (attribute.startsWith("simple", 2)) {
                    attribute.fulfill(1);
                    return contents.getContentsWithLore(lore, true);
                }

                return contents.getContentsWithLore(lore, false);
            }

            return contents.getContents(false);
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object InventoryTag
        // @name contents
        // @input ListTag(ItemTag)
        // @description
        // Sets the contents of the inventory.
        // @tags
        // <InventoryTag.list_contents>
        // <InventoryTag.list_contents.simple>
        // <InventoryTag.list_contents.with_lore[<lore>]>
        // <InventoryTag.list_contents.with_lore[<lore>].simple>
        // -->
        if (mechanism.matches("contents") && (inventory.isGeneric() || !mechanism.isProperty)) {
            inventory.setContents(mechanism.valueAsType(ListTag.class), mechanism.context);
        }

    }
}

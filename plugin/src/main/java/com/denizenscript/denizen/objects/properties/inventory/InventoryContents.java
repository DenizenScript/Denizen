package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InventoryContents extends ObjectProperty<InventoryTag, ListTag> {

    public static boolean describes(InventoryTag inventory) {
        return true;
    }

    @Override
    public ListTag getPropertyValue() {
        if (!object.isGeneric() && !object.isSaving) {
            return null;
        }
        ListTag contents = getContents(false);
        if (contents == null || contents.isEmpty()) {
            return null;
        }
        return contents;
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        if (object.isGeneric() || !mechanism.isProperty) {
            object.setContents(list, mechanism.context);
        }
    }

    @Override
    public String getPropertyId() {
        return "contents";
    }

    public InventoryContents(InventoryTag inventory) {
        object = inventory;
    }

    public ListTag getContents(boolean simple) {
        if (object.getInventory() == null) {
            return null;
        }
        int lastNonAir = -1;
        ListTag contents = new ListTag();
        for (ItemStack item : object.getInventory().getContents()) {
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
        if (object.getInventory() == null) {
            return null;
        }
        ListTag contents = new ListTag();
        lore = ChatColor.stripColor(lore);
        for (ItemStack item : object.getInventory().getContents()) {
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

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.map_slots>
        // @returns MapTag
        // @group properties
        // @description
        // Returns a map of inventory slots to the items in those slots (excludes air).
        // -->
        PropertyParser.registerTag(InventoryContents.class, MapTag.class, "map_slots", (attribute, prop) -> {
            MapTag map = new MapTag();
            ItemStack[] items = prop.object.getContents();
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
        PropertyParser.registerTag(InventoryContents.class, ListTag.class, "list_contents", (attribute, prop) -> {

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
                return prop.getContents(true);
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
                    return prop.getContentsWithLore(lore, true);
                }

                return prop.getContentsWithLore(lore, false);
            }

            return prop.getContents(false);
        });

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
        PropertyParser.registerMechanism(InventoryContents.class, ListTag.class, "contents", (prop, mechanism, param) -> {
            prop.setPropertyValue(param, mechanism);
        });
    }
}

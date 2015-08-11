package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Inventory Script Containers
    // @group Script Container System
    // @description
    // Inventory script containers are an easy way to pre-define custom inventories for use within scripts.
    // Inventory scripts work with the dInventory object, and can be fetched with the Object Fetcher by using the
    // dInventory constructor in@inventory_script_name.
    //
    // Example: - inventory open d:in@MyInventoryScript
    //
    // The following is the format for the container. They all are optional, but you have to have at least one
    // of them.
    //
    //
    // <code>
    // # The name of the script is the same name that you can use to construct a new
    // # dInventory based on this inventory script. For example, an inventory script named 'Super Cool Inventory'
    // # can be referred to as 'in@Super Cool Inventory'.
    // Inventory Script Name:
    //
    //   type: inventory
    //
    //   # Must be a valid inventory type.
    //   # Valid inventory types: BREWING, CHEST, DISPENSER, ENCHANTING, ENDER_CHEST, HOPPER, PLAYER, WORKBENCH
    //   inventory: inventory type
    //
    //   # The title can be anything you wish. Use color tags to make colored titles.
    //   # Note that titles only work for CHEST type inventories.
    //   title: custom title
    //
    //   # The size must be a multiple of 9. It is recommended not to go above 54, as it will not show
    //   # correctly when a player looks into it. Tags are allowed for advanced usage.
    //   size: 27
    //
    //   # You can use definitions to define items to use in the slots. These are not like normal
    //   # script definitions, and do not need %'s around them.
    //   definitions:
    //     my item: i@item
    //     other item: i@item
    //
    //   # You can specify the items in the slots of the inventory. For empty spaces, simply put
    //   # an empty "slot". Note the quotes around the entire lines.
    //   slots:
    //     - "[] [] [] [my item] [i@item] [] [other item] [] []"
    //     - "[my item] [] [] [] [] [i@item] [i@item] [] []"
    //     - "[] [] [] [] [] [] [] [] [other item]"
    // </code>
    //
    // -->

    public InventoryScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        InventoryScriptHelper.inventory_scripts.put(getName(), this);
    }

    public InventoryType getInventoryType() {
        String typeStr = getString("inventory", "CHEST");

        try {
            return InventoryType.valueOf(typeStr.toUpperCase());
        }
        catch (Exception e) {
            return InventoryType.CHEST;
        }
    }

    public dInventory getInventoryFrom(dPlayer player, dNPC npc) {

        dInventory inventory = null;
        BukkitTagContext context = new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this));

        try {
            if (contains("INVENTORY")) {
                if (InventoryType.valueOf(getString("INVENTORY").toUpperCase()) != null) {
                    inventory = new dInventory(InventoryType.valueOf(getString("INVENTORY").toUpperCase()));
                    inventory.setIdentifiers("script", getName());
                }
                else {
                    dB.echoError("Invalid inventory type specified. Assuming \"CHEST\"");
                }
            }
            int size = 0;
            if (contains("SIZE")) {
                if (inventory != null && !getInventoryType().name().equalsIgnoreCase("CHEST")) {
                    dB.echoError("You can only set the size of chest inventories!");
                }
                else {
                    size = aH.getIntegerFrom(TagManager.tag(getString("SIZE"), context));

                    if (size == 0) {
                        dB.echoError("Inventory size can't be 0. Assuming default of inventory type...");
                    }
                    if (size % 9 != 0) {
                        size = (int) Math.ceil(size / 9) * 9;
                        dB.echoError("Inventory size must be a multiple of 9! Rounding up to " + size + "...");
                    }
                    if (size < 0) {
                        size = size * -1;
                        dB.echoError("Inventory size must be a positive number! Inverting to " + size + "...");
                    }

                    inventory = new dInventory(size, contains("TITLE") ? TagManager.tag(getString("TITLE"), context) : "Chest");
                    inventory.setIdentifiers("script", getName());
                }
            }
            if (size == 0) {
                size = getInventoryType().getDefaultSize();
            }
            if (contains("SLOTS")) {
                ItemStack[] finalItems = new ItemStack[size];
                int itemsAdded = 0;
                for (String items : getStringList("SLOTS")) {
                    items = TagManager.tag(items, context).trim();
                    if (items.isEmpty()) {
                        continue;
                    }
                    if (!items.startsWith("[") || !items.endsWith("]")) {
                        dB.echoError("Inventory script \"" + getName() + "\" has an invalid slots line: ["
                                + items + "]... Ignoring it");
                        continue;
                    }
                    String[] itemsInLine = items.substring(1, items.length()-1).split("\\[?\\]?\\s+\\[");
                    for (String item : itemsInLine) {
                        if (contains("DEFINITIONS." + item)) {
                            dItem def = dItem.valueOf(TagManager.tag(getString("DEFINITIONS." + item), context), player, npc);
                            if (def == null) {
                                dB.echoError("Invalid definition '" + item + "' in inventory script '" + getName() + "'"
                                        + "... Ignoring it and assuming \"AIR\"");
                                finalItems[itemsAdded] = new ItemStack(Material.AIR);
                            }
                            else {
                                finalItems[itemsAdded] = def.getItemStack();
                            }
                        }
                        else if (dItem.matches(item)) {
                            finalItems[itemsAdded] = dItem.valueOf(item, player, npc).getItemStack();
                        }
                        else {
                            finalItems[itemsAdded] = new ItemStack(Material.AIR);
                            if (!item.isEmpty()) {
                                dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item: ["
                                        + item + "]... Ignoring it and assuming \"AIR\"");
                            }
                        }
                        itemsAdded++;
                    }
                }
                if (inventory == null) {
                    size = finalItems.length % 9 == 0 ? finalItems.length : Math.round(finalItems.length / 9) * 9;
                    inventory = new dInventory(size == 0 ? 9 : size,
                            contains("TITLE") ? TagManager.tag(getString("TITLE"), context) : "Chest");
                }
                inventory.setContents(finalItems);
            }
        }
        catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this inventory script!");
            dB.echoError(e);
            inventory = null;
        }

        if (inventory != null)
            InventoryScriptHelper.tempInventoryScripts.put(inventory.getInventory(), getName());

        return inventory;

    }
}

package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
    // The following is the format for the container.
    //
    // The 'inventory:' key is required, other keys vary based on the type.
    // Some types will require you define either 'size:' or 'slots:' (or both).
    // 'Procedural items:' and 'definitions:' are optional, and should only be defined if needed.
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
    //   # Note that titles only work for some inventory types, including CHEST, DISPENSER, FURNACE, ENCHANTING, and HOPPER.
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
    //   # Procedural items can be used to specify a list of dItems for the empty slots to be filled with.
    //   # Each item in the list represents the next available empty slot.
    //   # When the inventory has no more empty slots, it will discard any remaining items in the list.
    //   # A slot is considered empty when it has no value specified in the slots section.
    //   # If the slot is filled with air, it will no longer count as being empty.
    //   procedural items:
    //     - define list li@
    //     - foreach <server.list_online_players>:
    //       - define item i@human_skull[skull_skin=<def[value].name>]
    //       - define list <def[list].include[<def[item]>]>
    //     - determine <def[list]>
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

        // TODO: Clean all this code!

        dInventory inventory = null;
        BukkitTagContext context = new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this));

        try {
            if (contains("INVENTORY")) {
                if (InventoryType.valueOf(getString("INVENTORY").toUpperCase()) != null) {
                    inventory = new dInventory(InventoryType.valueOf(getString("INVENTORY").toUpperCase()));
                    if (contains("TITLE")) {
                        inventory.setTitle(TagManager.tag(getString("TITLE"), context));
                    }
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
                        size = (int) Math.ceil(size / 9.0) * 9;
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
            boolean[] filledSlots = new boolean[size];
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
                    String[] itemsInLine = items.substring(1, items.length() - 1).split("\\[?\\]?\\s+\\[", -1);
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
                            try {
                                finalItems[itemsAdded] = dItem.valueOf(item, player, npc).getItemStack();
                            }
                            catch (Exception ex) {
                                dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item: ["
                                        + item + "]...");
                                dB.echoError(ex);
                            }
                        }
                        else {
                            finalItems[itemsAdded] = new ItemStack(Material.AIR);
                            if (!item.isEmpty()) {
                                dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item: ["
                                        + item + "]... Ignoring it and assuming \"AIR\"");
                            }
                        }
                        filledSlots[itemsAdded] = !item.isEmpty();
                        itemsAdded++;
                    }
                }
                if (inventory == null) {
                    size = finalItems.length % 9 == 0 ? finalItems.length : (int) (Math.ceil(finalItems.length / 9.0) * 9);
                    inventory = new dInventory(size == 0 ? 9 : size,
                            contains("TITLE") ? TagManager.tag(getString("TITLE"), context) : "Chest");
                }
                inventory.setContents(finalItems);
            }
            if (contains("PROCEDURAL ITEMS")) {
                // TODO: Document this feature!
                if (inventory == null) {
                    size = InventoryType.CHEST.getDefaultSize();
                    inventory = new dInventory(size, contains("TITLE") ? TagManager.tag(getString("TITLE"), context) : "Chest");
                }
                List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "PROCEDURAL ITEMS");
                if (!entries.isEmpty()) {
                    InstantQueue queue = new InstantQueue("INV_SCRIPT_ITEM_PROC");
                    queue.addEntries(entries);
                    if (contains("DEFINITIONS")) {
                        YamlConfiguration section = getConfigurationSection("DEFINITIONS");
                        for (StringHolder string : section.getKeys(false)) {
                            String definition = string.str;
                            queue.addDefinition(definition, section.getString(definition));
                        }
                    }
                    queue.start();
                    if (queue.determinations != null) {
                        dList list = dList.getListFor(queue.determinations.getObject(0));
                        if (list != null) {
                            int x = 0;
                            for (dItem item : list.filter(dItem.class, this)) {
                                while (x < filledSlots.length && filledSlots[x]) {
                                    x++;
                                }
                                if (x >= filledSlots.length || filledSlots[x]) {
                                    break;
                                }
                                inventory.setSlots(x, item.getItemStack());
                                filledSlots[x] = true;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this inventory script!");
            dB.echoError(e);
            inventory = null;
        }

        if (inventory != null) {
            InventoryScriptHelper.tempInventoryScripts.put(inventory.getInventory(), getName());
        }

        return inventory;

    }
}

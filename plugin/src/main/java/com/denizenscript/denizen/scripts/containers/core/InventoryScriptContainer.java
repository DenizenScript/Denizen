package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class InventoryScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Inventory Script Containers
    // @group Script Container System
    // @description
    // Inventory script containers are an easy way to pre-define custom inventories for use within scripts.
    // Inventory scripts work with the InventoryTag object, and can be fetched with the Object Fetcher by using the
    // InventoryTag constructor InventoryTag_script_name.
    //
    // Example: - inventory open d:MyInventoryScript
    //
    // The following is the format for the container.
    //
    // The 'inventory:' key is required, other keys vary based on the type.
    // Some types will require you define either 'size:' or 'slots:' (or both).
    // 'Procedural items:' and 'definitions:' are optional, and should only be defined if needed.
    //
    // <code>
    // # The name of the script is the same name that you can use to construct a new
    // # InventoryTag based on this inventory script. For example, an inventory script named 'Super_Cool_Inventory'
    // # can be referred to as 'Super_Cool_Inventory'.
    // Inventory_Script_Name:
    //
    //     type: inventory
    //
    //     # Must be a valid inventory type.
    //     # Valid inventory types: ANVIL, BREWING, CHEST, DISPENSER, ENCHANTING, ENDER_CHEST, HOPPER, WORKBENCH
    //     # | All inventory scripts MUST have this key!
    //     inventory: inventory type
    //
    //     # The title can be anything you wish. Use color tags to make colored titles.
    //     # Note that titles only work for some inventory types, including ANVIL, CHEST, DISPENSER, FURNACE, ENCHANTING, HOPPER, WORKBENCH
    //     # | MOST inventory scripts should have this key!
    //     title: custom title
    //
    //     # The size must be a multiple of 9. It is recommended to not go above 54, as it will not show correctly when a player looks into it.
    //     # | Some inventory scripts should have this key! Most can exclude it if 'slots' is used.
    //     size: 27
    //
    //     # Set 'gui' to 'true' to indicate that the inventory is a GUI, meaning it's a set of buttons to be clicked, not a container of items.
    //     # This will prevent players from taking items out of or putting items into the inventory.
    //     # | SOME inventory scripts should have this key!
    //     gui: true
    //
    //     # You can use definitions to define items to use in the slots. These are not like normal script definitions, and do not need to be in a definition tag.
    //     # | Some inventory scripts MAY have this key, but it is optional. Most scripts will just specify items directly.
    //     definitions:
    //         my item: ItemTag
    //         other item: ItemTag
    //
    //     # Procedural items can be used to specify a list of ItemTags for the empty slots to be filled with.
    //     # Each item in the list represents the next available empty slot.
    //     # When the inventory has no more empty slots, it will discard any remaining items in the list.
    //     # A slot is considered empty when it has no value specified in the slots section.
    //     # If the slot is filled with air, it will no longer count as being empty.
    //     # | Most inventory scripts should exclude this key, but it may be useful in some cases.
    //     procedural items:
    //     - define list <list>
    //     - foreach <server.online_players>:
    //         # Insert some form of complex doesn't-fit-in-just-a-tag logic here
    //         - define item <[value].skull_item>
    //         - define list:->:<[item]>
    //     - determine <[list]>
    //
    //     # You can specify the items in the slots of the inventory. For empty spaces, simply put an empty "slot" value, like "[]".
    //     # | Most inventory scripts SHOULD have this key!
    //     slots:
    //     - [] [] [] [my item] [ItemTag] [] [other item] [] []
    //     - [my item] [] [] [] [] [ItemTag] [ItemTag] [] []
    //     - [] [] [] [] [] [] [] [] [other item]
    // </code>
    //
    // -->

    public InventoryScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        gui = CoreUtilities.equalsIgnoreCase(getString("gui", "false"), "true");
        InventoryScriptHelper.inventoryScripts.put(getName(), this);
    }

    public boolean gui;

    public TagContext fixContext(TagContext context) {
        context = (context == null ? CoreUtilities.basicContext : context).clone();
        context.script = new ScriptTag(this);
        context.debug = context.debug && shouldDebug();
        return context;
    }

    public InventoryTag getInventoryFrom(TagContext context) {
        InventoryTag inventory;
        context = fixContext(context);
        Debug.pushErrorContext(this);
        try {
            InventoryType type = InventoryType.CHEST;
            if (contains("inventory", String.class)) {
                try {
                    type = InventoryType.valueOf(getString("inventory").toUpperCase());
                }
                catch (IllegalArgumentException ex) {
                    Debug.echoError(this, "Invalid inventory type specified. Assuming \"CHEST\" (" + ex.getMessage() + ")");
                }
            }
            else {
                Debug.echoError(this, "Inventory script '" + getName() + "' does not specify an inventory type. Assuming \"CHEST\".");
            }
            if (type == InventoryType.PLAYER) {
                Debug.echoError(this, "Inventory type 'player' is not valid for inventory scripts - defaulting to 'CHEST'.");
                type = InventoryType.CHEST;
            }
            int size = 0;
            if (contains("size", String.class)) {
                if (type != InventoryType.CHEST) {
                    Debug.echoError(this, "You can only set the size of chest inventories!");
                }
                else {
                    String sizeText = TagManager.tag(getString("size"), context);
                    if (!ArgumentHelper.matchesInteger(sizeText)) {
                        Debug.echoError(this, "Invalid (not-a-number) size value.");
                    }
                    else {
                        size = Integer.parseInt(sizeText);
                    }
                    if (size == 0) {
                        Debug.echoError(this, "Inventory size can't be 0. Assuming default of inventory type...");
                    }
                    if (size % 9 != 0) {
                        size = (int) Math.ceil(size / 9.0) * 9;
                        Debug.echoError(this, "Inventory size must be a multiple of 9! Rounding up to " + size + "...");
                    }
                    if (size < 0) {
                        size = size * -1;
                        Debug.echoError(this, "Inventory size must be a positive number! Inverting to " + size + "...");
                    }
                }
            }
            if (size == 0) {
                if (contains("slots", List.class) && type == InventoryType.CHEST) {
                    size = getStringList("slots").size() * 9;
                }
                else {
                    size = type.getDefaultSize();
                }
            }
            String title;
            Debug.pushErrorContext("While reading 'title' input");
            try {
                title = contains("title", String.class) ? TagManager.tag(getString("title"), context) : null;
            }
            finally {
                Debug.popErrorContext();
            }
            if (type == InventoryType.CHEST) {
                inventory = new InventoryTag(size, title != null ? title : "Chest");
            }
            else if (InventoryScriptHelper.isPersonalSpecialInv(type)) {
                inventory = new InventoryTag(type);
                inventory.customTitle = title;
            }
            else {
                if (title == null) {
                    inventory = new InventoryTag(type);
                }
                else {
                    inventory = new InventoryTag(type, title);
                }
            }
            inventory.idType = "script";
            inventory.idHolder = new ScriptTag(this);
            boolean[] filledSlots = new boolean[size];
            Debug.pushErrorContext("While reading 'slots' input");
            try {
                if (contains("slots", List.class)) {
                    ItemStack[] finalItems = new ItemStack[size];
                    int itemsAdded = 0;
                    for (String items : getStringList("slots")) {
                        items = TagManager.tag(items, context).trim();
                        if (items.isEmpty()) {
                            continue;
                        }
                        if (!items.startsWith("[") || !items.endsWith("]")) {
                            Debug.echoError(this, "Invalid slots line: [" + items + "]... Ignoring it");
                            continue;
                        }
                        String[] itemsInLine = items.substring(1, items.length() - 1).split("\\[?\\]?\\s+\\[", -1);
                        for (String item : itemsInLine) {
                            if (item.isEmpty()) {
                                finalItems[itemsAdded++] = new ItemStack(Material.AIR);
                                continue;
                            }
                            filledSlots[itemsAdded] = true;
                            if (contains("definitions." + item, String.class)) {
                                ItemTag def = ItemTag.valueOf(TagManager.tag(getString("definitions." + item), context), context);
                                if (def == null) {
                                    Debug.echoError(this, "Invalid definition '" + item + "'... Ignoring it and assuming 'AIR'");
                                    finalItems[itemsAdded] = new ItemStack(Material.AIR);
                                }
                                else {
                                    finalItems[itemsAdded] = def.getItemStack();
                                }
                            }
                            else {
                                try {
                                    ItemTag itemTag = ItemTag.valueOf(item, context);
                                    if (itemTag == null) {
                                        finalItems[itemsAdded] = new ItemStack(Material.AIR);
                                        Debug.echoError(this, "Invalid slot item: [" + item + "]... ignoring it and assuming 'AIR'");
                                    }
                                    else {
                                        finalItems[itemsAdded] = itemTag.getItemStack();
                                    }
                                }
                                catch (Exception ex) {
                                    Debug.echoError(this, "Invalid slot item: [" + item + "]...");
                                    Debug.echoError(ex);
                                }
                            }
                            itemsAdded++;
                        }
                    }
                    inventory.setContents(finalItems);
                }
            }
            finally {
                Debug.popErrorContext();
            }
            if (containsScriptSection("procedural items")) {
                List<ScriptEntry> entries = getEntries(context.getScriptEntryData(), "procedural items");
                if (!entries.isEmpty()) {
                    InstantQueue queue = new InstantQueue(getName());
                    queue.addEntries(entries);
                    if (contains("definitions", Map.class)) {
                        for (Map.Entry<StringHolder, Object> entry : getConfigurationSection("definitions").getMap().entrySet()) {
                            ItemTag definitionValue = ItemTag.valueOf(TagManager.tag(entry.getValue().toString(), context), context);
                            if (definitionValue == null) {
                                Debug.echoError(this, "Invalid item '" + entry.getValue() + "' for definition '" + entry.getKey().str + "'");
                                continue;
                            }
                            queue.addDefinition(entry.getKey().low, definitionValue);
                        }
                    }
                    queue.procedural = true;
                    queue.start();
                    if (queue.determinations != null) {
                        ListTag list = ListTag.getListFor(queue.determinations.getObject(0), context);
                        if (list != null) {
                            int x = 0;
                            for (ItemTag item : list.filter(ItemTag.class, context, true)) {
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
            Debug.echoError(this, "Woah! An exception has been called while building this inventory script!");
            Debug.echoError(e);
            inventory = null;
        }
        finally {
            Debug.popErrorContext();
        }
        if (inventory != null) {
            InventoryTag.trackTemporaryInventory(inventory);
        }
        return inventory;
    }
}

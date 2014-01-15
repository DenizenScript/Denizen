package net.aufdemrand.denizen.scripts.containers.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
    //   # correctly when a player looks into it.
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

    public InventoryScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public Map<String, dItem> definitions = new HashMap<String, dItem>();

    public int getSize() {
        InventoryType invType = getInventoryType();
        int size = aH.getIntegerFrom(getString("SIZE", String.valueOf(invType.getDefaultSize())));

        return size;
    }

    public InventoryType getInventoryType() {
        String typeStr = getString("inventory", "chest");

        try {
            InventoryType type = InventoryType.valueOf(typeStr.toUpperCase());
            return type;
        }
        catch(Exception e) {
            return InventoryType.CHEST;
        }
    }

    public dInventory getInventoryFrom() {
        return getInventoryFrom(null, null);
    }

    public dInventory getInventoryFrom(dPlayer player, dNPC npc) {

        dInventory inventory = null;

        try {
            if (contains("INVENTORY")) {
                if (InventoryType.valueOf(getString("INVENTORY")) != null) {
                    inventory = new dInventory(InventoryType.valueOf(getString("INVENTORY")));
                    inventory.setIdentifiers("script", getName());
                }
                else {
                    dB.echoError("Invalid inventory type specified. Assuming \"CHEST\"");
                }
            }
            if (contains("SIZE")) {
                if (inventory != null && !getInventoryType().name().equalsIgnoreCase("CHEST")) {
                    dB.echoError("You can only set the size of chest inventories!");
                }
                else {
                    int size = aH.getIntegerFrom(getString("SIZE"));

                    if (size == 0) {
                        dB.echoError("Inventory size can't be 0. Assuming default...");
                        size = 27;
                    }
                    if (size % 9 != 0) {
                        dB.echoError("Inventory size must be a multiple of 9! Rounding...");
                        size = Math.round(size/9)*9;
                        if (size == 0)
                            size = 9;
                    }
                    if (size < 0) {
                        dB.echoError("Inventory size must be a positive number! Inverting...");
                        size = size*-1;
                    }

                    inventory = new dInventory(size,
                            contains("TITLE") ? TagManager.tag(player, npc, getString("TITLE")) : "Chest");
                    inventory.setIdentifiers("script", getName());
                }
            }
            if (contains("SLOTS")) {
                ItemStack[] finalItems = new ItemStack[getSize()];
                int itemsAdded = 0;
                for (String items : getStringList("SLOTS")) {
                    items = TagManager.tag(player, npc, items);
                    String[] itemsInLine = items.split(" ");
                    for (String item : itemsInLine) {
                        Matcher m = Pattern.compile("(\\[)(.*)(\\])").matcher(item);
                        if (!m.matches()) {
                            dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item.");
                            return null;
                        }
                        if (contains("DEFINITIONS." + m.group(2)) &&
                                dItem.matches(getString("DEFINITIONS." + m.group(2)))) {
                            finalItems[itemsAdded] = dItem.valueOf(TagManager.tag
                                    (player, npc, getString("DEFINITIONS." + m.group(2))))
                                        .getItemStack();
                        }
                        else if (dItem.matches(m.group(2))) {
                            finalItems[itemsAdded] = dItem.valueOf(TagManager.tag(player, npc, m.group(2)))
                                    .getItemStack();
                        }
                        else {
                            finalItems[itemsAdded] = new ItemStack(Material.AIR);
                            if (!m.group(2).trim().isEmpty()) {
                                dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item: ["
                                        + m.group(2) + "]... Ignoring it and assuming \"AIR\"");
                            }
                        }
                        itemsAdded++;
                    }
                }
                if (inventory == null) {
                    int size = finalItems.length%9==0?finalItems.length:Math.round(finalItems.length/9)*9;
                    inventory = new dInventory(size==0?9:size,
                            contains("TITLE") ? TagManager.tag(player, npc, getString("TITLE")) : "Chest");
                }
                inventory.setContents(finalItems);
            }
        }
        catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this inventory script!");
            dB.echoError(e);
            inventory = null;
        }

        return inventory;

    }

}

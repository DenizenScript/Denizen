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
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryScriptContainer extends ScriptContainer {
    
    public InventoryScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
    
    public Map<String, dItem> definitions = new HashMap<String, dItem>();
    
    public int getSize() {
        InventoryType invType = getInventoryType();
        Integer size = Integer.valueOf(getString("size"));
        
        if (size > 0) {
            if (invType == InventoryType.CHEST)
                return Math.round(size/9)*9;
        }
        
        return invType.getDefaultSize();
    }
    
    public InventoryType getInventoryType() {
        String typeStr = getString("inventory", "chest");
        
        try {
            InventoryType type = InventoryType.valueOf(typeStr);
            return type;
            
        } catch(Exception e) {
            return InventoryType.CHEST;
        }
    }
    
    public dInventory getInventoryFrom() {
        return getInventoryFrom(null, null);
    }
    
    public dInventory getInventoryFrom(dPlayer player, dNPC npc) {
        
        dInventory inventory = null;
        
        try {
            if (contains("SIZE")) {
                inventory = new dInventory(aH.getIntegerFrom(getString("SIZE")), "script", getName());
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
                        if (contains("DEFINITIONS." + m.group(2)) && dItem.matches(getString("DEFINITIONS." + m.group(2)))) {
                            finalItems[itemsAdded] = dItem.valueOf(getString("DEFINITIONS." + m.group(2))).getItemStack();
                        }
                        else if (dItem.matches(m.group(2))) {
                            finalItems[itemsAdded] = dItem.valueOf(m.group(2)).getItemStack();
                        }
                        else {
                            finalItems[itemsAdded] = new ItemStack(Material.AIR);
                            if (!m.group(2).trim().isEmpty()) {
                                dB.echoError("Inventory script \"" + getName() + "\" has an invalid slot item: [" + m.group(2) 
                                        + "]... Ignoring it and assuming \"AIR\"");
                            }
                        }
                        itemsAdded++;
                    }
                }
                inventory.setContents(finalItems);
            }
        } 
        catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this inventory script!");
            if (!dB.showStackTraces)
                dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
            else e.printStackTrace();
            inventory = null;
        }
        
        return inventory;
        
    }
    
}

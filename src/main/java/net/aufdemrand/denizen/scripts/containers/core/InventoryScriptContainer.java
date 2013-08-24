package net.aufdemrand.denizen.scripts.containers.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

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
            if (contains("DEFINITIONS")) {
                // TODO: Figure out when to load definitions
            }
            
            if (contains("SLOTS")) {
                for (String items : getStringList("SLOTS")) {
                    items = TagManager.tag(player, npc, items);
                    // TODO: Find a possible format for this
                }
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

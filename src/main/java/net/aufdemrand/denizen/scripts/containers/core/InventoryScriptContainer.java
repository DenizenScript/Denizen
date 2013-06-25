package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

public class InventoryScriptContainer extends ScriptContainer {
    
    public InventoryScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
    
    public int getSize() {
        InventoryType invType = getInventoryType();
        
        if(invType == InventoryType.CHEST) {
            return Integer.parseInt(getString("size", "27"));
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
    
}

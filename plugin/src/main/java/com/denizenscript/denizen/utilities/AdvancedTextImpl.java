package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdvancedTextImpl {

    public static AdvancedTextImpl instance = new AdvancedTextImpl();

    public Inventory createInventory(InventoryHolder holder, int slots, String title) {
        return Bukkit.getServer().createInventory(holder, slots, title);
    }

    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, type, title);
    }

    public String getTitle(Inventory inventory) {
        return NMSHandler.getInstance().getTitle(inventory);
    }

    public void setCustomName(Entity entity, String name) {
        entity.setCustomName(name);
    }

    public String getCustomName(Entity entity) {
        return entity.getCustomName();
    }
}

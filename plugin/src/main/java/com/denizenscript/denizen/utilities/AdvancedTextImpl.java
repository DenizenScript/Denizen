package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

    public String parseComponent(Object input, ChatColor baseColor) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            return (String) input;
        }
        else if (input instanceof BaseComponent[]) {
            return FormattedTextHelper.stringify((BaseComponent[]) input, baseColor);
        }
        else if (input instanceof BaseComponent) {
            return FormattedTextHelper.stringify((BaseComponent) input);
        }
        else {
            return input.toString();
        }
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

    public void setPlayerListName(Player player, String name) {
        player.setPlayerListName(name);
    }

    public String getPlayerListName(Player player) {
        return player.getPlayerListName();
    }

    public String[] getSignLines(Sign sign) {
        return sign.getLines();
    }

    public void setSignLine(Sign sign, int line, String text) {
        sign.setLine(line, text == null ? "" : text);
    }

    public void sendResourcePack(Player player, String url, String hash, boolean forced, String prompt) {
        byte[] hashData = new byte[20];
        for (int i = 0; i < 20; i++) {
            hashData[i] = (byte) Integer.parseInt(hash.substring(i * 2, i * 2 + 2), 16);
        }
        player.setResourcePack(url, hashData);
    }

    public void sendSignUpdate(Player player, Location loc, String[] text) {
        player.sendSignChange(loc, text);
    }
}

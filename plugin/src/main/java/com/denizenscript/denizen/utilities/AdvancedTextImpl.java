package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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
        return NMSHandler.instance.getTitle(inventory);
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

    public String getCustomName(Nameable object) {
        return object.getCustomName();
    }

    public void setCustomName(Nameable object, String name) {
        object.setCustomName(name);
    }

    public void sendConsoleMessage(CommandSender sender, String text) {
        sender.spigot().sendMessage(FormattedTextHelper.parse(text, net.md_5.bungee.api.ChatColor.WHITE));
    }

    public InventoryView openAnvil(Player player, Location loc) {
        throw new UnsupportedOperationException();
    }

    public void teleportPlayerRelative(Player player, Location loc) {
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void registerBrewingRecipe(String keyName, ItemStack result, ItemStack[] inputItem, boolean inputExact, ItemStack[] ingredientItem, boolean ingredientExact) {
        throw new UnsupportedOperationException();
    }

    public void clearBrewingRecipes() {
        throw new UnsupportedOperationException();
    }

    public boolean isDenizenMix(ItemStack currInput, ItemStack ingredient) {
        return false;
    }
}

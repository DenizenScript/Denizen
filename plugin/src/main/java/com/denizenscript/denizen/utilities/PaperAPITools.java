package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Consumer;

import java.util.List;

public class PaperAPITools {

    public static PaperAPITools instance = new PaperAPITools();

    public Inventory createInventory(InventoryHolder holder, int slots, String title) {
        return Bukkit.getServer().createInventory(holder, slots, title);
    }

    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, type, title);
    }

    public String parseComponent(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            return (String) input;
        }
        else if (input instanceof BaseComponent[]) {
            return FormattedTextHelper.stringify((BaseComponent[]) input);
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

    public void teleport(Entity entity, Location loc, PlayerTeleportEvent.TeleportCause cause, List<TeleportCommand.EntityState> entityTeleportFlags, List<TeleportCommand.Relative> relativeTeleportFlags) {
        entity.teleport(loc, cause);
    }

    public void registerBrewingRecipe(String keyName, ItemStack result, ItemStack[] inputItem, boolean inputExact, ItemStack[] ingredientItem, boolean ingredientExact) {
        throw new UnsupportedOperationException();
    }

    public void clearBrewingRecipes() {
    }

    public boolean isDenizenMix(ItemStack currInput, ItemStack ingredient) {
        return false;
    }

    public String getDeathMessage(PlayerDeathEvent event) {
        return event.getDeathMessage();
    }

    public void setDeathMessage(PlayerDeathEvent event, String message) {
        event.setDeathMessage(message);
    }

    public void setSkin(Player player, String name) {
        NMSHandler.instance.getProfileEditor().setPlayerSkin(player, name);
    }

    public void setSkinBlob(Player player, String blob) {
        NMSHandler.instance.getProfileEditor().setPlayerSkinBlob(player, blob);
    }

    public <T extends Entity> T spawnEntity(Location location, Class<T> type, Consumer<T> configure, CreatureSpawnEvent.SpawnReason reason) {
        return location.getWorld().spawn(location, type, configure);
    }

    public void setTeamPrefix(Team team, String prefix) {
        team.setPrefix(prefix);
    }

    public void setTeamSuffix(Team team, String suffix) {
        team.setSuffix(suffix);
    }

    public String getTeamPrefix(Team team) {
        return team.getPrefix();
    }

    public String getTeamSuffix(Team team) {
        return team.getSuffix();
    }

    public String convertTextToMiniMessage(String text, boolean splitNewlines) {
        return text;
    }

    public Merchant createMerchant(String title) {
        return Bukkit.createMerchant(title);
    }

    public String getText(TextDisplay textDisplay) {
        String text = textDisplay.getText();
        return text != null ? text : "";
    }

    public void setText(TextDisplay textDisplay, String text) {
        textDisplay.setText(text);
    }

    public void kickPlayer(Player player, String message) {
        player.kickPlayer(message);
    }
}

package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.AdvancedTextImpl;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PaperAdvancedTextImpl extends AdvancedTextImpl {

    @Override
    public Inventory createInventory(InventoryHolder holder, int slots, String title) {
        return Bukkit.getServer().createInventory(holder, slots, PaperModule.parseFormattedText(title, ChatColor.BLACK));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, type, PaperModule.parseFormattedText(title, ChatColor.BLACK));
    }

    @Override
    public String getTitle(Inventory inventory) {
        // TODO: Paper lacks an inventory.getTitle? 0.o
        return NMSHandler.getInstance().getTitle(inventory);
    }

    @Override
    public void setCustomName(Entity entity, String name) {
        entity.customName(PaperModule.parseFormattedText(name, ChatColor.WHITE));
    }

    @Override
    public String getCustomName(Entity entity) {
        return PaperModule.stringifyComponent(entity.customName(), ChatColor.WHITE);
    }

    @Override
    public void setPlayerListName(Player player, String name) {
        player.playerListName(PaperModule.parseFormattedText(name, ChatColor.WHITE));
    }

    @Override
    public String getPlayerListName(Player player) {
        return PaperModule.stringifyComponent(player.playerListName(), ChatColor.WHITE);
    }

    @Override
    public String[] getSignLines(Sign sign) {
        String[] output = new String[4];
        int i = 0;
        for (Component component : sign.lines()) {
            output[i++] = PaperModule.stringifyComponent(component, ChatColor.BLACK);
        }
        return output;
    }

    @Override
    public void setSignLine(Sign sign, int line, String text) {
        sign.line(line, PaperModule.parseFormattedText(text, ChatColor.BLACK));
    }

    @Override
    public void sendResourcePack(Player player, String url, String hash, boolean forced, String prompt) {
        player.setResourcePack(url, hash == null ? null : CoreUtilities.toLowerCase(hash), forced, prompt == null ? null : PaperModule.parseFormattedText(prompt, ChatColor.WHITE));
    }
}

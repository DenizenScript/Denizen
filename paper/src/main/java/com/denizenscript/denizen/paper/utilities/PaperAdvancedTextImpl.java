package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.AdvancedTextImpl;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.entity.RelativeTeleportFlag;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.potion.PotionBrewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    public String parseComponent(Object input, ChatColor baseColor) {
        if (input == null) {
            return null;
        }
        if (input instanceof Component) {
            return PaperModule.stringifyComponent((Component) input, baseColor);
        }
        return super.parseComponent(input, baseColor);
    }

    @Override
    public String getTitle(Inventory inventory) {
        // TODO: Paper lacks an inventory.getTitle? 0.o
        return NMSHandler.instance.getTitle(inventory);
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
        sign.line(line, PaperModule.parseFormattedText(text == null ? "" : text, ChatColor.BLACK));
    }

    @Override
    public void sendResourcePack(Player player, String url, String hash, boolean forced, String prompt) {
        if (prompt == null && !forced) {
            super.sendResourcePack(player, url, hash, false, null);
        }
        else {
            player.setResourcePack(url, CoreUtilities.toLowerCase(hash), forced, PaperModule.parseFormattedText(prompt, ChatColor.WHITE));
        }
    }

    @Override
    public void sendSignUpdate(Player player, Location loc, String[] text) {
        List<Component> components = new ArrayList<>();
        for (String line : text) {
            components.add(PaperModule.parseFormattedText(line, ChatColor.BLACK));
        }
        player.sendSignChange(loc, components);
    }

    @Override
    public String getCustomName(Nameable object) {
        return PaperModule.stringifyComponent(object.customName(), ChatColor.BLACK);
    }

    @Override
    public void setCustomName(Nameable object, String name) {
        object.customName(PaperModule.parseFormattedText(name, ChatColor.BLACK));
    }

    @Override
    public void sendConsoleMessage(CommandSender sender, String text) {
        sender.sendMessage(PaperModule.parseFormattedText(text, ChatColor.WHITE));
    }

    @Override
    public InventoryView openAnvil(Player player, Location loc) {
        return player.openAnvil(loc, true);
    }

    @Override
    public void teleportPlayerRelative(Player player, Location loc) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, true, false, RelativeTeleportFlag.values());
        }
        else {
            super.teleportPlayerRelative(player, loc);
        }
    }

    public static HashMap<NamespacedKey, PotionMix> potionMixes = new HashMap<>();

    @Override
    public void registerBrewingRecipe(String keyName, ItemStack result, ItemStack[] inputItem, boolean inputExact, ItemStack[] ingredientItem, boolean ingredientExact){
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)){
            throw new UnsupportedOperationException();
        }
        NamespacedKey key = new NamespacedKey(Denizen.getInstance(), keyName);
        RecipeChoice inputChoice = itemArrayToChoice(inputItem, inputExact);
        RecipeChoice ingredientChoice = itemArrayToChoice(ingredientItem, ingredientExact);
        PotionMix mix = new PotionMix(key, result, inputChoice, ingredientChoice);
        potionMixes.put(key, mix);
        Bukkit.getPotionBrewer().addPotionMix(mix);
    }

    @Override
    public void clearBrewingRecipes(){
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)){
            return;
        }
        PotionBrewer brewer = Bukkit.getPotionBrewer();
        for (NamespacedKey mix : potionMixes.keySet()){
            brewer.removePotionMix(mix);
            potionMixes.remove(mix);
        }
    }

    public static RecipeChoice itemArrayToChoice (ItemStack[] item, boolean exact){
        if (exact){
            return new RecipeChoice.ExactChoice(item);
        }
        Material[] mats = new Material[item.length];
        for (int i = 0; i < item.length; i++) {
            mats[i] = item[i].getType();
        }
        return new RecipeChoice.MaterialChoice(mats);
    }

    @Override
    public boolean isDenizenMix(ItemStack currInput, ItemStack ingredient){
        for(PotionMix mix : potionMixes.values()){
            if(mix.getInput().getItemStack().isSimilar(currInput) && mix.getIngredient().getItemStack().isSimilar(ingredient)){
                return true;
            }
        }
        return false;
    }
}

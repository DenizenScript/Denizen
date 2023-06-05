package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
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
import org.bukkit.potion.PotionBrewer;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Consumer;

import java.util.*;
import java.util.stream.Collectors;

public class PaperAPIToolsImpl extends PaperAPITools {

    @Override
    public Inventory createInventory(InventoryHolder holder, int slots, String title) {
        return Bukkit.getServer().createInventory(holder, slots, PaperModule.parseFormattedText(title, ChatColor.BLACK));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, type, PaperModule.parseFormattedText(title, ChatColor.BLACK));
    }

    @Override
    public String parseComponent(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Component) {
            return PaperModule.stringifyComponent((Component) input);
        }
        return super.parseComponent(input);
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
        return PaperModule.stringifyComponent(entity.customName());
    }

    @Override
    public void setPlayerListName(Player player, String name) {
        player.playerListName(PaperModule.parseFormattedText(name, ChatColor.WHITE));
    }

    @Override
    public String getPlayerListName(Player player) {
        return PaperModule.stringifyComponent(player.playerListName());
    }

    @Override
    public String[] getSignLines(Sign sign) {
        String[] output = new String[4];
        int i = 0;
        for (Component component : sign.lines()) {
            output[i++] = PaperModule.stringifyComponent(component);
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
        return PaperModule.stringifyComponent(object.customName());
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
    public void teleport(Entity entity, Location loc, PlayerTeleportEvent.TeleportCause cause, List<TeleportCommand.EntityState> entityTeleportFlags, List<TeleportCommand.Relative> relativeTeleportFlags) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            super.teleport(entity, loc, cause, null, null);
        }
        List<TeleportFlag> teleportFlags = new ArrayList<>();
        if (entityTeleportFlags != null) {
            for (TeleportCommand.EntityState entityTeleportFlag : entityTeleportFlags) {
                teleportFlags.add(TeleportFlag.EntityState.values()[entityTeleportFlag.ordinal()]);
            }
        }
        if (relativeTeleportFlags != null) {
            for (TeleportCommand.Relative relativeTeleportFlag : relativeTeleportFlags) {
                teleportFlags.add(TeleportFlag.Relative.values()[relativeTeleportFlag.ordinal()]);
            }
        }
        entity.teleport(loc, cause, teleportFlags.toArray(new TeleportFlag[0]));
    }

    public static HashMap<NamespacedKey, PotionMix> potionMixes = new HashMap<>();

    @Override
    public void registerBrewingRecipe(String keyName, ItemStack result, ItemStack[] inputItem, boolean inputExact, ItemStack[] ingredientItem, boolean ingredientExact) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
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
    public void clearBrewingRecipes() {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            return;
        }
        PotionBrewer brewer = Bukkit.getPotionBrewer();
        for (NamespacedKey mix : new ArrayList<>(potionMixes.keySet())) {
            brewer.removePotionMix(mix);
            potionMixes.remove(mix);
        }
    }

    public static RecipeChoice itemArrayToChoice(ItemStack[] item, boolean exact) {
        if (exact) {
            return new RecipeChoice.ExactChoice(item);
        }
        Material[] mats = new Material[item.length];
        for (int i = 0; i < item.length; i++) {
            mats[i] = item[i].getType();
        }
        return new RecipeChoice.MaterialChoice(mats);
    }

    @Override
    public boolean isDenizenMix(ItemStack currInput, ItemStack ingredient) {
        for (PotionMix mix : potionMixes.values()) {
            if (mix.getInput().getItemStack().isSimilar(currInput) && mix.getIngredient().getItemStack().isSimilar(ingredient)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDeathMessage(PlayerDeathEvent event) {
        return PaperModule.stringifyComponent(event.deathMessage());
    }

    @Override
    public void setDeathMessage(PlayerDeathEvent event, String message) {
        event.deathMessage(PaperModule.parseFormattedText(message, ChatColor.WHITE));
    }

    public Set<UUID> modifiedTextures = new HashSet<>();

    @Override
    public void setSkin(Player player, String name) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
            NMSHandler.instance.getProfileEditor().setPlayerSkin(player, name);
            return;
        }
        // Note: this API is present on all supported versions, but currently used for 1.19+ only
        PlayerProfile skinProfile = Bukkit.createProfile(name);
        boolean isOwnName = CoreUtilities.equalsIgnoreCase(player.getName(), name);
        if (isOwnName && modifiedTextures.contains(player.getUniqueId())) {
            skinProfile.removeProperty("textures");
        }
        Bukkit.getScheduler().runTaskAsynchronously(Denizen.instance, () -> {
            if (!skinProfile.complete()) {
                return;
            }
            DenizenCore.runOnMainThread(() -> {
                PlayerProfile playerProfile = player.getPlayerProfile();
                playerProfile.setProperty(getProfileProperty(skinProfile, "textures"));
                player.setPlayerProfile(playerProfile);
                if (isOwnName) {
                    modifiedTextures.remove(player.getUniqueId());
                }
                else {
                    modifiedTextures.add(player.getUniqueId());
                }
            });
        });
    }

    @Override
    public void setSkinBlob(Player player, String blob) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
            NMSHandler.instance.getProfileEditor().setPlayerSkinBlob(player, blob);
            return;
        }
        // Note: this API is present on all supported versions, but currently used for 1.19+ only
        List<String> split = CoreUtilities.split(blob, ';');
        PlayerProfile playerProfile = player.getPlayerProfile();
        ProfileProperty currentTextures = getProfileProperty(playerProfile, "textures");
        String value = split.get(0);
        String signature = split.size() > 1 ? split.get(1) : null;
        if (!value.equals(currentTextures.getValue()) && (signature == null || !signature.equals(currentTextures.getSignature()))) {
            modifiedTextures.add(player.getUniqueId());
        }
        playerProfile.setProperty(new ProfileProperty("textures", value, signature));
        player.setPlayerProfile(playerProfile);
    }

    public ProfileProperty getProfileProperty(PlayerProfile profile, String name) {
        for (ProfileProperty property : profile.getProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public <T extends Entity> T spawnEntity(Location location, Class<T> type, Consumer<T> configure, CreatureSpawnEvent.SpawnReason reason) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            return location.getWorld().spawn(location, type, configure, reason);
        }
        else {
            return super.spawnEntity(location, type, configure, reason);
        }
    }

    @Override
    public void setTeamPrefix(Team team, String prefix) {
        team.prefix(PaperModule.parseFormattedText(prefix, ChatColor.WHITE));
    }

    @Override
    public void setTeamSuffix(Team team, String suffix) {
        team.suffix(PaperModule.parseFormattedText(suffix, ChatColor.WHITE));
    }

    @Override
    public String getTeamPrefix(Team team) {
        return PaperModule.stringifyComponent(team.prefix());
    }

    @Override
    public String getTeamSuffix(Team team) {
        return PaperModule.stringifyComponent(team.suffix());
    }

    @Override
    public String convertTextToMiniMessage(String text, boolean splitNewlines) {
        if (splitNewlines) {
            List<String> lines = CoreUtilities.split(text, '\n');
            return lines.stream().map(l -> convertTextToMiniMessage(l, false)).collect(Collectors.joining("\n"));
        }
        Component parsed = PaperModule.jsonToComponent(FormattedTextHelper.componentToJson(FormattedTextHelper.parse(text, ChatColor.WHITE, false)));
        return MiniMessage.miniMessage().serialize(parsed);
    }

    @Override
    public Merchant createMerchant(String title) {
        return Bukkit.createMerchant(PaperModule.parseFormattedText(title, ChatColor.BLACK));
    }

    @Override
    public String getText(TextDisplay textDisplay) {
        return PaperModule.stringifyComponent(textDisplay.text());
    }

    @Override
    public void setText(TextDisplay textDisplay, String text) {
        textDisplay.text(PaperModule.parseFormattedText(text, ChatColor.WHITE));
    }

    @Override
    public void kickPlayer(Player player, String message) {
        player.kick(PaperModule.parseFormattedText(message, ChatColor.WHITE));
    }
}

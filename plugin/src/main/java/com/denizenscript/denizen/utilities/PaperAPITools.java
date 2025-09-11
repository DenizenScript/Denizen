package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Consumer;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

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
        if (input instanceof String str) {
            return str;
        }
        else if (input instanceof BaseComponent[] components) {
            return components.length == 1 ? components[0].toLegacyText() : new TextComponent(components).toLegacyText();
        }
        else if (input instanceof BaseComponent component) {
            return component.toLegacyText();
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

    public InventoryView openAnvil(Player player, Location loc) {
        throw new UnsupportedOperationException();
    }

    public void teleport(Entity entity, Location loc, PlayerTeleportEvent.TeleportCause cause, List<TeleportCommand.EntityState> entityTeleportFlags, List<TeleportCommand.Relative> relativeTeleportFlags) {
        entity.teleport(loc, cause);
    }

    public void registerBrewingRecipe(String keyName, ItemStack result, String input, String ingredient, ItemScriptContainer itemScriptContainer) {
        throw new UnsupportedOperationException();
    }

    public void clearBrewingRecipes() {
    }

    public String getBrewingRecipeInputMatcher(NamespacedKey recipeId) {
        return null;
    }

    public String getBrewingRecipeIngredientMatcher(NamespacedKey recipeId) {
        return null;
    }

    public RecipeChoice createPredicateRecipeChoice(Predicate<ItemStack> predicate) {
        throw new UnsupportedOperationException();
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

    public static MethodHandle WORLD_SPAWN_BUKKIT_CONSUMER = null;

    // TODO once 1.20 is the minimum supported version, use the modern java.util.Consumer
    public <T extends Entity> T spawnEntity(Location location, Class<T> type, Consumer<T> configure, CreatureSpawnEvent.SpawnReason reason) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            // Takes the deprecated bukkit consumer on older versions
            if (WORLD_SPAWN_BUKKIT_CONSUMER == null) {
                WORLD_SPAWN_BUKKIT_CONSUMER = ReflectionHelper.getMethodHandle(RegionAccessor.class, "spawn", Location.class, Class.class, Consumer.class);
            }
            try {
                return (T) WORLD_SPAWN_BUKKIT_CONSUMER.invoke(location.getWorld(), location, type, configure);
            }
            catch (Throwable e) {
                Debug.echoError(e);
                return null;
            }
        }
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

    public String getClientBrand(Player player) {
        NetworkInterceptHelper.enable();
        return NMSHandler.playerHelper.getClientBrand(player);
    }

    public boolean canUseEquipmentSlot(LivingEntity entity, EquipmentSlot slot) {
        return true;
    }

    // TODO workaround Paper issue - https://github.com/PaperMC/Paper/issues/11732
    public boolean hasCustomName(PotionMeta meta) {
        return meta.hasCustomName();
    }

    public void setMaterialTags(Material type, Set<NamespacedKey> tags) {
        NMSHandler.blockHelper.setVanillaTags(type, tags);
    }

    public String getPage(BookMeta meta, int page) {
        return meta.getPage(page);
    }

    public ListTag getPages(BookMeta meta) {
        return new ListTag(meta.getPages());
    }

    public void addPage(BookMeta meta, String page) {
        meta.addPage(page);
    }

    public void setPages(BookMeta meta, List<String> pages) {
        meta.setPages(pages);
    }

    public void setJsonPages(BookMeta meta, List<String> jsonPages) {
        List<BaseComponent[]> parsedPages = new ArrayList<>(jsonPages.size());
        for (String jsonPage : jsonPages) {
            parsedPages.add(jsonToBungee(jsonPage));
        }
        meta.spigot().setPages(parsedPages);
    }

    public void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(text);
    }

    public void sendMessage(CommandSender sender, String text, UUID senderId) {
        sender.sendMessage(senderId, text);
    }

    public void broadcast(String text, Predicate<Player> filter) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (filter == null || filter.test(player)) {
                player.sendMessage(text);
            }
        }
    }

    public void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(text));
    }

    public enum BaseColor { WHITE, BLACK, GRAY }

    public String parseTextToJson(String formattedText, BaseColor baseColor) {
        TextComponent textComponent = new TextComponent(formattedText);
        textComponent.setBold(false);
        textComponent.setItalic(false);
        textComponent.setStrikethrough(false);
        textComponent.setUnderlined(false);
        textComponent.setObfuscated(false);
        textComponent.setColor(switch (baseColor) {
            case WHITE -> ChatColor.WHITE;
            case BLACK -> ChatColor.BLACK;
            case GRAY -> ChatColor.GRAY;
        });
        TextComponent base = new TextComponent();
        base.addExtra(textComponent);
        return bungeeToJson(base);
    }

    public String parseJsonToText(String json) {
        BaseComponent[] components = jsonToBungee(json);
        return components.length == 1 ? components[0].toLegacyText() : new TextComponent(components).toLegacyText();
    }

    protected String bungeeToJson(BaseComponent component) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_20)) {
            return ComponentSerializer.toString(component);
        }
        return VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5).toString(component);
    }

    protected BaseComponent[] jsonToBungee(String json) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_20)) {
            return ComponentSerializer.parse(json);
        }
        return VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5).parse(json);
    }
}

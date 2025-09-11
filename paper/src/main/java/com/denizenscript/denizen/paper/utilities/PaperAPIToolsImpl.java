package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.potion.PotionBrewer;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Consumer;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PaperAPIToolsImpl extends PaperAPITools {

    @Override
    public Inventory createInventory(InventoryHolder holder, int slots, String title) {
        return Bukkit.getServer().createInventory(holder, slots, FormattedTextHelper.parse(title, NamedTextColor.BLACK));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.getServer().createInventory(holder, type, FormattedTextHelper.parse(title, NamedTextColor.BLACK));
    }

    @Override
    public String parseComponent(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Component component) {
            return FormattedTextHelper.stringify(component);
        }
        else if (input instanceof BaseComponent[] components) {
            return FormattedTextHelper.stringify(PaperModule.jsonToComponent(bungeeToJson(components.length == 1 ? components[0] : new TextComponent(components))));
        }
        else if (input instanceof BaseComponent component) {
            return FormattedTextHelper.stringify(PaperModule.jsonToComponent(bungeeToJson(component)));
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
        entity.customName(FormattedTextHelper.parse(name, NamedTextColor.WHITE));
    }

    @Override
    public String getCustomName(Entity entity) {
        return FormattedTextHelper.stringify(entity.customName());
    }

    @Override
    public void setPlayerListName(Player player, String name) {
        player.playerListName(FormattedTextHelper.parse(name, NamedTextColor.WHITE));
    }

    @Override
    public String getPlayerListName(Player player) {
        return FormattedTextHelper.stringify(player.playerListName());
    }

    @Override
    public String[] getSignLines(Sign sign) {
        String[] output = new String[4];
        int i = 0;
        for (Component component : sign.lines()) {
            output[i++] = FormattedTextHelper.stringify(component);
        }
        return output;
    }

    @Override
    public void setSignLine(Sign sign, int line, String text) {
        sign.line(line, FormattedTextHelper.parse(text == null ? "" : text, NamedTextColor.BLACK));
    }

    @Override
    public void sendResourcePack(Player player, String url, String hash, boolean forced, String prompt) {
        if (prompt == null && !forced) {
            super.sendResourcePack(player, url, hash, false, null);
        }
        else {
            player.setResourcePack(url, CoreUtilities.toLowerCase(hash), forced, FormattedTextHelper.parse(prompt, NamedTextColor.WHITE));
        }
    }

    @Override
    public void sendSignUpdate(Player player, Location loc, String[] text) {
        List<Component> components = new ArrayList<>();
        for (String line : text) {
            components.add(FormattedTextHelper.parse(line, NamedTextColor.BLACK));
        }
        player.sendSignChange(loc, components);
    }

    @Override
    public String getCustomName(Nameable object) {
        return FormattedTextHelper.stringify(object.customName());
    }

    @Override
    public void setCustomName(Nameable object, String name) {
        object.customName(FormattedTextHelper.parse(name, NamedTextColor.BLACK));
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
            // TODO: MC 1.21.3: Paper updated this API to work differently due to underlying Minecraft changes.
            for (TeleportCommand.Relative relativeTeleportFlag : relativeTeleportFlags) {
                teleportFlags.add(new ElementTag(relativeTeleportFlag.name()).asEnum(TeleportFlag.Relative.class));
            }
        }
        entity.teleport(loc, cause, teleportFlags.toArray(new TeleportFlag[0]));
    }

    record BrewingRecipeMatchers(String inputMatcher, String ingredientMatcher) {}
    public static final Map<NamespacedKey, BrewingRecipeMatchers> potionMixes = new HashMap<>();

    @Override
    public void registerBrewingRecipe(String keyName, ItemStack result, String input, String ingredient, ItemScriptContainer itemScriptContainer) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            throw new UnsupportedOperationException();
        }
        TagContext context = DenizenCore.implementation.getTagContext(itemScriptContainer);
        RecipeChoice inputChoice = parseBrewingRecipeChoice(itemScriptContainer, input, context);
        if (inputChoice == null) {
            return;
        }
        RecipeChoice ingredientChoice = parseBrewingRecipeChoice(itemScriptContainer, ingredient, context);
        if (ingredientChoice == null) {
            return;
        }
        NamespacedKey key = new NamespacedKey(Denizen.getInstance(), keyName);
        potionMixes.put(key, new BrewingRecipeMatchers(input.startsWith("matcher:") ? input : null, ingredient.startsWith("matcher:") ? ingredient : null));
        Bukkit.getPotionBrewer().addPotionMix(new PotionMix(key, result, inputChoice, ingredientChoice));
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

    public static RecipeChoice parseBrewingRecipeChoice(ItemScriptContainer container, String choice, TagContext context) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && choice.startsWith("matcher:")) {
            String matcher = choice.substring("matcher:".length());
            return PotionMix.createPredicateChoice(item -> new ItemTag(item).tryAdvancedMatcher(matcher, context));
        }
        boolean exact = true;
        if (choice.startsWith("material:")) {
            choice = choice.substring("material:".length());
            exact = false;
        }
        ItemStack[] items = ItemScriptHelper.textToItemArray(container, choice, exact);
        if (items == null) {
            return null;
        }
        if (exact) {
            return new RecipeChoice.ExactChoice(items);
        }
        Material[] mats = new Material[items.length];
        for (int i = 0; i < items.length; i++) {
            mats[i] = items[i].getType();
        }
        return new RecipeChoice.MaterialChoice(mats);
    }

    @Override
    public String getBrewingRecipeInputMatcher(NamespacedKey recipeId) {
        return potionMixes.get(recipeId).inputMatcher();
    }

    @Override
    public String getBrewingRecipeIngredientMatcher(NamespacedKey recipeId) {
        return potionMixes.get(recipeId).ingredientMatcher();
    }

    @Override
    public RecipeChoice createPredicateRecipeChoice(Predicate<ItemStack> predicate) {
        return PotionMix.createPredicateChoice(predicate);
    }

    @Override
    public String getDeathMessage(PlayerDeathEvent event) {
        return FormattedTextHelper.stringify(event.deathMessage());
    }

    @Override
    public void setDeathMessage(PlayerDeathEvent event, String message) {
        event.deathMessage(FormattedTextHelper.parse(message, NamedTextColor.WHITE));
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
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            // Takes the deprecated bukkit consumer on older versions
            if (WORLD_SPAWN_BUKKIT_CONSUMER == null) {
                WORLD_SPAWN_BUKKIT_CONSUMER = ReflectionHelper.getMethodHandle(RegionAccessor.class, "spawn", Location.class, Class.class, Consumer.class, CreatureSpawnEvent.SpawnReason.class);
            }
            try {
                return (T) WORLD_SPAWN_BUKKIT_CONSUMER.invoke(location.getWorld(), location, type, configure, reason);
            }
            catch (Throwable e) {
                Debug.echoError(e);
                return null;
            }
        }
        return location.getWorld().spawn(location, type, configure, reason);
    }

    @Override
    public void setTeamPrefix(Team team, String prefix) {
        team.prefix(FormattedTextHelper.parse(prefix, NamedTextColor.WHITE));
    }

    @Override
    public void setTeamSuffix(Team team, String suffix) {
        team.suffix(FormattedTextHelper.parse(suffix, NamedTextColor.WHITE));
    }

    @Override
    public String getTeamPrefix(Team team) {
        return FormattedTextHelper.stringify(team.prefix());
    }

    @Override
    public String getTeamSuffix(Team team) {
        return FormattedTextHelper.stringify(team.suffix());
    }

    @Override
    public String convertTextToMiniMessage(String text, boolean splitNewlines) {
        if (splitNewlines) {
            List<String> lines = CoreUtilities.split(text, '\n');
            return lines.stream().map(l -> convertTextToMiniMessage(l, false)).collect(Collectors.joining("\n"));
        }
        return MiniMessage.miniMessage().serialize(FormattedTextHelper.parse(text, NamedTextColor.WHITE, false));
    }

    @Override
    public Merchant createMerchant(String title) {
        return Bukkit.createMerchant(FormattedTextHelper.parse(title, NamedTextColor.BLACK));
    }

    @Override
    public String getText(TextDisplay textDisplay) {
        return FormattedTextHelper.stringify(textDisplay.text());
    }

    @Override
    public void setText(TextDisplay textDisplay, String text) {
        textDisplay.text(FormattedTextHelper.parse(text, NamedTextColor.WHITE));
    }

    @Override
    public void kickPlayer(Player player, String message) {
        player.kick(FormattedTextHelper.parse(message, NamedTextColor.WHITE));
    }

    @Override
    public String getClientBrand(Player player) {
        String clientBrand = player.getClientBrandName();
        return clientBrand != null ? clientBrand : "unknown";
    }

    @Override
    public boolean canUseEquipmentSlot(LivingEntity entity, EquipmentSlot slot) {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) ? entity.canUseEquipmentSlot(slot) : super.canUseEquipmentSlot(entity, slot);
    }

    @Override
    public boolean hasCustomName(PotionMeta meta) {
        return meta.hasCustomPotionName();
    }

    @Override
    public void setMaterialTags(Material type, Set<NamespacedKey> tags) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            super.setMaterialTags(type, tags);
            return;
        }
        BlockTagsSetter.INSTANCE.setTags(type, tags);
    }

    @Override
    public String getPage(BookMeta meta, int page) {
        return FormattedTextHelper.stringify(meta.page(page));
    }

    @Override
    public ListTag getPages(BookMeta meta) {
        return new ListTag(meta.pages(), page -> new ElementTag(FormattedTextHelper.stringify(page), true));
    }

    @Override
    public void addPage(BookMeta meta, String page) {
        meta.addPages(FormattedTextHelper.parse(page, NamedTextColor.BLACK));
    }

    @Override
    public void setPages(BookMeta meta, List<String> pages) {
        List<Component> parsedPages = new ArrayList<>(pages.size());
        for (String page : pages) {
            parsedPages.add(FormattedTextHelper.parse(page, NamedTextColor.BLACK));
        }
        meta.pages(parsedPages);
    }

    @Override
    public void setJsonPages(BookMeta meta, List<String> jsonPages) {
        List<Component> parsedPages = new ArrayList<>(jsonPages.size());
        for (String jsonPage : jsonPages) {
            parsedPages.add(PaperModule.jsonToComponent(jsonPage));
        }
        meta.pages(parsedPages);
    }

    @Override
    public void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(FormattedTextHelper.parse(text, NamedTextColor.WHITE));
    }

    @Override
    public void sendMessage(CommandSender sender, String text, UUID senderId) {
        sender.sendMessage(Identity.identity(senderId), FormattedTextHelper.parse(text, NamedTextColor.WHITE));
    }

    @Override
    public void broadcast(String text, Predicate<Player> filter) {
        Component message = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (filter == null || filter.test(player)) {
                if (message == null) {
                    message = FormattedTextHelper.parse(text, NamedTextColor.WHITE);
                }
                player.sendMessage(message);
            }
        }
    }

    @Override
    public void sendActionBar(Player player, String text) {
        player.sendActionBar(FormattedTextHelper.parse(text, NamedTextColor.WHITE));
    }

    @Override
    public String parseTextToJson(String formattedText, BaseColor baseColor) {
        return PaperModule.componentToJson(FormattedTextHelper.parse(formattedText, switch (baseColor) {
            case WHITE -> NamedTextColor.WHITE;
            case BLACK -> NamedTextColor.BLACK;
            case GRAY -> NamedTextColor.GRAY;
        }));
    }

    @Override
    public String parseJsonToText(String json) {
        return FormattedTextHelper.stringify(PaperModule.jsonToComponent(json));
    }
}

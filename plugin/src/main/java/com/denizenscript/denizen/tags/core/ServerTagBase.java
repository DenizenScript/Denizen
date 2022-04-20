package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.commands.server.BossBarCommand;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptHelper;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.events.core.TickScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.scripts.commands.core.SQLCommand;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class ServerTagBase {

    public ServerTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                serverTag(event);
            }
        }, "server");
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "global", (attribute) -> {
            Deprecations.globalTagName.warn(attribute.context);
            return null;
        });
    }

    public static final long serverStartTimeMillis = System.currentTimeMillis();

    public void serverTag(ReplaceableTagEvent event) {
        if (!event.matches("server") || event.replaced()) {
            return;
        }
        Attribute attribute = event.getAttributes().fulfill(1);

        if (attribute.startsWith("economy")) {
            if (Depends.economy == null) {
                attribute.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                return;
            }
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <server.economy.format[<#.#>]>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the amount of money, formatted according to the server's economy.
            // -->
            if (attribute.startsWith("format") && attribute.hasParam()) {
                double amount = attribute.getDoubleParam();
                event.setReplacedObject(new ElementTag(Depends.economy.format(amount))
                        .getObjectAttribute(attribute.fulfill(1)));
                return;
            }

            // <--[tag]
            // @attribute <server.economy.currency_name[<#.#>]>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (automatically singular or plural based on input value).
            // -->
            if (attribute.startsWith("currency_name") && attribute.hasParam()) {
                double amount = attribute.getDoubleParam();
                event.setReplacedObject(new ElementTag(amount == 1 ? Depends.economy.currencyNameSingular() : Depends.economy.currencyNamePlural())
                        .getObjectAttribute(attribute.fulfill(1)));
                return;
            }

            // <--[tag]
            // @attribute <server.economy.currency_plural>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (in the plural form, like "Dollars").
            // -->
            if (attribute.startsWith("currency_plural")) {
                event.setReplacedObject(new ElementTag(Depends.economy.currencyNamePlural())
                        .getObjectAttribute(attribute.fulfill(1)));
                return;
            }

            // <--[tag]
            // @attribute <server.economy.currency_singular>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (in the singular form, like "Dollar").
            // -->
            if (attribute.startsWith("currency_singular")) {
                event.setReplacedObject(new ElementTag(Depends.economy.currencyNameSingular())
                        .getObjectAttribute(attribute.fulfill(1)));
                return;
            }
            return;
        }

        // <--[tag]
        // @attribute <server.slot_id[<slot>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the slot ID number for an input slot (see <@link language Slot Inputs>).
        // -->
        if (attribute.startsWith("slot_id") && attribute.hasParam()) {
            int slotId = SlotHelper.nameToIndex(attribute.getParam(), null);
            if (slotId != -1) {
                event.setReplacedObject(new ElementTag(slotId).getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.parse_bukkit_item[<serial>]>
        // @returns ItemTag
        // @description
        // Returns the ItemTag resultant from parsing Bukkit item serialization data (under subkey "item").
        // -->
        if (attribute.startsWith("parse_bukkit_item") && attribute.hasParam()) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(attribute.getParam());
                ItemStack item = config.getItemStack("item");
                if (item != null) {
                    event.setReplacedObject(new ItemTag(item).getObjectAttribute(attribute.fulfill(1)));
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_ids[(<type>)]>
        // @returns ListTag
        // @description
        // Returns a list of all recipe IDs on the server.
        // Returns a list in the Namespace:Key format, for example "minecraft:gold_nugget".
        // Optionally, specify a recipe type (CRAFTING, FURNACE, COOKING, BLASTING, SHAPED, SHAPELESS, SMOKING, CAMPFIRE, STONECUTTING)
        // to limit to just recipes of that type.
        // Note: this will produce an error if all recipes of any one type have been removed from the server, due to an error in Spigot.
        // -->
        if (attribute.startsWith("recipe_ids") || attribute.startsWith("list_recipe_ids")) {
            listDeprecateWarn(attribute);
            String type = attribute.hasParam() ? CoreUtilities.toLowerCase(attribute.getParam()) : null;
            ListTag list = new ListTag();
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (Utilities.isRecipeOfType(recipe, type) && recipe instanceof Keyed) {
                    list.add(((Keyed) recipe).getKey().toString());
                }
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_items[<id>]>
        // @returns ListTag(ItemTag)
        // @description
        // Returns a list of the items used as input to the recipe within the input ID.
        // This is formatted equivalently to the item script recipe input, with "material:" for non-exact matches, and a full ItemTag for exact matches.
        // Note that this won't represent all recipes perfectly (primarily those with multiple input choices per slot).
        // For furnace-style recipes, this will return a list with only 1 item.
        // For shaped recipes, this will include 'air' for slots that are part of the shape but don't require an item.
        // -->
        if (attribute.startsWith("recipe_items") && attribute.hasParam()) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getParam());
            Recipe recipe = NMSHandler.itemHelper.getRecipeById(key);
            if (recipe == null) {
                return;
            }
            ListTag result = new ListTag();
            Consumer<RecipeChoice> addChoice = (choice) -> {
                if (choice == null) {
                    result.addObject(new ItemTag(Material.AIR));
                }
                else {
                    if (choice instanceof RecipeChoice.ExactChoice) {
                        result.addObject(new ItemTag(choice.getItemStack()));
                    }
                    else {
                        result.add("material:" + choice.getItemStack().getType().name());
                    }
                }
            };
            if (recipe instanceof ShapedRecipe) {
                for (String row : ((ShapedRecipe) recipe).getShape()) {
                    for (char column : row.toCharArray()) {
                        addChoice.accept(((ShapedRecipe) recipe).getChoiceMap().get(column));
                    }
                }
            }
            else if (recipe instanceof ShapelessRecipe) {
                for (RecipeChoice choice : ((ShapelessRecipe) recipe).getChoiceList()) {
                    addChoice.accept(choice);
                }
            }
            else if (recipe instanceof CookingRecipe<?>) {
                addChoice.accept(((CookingRecipe) recipe).getInputChoice());
            }
            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_shape[<id>]>
        // @returns ElementTag
        // @description
        // Returns the shape of a shaped recipe, like '2x2' or '3x3'.
        // -->
        if (attribute.startsWith("recipe_shape") && attribute.hasParam()) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getParam());
            Recipe recipe = NMSHandler.itemHelper.getRecipeById(key);
            if (!(recipe instanceof ShapedRecipe)) {
                return;
            }
            String[] shape = ((ShapedRecipe) recipe).getShape();
            event.setReplacedObject(new ElementTag(shape[0].length() + "x" + shape.length).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_type[<id>]>
        // @returns ElementTag
        // @description
        // Returns the type of recipe that the given recipe ID is.
        // Will be one of FURNACE, BLASTING, SHAPED, SHAPELESS, SMOKING, CAMPFIRE, STONECUTTING.
        // -->
        if (attribute.startsWith("recipe_type") && attribute.hasParam()) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getParam());
            Recipe recipe = NMSHandler.itemHelper.getRecipeById(key);
            if (recipe == null) {
                return;
            }
            event.setReplacedObject(new ElementTag(Utilities.getRecipeType(recipe)).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_result[<id>]>
        // @returns ItemTag
        // @description
        // Returns the item that a recipe will create when crafted.
        // -->
        if (attribute.startsWith("recipe_result") && attribute.hasParam()) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getParam());
            Recipe recipe = NMSHandler.itemHelper.getRecipeById(key);
            if (recipe == null) {
                return;
            }
            event.setReplacedObject(new ItemTag(recipe.getResult()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.scoreboards>
        // @returns ListTag
        // @description
        // Returns a list of scoreboard IDs currently registered on the server.
        // -->
        if (attribute.startsWith("scoreboards")) {
            ListTag result = new ListTag();
            for (String board : ScoreboardHelper.scoreboardMap.keySet()) {
                result.addObject(new ElementTag(board));
            }
            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        if (attribute.startsWith("scoreboard")) {
            Scoreboard board;
            String name = "main";
            if (attribute.hasParam()) {
                name = attribute.getParam();
                board = ScoreboardHelper.getScoreboard(name);
            }
            else {
                board = ScoreboardHelper.getMain();
            }
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <server.scoreboard[<board>].exists>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether a given scoreboard exists on the server.
            // -->
            if (attribute.startsWith("exists")) {
                event.setReplacedObject(new ElementTag(board != null).getObjectAttribute(attribute.fulfill(1)));
                return;
            }
            if (board == null) {
                attribute.echoError("Scoreboard '" + name + "' does not exist.");
                return;
            }

            // <--[tag]
            // @attribute <server.scoreboard[(<board>)].objectives>
            // @returns ListTag
            // @description
            // Returns a list of all objective names in the scoreboard.
            // Optionally, specify which scoreboard to use.
            // -->
            if (attribute.startsWith("objectives")) {
                ListTag list = new ListTag();
                for (Objective objective : board.getObjectives()) {
                    list.add(objective.getName());
                }
                event.setReplacedObject((list).getObjectAttribute(attribute.fulfill(1)));
            }

            if (attribute.startsWith("objective") && attribute.hasParam()) {
                Objective objective = board.getObjective(attribute.getParam());
                if (objective == null) {
                    attribute.echoError("Scoreboard objective '" + attribute.getParam() + "' does not exist.");
                    return;
                }
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].criteria>
                // @returns ElementTag
                // @description
                // Returns the criteria specified for the given objective.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("criteria")) {
                    event.setReplacedObject(new ElementTag(objective.getCriteria()).getObjectAttribute(attribute.fulfill(1)));
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].display_name>
                // @returns ElementTag
                // @description
                // Returns the display name specified for the given objective.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("display_name")) {
                    event.setReplacedObject(new ElementTag(objective.getDisplayName()).getObjectAttribute(attribute.fulfill(1)));
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].display_slot>
                // @returns ElementTag
                // @description
                // Returns the display slot specified for the given objective. Can be: BELOW_NAME, PLAYER_LIST, or SIDEBAR.
                // Note that not all objectives have a display slot.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("display_slot")) {
                    if (objective.getDisplaySlot() == null) {
                        return;
                    }
                    event.setReplacedObject(new ElementTag(objective.getDisplaySlot().name()).getObjectAttribute(attribute.fulfill(1)));
                }
            }

            // <--[tag]
            // @attribute <server.scoreboard[(<board>)].team_names>
            // @returns ListTag
            // @description
            // Returns a list of the names of all teams within the scoreboard.
            // Optionally, specify which scoreboard to use.
            // -->
            if (attribute.startsWith("team_names")) {
                ListTag result = new ListTag();
                for (Team team : board.getTeams()) {
                    result.add(team.getName());
                }
                event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
            }

            if (attribute.startsWith("team") && attribute.hasParam()) {
                Team team = board.getTeam(attribute.getParam());
                if (team == null) {
                    attribute.echoError("Scoreboard team '" + attribute.getParam() + "' does not exist.");
                    return;
                }
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].team[<team>].members>
                // @returns ListTag
                // @description
                // Returns a list of all members of a scoreboard team. Generally returns as a list of names or text entries.
                // Members are not necessarily written in any given format and are not guaranteed to validly fit any requirements.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("members")) {
                    event.setReplacedObject(new ListTag(team.getEntries()).getObjectAttribute(attribute.fulfill(1)));
                }
                return;
            }
        }

        // <--[tag]
        // @attribute <server.object_is_valid[<object>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the object is a valid object (non-null), as well as not an ElementTag.
        // -->
        if (attribute.startsWith("object_is_valid")) {
            ObjectTag o = ObjectFetcher.pickObjectFor(attribute.getParam(), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag(!(o == null || o instanceof ElementTag)).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_whitelist>
        // @returns ElementTag(Boolean)
        // @mechanism server.has_whitelist
        // @description
        // Returns true if the server's whitelist is active, otherwise returns false.
        // -->
        if (attribute.startsWith("has_whitelist")) {
            event.setReplacedObject(new ElementTag(Bukkit.hasWhitelist()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.whitelisted_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players whitelisted on this server.
        // -->
        if (attribute.startsWith("whitelisted_players")) {
            ListTag result = new ListTag();
            for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                result.addObject(new PlayerTag(player));
            }
            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // See <@link tag FlaggableObject.has_flag>
        // -->
        if (attribute.startsWith("has_flag")) {
            event.setReplacedObject(DenizenCore.serverFlagMap.doHasFlagTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }
        // <--[tag]
        // @attribute <server.flag_expiration[<flag_name>]>
        // @returns TimeTag
        // @description
        // See <@link tag FlaggableObject.flag_expiration>
        // -->
        if (attribute.startsWith("flag_expiration")) {
            TimeTag exp = DenizenCore.serverFlagMap.doFlagExpirationTag(attribute);
            if (exp != null) {
                event.setReplacedObject(exp
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }
        // <--[tag]
        // @attribute <server.flag[<flag_name>]>
        // @returns ObjectTag
        // @description
        // See <@link tag FlaggableObject.flag>
        // -->
        if (attribute.startsWith("flag")) {
            ObjectTag flag = DenizenCore.serverFlagMap.doFlagTag(attribute);
            if (flag != null) {
                event.setReplacedObject(flag
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }
        // <--[tag]
        // @attribute <server.list_flags>
        // @returns ListTag
        // @description
        // See <@link tag FlaggableObject.list_flags>
        // -->
        if (attribute.startsWith("list_flags")) {
            event.setReplacedObject(DenizenCore.serverFlagMap.doListFlagsTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }
        // <--[tag]
        // @attribute <server.flag_map>
        // @returns MapTag
        // @description
        // See <@link tag FlaggableObject.flag_map>
        // -->
        if (attribute.startsWith("flag_map")) {
            event.setReplacedObject(DenizenCore.serverFlagMap.doFlagMapTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.gamerules>
        // @returns ListTag
        // @description
        // Returns a list of all available gamerules on the server.
        // -->
        if (attribute.startsWith("gamerules")) {
            ListTag allGameRules = new ListTag();
            for (GameRule rule : GameRule.values()) {
                allGameRules.add(rule.getName());
            }
            event.setReplacedObject(allGameRules.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.traits>
        // @Plugin Citizens
        // @returns ListTag
        // @description
        // Returns a list of all available NPC traits on the server.
        // -->
        if ((attribute.startsWith("traits") || attribute.startsWith("list_traits")) && Depends.citizens != null) {
            listDeprecateWarn(attribute);
            ListTag allTraits = new ListTag();
            for (TraitInfo trait : CitizensAPI.getTraitFactory().getRegisteredTraits()) {
                allTraits.add(trait.getTraitName());
            }
            event.setReplacedObject(allTraits.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.commands>
        // @returns ListTag
        // @description
        // Returns a list of all registered command names in Bukkit.
        // -->
        if (attribute.startsWith("commands") || attribute.startsWith("list_commands")) {
            listDeprecateWarn(attribute);
            CommandScriptHelper.init();
            ListTag list = new ListTag(CommandScriptHelper.knownCommands.keySet());
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.command_source_plugin[<name>]>
        // @returns PluginTag
        // @description
        // Returns the plugin that created a command (if known).
        // For example, <server.command_plugin[ex]> should return a PluginTag of Denizen.
        // -->
        if (attribute.startsWith("command_plugin")) {
            PluginCommand cmd = Bukkit.getPluginCommand(attribute.getParam());
            if (cmd != null) {
                event.setReplacedObject(new PluginTag(cmd.getPlugin()).getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.color_names>
        // @returns ListTag
        // @description
        // Returns a list of all available color names that would be accepted by <@link objecttype ColorTag>.
        // This is only their Bukkit names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Color.html>.
        // -->
        if (attribute.startsWith("color_names")) {
            ListTag list = new ListTag();
            for (String color : ColorTag.colorsByName.keySet()) {
                list.add(color);
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.art_types>
        // @returns ListTag
        // @description
        // Returns a list of all known art types.
        // Generally used with <@link tag EntityTag.painting> and <@link mechanism EntityTag.painting>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Art.html>.
        // -->
        if (attribute.startsWith("art_types")) {
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            for (Art art : Art.values()) {
                list.add(art.name());
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.advancement_types>
        // @returns ListTag
        // @description
        // Returns a list of all registered advancement names.
        // Generally used with <@link tag PlayerTag.has_advancement>.
        // See also <@link url https://minecraft.fandom.com/wiki/Advancement>.
        // -->
        if (attribute.startsWith("advancement_types") || attribute.startsWith("list_advancements")) {
            if (attribute.matches("list_advancements")) {
                Debug.echoError("list_advancements is deprecated: use advancement_types");
            }
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            Bukkit.advancementIterator().forEachRemaining((adv) -> {
                list.add(adv.getKey().toString());
            });
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.nbt_attribute_types>
        // @returns ListTag
        // @description
        // Returns a list of all registered attribute names.
        // Generally used with <@link tag EntityTag.has_attribute>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>.
        // -->
        if (attribute.startsWith("nbt_attribute_types") || attribute.startsWith("list_nbt_attribute_types")) {
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            for (org.bukkit.attribute.Attribute attribType : org.bukkit.attribute.Attribute.values()) {
                list.add(attribType.name());
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.damage_causes>
        // @returns ListTag
        // @description
        // Returns a list of all registered damage causes.
        // Generally used with <@link event entity damaged>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html>.
        // -->
        if (attribute.startsWith("damage_causes") || attribute.startsWith("list_damage_causes")) {
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            for (EntityDamageEvent.DamageCause damageCause : EntityDamageEvent.DamageCause.values()) {
                list.add(damageCause.name());
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.teleport_causes>
        // @returns ListTag
        // @description
        // Returns a list of all registered player teleport causes.
        // Generally used with <@link event entity teleports>.
        // See <@link language teleport cause> for the current list of causes.
        // -->
        if (attribute.startsWith("teleport_causes")) {
            ListTag list = new ListTag();
            for (PlayerTeleportEvent.TeleportCause teleportCause : PlayerTeleportEvent.TeleportCause.values()) {
                list.add(teleportCause.name());
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.biome_types>
        // @returns ListTag(BiomeTag)
        // @description
        // Returns a list of all biomes known to the server.
        // Generally used with <@link objecttype BiomeTag>.
        // This is based on Bukkit Biome enum, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html>.
        // -->
        if (attribute.startsWith("biome_types") || attribute.startsWith("list_biome_types")) {
            listDeprecateWarn(attribute);
            ListTag allBiomes = new ListTag();
            for (Biome biome : Biome.values()) {
                if (biome != Biome.CUSTOM) {
                    allBiomes.addObject(new BiomeTag(biome));
                }
            }
            event.setReplacedObject(allBiomes.getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("list_biomes")) {
            Deprecations.serverListBiomeNames.warn(attribute.context);
            ListTag allBiomes = new ListTag();
            for (Biome biome : Biome.values()) {
                allBiomes.add(biome.name());
            }
            event.setReplacedObject(allBiomes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.enchantments>
        // @returns ListTag(EnchantmentTag)
        // @description
        // Returns a list of all enchantments known to the server.
        // -->
        if (attribute.startsWith("enchantments")) {
            ListTag enchants = new ListTag();
            for (Enchantment ench : Enchantment.values()) {
                enchants.addObject(new EnchantmentTag(ench));
            }
            event.setReplacedObject(enchants.getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("enchantment_types") || attribute.startsWith("list_enchantments")) {
            Deprecations.echantmentTagUpdate.warn(attribute.context);
            if (attribute.matches("list_enchantments")) {
                Debug.echoError("list_enchantments is deprecated: use enchantment_types");
            }
            listDeprecateWarn(attribute);
            ListTag enchants = new ListTag();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getName());
            }
            event.setReplacedObject(enchants.getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("enchantment_keys") || attribute.startsWith("list_enchantment_keys")) {
            Deprecations.echantmentTagUpdate.warn(attribute.context);
            listDeprecateWarn(attribute);
            ListTag enchants = new ListTag();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getKey().getKey());
            }
            event.setReplacedObject(enchants.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.entity_types>
        // @returns ListTag
        // @description
        // Returns a list of all entity types known to the server.
        // Generally used with <@link objecttype EntityTag>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html>.
        // -->
        if (attribute.startsWith("entity_types") || attribute.startsWith("list_entity_types")) {
            listDeprecateWarn(attribute);
            ListTag allEnt = new ListTag();
            for (EntityType entity : EntityType.values()) {
                if (entity != EntityType.UNKNOWN) {
                    allEnt.add(entity.name());
                }
            }
            event.setReplacedObject(allEnt.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.material_types>
        // @returns ListTag(MaterialTag)
        // @description
        // Returns a list of all materials known to the server.
        // Generally used with <@link objecttype MaterialTag>.
        // This is only types listed in the Bukkit Material enum, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>.
        // -->
        if (attribute.startsWith("material_types") || attribute.startsWith("list_material_types")) {
            listDeprecateWarn(attribute);
            ListTag allMats = new ListTag();
            for (Material mat : Material.values()) {
                allMats.addObject(new MaterialTag(mat));
            }
            event.setReplacedObject(allMats.getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("list_materials")) {
            Deprecations.serverListMaterialNames.warn(attribute.context);
            ListTag allMats = new ListTag();
            for (Material mat : Material.values()) {
                allMats.add(mat.name());
            }
            event.setReplacedObject(allMats.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.sound_types>
        // @returns ListTag
        // @description
        // Returns a list of all sounds known to the server.
        // Generally used with <@link command playsound>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>.
        // -->
        if (attribute.startsWith("sound_types") || attribute.startsWith("list_sounds")) {
            if (attribute.matches("list_sounds")) {
                Debug.echoError("list_sounds is deprecated: use sound_types");
            }
            listDeprecateWarn(attribute);
            ListTag sounds = new ListTag();
            for (Sound s : Sound.values()) {
                sounds.add(s.toString());
            }
            event.setReplacedObject(sounds.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.particle_types>
        // @returns ListTag
        // @description
        // Returns a list of all particle effect types known to the server.
        // Generally used with <@link command playeffect>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html>.
        // Refer also to <@link tag server.effect_types>.
        // -->
        if (attribute.startsWith("particle_types") || attribute.startsWith("list_particles")) {
            if (attribute.matches("list_particles")) {
                Debug.echoError("list_particles is deprecated: use particle_types");
            }
            listDeprecateWarn(attribute);
            ListTag particleTypes = new ListTag();
            for (Particle particle : Particle.values()) {
                particleTypes.add(particle.toString());
            }
            event.setReplacedObject(particleTypes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.effect_types>
        // @returns ListTag
        // @description
        // Returns a list of all 'effect' types known to the server.
        // Generally used with <@link command playeffect>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html>.
        // Refer also to <@link tag server.particle_types>.
        // -->
        if (attribute.startsWith("effect_types") || attribute.startsWith("list_effects")) {
            if (attribute.matches("list_effects")) {
                Debug.echoError("list_effects is deprecated: use effect_types");
            }
            listDeprecateWarn(attribute);
            ListTag effectTypes = new ListTag();
            for (Effect effect : Effect.values()) {
                effectTypes.add(effect.toString());
            }
            event.setReplacedObject(effectTypes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.pattern_types>
        // @returns ListTag
        // @description
        // Returns a list of all banner patterns known to the server.
        // Generally used with <@link tag ItemTag.patterns>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // -->
        if (attribute.startsWith("pattern_types") || attribute.startsWith("list_patterns")) {
            if (attribute.matches("list_patterns")) {
                Debug.echoError("list_patterns is deprecated: use pattern_types");
            }
            listDeprecateWarn(attribute);
            ListTag allPatterns = new ListTag();
            for (PatternType pat : PatternType.values()) {
                allPatterns.add(pat.toString());
            }
            event.setReplacedObject(allPatterns.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.potion_effect_types>
        // @returns ListTag
        // @description
        // Returns a list of all potion effects known to the server.
        // Can be used with <@link command cast>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // Refer also to <@link tag server.potion_types>.
        // -->
        if (attribute.startsWith("potion_effect_types") || attribute.startsWith("list_potion_effects")) {
            if (attribute.matches("list_potion_effects")) {
                Debug.echoError("list_potion_effects is deprecated: use potion_effect_types");
            }
            listDeprecateWarn(attribute);
            ListTag statuses = new ListTag();
            for (PotionEffectType effect : PotionEffectType.values()) {
                if (effect != null) {
                    statuses.add(effect.getName());
                }
            }
            event.setReplacedObject(statuses.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.potion_types>
        // @returns ListTag
        // @description
        // Returns a list of all potion types known to the server.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // Refer also to <@link tag server.potion_effect_types>.
        // -->
        if (attribute.startsWith("potion_types") || attribute.startsWith("list_potion_types")) {
            listDeprecateWarn(attribute);
            ListTag potionTypes = new ListTag();
            for (PotionType type : PotionType.values()) {
                potionTypes.add(type.toString());
            }
            event.setReplacedObject(potionTypes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.tree_types>
        // @returns ListTag
        // @description
        // Returns a list of all tree types known to the server.
        // Generally used with <@link mechanism LocationTag.generate_tree>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/TreeType.html>.
        // -->
        if (attribute.startsWith("tree_types") || attribute.startsWith("list_tree_types")) {
            listDeprecateWarn(attribute);
            ListTag allTrees = new ListTag();
            for (TreeType tree : TreeType.values()) {
                allTrees.add(tree.name());
            }
            event.setReplacedObject(allTrees.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.map_cursor_types>
        // @returns ListTag
        // @description
        // Returns a list of all map cursor types known to the server.
        // Generally used with <@link command map> and <@link language Map Script Containers>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapCursor.Type.html>.
        // -->
        if (attribute.startsWith("map_cursor_types") || attribute.startsWith("list_map_cursor_types")) {
            listDeprecateWarn(attribute);
            ListTag mapCursors = new ListTag();
            for (MapCursor.Type cursor : MapCursor.Type.values()) {
                mapCursors.add(cursor.toString());
            }
            event.setReplacedObject(mapCursors.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.world_types>
        // @returns ListTag
        // @description
        // Returns a list of all world types known to the server.
        // Generally used with <@link command createworld>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>.
        // -->
        if (attribute.startsWith("world_types") || attribute.startsWith("list_world_types")) {
            listDeprecateWarn(attribute);
            ListTag worldTypes = new ListTag();
            for (WorldType world : WorldType.values()) {
                worldTypes.add(world.toString());
            }
            event.setReplacedObject(worldTypes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.statistic_types[(<type>)]>
        // @returns ListTag
        // @description
        // Returns a list of all statistic types known to the server.
        // Generally used with <@link tag PlayerTag.statistic>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html>.
        // Optionally, specify a type to limit to statistics of a given type. Valid types: UNTYPED, ITEM, ENTITY, or BLOCK.
        // Refer also to <@link tag server.statistic_type>.
        // -->
        if (attribute.startsWith("statistic_types") || attribute.startsWith("list_statistics")) {
            listDeprecateWarn(attribute);
            if (attribute.matches("list_statistics")) {
                Debug.echoError("list_statistics is deprecated: use statistic_types");
            }
            Statistic.Type type = null;
            if (attribute.hasParam()) {
                type = Statistic.Type.valueOf(attribute.getParam().toUpperCase());
            }
            ListTag statisticTypes = new ListTag();
            for (Statistic stat : Statistic.values()) {
                if (type == null || type == stat.getType()) {
                    statisticTypes.add(stat.toString());
                }
            }
            event.setReplacedObject(statisticTypes.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.structure_types>
        // @returns ListTag
        // @description
        // Returns a list of all structure types known to the server.
        // Generally used with <@link tag LocationTag.find.structure.within>.
        // This is NOT their Bukkit names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/StructureType.html>.
        // Instead these are the internal names tracked by Spigot and presumably matching Minecraft internals.
        // These are all lowercase, as the internal names are lowercase and supposedly are case-sensitive.
        // It is unclear why the "StructureType" class in Bukkit is not simply an enum as most similar listings are.
        // -->
        if (attribute.startsWith("structure_types") || attribute.startsWith("list_structure_types")) {
            listDeprecateWarn(attribute);
            event.setReplacedObject(new ListTag(StructureType.getStructureTypes().keySet()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.notes[<type>]>
        // @returns ListTag
        // @description
        // Lists all saved notable objects of a specific type currently on the server.
        // Valid types: locations, cuboids, ellipsoids, inventories, polygons
        // This is primarily intended for debugging purposes, and it's best to avoid using this in a live script if possible.
        // -->
        if (attribute.startsWith("notes") || attribute.startsWith("list_notables") || attribute.startsWith("notables")) {
            listDeprecateWarn(attribute);
            ListTag allNotables = new ListTag();
            if (attribute.hasParam()) {
                String type = CoreUtilities.toLowerCase(attribute.getParam());
                for (Map.Entry<String, Class> typeClass : NoteManager.namesToTypes.entrySet()) {
                    if (type.equals(CoreUtilities.toLowerCase(typeClass.getKey()))) {
                        for (Object notable : NoteManager.getAllType(typeClass.getValue())) {
                            allNotables.addObject((ObjectTag) notable);
                        }
                        break;
                    }
                }
            }
            else {
                for (Notable notable : NoteManager.nameToObject.values()) {
                    allNotables.addObject((ObjectTag) notable);
                }
            }
            event.setReplacedObject(allNotables.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.statistic_type[<statistic>]>
        // @returns ElementTag
        // @description
        // Returns the qualifier type of the given statistic.
        // Generally relevant to usage with <@link tag PlayerTag.statistic.qualifier>.
        // Returns UNTYPED, ITEM, ENTITY, or BLOCK.
        // Refer also to <@link tag server.statistic_types>.
        // -->
        if (attribute.startsWith("statistic_type") && attribute.hasParam()) {
            Statistic statistic;
            try {
                statistic = Statistic.valueOf(attribute.getParam().toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                attribute.echoError("Statistic '" + attribute.getParam() + "' does not exist: " + ex.getMessage());
                return;
            }
            event.setReplacedObject(new ElementTag(statistic.getType().name()).getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("enchantment_max_level") && attribute.hasParam()) {
            Deprecations.echantmentTagUpdate.warn(attribute.context);
            EnchantmentTag ench = EnchantmentTag.valueOf(attribute.getParam(), attribute.context);
            if (ench == null) {
                attribute.echoError("Enchantment '" + attribute.getParam() + "' does not exist.");
                return;
            }
            event.setReplacedObject(new ElementTag(ench.enchantment.getMaxLevel()).getObjectAttribute(attribute.fulfill(1)));
        }

        if (attribute.startsWith("enchantment_start_level") && attribute.hasParam()) {
            Deprecations.echantmentTagUpdate.warn(attribute.context);
            EnchantmentTag ench = EnchantmentTag.valueOf(attribute.getParam(), attribute.context);
            if (ench == null) {
                attribute.echoError("Enchantment '" + attribute.getParam() + "' does not exist.");
                return;
            }
            event.setReplacedObject(new ElementTag(ench.enchantment.getStartLevel()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.started_time>
        // @returns TimeTag
        // @description
        // Returns the time the server started.
        // -->
        if (attribute.startsWith("started_time")) {
            event.setReplacedObject(new TimeTag(DenizenCore.startTime)
                    .getObjectAttribute(attribute.fulfill(1)));
        }
        if (attribute.startsWith("start_time")) {
            Deprecations.timeTagRewrite.warn(attribute.context);
            event.setReplacedObject(new DurationTag(DenizenCore.startTime / 50)
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.disk_free>
        // @returns ElementTag(Number)
        // @description
        // How much remaining disk space is available to this server, in bytes.
        // This counts only the drive the server folder is on, not any other drives.
        // This may be limited below the actual drive capacity by operating system settings.
        // -->
        if (attribute.startsWith("disk_free")) {
            File folder = Denizen.getInstance().getDataFolder();
            event.setReplacedObject(new ElementTag(folder.getUsableSpace())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.disk_total>
        // @returns ElementTag(Number)
        // @description
        // How much total disk space is on the drive containing this server, in bytes.
        // This counts only the drive the server folder is on, not any other drives.
        // -->
        if (attribute.startsWith("disk_total")) {
            File folder = Denizen.getInstance().getDataFolder();
            event.setReplacedObject(new ElementTag(folder.getTotalSpace())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.disk_usage>
        // @returns ElementTag(Number)
        // @description
        // How much space on the drive is already in use, in bytes.
        // This counts only the drive the server folder is on, not any other drives.
        // This is approximately equivalent to "disk_total" minus "disk_free", but is not always exactly the same,
        // as this tag will not include space "used" by operating system settings that simply deny the server write access.
        // -->
        if (attribute.startsWith("disk_usage")) {
            File folder = Denizen.getInstance().getDataFolder();
            event.setReplacedObject(new ElementTag(folder.getTotalSpace() - folder.getFreeSpace())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_allocated>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is allocated to the server, in bytes (total memory).
        // This is how much of the system memory is reserved by the Java process, NOT how much is actually in use
        // by the minecraft server.
        // -->
        if (attribute.startsWith("ram_allocated")) {
            event.setReplacedObject(new ElementTag(Runtime.getRuntime().totalMemory())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_max>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is available to the server (total), in bytes (max memory).
        // -->
        if (attribute.startsWith("ram_max")) {
            event.setReplacedObject(new ElementTag(Runtime.getRuntime().maxMemory())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_free>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is unused but available on the server, in bytes (free memory).
        // -->
        if (attribute.startsWith("ram_free")) {
            event.setReplacedObject(new ElementTag(Runtime.getRuntime().freeMemory())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_usage>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is used by the server, in bytes (free memory).
        // Equivalent to ram_max minus ram_free
        // -->
        if (attribute.startsWith("ram_usage")) {
            event.setReplacedObject(new ElementTag(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.available_processors>
        // @returns ElementTag(Number)
        // @description
        // How many virtual processors are available to the server.
        // (In general, Minecraft only uses one, unfortunately.)
        // -->
        if (attribute.startsWith("available_processors")) {
            event.setReplacedObject(new ElementTag(Runtime.getRuntime().availableProcessors())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_tick>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of ticks since the server was started.
        // Note that this is NOT an accurate indicator for real server uptime, as ticks fluctuate based on server lag.
        // -->
        if (attribute.startsWith("current_tick")) {
            event.setReplacedObject(new ElementTag(TickScriptEvent.instance.ticks)
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.delta_time_since_start>
        // @returns DurationTag
        // @description
        // Returns the duration of delta time since the server started.
        // Note that this is delta time, not real time, meaning it is calculated based on the server tick,
        // which may change longer or shorter than expected due to lag or other influences.
        // If you want real time instead of delta time, use <@link tag server.real_time_since_start>.
        // -->
        if (attribute.startsWith("delta_time_since_start")) {
            event.setReplacedObject(new DurationTag(TickScriptEvent.instance.ticks)
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.real_time_since_start>
        // @returns DurationTag
        // @description
        // Returns the duration of real time since the server started.
        // Note that this is real time, not delta time, meaning that the it is accurate to the system clock, not the server's tick.
        // System clock changes may cause this value to become inaccurate.
        // In many cases <@link tag server.delta_time_since_start> is preferable.
        // -->
        if (attribute.startsWith("real_time_since_start")) {
            event.setReplacedObject(new DurationTag((System.currentTimeMillis() - serverStartTimeMillis) / 1000.0)
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_time_millis>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of milliseconds since Jan 1, 1970.
        // Note that this can change every time the tag is read!
        // -->
        if (attribute.startsWith("current_time_millis")) {
            event.setReplacedObject(new ElementTag(System.currentTimeMillis())
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.selected_npc>
        // @returns NPCTag
        // @description
        // Returns the server's currently selected NPC.
        // -->
        if (attribute.startsWith("selected_npc")) {
            NPC npc = ((Citizens) Bukkit.getPluginManager().getPlugin("Citizens"))
                    .getNPCSelector().getSelected(Bukkit.getConsoleSender());
            if (npc == null) {
                return;
            }
            else {
                event.setReplacedObject(new NPCTag(npc).getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.npcs_named[<name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of NPCs with a certain name.
        // -->
        if ((attribute.startsWith("npcs_named") || attribute.startsWith("list_npcs_named")) && Depends.citizens != null && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            ListTag npcs = new ListTag();
            String name = attribute.getParam();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (CoreUtilities.equalsIgnoreCase(npc.getName(), name)) {
                    npcs.addObject(new NPCTag(npc));
                }
            }
            event.setReplacedObject(npcs.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_file[<name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the specified file exists. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("has_file") && attribute.hasParam()) {
            File f = new File(Denizen.getInstance().getDataFolder(), attribute.getParam());
            try {
                if (!Utilities.canReadFile(f)) {
                    Debug.echoError("Cannot read from that file path due to security settings in Denizen/config.yml.");
                    return;
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return;
            }
            event.setReplacedObject(new ElementTag(f.exists()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_files[<path>]>
        // @returns ListTag
        // @description
        // Returns a list of all files (and directories) in the specified directory. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("list_files") && attribute.hasParam()) {
            File folder = new File(Denizen.getInstance().getDataFolder(), attribute.getParam());
            try {
                if (!Utilities.canReadFile(folder)) {
                    Debug.echoError("Cannot read from that file path due to security settings in Denizen/config.yml.");
                    return;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    attribute.echoError("Invalid path specified. No directory exists at that path.");
                    return;
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return;
            }
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            ListTag list = new ListTag();
            for (File file : files) {
                list.add(file.getName());
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_permissions>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the server has a known permission plugin loaded.
        // Note: should not be considered incredibly reliable.
        // -->
        if (attribute.startsWith("has_permissions")) {
            event.setReplacedObject(new ElementTag(Depends.permissions != null && Depends.permissions.isEnabled())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_economy>
        // @returns ElementTag(Boolean)
        // @plugin Vault
        // @description
        // Returns whether the server has a known economy plugin loaded.
        // -->
        if (attribute.startsWith("has_economy")) {
            event.setReplacedObject(new ElementTag(Depends.economy != null && Depends.economy.isEnabled())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.denizen_version>
        // @returns ElementTag
        // @description
        // Returns the version of Denizen currently being used.
        // -->
        if (attribute.startsWith("denizen_version")) {
            event.setReplacedObject(new ElementTag(Denizen.getInstance().getDescription().getVersion())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.bukkit_version>
        // @returns ElementTag
        // @description
        // Returns the version of Bukkit currently being used.
        // -->
        if (attribute.startsWith("bukkit_version")) {
            event.setReplacedObject(new ElementTag(Bukkit.getBukkitVersion())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.version>
        // @returns ElementTag
        // @description
        // Returns the version of the server.
        // -->
        if (attribute.startsWith("version")) {
            event.setReplacedObject(new ElementTag(Bukkit.getServer().getVersion())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.java_version>
        // @returns ElementTag
        // @description
        // Returns the current Java version of the server.
        // -->
        if (attribute.startsWith("java_version")) {
            event.setReplacedObject(new ElementTag(System.getProperty("java.version"))
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.max_players>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum number of players allowed on the server.
        // -->
        if (attribute.startsWith("max_players")) {
            event.setReplacedObject(new ElementTag(Bukkit.getServer().getMaxPlayers())
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.sql_connections>
        // @returns ListTag
        // @description
        // Returns a list of all SQL connections opened by <@link command sql>.
        // -->
        if (attribute.startsWith("sql_connections") || attribute.startsWith("list_sql_connections")) {
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            for (Map.Entry<String, Connection> entry : SQLCommand.connections.entrySet()) {
                try {
                    if (!entry.getValue().isClosed()) {
                        list.add(entry.getKey());
                    }
                    else {
                        SQLCommand.connections.remove(entry.getKey());
                    }
                }
                catch (SQLException e) {
                    Debug.echoError(attribute.getScriptEntry(), e);
                }
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.group_prefix[<group>]>
        // @returns ElementTag
        // @description
        // Returns an ElementTag of a group's chat prefix.
        // -->
        if (attribute.startsWith("group_prefix")) {

            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getParam();

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                attribute.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_prefix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat prefix for the specified WorldTag.
            // -->
            if (attribute.startsWith("world", 2)) {
                WorldTag world = attribute.contextAsType(2, WorldTag.class);
                if (world != null) {
                    event.setReplacedObject(new ElementTag(Depends.chat.getGroupPrefix(world.getWorld(), group))
                            .getObjectAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Prefix in default world
            event.setReplacedObject(new ElementTag(Depends.chat.getGroupPrefix(Bukkit.getWorlds().get(0), group))
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.group_suffix[<group>]>
        // @returns ElementTag
        // @description
        // Returns an ElementTag of a group's chat suffix.
        // -->
        if (attribute.startsWith("group_suffix")) {

            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getParam();

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                attribute.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_suffix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat suffix for the specified WorldTag.
            // -->
            if (attribute.startsWith("world", 2)) {
                WorldTag world = attribute.contextAsType(2, WorldTag.class);
                if (world != null) {
                    event.setReplacedObject(new ElementTag(Depends.chat.getGroupSuffix(world.getWorld(), group))
                            .getObjectAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Suffix in default world
            event.setReplacedObject(new ElementTag(Depends.chat.getGroupSuffix(Bukkit.getWorlds().get(0), group))
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.permission_groups>
        // @returns ListTag
        // @description
        // Returns a list of all permission groups on the server.
        // -->
        if (attribute.startsWith("permission_groups") || attribute.startsWith("list_permission_groups")) {
            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }
            listDeprecateWarn(attribute);
            event.setReplacedObject(new ListTag(Arrays.asList(Depends.permissions.getGroups())).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        if (attribute.startsWith("list_plugin_names")) {
            Deprecations.serverPluginNamesTag.warn(attribute.context);
            ListTag plugins = new ListTag();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.add(plugin.getName());
            }
            event.setReplacedObject(plugins.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.scripts>
        // @returns ListTag(ScriptTag)
        // @description
        // Gets a list of all scripts currently loaded into Denizen.
        // -->
        if (attribute.startsWith("scripts") || attribute.startsWith("list_scripts")) {
            listDeprecateWarn(attribute);
            ListTag scripts = new ListTag();
            for (ScriptContainer script : ScriptRegistry.scriptContainers.values()) {
                scripts.addObject(new ScriptTag(script));
            }
            event.setReplacedObject(scripts.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.match_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns the online player that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns player object for 'bob',
        // input 'bobb' returns player object for 'bobby', and input 'b' returns player object for 'bo'.
        // -->
        if (attribute.startsWith("match_player") && attribute.hasParam()) {
            Player matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getParam());
            if (matchInput.isEmpty()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                String nameLow = CoreUtilities.toLowerCase(player.getName());
                if (nameLow.equals(matchInput)) {
                    matchPlayer = player;
                    break;
                }
                else if (nameLow.contains(matchInput)) {
                    if (matchPlayer == null || nameLow.startsWith(matchInput)) {
                        matchPlayer = player;
                    }
                }
            }

            if (matchPlayer != null) {
                event.setReplacedObject(new PlayerTag(matchPlayer).getObjectAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.match_offline_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns any player (online or offline) that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns player object for 'bob',
        // input 'bobb' returns player object for 'bobby', and input 'b' returns player object for 'bo'.
        // When both an online player and an offline player match the name search, the online player will be returned.
        // -->
        if (attribute.startsWith("match_offline_player") && attribute.hasParam()) {
            PlayerTag matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getParam());
            if (matchInput.isEmpty()) {
                return;
            }
            for (Map.Entry<String, UUID> entry : PlayerTag.getAllPlayers().entrySet()) {
                String nameLow = CoreUtilities.toLowerCase(entry.getKey());
                if (nameLow.equals(matchInput)) {
                    matchPlayer = new PlayerTag(entry.getValue());
                    break;
                }
                else if (nameLow.contains(matchInput)) {
                    PlayerTag newMatch = new PlayerTag(entry.getValue());
                    if (matchPlayer == null) {
                        matchPlayer = newMatch;
                    }
                    else if (newMatch.isOnline() && !matchPlayer.isOnline()) {
                        matchPlayer = newMatch;
                    }
                    else if (nameLow.startsWith(matchInput) && (newMatch.isOnline() == matchPlayer.isOnline())) {
                        matchPlayer = newMatch;
                    }
                }
            }
            if (matchPlayer != null) {
                event.setReplacedObject(matchPlayer.getObjectAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.npcs_assigned[<assignment_script>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs assigned to a specified script.
        // -->
        if ((attribute.startsWith("npcs_assigned") || attribute.startsWith("list_npcs_assigned")) && Depends.citizens != null
                && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            ScriptTag script = attribute.paramAsType(ScriptTag.class);
            if (script == null || !(script.getContainer() instanceof AssignmentScriptContainer)) {
                attribute.echoError("Invalid script specified.");
            }
            else {
                AssignmentScriptContainer container = (AssignmentScriptContainer) script.getContainer();
                ListTag npcs = new ListTag();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(AssignmentTrait.class) && npc.getOrAddTrait(AssignmentTrait.class).isAssigned(container)) {
                        npcs.addObject(new NPCTag(npc));
                    }
                }
                event.setReplacedObject(npcs.getObjectAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <server.online_players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players with a specified flag set.
        // -->
        if ((attribute.startsWith("online_players_flagged") || attribute.startsWith("list_online_players_flagged"))
                && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            String flag = attribute.getParam();
            ListTag players = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerTag plTag = new PlayerTag(player);
                if (plTag.getFlagTracker().hasFlag(flag)) {
                    players.addObject(plTag);
                }
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players (online or offline) with a specified flag set.
        // Warning: this will cause the player flag cache to temporarily fill with ALL historical playerdata.
        // -->
        if ((attribute.startsWith("players_flagged") || attribute.startsWith("list_players_flagged"))
                && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            String flag = attribute.getParam();
            ListTag players = new ListTag();
            for (Map.Entry<String, UUID> entry : PlayerTag.getAllPlayers().entrySet()) {
                PlayerTag plTag = new PlayerTag(entry.getValue());
                if (plTag.getFlagTracker().hasFlag(flag)) {
                    players.addObject(plTag);
                }
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.spawned_npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all spawned NPCs with a specified flag set.
        // -->
        if ((attribute.startsWith("spawned_npcs_flagged") || attribute.startsWith("list_spawned_npcs_flagged")) && Depends.citizens != null
                && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            String flag = attribute.getParam();
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag dNpc = new NPCTag(npc);
                if (dNpc.isSpawned() && dNpc.hasFlag(flag)) {
                    npcs.addObject(dNpc);
                }
            }
            event.setReplacedObject(npcs.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs with a specified flag set.
        // -->
        if ((attribute.startsWith("npcs_flagged") || attribute.startsWith("list_npcs_flagged")) && Depends.citizens != null
                && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            String flag = attribute.getParam();
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag dNpc = new NPCTag(npc);
                if (dNpc.hasFlag(flag)) {
                    npcs.addObject(dNpc);
                }
            }
            event.setReplacedObject(npcs.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.npc_registries>
        // @returns ListTag
        // @description
        // Returns a list of all NPC registries.
        // -->
        if (attribute.startsWith("npc_registries") && Depends.citizens != null) {
            ListTag result = new ListTag();
            for (NPCRegistry registry : CitizensAPI.getNPCRegistries()) {
                result.add(registry.getName());
            }
            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.npcs[(<registry>)]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs.
        // -->
        if ((attribute.startsWith("npcs") || attribute.startsWith("list_npcs")) && Depends.citizens != null) {
            listDeprecateWarn(attribute);
            ListTag npcs = new ListTag();
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            if (attribute.hasParam()) {
                registry = NPCTag.getRegistryByName(attribute.getParam());
                if (registry == null) {
                    attribute.echoError("NPC Registry '" + attribute.getParam() + "' does not exist.");
                    return;
                }
            }
            for (NPC npc : registry) {
                npcs.addObject(new NPCTag(npc));
            }
            event.setReplacedObject(npcs.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.worlds>
        // @returns ListTag(WorldTag)
        // @description
        // Returns a list of all worlds.
        // -->
        if (attribute.startsWith("worlds") || attribute.startsWith("list_worlds")) {
            listDeprecateWarn(attribute);
            ListTag worlds = new ListTag();
            for (World world : Bukkit.getWorlds()) {
                worlds.addObject(WorldTag.mirrorBukkitWorld(world));
            }
            event.setReplacedObject(worlds.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.plugins>
        // @returns ListTag(PluginTag)
        // @description
        // Gets a list of currently enabled PluginTags from the server.
        // -->
        if (attribute.startsWith("plugins") || attribute.startsWith("list_plugins")) {
            listDeprecateWarn(attribute);
            ListTag plugins = new ListTag();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.addObject(new PluginTag(plugin));
            }
            event.setReplacedObject(plugins.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("players") || attribute.startsWith("list_players")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                players.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.online_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players.
        // -->
        if (attribute.startsWith("online_players") || attribute.startsWith("list_online_players")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.offline_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline players.
        // This specifically excludes currently online players.
        // -->
        if (attribute.startsWith("offline_players") || attribute.startsWith("list_offline_players")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (!player.isOnline()) {
                    players.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.banned_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all banned players.
        // -->
        if (attribute.startsWith("banned_players") || attribute.startsWith("list_banned_players")) {
            listDeprecateWarn(attribute);
            ListTag banned = new ListTag();
            for (OfflinePlayer player : Bukkit.getBannedPlayers()) {
                banned.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            event.setReplacedObject(banned.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.banned_addresses>
        // @returns ListTag
        // @description
        // Returns a list of all banned ip addresses.
        // -->
        if (attribute.startsWith("banned_addresses") || attribute.startsWith("list_banned_addresses")) {
            listDeprecateWarn(attribute);
            ListTag list = new ListTag();
            list.addAll(Bukkit.getIPBans());
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.is_banned[<address>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the given ip address is banned.
        // -->
        if (attribute.startsWith("is_banned") && attribute.hasParam()) {
            // BanList contains an isBanned method that doesn't check expiration time
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(attribute.getParam());

            if (ban == null) {
                event.setReplacedObject(new ElementTag(false).getObjectAttribute(attribute.fulfill(1)));
            }
            else if (ban.getExpiration() == null) {
                event.setReplacedObject(new ElementTag(true).getObjectAttribute(attribute.fulfill(1)));
            }
            else {
                event.setReplacedObject(new ElementTag(ban.getExpiration().after(new Date())).getObjectAttribute(attribute.fulfill(1)));
            }

            return;
        }

        if (attribute.startsWith("ban_info") && attribute.hasParam()) {
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(attribute.getParam());
            attribute.fulfill(1);
            if (ban == null || (ban.getExpiration() != null && ban.getExpiration().before(new Date()))) {
                return;
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].expiration_time>
            // @returns TimeTag
            // @description
            // Returns the expiration of the ip address's ban, if it is banned.
            // Potentially can be null.
            // -->
            if (attribute.startsWith("expiration_time") && ban.getExpiration() != null) {
                event.setReplacedObject(new TimeTag(ban.getExpiration().getTime())
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            else if (attribute.startsWith("expiration") && ban.getExpiration() != null) {
                Deprecations.timeTagRewrite.warn(attribute.context);
                event.setReplacedObject(new DurationTag(ban.getExpiration().getTime() / 50)
                        .getObjectAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].reason>
            // @returns ElementTag
            // @description
            // Returns the reason for the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("reason")) {
                event.setReplacedObject(new ElementTag(ban.getReason())
                        .getObjectAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].created_time>
            // @returns TimeTag
            // @description
            // Returns when the ip address's ban was created, if it is banned.
            // -->
            else if (attribute.startsWith("created_time")) {
                event.setReplacedObject(new TimeTag(ban.getCreated().getTime())
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            else if (attribute.startsWith("created")) {
                Deprecations.timeTagRewrite.warn(attribute.context);
                event.setReplacedObject(new DurationTag(ban.getCreated().getTime() / 50)
                        .getObjectAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].source>
            // @returns ElementTag
            // @description
            // Returns the source of the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("source")) {
                event.setReplacedObject(new ElementTag(ban.getSource())
                        .getObjectAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all ops, online or not.
        // -->
        if (attribute.startsWith("ops") || attribute.startsWith("list_ops")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                players.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.online_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online ops.
        // -->
        if (attribute.startsWith("online_ops") || attribute.startsWith("list_online_ops")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                if (player.isOnline()) {
                    players.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.offline_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline ops.
        // -->
        if (attribute.startsWith("offline_ops") || attribute.startsWith("list_offline_ops")) {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isOp() && !player.isOnline()) {
                    players.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            event.setReplacedObject(players.getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.motd>
        // @returns ElementTag
        // @description
        // Returns the server's current MOTD.
        // -->
        if (attribute.startsWith("motd")) {
            event.setReplacedObject(new ElementTag(Bukkit.getServer().getMotd()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.view_distance>
        // @returns ElementTag(Number)
        // @description
        // Returns the server's current view distance.
        // -->
        if (attribute.startsWith("view_distance")) {
            event.setReplacedObject(new ElementTag(Bukkit.getServer().getViewDistance()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }
        else if (attribute.startsWith("entity_is_spawned")
                && attribute.hasParam()) {
            Deprecations.isValidTag.warn(attribute.context);
            EntityTag ent = EntityTag.valueOf(attribute.getParam(), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag((ent != null && ent.isUnique() && ent.isSpawnedOrValidForTag()) ? "true" : "false")
                    .getObjectAttribute(attribute.fulfill(1)));
        }
        else if (attribute.startsWith("player_is_valid")
                && attribute.hasParam()) {
            Deprecations.isValidTag.warn(attribute.context);
            event.setReplacedObject(new ElementTag(PlayerTag.playerNameIsValid(attribute.getParam()))
                    .getObjectAttribute(attribute.fulfill(1)));
        }
        else if (attribute.startsWith("npc_is_valid")
                && attribute.hasParam()) {
            Deprecations.isValidTag.warn(attribute.context);
            NPCTag npc = NPCTag.valueOf(attribute.getParam(), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag((npc != null && npc.isValid()))
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_bossbars>
        // @returns ListTag
        // @description
        // Returns a list of all currently active boss bar IDs from <@link command bossbar>.
        // -->
        else if (attribute.startsWith("current_bossbars")) {
            ListTag dl = new ListTag(BossBarCommand.bossBarMap.keySet());
            event.setReplacedObject(dl.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.bossbar_viewers[<bossbar_id>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of players that should be able to see the given bossbar ID from <@link command bossbar>.
        // -->
        else if (attribute.startsWith("bossbar_viewers") && attribute.hasParam()) {
            BossBar bar = BossBarCommand.bossBarMap.get(CoreUtilities.toLowerCase(attribute.getParam()));
            if (bar != null) {
                ListTag list = new ListTag();
                for (Player player : bar.getPlayers()) {
                    list.addObject(new PlayerTag(player));
                }
                event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            }
        }

        // <--[tag]
        // @attribute <server.recent_tps>
        // @returns ListTag
        // @description
        // Returns the 3 most recent ticks per second measurements.
        // -->
        else if (attribute.startsWith("recent_tps")) {
            ListTag list = new ListTag();
            for (double tps : NMSHandler.instance.getRecentTps()) {
                list.addObject(new ElementTag(tps));
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.port>
        // @returns ElementTag(Number)
        // @description
        // Returns the port that the server is running on.
        // -->
        else if (attribute.startsWith("port")) {
            event.setReplacedObject(new ElementTag(NMSHandler.instance.getPort()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.debug_enabled>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether script debug is currently globally enabled on the server.
        // -->
        else if (attribute.startsWith("debug_enabled")) {
            event.setReplacedObject(new ElementTag(com.denizenscript.denizen.utilities.debugging.Debug.showDebug).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.last_reload>
        // @returns TimeTag
        // @description
        // Returns the time that Denizen scripts were last reloaded.
        // -->
        else if (attribute.startsWith("last_reload")) {
            event.setReplacedObject(new TimeTag(DenizenCore.lastReloadTime).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.idle_timeout>
        // @returns DurationTag
        // @mechanism server.idle_timeout
        // @description
        // Returns the server's current idle timeout limit (how long a player can sit still before getting kicked).
        // Internally used with <@link tag PlayerTag.last_action_time>.
        // -->
        else if (attribute.startsWith("idle_timeout")) {
            event.setReplacedObject(new DurationTag(Bukkit.getIdleTimeout() * 60).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.vanilla_tags>
        // @returns ListTag
        // @description
        // Returns a list of vanilla tags (applicable to blocks, fluids, or items). See also <@link url https://minecraft.fandom.com/wiki/Tag>.
        // -->
        else if (attribute.startsWith("vanilla_tags")) {
            event.setReplacedObject(new ListTag(VanillaTagHelper.tagsByKey.keySet()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.vanilla_tagged_materials[<tag>]>
        // @returns ListTag(MaterialTag)
        // @description
        // Returns a list of materials referred to by the specified vanilla tag. See also <@link url https://minecraft.fandom.com/wiki/Tag>.
        // -->
        else if (attribute.startsWith("vanilla_tagged_materials")) {
            if (!attribute.hasParam()) {
                return;
            }
            HashSet<Material> materials = VanillaTagHelper.tagsByKey.get(CoreUtilities.toLowerCase(attribute.getParam()));
            if (materials == null) {
                return;
            }
            ListTag list = new ListTag();
            for (Material material : materials) {
                list.addObject(new MaterialTag(material));
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.plugins_handling_event[<bukkit_event>]>
        // @returns ListTag(PluginTag)
        // @description
        // Returns a list of all plugins that handle a given Bukkit event.
        // Can specify by ScriptEvent name ("PlayerBreaksBlock"), or by full Bukkit class name ("org.bukkit.event.block.BlockBreakEvent").
        // This is a primarily a dev tool and is not necessarily useful to most players or scripts.
        // -->
        else if ((attribute.matches("plugins_handling_event") || attribute.matches("list_plugins_handling_event")) && attribute.hasParam()) {
            listDeprecateWarn(attribute);
            String eventName = attribute.getParam();
            if (CoreUtilities.contains(eventName, '.')) {
                try {
                    Class clazz = Class.forName(eventName, false, ServerTagBase.class.getClassLoader());
                    ListTag result = getHandlerPluginList(clazz);
                    if (result != null) {
                        event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
                    }
                }
                catch (ClassNotFoundException ex) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError(ex);
                    }
                }
            }
            else {
                ScriptEvent scriptEvent = ScriptEvent.eventLookup.get(CoreUtilities.toLowerCase(eventName));
                if (scriptEvent instanceof Listener) {
                    Plugin plugin = Denizen.getInstance();
                    for (Class eventClass : plugin.getPluginLoader()
                            .createRegisteredListeners((Listener) scriptEvent, plugin).keySet()) {
                        ListTag result = getHandlerPluginList(eventClass);
                        // Return results for the first valid match.
                        if (result != null && result.size() > 0) {
                            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
                            return;
                        }
                    }
                    event.setReplacedObject(new ListTag().getObjectAttribute(attribute.fulfill(1)));
                }
            }
        }

        // <--[tag]
        // @attribute <server.generate_loot_table[<map>]>
        // @returns ListTag(ItemTag)
        // @description
        // Returns a list of items from a loot table, given a map of input data.
        // Required input: id: the loot table ID, location: the location where it's being generated (LocationTag).
        // Optional inputs: killer: an online player (or player-type NPC) that is looting, entity: a dead entity being looted from (a valid EntityTag instance that is or was spawned in the world),
        // loot_bonus: the loot bonus level (defaults to -1) as an integer number, luck: the luck potion level (defaults to 0) as a decimal number.
        //
        // Some inputs will be strictly required for some loot tables, and ignored for others.
        //
        // A list of valid loot tables can be found here: <@link url https://minecraft.fandom.com/wiki/Loot_table#List_of_loot_tables>
        // Note that the tree view represented on the wiki should be split by slashes for the input - for example, "cow" is under "entities" in the tree so "entities/cow" is how you input that.
        // CAUTION: Invalid loot table IDs will generate an empty list rather than an error.
        //
        // @example
        // - give <server.generate_loot_table[id=chests/spawn_bonus_chest;killer=<player>;location=<player.location>]>
        // -->
        else if (attribute.startsWith("generate_loot_table") && attribute.hasParam()) {
            MapTag map = attribute.paramAsType(MapTag.class);
            ObjectTag idObj = map.getObject("id");
            ObjectTag locationObj = map.getObject("location");
            if (idObj == null || locationObj == null) {
                return;
            }
            NamespacedKey key = NamespacedKey.fromString(CoreUtilities.toLowerCase(idObj.toString()));
            if (key == null) {
                return;
            }
            LootTable table = Bukkit.getLootTable(key);
            LootContext.Builder context = new LootContext.Builder(locationObj.asType(LocationTag.class, attribute.context));
            ObjectTag killer = map.getObject("killer");
            ObjectTag luck = map.getObject("luck");
            ObjectTag bonus = map.getObject("loot_bonus");
            ObjectTag entity = map.getObject("entity");
            if (entity != null) {
                context = context.lootedEntity(entity.asType(EntityTag.class, attribute.context).getBukkitEntity());
            }
            if (killer != null) {
                context = context.killer((HumanEntity) killer.asType(EntityTag.class, attribute.context).getLivingEntity());
            }
            if (luck != null) {
                context = context.luck(luck.asElement().asFloat());
            }
            if (bonus != null) {
                context = context.lootingModifier(bonus.asElement().asInt());
            }
            Collection<ItemStack> items;
            try {
                items = table.populateLoot(CoreUtilities.getRandom(), context.build());
            }
            catch (Throwable ex) {
                attribute.echoError("Loot table failed to generate: " + ex.getMessage());
                if (CoreConfiguration.debugVerbose) {
                    attribute.echoError(ex);
                }
                return;
            }
            ListTag result = new ListTag();
            for (ItemStack item : items) {
                if (item != null && item.getType() != Material.AIR) {
                    result.addObject(new ItemTag(item));
                }
            }
            event.setReplacedObject(result.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.stack_trace>
        // @returns ElementTag
        // @description
        // Generates and shows a stack trace for the current context.
        // This tag is strictly for internal debugging reasons.
        // WARNING: Different Java versions generate different stack trace formats and details.
        // WARNING: Java internally limits stack trace generation in a variety of ways. This tag cannot be relied on to output anything.
        // -->
        else if (attribute.startsWith("stack_trace")) {
            String trace = com.denizenscript.denizen.utilities.debugging.Debug.getFullExceptionMessage(new RuntimeException("TRACE"), false);
            event.setReplacedObject(new ElementTag(trace).getObjectAttribute(attribute.fulfill(1)));
        }
    }

    public static void listDeprecateWarn(Attribute attribute) {
        if (CoreConfiguration.futureWarningsEnabled && attribute.getAttribute(1).startsWith("list_")) {
            Deprecations.listStyleTags.warn(attribute.context);
            Debug.echoError("Tag '" + attribute.getAttribute(1) + "' is deprecated: remove the 'list_' prefix.");
        }
    }

    public static ListTag getHandlerPluginList(Class eventClass) {
        if (Event.class.isAssignableFrom(eventClass)) {
            HandlerList handlers = BukkitScriptEvent.getEventListeners(eventClass);
            if (handlers != null) {
                ListTag result = new ListTag();
                HashSet<String> deduplicationSet = new HashSet<>();
                for (RegisteredListener listener : handlers.getRegisteredListeners()) {
                    if (deduplicationSet.add(listener.getPlugin().getName())) {
                        result.addObject(new PluginTag(listener.getPlugin()));
                    }
                }
                return result;
            }
        }
        return null;
    }

    // <--[ObjectType]
    // @name server
    // @prefix None
    // @base None
    // @ExampleAdjustObject server
    // @format
    // N/A
    //
    // @description
    // "server" is an internal pseudo-ObjectType that is used as a mechanism adjust target for some global mechanisms.
    //
    // -->

    public static void adjustServer(Mechanism mechanism) {

        // <--[mechanism]
        // @object server
        // @name delete_file
        // @input ElementTag
        // @description
        // Deletes the given file from the server.
        // File path starts in the Denizen folder.
        // Require config file setting "Commands.Delete.Allow file deletion".
        // @example
        // - adjust server delete_file:schematics/house.schem
        // @tags
        // <server.has_file[<file>]>
        // -->
        if (mechanism.matches("delete_file") && mechanism.hasValue()) {
            if (!Settings.allowDelete()) {
                Debug.echoError("File deletion disabled by administrator (refer to mechanism documentation).");
                return;
            }
            File file = new File(Denizen.getInstance().getDataFolder(), mechanism.getValue().asString());
            if (!Utilities.canWriteToFile(file)) {
                Debug.echoError("Cannot write to that file path due to security settings in Denizen/config.yml.");
                return;
            }
            try {
                if (!file.delete()) {
                    Debug.echoError("Failed to delete file: returned false");
                }
            }
            catch (Exception e) {
                Debug.echoError("Failed to delete file: " + e.getMessage());
            }
        }

        if (mechanism.matches("redirect_logging") && mechanism.hasValue()) {
            Deprecations.serverRedirectLogging.warn(mechanism.context);
            if (!CoreConfiguration.allowConsoleRedirection) {
                Debug.echoError("Console redirection disabled by administrator.");
                return;
            }
            if (mechanism.getValue().asBoolean()) {
                DenizenCore.logInterceptor.redirectOutput();
            }
            else {
                DenizenCore.logInterceptor.standardOutput();
            }
        }

        // <--[mechanism]
        // @object server
        // @name reset_event_stats
        // @input None
        // @description
        // Resets the statistics on events used for <@link tag util.event_stats>
        // @tags
        // <util.event_stats>
        // <util.event_stats_data>
        // -->
        if (mechanism.matches("reset_event_stats")) {
            for (ScriptEvent se : ScriptEvent.events) {
                se.eventData.stats_fires = 0;
                se.eventData.stats_scriptFires = 0;
                se.eventData.stats_nanoTimes = 0;
            }
        }

        // <--[mechanism]
        // @object server
        // @name reset_recipes
        // @input None
        // @description
        // Resets the server's recipe list to the default vanilla recipe list + item script recipes.
        // @tags
        // <server.recipe_ids[(<type>)]>
        // -->
        if (mechanism.matches("reset_recipes")) {
            Bukkit.resetRecipes();
            Denizen.getInstance().itemScriptHelper.rebuildRecipes();
        }

        // <--[mechanism]
        // @object server
        // @name remove_recipes
        // @input ListTag
        // @description
        // Removes a recipe or list of recipes from the server, in Namespace:Key format.
        // @example
        // - adjust server remove_recipes:<item[torch].recipe_ids>
        // @tags
        // <server.recipe_ids[(<type>)]>
        // -->
        if (mechanism.matches("remove_recipes")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                NMSHandler.itemHelper.removeRecipe(Utilities.parseNamespacedKey(str));
            }
        }

        // <--[mechanism]
        // @object server
        // @name idle_timeout
        // @input DurationTag
        // @description
        // Sets the server's current idle timeout limit (how long a player can sit still before getting kicked).
        // Will be rounded to the nearest number of minutes.
        // Set to 0 to disable automatic timeout kick.
        // @tags
        // <server.idle_timeout>
        // -->
        if (mechanism.matches("idle_timeout") && mechanism.requireObject(DurationTag.class)) {
            Bukkit.setIdleTimeout((int) Math.round(mechanism.valueAsType(DurationTag.class).getSeconds() / 60));
        }

        // <--[mechanism]
        // @object server
        // @name cleanmem
        // @input None
        // @description
        // Suggests to the internal systems that it's a good time to clean the memory.
        // Does NOT force a memory cleaning.
        // This should generally not be used unless you have a very good specific reason to use it.
        // @tags
        // <server.ram_free>
        // -->
        if (mechanism.matches("cleanmem")) {
            System.gc();
        }

        // <--[mechanism]
        // @object server
        // @name restart
        // @input None
        // @description
        // Immediately stops the server entirely (Plugins will still finalize, and the shutdown event will fire), then starts it again.
        // Requires config file setting "Commands.Restart.Allow server restart"!
        // Note that if your server is not configured to restart, this mechanism will simply stop the server without starting it again!
        // -->
        if (mechanism.matches("restart")) {
            if (!Settings.allowServerRestart()) {
                Debug.echoError("Server restart disabled by administrator (refer to mechanism documentation). Consider using 'shutdown'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server restarted by a Denizen script, see config to prevent this!");
            Bukkit.spigot().restart();
        }

        // <--[mechanism]
        // @object server
        // @name save
        // @input None
        // @description
        // Immediately saves the Denizen saves files.
        // -->
        if (mechanism.matches("save")) {
            DenizenCore.saveAll();
            Denizen.getInstance().saveSaves(true);
        }

        // <--[mechanism]
        // @object server
        // @name save_citizens
        // @input None
        // @description
        // Immediately saves the Citizens saves files.
        // -->
        if (Depends.citizens != null && mechanism.matches("save_citizens")) {
            Depends.citizens.storeNPCs(new CommandContext(new String[0]));
        }

        // <--[mechanism]
        // @object server
        // @name shutdown
        // @input None
        // @description
        // Immediately stops the server entirely (Plugins will still finalize, and the shutdown event will fire).
        // The server will remain shutdown until externally started again.
        // Requires config file setting "Commands.Restart.Allow server stop"!
        // -->
        if (mechanism.matches("shutdown")) {
            if (!Settings.allowServerStop()) {
                Debug.echoError("Server stop disabled by administrator (refer to mechanism documentation). Consider using 'restart'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server shutdown by a Denizen script, see config to prevent this!");
            Bukkit.shutdown();
        }

        // <--[mechanism]
        // @object server
        // @name has_whitelist
        // @input ElementTag(Boolean)
        // @description
        // Toggles whether the server's whitelist is enabled.
        // @tags
        // <server.has_whitelist>
        // -->
        if (mechanism.matches("has_whitelist") && mechanism.requireBoolean()) {
            Bukkit.setWhitelist(mechanism.getValue().asBoolean());
        }
    }
}

package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.commands.server.BossBarCommand;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.core.AdjustCommand;
import com.denizenscript.denizencore.scripts.commands.core.SQLCommand;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.PseudoObjectTagBase;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.core.UtilTagBase;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.citizensnpcs.api.CitizensAPI;
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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.*;
import org.bukkit.util.permissions.DefaultPermissions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class ServerTagBase extends PseudoObjectTagBase<ServerTagBase> {

    public static ServerTagBase instance;

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

    public ServerTagBase() {
        instance = this;
        TagManager.registerStaticTagBaseHandler(ServerTagBase.class, "server", (t) -> instance);
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "global", (attribute) -> {
            BukkitImplDeprecations.globalTagName.warn(attribute.context);
            return null;
        });
        AdjustCommand.specialAdjustables.put("server", mechanism -> tagProcessor.processMechanism(instance, mechanism));
    }

    @Override
    public void register() {
        tagProcessor.registerTag(ElementTag.class, "economy", (attribute, object) -> {
            if (Depends.economy == null) {
                attribute.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                return null;
            }

            // <--[tag]
            // @attribute <server.economy.format[<#.#>]>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the amount of money, formatted according to the server's economy.
            // -->
            if (attribute.startsWith("format", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                return new ElementTag(Depends.economy.format(attribute.getDoubleParam()));
            }

            // <--[tag]
            // @attribute <server.economy.currency_name[<#.#>]>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (automatically singular or plural based on input value).
            // -->
            if (attribute.startsWith("currency_name", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                return new ElementTag(attribute.getDoubleParam() == 1 ? Depends.economy.currencyNameSingular() : Depends.economy.currencyNamePlural());
            }

            // <--[tag]
            // @attribute <server.economy.currency_plural>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (in the plural form, like "Dollars").
            // -->
            if (attribute.startsWith("currency_plural", 2)) {
                attribute.fulfill(1);
                return new ElementTag(Depends.economy.currencyNamePlural());
            }

            // <--[tag]
            // @attribute <server.economy.currency_singular>
            // @returns ElementTag
            // @plugin Vault
            // @description
            // Returns the server's economy currency name (in the singular form, like "Dollar").
            // -->
            if (attribute.startsWith("currency_singular", 2)) {
                attribute.fulfill(1);
                return new ElementTag(Depends.economy.currencyNameSingular());
            }

            return null;
        });

        // <--[tag]
        // @attribute <server.slot_id[<slot>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the slot ID number for an input slot (see <@link language Slot Inputs>).
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "slot_id", (attribute, object, input) -> {
            int slotId = SlotHelper.nameToIndex(input.asString(), null);
            return slotId != -1 ? new ElementTag(slotId) : null;
        });

        // <--[tag]
        // @attribute <server.parse_bukkit_item[<serial>]>
        // @returns ItemTag
        // @description
        // Returns the ItemTag resultant from parsing Bukkit item serialization data (under subkey "item").
        // -->
        tagProcessor.registerTag(ItemTag.class, ElementTag.class, "parse_bukkit_item", (attribute, object, input) -> {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(input.asString());
                ItemStack item = config.getItemStack("item");
                if (item != null) {
                    return new ItemTag(item);
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            return null;
        });

        // <--[tag]
        // @attribute <server.recipe_ids[(<type>)]>
        // @returns ListTag
        // @description
        // Returns a list of all recipe IDs on the server.
        // Returns a list in the Namespace:Key format, for example "minecraft:gold_nugget".
        // Optionally, specify a recipe type (CRAFTING, FURNACE, COOKING, BLASTING, SHAPED, SHAPELESS, SMOKING, CAMPFIRE, STONECUTTING, SMITHING, BREWING)
        // to limit to just recipes of that type.
        // Brewing recipes are only supported on Paper, and only custom ones are available.
        // Note: this will produce an error if all recipes of any one type have been removed from the server, due to an error in Spigot.
        // -->
        tagProcessor.registerTag(ListTag.class, "recipe_ids", (attribute, object) -> {
            listDeprecateWarn(attribute);
            String type = attribute.hasParam() ? CoreUtilities.toLowerCase(attribute.getParam()) : null;
            ListTag recipeIds = new ListTag();
            if (type == null || !type.equals("brewing")) {
                Bukkit.recipeIterator().forEachRemaining(recipe -> {
                    if (recipe instanceof Keyed keyedRecipe && Utilities.isRecipeOfType(recipe, type)) {
                        recipeIds.add(keyedRecipe.getKey().toString());
                    }
                });
            }
            if (Denizen.supportsPaper && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && (type == null || type.equals("brewing"))) {
                for (NamespacedKey brewingRecipe : NMSHandler.itemHelper.getCustomBrewingRecipes().keySet()) {
                    recipeIds.add(brewingRecipe.toString());
                }
            }
            return recipeIds;
        }, "list_recipe_ids");

        // <--[tag]
        // @attribute <server.recipe_items[<id>]>
        // @returns ListTag(ItemTag)
        // @description
        // Returns a list of the items used as input to the recipe within the input ID.
        // This is formatted equivalently to the item script recipe input, with "material:" for non-exact matches, and a full ItemTag for exact matches.
        // Note that this won't represent all recipes perfectly (primarily those with multiple input choices per slot).
        // Brewing recipes are only supported on Paper, and only custom ones are available.
        // For brewing recipes, currently "matcher:<item matcher>" input options are only supported in recipes added by Denizen.
        // For furnace-style and stonecutting recipes, this will return a list with only 1 item.
        // For shaped recipes, this will include 'air' for slots that are part of the shape but don't require an item.
        // For smithing recipes, this will return a list with the 'base' item and the 'addition'.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "recipe_items", (attribute, object, input) -> {
            NamespacedKey recipeKey = Utilities.parseNamespacedKey(input.asString());
            Recipe recipe = Bukkit.getRecipe(recipeKey);
            ItemHelper.BrewingRecipe brewingRecipe = Denizen.supportsPaper && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) ? NMSHandler.itemHelper.getCustomBrewingRecipes().get(recipeKey) : null;
            if (recipe == null && brewingRecipe == null) {
                return null;
            }
            ListTag recipeItems = new ListTag();
            Consumer<RecipeChoice> addChoice = (choice) -> {
                if (choice == null) {
                    recipeItems.addObject(new ItemTag(Material.AIR));
                }
                else {
                    if (choice instanceof RecipeChoice.ExactChoice) {
                        recipeItems.addObject(new ItemTag(choice.getItemStack()));
                    }
                    else {
                        recipeItems.add("material:" + choice.getItemStack().getType().name());
                    }
                }
            };
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();
                for (String row : shapedRecipe.getShape()) {
                    for (char column : row.toCharArray()) {
                        addChoice.accept(choiceMap.get(column));
                    }
                }
            }
            else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                for (RecipeChoice choice : shapelessRecipe.getChoiceList()) {
                    addChoice.accept(choice);
                }
            }
            else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                addChoice.accept(cookingRecipe.getInputChoice());
            }
            else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
                addChoice.accept(stonecuttingRecipe.getInputChoice());
            }
            else if (recipe instanceof SmithingRecipe smithingRecipe) {
                addChoice.accept(smithingRecipe.getBase());
                addChoice.accept(smithingRecipe.getAddition());
            }
            else if (brewingRecipe != null) {
                if (brewingRecipe.ingredient() instanceof RecipeChoice.ExactChoice || brewingRecipe.ingredient() instanceof RecipeChoice.MaterialChoice) {
                    addChoice.accept(brewingRecipe.ingredient());
                }
                else {
                    recipeItems.addObject(new ElementTag(PaperAPITools.instance.getBrewingRecipeIngredientMatcher(recipeKey), true));
                }
                if (brewingRecipe.input() instanceof RecipeChoice.ExactChoice || brewingRecipe.input() instanceof RecipeChoice.MaterialChoice) {
                    addChoice.accept(brewingRecipe.input());
                }
                else {
                    recipeItems.addObject(new ElementTag(PaperAPITools.instance.getBrewingRecipeInputMatcher(recipeKey), true));
                }
            }
            return recipeItems;
        });

        // <--[tag]
        // @attribute <server.recipe_shape[<id>]>
        // @returns ElementTag
        // @description
        // Returns the shape of a shaped recipe, like '2x2' or '3x3'.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "recipe_shape", (attribute, object, input) -> {
            if (Bukkit.getRecipe(Utilities.parseNamespacedKey(input.asString())) instanceof ShapedRecipe shapedRecipe) {
                String[] shape = shapedRecipe.getShape();
                return new ElementTag(shape[0].length() + "x" + shape.length);
            }
            return null;
        });

        // <--[tag]
        // @attribute <server.recipe_type[<id>]>
        // @returns ElementTag
        // @description
        // Returns the type of recipe that the given recipe ID is.
        // Will be one of FURNACE, BLASTING, SHAPED, SHAPELESS, SMOKING, CAMPFIRE, STONECUTTING, SMITHING, BREWING.
        // Brewing recipes are only supported on Paper, and only custom ones are available.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "recipe_type", (attribute, object, input) -> {
            NamespacedKey recipeKey = Utilities.parseNamespacedKey(input.asString());
            Recipe recipe = Bukkit.getRecipe(recipeKey);
            if (recipe != null) {
                return new ElementTag(Utilities.getRecipeType(recipe));
            }
            if (Denizen.supportsPaper && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && NMSHandler.itemHelper.getCustomBrewingRecipes().containsKey(recipeKey)) {
                return new ElementTag("brewing");
            }
            return null;
        });

        // <--[tag]
        // @attribute <server.recipe_result[<id>]>
        // @returns ItemTag
        // @description
        // Returns the item that a recipe will create when crafted.
        // Brewing recipes are only supported on Paper, and only custom ones are available.
        // -->
        tagProcessor.registerTag(ItemTag.class, ElementTag.class, "recipe_result", (attribute, object, input) -> {
            NamespacedKey recipeKey = Utilities.parseNamespacedKey(input.asString());
            Recipe recipe = Bukkit.getRecipe(recipeKey);
            if (recipe != null) {
                return new ItemTag(recipe.getResult());
            }
            if (Denizen.supportsPaper && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                ItemHelper.BrewingRecipe brewingRecipe = NMSHandler.itemHelper.getCustomBrewingRecipes().get(recipeKey);
                if (brewingRecipe != null) {
                    return new ItemTag(brewingRecipe.result());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <server.scoreboards>
        // @returns ListTag
        // @description
        // Returns a list of scoreboard IDs currently registered on the server.
        // -->
        tagProcessor.registerTag(ListTag.class, "scoreboards", (attribute, object) -> {
            ListTag scoreboards = new ListTag(ScoreboardHelper.scoreboardMap.size());
            for (String board : ScoreboardHelper.scoreboardMap.keySet()) {
                scoreboards.addObject(new ElementTag(board, true));
            }
            return scoreboards;
        });

        tagProcessor.registerTag(ObjectTag.class, "scoreboard", (attribute, object) -> {
            String name = "main";
            Scoreboard board;
            if (attribute.hasParam()) {
                name = attribute.getParam();
                board = ScoreboardHelper.getScoreboard(name);
            }
            else {
                board = ScoreboardHelper.getMain();
            }

            // <--[tag]
            // @attribute <server.scoreboard[<board>].exists>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether a given scoreboard exists on the server.
            // -->
            if (attribute.startsWith("exists", 2)) {
                attribute.fulfill(1);
                return new ElementTag(board != null);
            }
            if (board == null) {
                attribute.echoError("Scoreboard '" + name + "' does not exist.");
                return null;
            }

            // <--[tag]
            // @attribute <server.scoreboard[(<board>)].objectives>
            // @returns ListTag
            // @description
            // Returns a list of all objective names in the scoreboard.
            // Optionally, specify which scoreboard to use.
            // -->
            if (attribute.startsWith("objectives", 2)) {
                attribute.fulfill(1);
                ListTag objectives = new ListTag();
                for (Objective objective : board.getObjectives()) {
                    objectives.add(objective.getName());
                }
                return objectives;
            }

            if (attribute.startsWith("objective", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                Objective objective = board.getObjective(attribute.getParam());
                if (objective == null) {
                    attribute.echoError("Scoreboard objective '" + attribute.getParam() + "' does not exist.");
                    return null;
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].criteria>
                // @returns ElementTag
                // @description
                // Returns the criteria specified for the given objective.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("criteria", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(objective.getCriteria()); // TODO: once minimum supported version is 1.19 or above, use getTrackedCriteria().getName()
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].display_name>
                // @returns ElementTag
                // @description
                // Returns the display name specified for the given objective.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("display_name", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(objective.getDisplayName());
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].display_slot>
                // @returns ElementTag
                // @description
                // Returns the display slot specified for the given objective. Can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/scoreboard/DisplaySlot.html>.
                // Note that not all objectives have a display slot.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("display_slot", 2)) {
                    attribute.fulfill(1);
                    DisplaySlot displaySlot = objective.getDisplaySlot();
                    return displaySlot != null ? new ElementTag(displaySlot) : null;
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].objective[<name>].score[<input>]>
                // @returns ElementTag(Number)
                // @description
                // Returns the current score in the objective for the given input.
                // Input can be a PlayerTag (translates to name internally), EntityTag (translates to UUID internally) or any plaintext score holder label.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("score", 2) && attribute.hasContext(2)) {
                    attribute.fulfill(1);
                    String value;
                    ObjectTag param = attribute.getParamObject();
                    if (param.shouldBeType(PlayerTag.class)) {
                        value = param.asType(PlayerTag.class, attribute.context).getName();
                    }
                    else if (param.shouldBeType(EntityTag.class)) {
                        value = param.asType(EntityTag.class, attribute.context).getUUID().toString();
                    }
                    else {
                        value = param.toString();
                    }
                    Score score = objective.getScore(value);
                    return score.isScoreSet() ? new ElementTag(score.getScore()) : null;
                }
            }

            // <--[tag]
            // @attribute <server.scoreboard[(<board>)].team_names>
            // @returns ListTag
            // @description
            // Returns a list of the names of all teams within the scoreboard.
            // Optionally, specify which scoreboard to use.
            // -->
            if (attribute.startsWith("team_names", 2)) {
                attribute.fulfill(1);
                ListTag teams = new ListTag();
                for (Team team : board.getTeams()) {
                    teams.add(team.getName());
                }
                return teams;
            }

            if (attribute.startsWith("team", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                Team team = board.getTeam(attribute.getParam());
                if (team == null) {
                    attribute.echoError("Scoreboard team '" + attribute.getParam() + "' does not exist.");
                    return null;
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].team[<team>].members>
                // @returns ListTag
                // @description
                // Returns a list of all members of a scoreboard team. Generally returns as a list of names or text entries.
                // Members are not necessarily written in any given format and are not guaranteed to validly fit any requirements.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("members", 2)) {
                    attribute.fulfill(1);
                    return new ListTag(team.getEntries());
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].team[<team>].prefix>
                // @returns ElementTag
                // @description
                // Returns the team's prefix.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("prefix", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(PaperAPITools.instance.getTeamPrefix(team));
                }

                // <--[tag]
                // @attribute <server.scoreboard[(<board>)].team[<team>].suffix>
                // @returns ElementTag
                // @description
                // Returns the team's suffix.
                // Optionally, specify which scoreboard to use.
                // -->
                if (attribute.startsWith("suffix", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(PaperAPITools.instance.getTeamSuffix(team));
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <server.object_is_valid[<object>]>
        // @returns ElementTag(Boolean)
        // @deprecated Use 'exists' or 'is_truthy'
        // @description
        // Deprecated in favor of <@link tag ObjectTag.exists> or <@link tag ObjectTag.is_truthy>
        // -->
        tagProcessor.registerTag(ElementTag.class, ObjectTag.class, "object_is_valid", (attribute, object, input) -> {
            BukkitImplDeprecations.serverObjectExistsTags.warn(attribute.context);
            ObjectTag o = ObjectFetcher.pickObjectFor(input.toString(), CoreUtilities.noDebugContext);
            return new ElementTag(!(o == null || o instanceof ElementTag));
        });

        // <--[tag]
        // @attribute <server.has_whitelist>
        // @returns ElementTag(Boolean)
        // @mechanism server.has_whitelist
        // @description
        // Returns whether the server's whitelist is active.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_whitelist", (attribute, object) -> {
            return new ElementTag(Bukkit.hasWhitelist());
        });

        // <--[tag]
        // @attribute <server.whitelisted_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players whitelisted on this server.
        // -->
        tagProcessor.registerTag(ListTag.class, "whitelisted_players", (attribute, object) -> {
            ListTag whitelisted = new ListTag();
            for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                whitelisted.addObject(new PlayerTag(player));
            }
            return whitelisted;
        });

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // See <@link tag FlaggableObject.has_flag>
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "has_flag", (attribute, object, input) -> {
            return DenizenCore.serverFlagMap.doHasFlagTag(attribute);
        });

        // <--[tag]
        // @attribute <server.flag_expiration[<flag_name>]>
        // @returns TimeTag
        // @description
        // See <@link tag FlaggableObject.flag_expiration>
        // -->
        tagProcessor.registerTag(TimeTag.class, ElementTag.class, "flag_expiration", (attribute, object, input) -> {
            return DenizenCore.serverFlagMap.doFlagExpirationTag(attribute);
        });
        // <--[tag]
        // @attribute <server.flag[<flag_name>]>
        // @returns ObjectTag
        // @description
        // See <@link tag FlaggableObject.flag>
        // -->
        tagProcessor.registerTag(ObjectTag.class, ElementTag.class, "flag", (attribute, object, input) -> {
            return DenizenCore.serverFlagMap.doFlagTag(attribute);
        });

        // <--[tag]
        // @attribute <server.list_flags>
        // @returns ListTag
        // @description
        // See <@link tag FlaggableObject.list_flags>
        // -->
        tagProcessor.registerTag(ListTag.class, "list_flags", (attribute, object) -> {
            return DenizenCore.serverFlagMap.doListFlagsTag(attribute);
        });

        // <--[tag]
        // @attribute <server.flag_map[<name>|...]>
        // @returns MapTag
        // @description
        // See <@link tag FlaggableObject.flag_map>
        // -->
        tagProcessor.registerTag(MapTag.class, "flag_map", (attribute, object) -> {
            return DenizenCore.serverFlagMap.doFlagMapTag(attribute);
        });

        // <--[tag]
        // @attribute <server.gamerules>
        // @returns ListTag
        // @description
        // Returns a list of all available gamerules on the server.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "gamerules", (attribute, object) -> {
            ListTag gamerules = new ListTag();
            for (GameRule<?> rule : GameRule.values()) {
                gamerules.add(rule.getName());
            }
            return gamerules;
        });

        // <--[tag]
        // @attribute <server.commands>
        // @returns ListTag
        // @description
        // Returns a list of all registered command names in Bukkit.
        // -->
        tagProcessor.registerTag(ListTag.class, "commands", (attribute, object) -> {
            listDeprecateWarn(attribute);
            CommandScriptHelper.init();
            return new ListTag(CommandScriptHelper.knownCommands.keySet());
        }, "list_commands");

        // <--[tag]
        // @attribute <server.command_plugin[<name>]>
        // @returns PluginTag
        // @description
        // Returns the plugin that created a command (if known).
        // @example
        // # Should show "Denizen".
        // - narrate <server.command_plugin[ex].name>
        // -->
        tagProcessor.registerTag(PluginTag.class, ElementTag.class, "command_plugin", (attribute, object, input) -> {
            PluginCommand command = Bukkit.getPluginCommand(input.asString());
            return command != null ? new PluginTag(command.getPlugin()) : null;
        });

        // <--[tag]
        // @attribute <server.art_types>
        // @returns ListTag
        // @description
        // Returns a list of all known art types.
        // Generally used with <@link tag EntityTag.painting> and <@link mechanism EntityTag.painting>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Art.html>.
        // -->
        registerEnumListTag("art_types", Art.class);

        // <--[tag]
        // @attribute <server.advancement_types>
        // @returns ListTag
        // @description
        // Returns a list of all registered advancement names.
        // Generally used with <@link tag PlayerTag.has_advancement>.
        // See also <@link url https://minecraft.wiki/w/Advancement>.
        // -->
        tagProcessor.registerTag(ListTag.class, "advancement_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag advancements = new ListTag();
            Bukkit.advancementIterator().forEachRemaining(adv -> advancements.add(adv.getKey().toString()));
            return advancements;
        }, "list_advancements");

        // <--[tag]
        // @attribute <server.nbt_attribute_types>
        // @returns ListTag
        // @description
        // Returns a list of all registered attribute names.
        // Generally used with <@link tag EntityTag.has_attribute>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>.
        // -->
        registerEnumListTag("nbt_attribute_types", org.bukkit.attribute.Attribute.class, "list_nbt_attribute_types");

        // <--[tag]
        // @attribute <server.damage_causes>
        // @returns ListTag
        // @description
        // Returns a list of all registered damage causes.
        // Generally used with <@link event entity damaged>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html>.
        // -->
        registerEnumListTag("damage_causes", EntityDamageEvent.DamageCause.class, "list_damage_causes");

        // <--[tag]
        // @attribute <server.teleport_causes>
        // @returns ListTag
        // @description
        // Returns a list of all registered player teleport causes.
        // Generally used with <@link event entity teleports>.
        // See <@link language teleport cause> for the current list of causes.
        // -->
        registerEnumListTag("teleport_causes", PlayerTeleportEvent.TeleportCause.class);

        // <--[tag]
        // @attribute <server.biome_types>
        // @returns ListTag(BiomeTag)
        // @description
        // Returns a list of all biomes known to the server.
        // Generally used with <@link objecttype BiomeTag>.
        // This is based on Bukkit Biome enum, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html>.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "biome_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag biomes = new ListTag();
            for (Biome biome : Biome.values()) {
                BiomeTag biomeTag = new BiomeTag(biome);
                if (biomeTag.getBiome() != null) {
                    biomes.addObject(biomeTag);
                }
            }
            return biomes;
        }, "list_biome_types");

        // <--[tag]
        // @attribute <server.enchantments>
        // @returns ListTag(EnchantmentTag)
        // @description
        // Returns a list of all enchantments known to the server.
        // -->
        tagProcessor.registerTag(ListTag.class, "enchantments", (attribute, object) -> {
            ListTag enchantments = new ListTag();
            for (Enchantment enchantment : Enchantment.values()) {
                enchantments.addObject(new EnchantmentTag(enchantment));
            }
            return enchantments;
        });

        tagProcessor.registerTag(ListTag.class, "enchantment_types", (attribute, object) -> {
            BukkitImplDeprecations.echantmentTagUpdate.warn(attribute.context);
            listDeprecateWarn(attribute);
            ListTag enchants = new ListTag();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getName());
            }
            return enchants;
        }, "list_enchantments");

        tagProcessor.registerTag(ListTag.class, "enchantment_keys", (attribute, object) -> {
            BukkitImplDeprecations.echantmentTagUpdate.warn(attribute.context);
            listDeprecateWarn(attribute);
            ListTag enchants = new ListTag();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getKey().getKey());
            }
            return enchants;
        }, "list_enchantment_keys");

        // <--[tag]
        // @attribute <server.entity_types>
        // @returns ListTag
        // @description
        // Returns a list of all entity types known to the server.
        // Generally used with <@link objecttype EntityTag>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html>.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "entity_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag entityTypes = new ListTag();
            for (EntityType entityType : EntityType.values()) {
                if (entityType != EntityType.UNKNOWN) {
                    entityTypes.add(entityType.name());
                }
            }
            return entityTypes;
        }, "list_entity_types");

        // <--[tag]
        // @attribute <server.material_types>
        // @returns ListTag(MaterialTag)
        // @description
        // Returns a list of all materials known to the server.
        // Generally used with <@link objecttype MaterialTag>.
        // This is only types listed in the Bukkit Material enum, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "material_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag materials = new ListTag();
            for (Material material : Material.values()) {
                materials.addObject(new MaterialTag(material));
            }
            return materials;
        }, "list_material_types");

        // <--[tag]
        // @attribute <server.sound_types>
        // @returns ListTag
        // @description
        // Returns a list of all sounds known to the server.
        // Generally used with <@link command playsound>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>.
        // -->
        registerEnumListTag("sound_types", Sound.class, "list_sounds");

        // <--[tag]
        // @attribute <server.particle_types>
        // @returns ListTag
        // @description
        // Returns a list of all particle effect types known to the server.
        // Generally used with <@link command playeffect>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html>.
        // Refer also to <@link tag server.effect_types>.
        // -->
        registerEnumListTag("particle_types", Particle.class, "list_particles");

        // <--[tag]
        // @attribute <server.effect_types>
        // @returns ListTag
        // @description
        // Returns a list of all 'effect' types known to the server.
        // Generally used with <@link command playeffect>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html>.
        // Refer also to <@link tag server.particle_types>.
        // -->
        registerEnumListTag("effect_types", Effect.class, "list_effects");

        // <--[tag]
        // @attribute <server.pattern_types>
        // @returns ListTag
        // @description
        // Returns a list of all banner patterns known to the server.
        // Generally used with <@link tag ItemTag.patterns>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // -->
        registerEnumListTag("pattern_types", PatternType.class, "list_patterns");

        // <--[tag]
        // @attribute <server.potion_effect_types>
        // @returns ListTag
        // @description
        // Returns a list of all potion effects known to the server.
        // Can be used with <@link command cast>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // Refer also to <@link tag server.potion_types>.
        // -->
        tagProcessor.registerTag(ListTag.class, "potion_effect_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag potionEffects = new ListTag();
            for (PotionEffectType potionEffect : PotionEffectType.values()) {
                if (potionEffect != null) {
                    potionEffects.add(potionEffect.getName());
                }
            }
            return potionEffects;
        }, "list_potion_effects");

        // <--[tag]
        // @attribute <server.potion_types>
        // @returns ListTag
        // @description
        // Returns a list of all potion types known to the server.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // Refer also to <@link tag server.potion_effect_types>.
        // -->
        registerEnumListTag("potion_types", PotionType.class, "list_potion_types");

        // <--[tag]
        // @attribute <server.tree_types>
        // @returns ListTag
        // @description
        // Returns a list of all tree types known to the server.
        // Generally used with <@link mechanism LocationTag.generate_tree>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/TreeType.html>.
        // -->
        registerEnumListTag("tree_types", TreeType.class, "list_tree_types");

        // <--[tag]
        // @attribute <server.map_cursor_types>
        // @returns ListTag
        // @description
        // Returns a list of all map cursor types known to the server.
        // Generally used with <@link command map> and <@link language Map Script Containers>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapCursor.Type.html>.
        // -->
        registerEnumListTag("map_cursor_types", MapCursor.Type.class, "list_map_cursor_types");

        // <--[tag]
        // @attribute <server.world_types>
        // @returns ListTag
        // @description
        // Returns a list of all world types known to the server.
        // Generally used with <@link command createworld>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>.
        // -->
        registerEnumListTag("world_types", WorldType.class, "list_world_types");

        // <--[tag]
        // @attribute <server.statistic_types[(<type>)]>
        // @returns ListTag
        // @description
        // Returns a list of all statistic types known to the server.
        // Generally used with <@link tag PlayerTag.statistic>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html>.
        // Optionally, specify a type to limit to statistics of a given type. Can be any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.Type.html>.
        // Refer also to <@link tag server.statistic_type>.
        // -->
        tagProcessor.registerStaticTag(ListTag.class, "statistic_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            Statistic.Type type = attribute.hasParam() ? attribute.getParamElement().asEnum(Statistic.Type.class) : null;
            ListTag statistics = new ListTag();
            for (Statistic statistic : Statistic.values()) {
                if (type == null || type == statistic.getType()) {
                    statistics.add(statistic.name());
                }
            }
            return statistics;
        }, "list_statistics");

        // <--[tag]
        // @attribute <server.structure_types>
        // @returns ListTag
        // @deprecated use 'server.structures' on 1.19+.
        // @description
        // Deprecated in favor of <@link tag server.structures> on 1.19+.
        // -->
        tagProcessor.registerTag(ListTag.class, "structure_types", (attribute, object) -> {
            listDeprecateWarn(attribute);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                BukkitImplDeprecations.oldStructureTypes.warn(attribute.context);
            }
            return new ListTag(StructureType.getStructureTypes().keySet());
        }, "list_structure_types");

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[tag]
            // @attribute <server.structures>
            // @returns ListTag
            // @description
            // Returns a list of all structures known to the server, including custom ones added by datapacks.
            // For more information and a list of default structures, see <@link url https://minecraft.wiki/w/Structure>.
            // For locating specific structures, see <@link language Structure lookups>.
            // -->
            tagProcessor.registerTag(ListTag.class, "structures", (attribute, object) -> {
                return new ListTag(Registry.STRUCTURE.stream().toList(), structure -> new ElementTag(Utilities.namespacedKeyToString(structure.getKey()), true));
            });
        }

        // <--[tag]
        // @attribute <server.statistic_type[<statistic>]>
        // @returns ElementTag
        // @description
        // Returns the qualifier type of the given statistic, will be one of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.Type.html>.
        // Generally relevant to usage with <@link tag PlayerTag.statistic.qualifier>.
        // Refer also to <@link tag server.statistic_types>.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "statistic_type", (attribute, object, input) -> {
            Statistic statistic = input.asEnum(Statistic.class);
            if (statistic == null) {
                attribute.echoError("Statistic '" + input + "' does not exist.");
                return null;
            }
            return new ElementTag(statistic.getType());
        });

        tagProcessor.registerTag(ElementTag.class, EnchantmentTag.class, "enchantment_max_level", (attribute, object, input) -> {
            BukkitImplDeprecations.echantmentTagUpdate.warn(attribute.context);
            return new ElementTag(input.enchantment.getMaxLevel());
        });
        tagProcessor.registerTag(ElementTag.class, EnchantmentTag.class, "enchantment_start_level", (attribute, object, input) -> {
            BukkitImplDeprecations.echantmentTagUpdate.warn(attribute.context);
            return new ElementTag(input.enchantment.getStartLevel());
        });

        // Historical variants of "notes" tag
        tagProcessor.registerTag(ListTag.class, "notables", (attribute, object) -> {
            listDeprecateWarn(attribute);
            BukkitImplDeprecations.serverUtilTags.warn(attribute.context);
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
            return allNotables;
        }, "list_notables");
        // Historical variant of "sql_connections" tag
        tagProcessor.registerTag(ListTag.class, "list_sql_connections", (attribute, object) -> {
            BukkitImplDeprecations.listStyleTags.warn(attribute.context);
            BukkitImplDeprecations.serverUtilTags.warn(attribute.context);
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
            return list;
        });
        // Historical variant of "scripts" tag
        tagProcessor.registerTag(ListTag.class, "list_scripts", (attribute, object) -> {
            BukkitImplDeprecations.listStyleTags.warn(attribute.context);
            BukkitImplDeprecations.serverUtilTags.warn(attribute.context);
            ListTag scripts = new ListTag();
            for (ScriptContainer script : ScriptRegistry.scriptContainers.values()) {
                scripts.addObject(new ScriptTag(script));
            }
            return scripts;
        });
        // Pre-timetag-rewrite historical tag
        tagProcessor.registerTag(DurationTag.class, "start_time", (attribute, object) -> {
            Deprecations.timeTagRewrite.warn(attribute.context);
            return new DurationTag(CoreUtilities.monotonicMillisToReal(DenizenCore.startTime) / 50);
        });

        // <--[tag]
        // @attribute <server.has_permissions>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the server has a known permission plugin loaded.
        // Note: should not be considered incredibly reliable.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "has_permissions", (attribute, object) -> {
            return new ElementTag(Depends.permissions != null && Depends.permissions.isEnabled());
        });

        // <--[tag]
        // @attribute <server.has_economy>
        // @returns ElementTag(Boolean)
        // @plugin Vault
        // @description
        // Returns whether the server has a known economy plugin loaded.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_economy", (attribute, object) -> {
            return new ElementTag(Depends.economy != null && Depends.economy.isEnabled());
        });

        // <--[tag]
        // @attribute <server.denizen_version>
        // @returns ElementTag
        // @description
        // Returns the version of Denizen currently being used.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "denizen_version", (attribute, object) -> {
            return new ElementTag(Denizen.versionTag);
        });

        // <--[tag]
        // @attribute <server.bukkit_version>
        // @returns ElementTag
        // @description
        // Returns the version of Bukkit currently being used.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "bukkit_version", (attribute, object) -> {
            return new ElementTag(Bukkit.getBukkitVersion());
        });

        // <--[tag]
        // @attribute <server.version>
        // @returns ElementTag
        // @description
        // Returns the version of the server.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "version", (attribute, object) -> {
            return new ElementTag(Bukkit.getVersion());
        });

        // <--[tag]
        // @attribute <server.max_players>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum number of players allowed on the server.
        // -->
        tagProcessor.registerTag(ElementTag.class, "max_players", (attribute, object) -> {
            return new ElementTag(Bukkit.getMaxPlayers());
        });

        // <--[tag]
        // @attribute <server.group_prefix[<group>]>
        // @returns ElementTag
        // @description
        // Returns an ElementTag of a group's chat prefix.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "group_prefix", (attribute, object, input) -> {
            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String group = input.asString();
            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                attribute.echoError("Invalid group! '" + group + "' could not be found.");
                return null;
            }

            // <--[tag]
            // @attribute <server.group_prefix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat prefix for the specified WorldTag.
            // -->
            if (attribute.startsWith("world", 2)) {
                attribute.fulfill(1);
                WorldTag world = attribute.paramAsType(WorldTag.class);
                return world != null ? new ElementTag(Depends.chat.getGroupPrefix(world.getWorld(), group)) : null;
            }

            // Prefix in default world
            return new ElementTag(Depends.chat.getGroupPrefix(Bukkit.getWorlds().get(0), group));
        });

        // <--[tag]
        // @attribute <server.group_suffix[<group>]>
        // @returns ElementTag
        // @description
        // Returns an ElementTag of a group's chat suffix.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "group_suffix", (attribute, object, input) -> {
            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String group = input.asString();
            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                attribute.echoError("Invalid group! '" + group + "' could not be found.");
                return null;
            }

            // <--[tag]
            // @attribute <server.group_suffix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat suffix for the specified WorldTag.
            // -->
            if (attribute.startsWith("world", 2)) {
                attribute.fulfill(1);
                WorldTag world = attribute.paramAsType(WorldTag.class);
                return world != null ? new ElementTag(Depends.chat.getGroupSuffix(world.getWorld(), group)) : null;
            }

            // Suffix in default world
            return new ElementTag(Depends.chat.getGroupSuffix(Bukkit.getWorlds().get(0), group));
        });

        // <--[tag]
        // @attribute <server.permission_groups>
        // @returns ListTag
        // @description
        // Returns a list of all permission groups on the server.
        // -->
        tagProcessor.registerTag(ListTag.class, "permission_groups", (attribute, object) -> {
            listDeprecateWarn(attribute);
            if (Depends.permissions == null) {
                attribute.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }
            return new ListTag(Arrays.asList(Depends.permissions.getGroups()));
        }, "list_permission_groups");

        // <--[tag]
        // @attribute <server.match_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns the online player that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns player object for 'bob',
        // input 'bobb' returns player object for 'bobby', and input 'b' returns player object for 'bo'.
        // -->
        tagProcessor.registerTag(PlayerTag.class, ElementTag.class, "match_player", (attribute, object, input) -> {
            Player matchPlayer = null;
            String matchInput = input.asLowerString();
            if (matchInput.isEmpty()) {
                return null;
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
            return matchPlayer != null ? new PlayerTag(matchPlayer) : null;
        });

        // <--[tag]
        // @attribute <server.match_offline_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns any player (online or offline) that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns player object for 'bob',
        // input 'bobb' returns player object for 'bobby', and input 'b' returns player object for 'bo'.
        // When both an online player and an offline player match the name search, the online player will be returned.
        // -->
        tagProcessor.registerTag(PlayerTag.class, ElementTag.class, "match_offline_player", (attribute, object, input) -> {
            PlayerTag matchPlayer = null;
            String matchInput = input.asLowerString();
            if (matchInput.isEmpty()) {
                return null;
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
            return matchPlayer;
        });

        // <--[tag]
        // @attribute <server.online_players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players with a specified flag set.
        // Can use "!<flag_name>" style to only return players *without* the flag.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "online_players_flagged", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            String flag = input.asString();
            ListTag flaggedPlayers = new ListTag();
            boolean want = true;
            if (flag.startsWith("!")) {
                want = false;
                flag = flag.substring(1);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerTag playerTag = new PlayerTag(player);
                if (playerTag.getFlagTracker().hasFlag(flag) == want) {
                    flaggedPlayers.addObject(playerTag);
                }
            }
            return flaggedPlayers;
        }, "list_online_players_flagged");

        // <--[tag]
        // @attribute <server.players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players (online or offline) with a specified flag set.
        // Warning: this will cause the player flag cache to temporarily fill with ALL historical playerdata.
        // Can use "!<flag_name>" style to only return players *without* the flag.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "players_flagged", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            String flag = input.asString();
            ListTag flaggedPlayers = new ListTag();
            boolean want = true;
            if (flag.startsWith("!")) {
                want = false;
                flag = flag.substring(1);
            }
            for (UUID playerId : PlayerTag.getAllPlayers().values()) {
                PlayerTag player = new PlayerTag(playerId);
                if (player.getFlagTracker().hasFlag(flag) == want) {
                    flaggedPlayers.addObject(player);
                }
            }
            return flaggedPlayers;
        }, "list_players_flagged");

        // <--[tag]
        // @attribute <server.worlds>
        // @returns ListTag(WorldTag)
        // @description
        // Returns a list of all worlds.
        // -->
        tagProcessor.registerTag(ListTag.class, "worlds", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag worlds = new ListTag();
            for (World world : Bukkit.getWorlds()) {
                worlds.addObject(new WorldTag(world));
            }
            return worlds;
        }, "list_worlds");

        // <--[tag]
        // @attribute <server.plugins>
        // @returns ListTag(PluginTag)
        // @description
        // Gets a list of currently enabled PluginTags from the server.
        // -->
        tagProcessor.registerTag(ListTag.class, "plugins", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag plugins = new ListTag();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                plugins.addObject(new PluginTag(plugin));
            }
            return plugins;
        }, "list_plugins");

        // <--[tag]
        // @attribute <server.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        tagProcessor.registerTag(ListTag.class, "players", (attribute, object) -> {
            listDeprecateWarn(attribute);
            OfflinePlayer[] allPlayers = Bukkit.getOfflinePlayers();
            ListTag players = new ListTag(allPlayers.length);
            for (OfflinePlayer player : allPlayers) {
                players.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            return players;
        }, "list_players");

        // <--[tag]
        // @attribute <server.online_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_players", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            return players;
        }, "list_online_players");

        // <--[tag]
        // @attribute <server.offline_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline players.
        // This specifically excludes currently online players.
        // -->
        tagProcessor.registerTag(ListTag.class, "offline_players", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (!player.isOnline()) {
                    players.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return players;
        }, "list_offline_players");

        // <--[tag]
        // @attribute <server.banned_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all banned players.
        // -->
        tagProcessor.registerTag(ListTag.class, "banned_players", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag banned = new ListTag();
            for (OfflinePlayer player : Bukkit.getBannedPlayers()) {
                banned.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            return banned;
        }, "list_banned_players");

        // <--[tag]
        // @attribute <server.banned_addresses>
        // @returns ListTag
        // @description
        // Returns a list of all banned ip addresses.
        // -->
        tagProcessor.registerTag(ListTag.class, "banned_addresses", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag bannedIPs = new ListTag();
            bannedIPs.addAll(Bukkit.getIPBans());
            return bannedIPs;
        }, "list_banned_addresses");

        // <--[tag]
        // @attribute <server.is_banned[<address>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the given ip address is banned.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "is_banned", (attribute, object, input) -> {
            // BanList contains an isBanned method that doesn't check expiration time
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(input.asString());
            if (ban == null) {
                return new ElementTag(false);
            }
            return new ElementTag(ban.getExpiration() == null || ban.getExpiration().after(new Date()));
        });

        tagProcessor.registerTag(ObjectTag.class, ElementTag.class, "ban_info", (attribute, object, input) -> {
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(input.asString());
            Date expiration;
            if (ban == null || ((expiration = ban.getExpiration()) != null && expiration.before(new Date()))) {
                return null;
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].expiration_time>
            // @returns TimeTag
            // @description
            // Returns the expiration of the ip address's ban, if it is banned.
            // Potentially can be null.
            // -->
            if (attribute.startsWith("expiration_time", 2)) {
                attribute.fulfill(1);
                return expiration != null ? new TimeTag(expiration.getTime()) : null;
            }
            if (attribute.startsWith("expiration", 2)) {
                attribute.fulfill(1);
                Deprecations.timeTagRewrite.warn(attribute.context);
                return expiration != null ? new DurationTag(expiration.getTime() / 50) : null;
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].reason>
            // @returns ElementTag
            // @description
            // Returns the reason for the ip address's ban, if it is banned.
            // -->
            if (attribute.startsWith("reason", 2)) {
                attribute.fulfill(1);
                return new ElementTag(ban.getReason());
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].created_time>
            // @returns TimeTag
            // @description
            // Returns when the ip address's ban was created, if it is banned.
            // -->
            if (attribute.startsWith("created_time", 2)) {
                attribute.fulfill(1);
                return new TimeTag(ban.getCreated().getTime());
            }
            if (attribute.startsWith("created", 2)) {
                attribute.fulfill(1);
                Deprecations.timeTagRewrite.warn(attribute.context);
                return new DurationTag(ban.getCreated().getTime() / 50);
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].source>
            // @returns ElementTag
            // @description
            // Returns the source of the ip address's ban, if it is banned.
            // -->
            if (attribute.startsWith("source", 2)) {
                attribute.fulfill(1);
                return new ElementTag(ban.getSource());
            }

            return null;
        });

        // <--[tag]
        // @attribute <server.ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all ops, online or not.
        // -->
        tagProcessor.registerTag(ListTag.class, "ops", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag ops = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                ops.addObject(PlayerTag.mirrorBukkitPlayer(player));
            }
            return ops;
        }, "list_ops");

        // <--[tag]
        // @attribute <server.online_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online ops.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_ops", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag onlineOps = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                if (player.isOnline()) {
                    onlineOps.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return onlineOps;
        }, "list_online_ops");

        // <--[tag]
        // @attribute <server.offline_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline ops.
        // -->
        tagProcessor.registerTag(ListTag.class, "offline_ops", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag offlineOps = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                if (!player.isOnline()) {
                    offlineOps.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return offlineOps;
        }, "list_offline_ops");

        // <--[tag]
        // @attribute <server.motd>
        // @returns ElementTag
        // @description
        // Returns the server's current MOTD.
        // -->
        tagProcessor.registerTag(ElementTag.class, "motd", (attribute, object) -> {
            return new ElementTag(Bukkit.getMotd());
        });

        // <--[tag]
        // @attribute <server.view_distance>
        // @returns ElementTag(Number)
        // @description
        // Returns the server's current view distance.
        // -->
        tagProcessor.registerTag(ElementTag.class, "view_distance", (attribute, object) -> {
            return new ElementTag(Bukkit.getViewDistance());
        });

        tagProcessor.registerTag(ElementTag.class, ObjectTag.class, "entity_is_spawned", (attribute, object, input) -> {
            BukkitImplDeprecations.isValidTag.warn(attribute.context);
            EntityTag entity = input.canBeType(EntityTag.class) ? input.asType(EntityTag.class, attribute.context) : null;
            return new ElementTag(entity != null && entity.isUnique() && entity.isSpawnedOrValidForTag());
        });
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "player_is_valid", (attribute, object, input) -> {
            BukkitImplDeprecations.isValidTag.warn(attribute.context);
            return new ElementTag(PlayerTag.playerNameIsValid(input.asString()));
        });
        tagProcessor.registerTag(ElementTag.class, ObjectTag.class, "npc_is_valid", (attribute, object, input) -> {
            BukkitImplDeprecations.isValidTag.warn(attribute.context);
            NPCTag npc = input.canBeType(NPCTag.class) ? input.asType(NPCTag.class, attribute.context) : null;
            return new ElementTag(npc != null && npc.isValid());
        });

        // <--[tag]
        // @attribute <server.current_bossbars>
        // @returns ListTag
        // @description
        // Returns a list of all currently active boss bar IDs from <@link command bossbar>.
        // -->
        tagProcessor.registerTag(ListTag.class, "current_bossbars", (attribute, context) -> {
            return new ListTag(BossBarCommand.bossBarMap.keySet());
        });

        // <--[tag]
        // @attribute <server.bossbar_viewers[<bossbar_id>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of players that should be able to see the given bossbar ID from <@link command bossbar>.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "bossbar_viewers", (attribute, object, input) -> {
            BossBar bar = BossBarCommand.bossBarMap.get(input.asLowerString());
            if (bar == null) {
                return null;
            }
            ListTag viewers = new ListTag();
            for (Player player : bar.getPlayers()) {
                viewers.addObject(new PlayerTag(player));
            }
            return viewers;
        });

        // <--[tag]
        // @attribute <server.recent_tps>
        // @returns ListTag
        // @description
        // Returns the 3 most recent ticks per second measurements.
        // -->
        tagProcessor.registerTag(ListTag.class, "recent_tps", (attribute, object) -> {
            ListTag recentTPS = new ListTag(3);
            for (double tps : NMSHandler.instance.getRecentTps()) {
                recentTPS.addObject(new ElementTag(tps));
            }
            return recentTPS;
        });

        // <--[tag]
        // @attribute <server.port>
        // @returns ElementTag(Number)
        // @description
        // Returns the port that the server is running on.
        // -->
        tagProcessor.registerTag(ElementTag.class, "port", (attribute, object) -> {
            return new ElementTag(Bukkit.getPort());
        });

        // <--[tag]
        // @attribute <server.idle_timeout>
        // @returns DurationTag
        // @mechanism server.idle_timeout
        // @description
        // Returns the server's current idle timeout limit (how long a player can sit still before getting kicked).
        // Internally used with <@link tag PlayerTag.last_action_time>.
        // -->
        tagProcessor.registerTag(DurationTag.class, "idle_timeout", (attribute, object) -> {
            return new DurationTag(Bukkit.getIdleTimeout() * 60);
        });

        // <--[tag]
        // @attribute <server.vanilla_entity_tags>
        // @returns ListTag
        // @description
        // Returns a list of vanilla tags applicable to entity types. See also <@link url https://minecraft.wiki/w/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, "vanilla_entity_tags", (attribute, object) -> {
            return new ListTag(VanillaTagHelper.entityTagsByKey.keySet());
        });

        // <--[tag]
        // @attribute <server.vanilla_tagged_entities[<tag>]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entity types referred to by the specified vanilla tag. See also <@link url https://minecraft.wiki/w/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "vanilla_tagged_entities", (attribute, object, tag) -> {
            Set<EntityType> entityTypes = VanillaTagHelper.entityTagsByKey.get(tag.asLowerString());
            if (entityTypes == null) {
                return null;
            }
            ListTag taggedEntities = new ListTag(entityTypes.size());
            for (EntityType entityType : entityTypes) {
                taggedEntities.addObject(new EntityTag(entityType));
            }
            return taggedEntities;
        });

        // <--[tag]
        // @attribute <server.vanilla_material_tags>
        // @returns ListTag
        // @description
        // Returns a list of vanilla tags applicable to blocks, fluids, or items. See also <@link url https://minecraft.wiki/w/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, "vanilla_material_tags", (attribute, object) -> {
            return new ListTag(VanillaTagHelper.materialTagsByKey.keySet());
        }, "vanilla_tags");

        // <--[tag]
        // @attribute <server.vanilla_tagged_materials[<tag>]>
        // @returns ListTag(MaterialTag)
        // @description
        // Returns a list of materials referred to by the specified vanilla tag. See also <@link url https://minecraft.wiki/w/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "vanilla_tagged_materials", (attribute, object, tag) -> {
            Set<Material> materials = VanillaTagHelper.materialTagsByKey.get(tag.asLowerString());
            if (materials == null) {
                return null;
            }
            ListTag taggedMaterials = new ListTag(materials.size());
            for (Material material : materials) {
                taggedMaterials.addObject(new MaterialTag(material));
            }
            return taggedMaterials;
        });

        // <--[tag]
        // @attribute <server.plugins_handling_event[<bukkit_event>]>
        // @returns ListTag(PluginTag)
        // @description
        // Returns a list of all plugins that handle a given Bukkit event.
        // Can specify by ScriptEvent name ("PlayerBreaksBlock"), or by full Bukkit class name ("org.bukkit.event.block.BlockBreakEvent").
        // This is a primarily a dev tool and is not necessarily useful to most players or scripts.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "plugins_handling_event", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            String eventName = input.asString();
            if (CoreUtilities.contains(eventName, '.')) {
                try {
                    Class<?> clazz = Class.forName(eventName, false, ServerTagBase.class.getClassLoader());
                    ListTag result = getHandlerPluginList(clazz);
                    if (result != null) {
                        return result;
                    }
                }
                catch (ClassNotFoundException ex) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError(ex);
                    }
                }
            }
            else {
                ScriptEvent scriptEvent = ScriptEvent.eventLookup.get(input.asLowerString());
                if (scriptEvent instanceof Listener listener) {
                    Plugin plugin = Denizen.getInstance();
                    for (Class<? extends Event> eventClass : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).keySet()) {
                        ListTag result = getHandlerPluginList(eventClass);
                        // Return results for the first valid match.
                        if (result != null && result.size() > 0) {
                            return result;
                        }
                    }
                    return new ListTag();
                }
            }
            return null;
        }, "list_plugins_handling_event");

        // <--[tag]
        // @attribute <server.generate_loot_table[id=<id>;location=<location>;(killer=<entity>);(entity=<entity>);(loot_bonus=<#>/{-1});(luck=<#.#>/{0})]>
        // @returns ListTag(ItemTag)
        // @description
        // Returns a list of items from a loot table, given a map of input data.
        // Required input: id: the loot table ID, location: the location where it's being generated (LocationTag).
        // Optional inputs: killer: an online player (or player-type NPC) that is looting, entity: a dead entity being looted from (a valid EntityTag instance that is or was spawned in the world),
        // loot_bonus: the loot bonus level (defaults to -1) as an integer number, luck: the luck potion level (defaults to 0) as a decimal number.
        //
        // Some inputs will be strictly required for some loot tables, and ignored for others.
        //
        // A list of valid loot tables can be found here: <@link url https://minecraft.wiki/w/Loot_table#List_of_loot_tables>
        // Note that the tree view represented on the wiki should be split by slashes for the input - for example, "cow" is under "entities" in the tree so "entities/cow" is how you input that.
        // CAUTION: Invalid loot table IDs will generate an empty list rather than an error.
        //
        // @example
        // - give <server.generate_loot_table[id=chests/spawn_bonus_chest;killer=<player>;location=<player.location>]>
        // -->
        tagProcessor.registerTag(ListTag.class, MapTag.class, "generate_loot_table", (attribute, object, map) -> {
            ElementTag idObj = map.getRequiredObjectAs("id", ElementTag.class, attribute);
            LocationTag locationObj = map.getRequiredObjectAs("location", LocationTag.class, attribute);
            if (idObj == null || locationObj == null) {
                return null;
            }
            LootTable table = Bukkit.getLootTable(Utilities.parseNamespacedKey(idObj.asLowerString()));
            if (table == null) {
                attribute.echoError("Invalid loot table ID '" + idObj + "' specified.");
                return null;
            }
            LootContext.Builder context = new LootContext.Builder(locationObj);
            EntityTag killer = map.getObjectAs("killer", EntityTag.class, attribute.context);
            ElementTag luck = map.getElement("luck");
            ElementTag bonus = map.getElement("loot_bonus");
            EntityTag entity = map.getObjectAs("entity", EntityTag.class, attribute.context);
            if (entity != null) {
                context = context.lootedEntity(entity.getBukkitEntity());
            }
            if (killer != null) {
                if (killer.getLivingEntity() instanceof HumanEntity humanEntity) {
                     context = context.killer(humanEntity);
                }
                else {
                    attribute.echoError("Invalid killer '" + killer + "' specified: must be an online player or a player-type NPC.");
                }
            }
            if (luck != null) {
                context = context.luck(luck.asFloat());
            }
            if (bonus != null) {
                context = context.lootingModifier(bonus.asInt());
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
                return null;
            }
            ListTag lootItems = new ListTag(items.size());
            for (ItemStack item : items) {
                lootItems.addObject(new ItemTag(item));
            }
            return lootItems;
        });

        // <--[tag]
        // @attribute <server.area_notes_debug>
        // @returns MapTag
        // @description
        // Generates a report about noted area tracking.
        // This tag is strictly for internal debugging reasons.
        // -->
        tagProcessor.registerTag(MapTag.class, "area_notes_debug", (attribute, object) -> {
            MapTag worlds = new MapTag();
            for (Map.Entry<String, NotedAreaTracker.PerWorldSet> set : NotedAreaTracker.worlds.entrySet()) {
                MapTag worldData = new MapTag();
                worldData.putObject("global", new ListTag(set.getValue().globalSet.list, trackedArea -> trackedArea.area));
                worldData.putObject("x50", areaNotesDebug(set.getValue().sets50));
                worldData.putObject("x50_offset", areaNotesDebug(set.getValue().sets50_offset));
                worldData.putObject("x200", areaNotesDebug(set.getValue().sets200));
                worldData.putObject("x200_offset", areaNotesDebug(set.getValue().sets200_offset));
                worlds.putObject(set.getKey(), worldData);
            }
            return worlds;
        });

        // <--[mechanism]
        // @object server
        // @name clean_flags
        // @input None
        // @description
        // Cleans any expired flags from the object.
        // Generally doesn't need to be called, using the 'skip flag cleanings' setting was enabled.
        // This is an internal/special case mechanism, and should be avoided where possible.
        // Does not function on all flaggable objects, particularly those that just store their flags into other objects.
        // -->
        tagProcessor.registerMechanism("clean_flags", false, (object, mechanism) -> {
            DenizenCore.serverFlagMap.doTotalClean();
        });

        // <--[mechanism]
        // @object server
        // @name reset_recipes
        // @input None
        // @description
        // Resets the server's recipe list to the default vanilla recipe list + item script recipes.
        // @tags
        // <server.recipe_ids[(<type>)]>
        // -->
        tagProcessor.registerMechanism("reset_recipes", false, (object, mechanism) -> {
            Bukkit.resetRecipes();
            ItemScriptHelper.rebuildRecipes();
        });

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
        tagProcessor.registerMechanism("remove_recipes", false, ListTag.class, (object, mechanism, recipes) -> {
            for (String recipe : recipes) {
                Bukkit.removeRecipe(Utilities.parseNamespacedKey(recipe));
            }
        });

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
        tagProcessor.registerMechanism("idle_timeout", false, DurationTag.class, (object, mechanism, timeout) -> {
            Bukkit.setIdleTimeout((int) Math.round(timeout.getSeconds() / 60));
        });

        // <--[mechanism]
        // @object server
        // @name restart
        // @input None
        // @description
        // Immediately stops the server entirely (Plugins will still finalize, and the shutdown event will fire), then starts it again.
        // Requires config file setting "Commands.Restart.Allow server restart"!
        // Note that if your server is not configured to restart, this mechanism will simply stop the server without starting it again!
        // -->
        tagProcessor.registerMechanism("restart", false, (object, mechanism) -> {
            if (!Settings.allowServerRestart()) {
                Debug.echoError("Server restart disabled by administrator (refer to mechanism documentation). Consider using 'shutdown'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server restarted by a Denizen script, see config to prevent this!");
            Bukkit.spigot().restart();
        });

        // <--[mechanism]
        // @object server
        // @name save
        // @input None
        // @description
        // Immediately saves the Denizen saves files.
        // -->
        tagProcessor.registerMechanism("save", false, (object, mechanism) -> {
            DenizenCore.saveAll(false);
            Denizen.getInstance().saveSaves(false);
        });

        // <--[mechanism]
        // @object server
        // @name shutdown
        // @input None
        // @description
        // Immediately stops the server entirely (Plugins will still finalize, and the shutdown event will fire).
        // The server will remain shutdown until externally started again.
        // Requires config file setting "Commands.Restart.Allow server stop"!
        // -->
        tagProcessor.registerMechanism("shutdown", false, (object, mechanism) -> {
            if (!Settings.allowServerStop()) {
                Debug.echoError("Server stop disabled by administrator (refer to mechanism documentation). Consider using 'restart'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server shutdown by a Denizen script, see config to prevent this!");
            Bukkit.shutdown();
        });

        // <--[mechanism]
        // @object server
        // @name has_whitelist
        // @input ElementTag(Boolean)
        // @description
        // Toggles whether the server's whitelist is enabled.
        // @tags
        // <server.has_whitelist>
        // -->
        tagProcessor.registerMechanism("has_whitelist", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                Bukkit.setWhitelist(input.asBoolean());
            }
        });

        // <--[mechanism]
        // @object server
        // @name register_permission
        // @input MapTag
        // @description
        // Input must be a map with the key 'name' set to the permission name.
        // Can also set 'description' to a description of the permission.
        // Can also set 'parent' to the name of the parent permission (must already be registered).
        // Can also set 'default' to any of <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/permissions/PermissionDefault.html> to define default accessibility.
        // This mechanism should probably be executed during <@link event server prestart>.
        // @tags
        // <server.has_whitelist>
        // -->
        tagProcessor.registerMechanism("register_permission", false, MapTag.class, (object, mechanism, input) -> {
            ElementTag name = input.getElement("name"), parentInput = input.getElement("parent"),
                    defaultInput = input.getElement("default"), description = input.getElement("description");
            Permission parent = parentInput == null ? null : Bukkit.getPluginManager().getPermission(parentInput.asString());
            PermissionDefault permissionDefault = defaultInput == null ? null : defaultInput.asEnum(PermissionDefault.class);
            if (parent == null) {
                DefaultPermissions.registerPermission(name.asString(), description == null ? null : description.asString(), permissionDefault);
            }
            else {
                DefaultPermissions.registerPermission(name.asString(), description == null ? null : description.asString(), permissionDefault, parent);
            }
        });

        // <--[mechanism]
        // @object server
        // @name default_colors
        // @input MapTag
        // @description
        // Sets a default value of a custom color, to be used if the config.yml does not specify a value for that color name.
        // Input must be a map with the keys as custom color names, and the values as the default color.
        // This mechanism should probably be executed during <@link event scripts loaded>.
        // @tags
        // <&>
        // <ElementTag.custom_color>
        // @Example
        // on scripts loaded:
        // - adjust server default_colors:[mymagenta=<&color[#ff00ff]>;myred=<&c>]
        // - debug log "The custom red is <&[myred]>"
        // -->
        tagProcessor.registerMechanism("default_colors", false, MapTag.class, (object, mechanism, input) -> {
            for (Map.Entry<StringHolder, ObjectTag> pair : input.entrySet()) {
                String name = pair.getKey().low;
                if (!CustomColorTagBase.customColors.containsKey(name)) {
                    CustomColorTagBase.customColors.put(name, pair.getValue().toString().replace("<", "<&lt>"));
                }
            }
        });

        // <--[tag]
        // @attribute <server.notes[<type>]>
        // @returns ListTag
        // @deprecated use util.notes
        // @description
        // Deprecated in favor of <@link tag util.notes>
        // -->

        // <--[tag]
        // @attribute <server.started_time>
        // @returns TimeTag
        // @deprecated use util.started_time
        // @description
        // Deprecated in favor of <@link tag util.started_time>
        // -->

        // <--[tag]
        // @attribute <server.disk_free>
        // @returns ElementTag(Number)
        // @deprecated use util.disk_free
        // @description
        // Deprecated in favor of <@link tag util.disk_free>
        // -->

        // <--[tag]
        // @attribute <server.disk_total>
        // @returns ElementTag(Number)
        // @deprecated use util.disk_total
        // @description
        // Deprecated in favor of <@link tag util.disk_total>
        // -->

        // <--[tag]
        // @attribute <server.disk_usage>
        // @returns ElementTag(Number)
        // @deprecated use util.disk_usage
        // @description
        // Deprecated in favor of <@link tag util.disk_usage>
        // -->

        // <--[tag]
        // @attribute <server.ram_allocated>
        // @returns ElementTag(Number)
        // @deprecated use util.ram_allocated
        // @description
        // Deprecated in favor of <@link tag util.ram_allocated>
        // -->

        // <--[tag]
        // @attribute <server.ram_max>
        // @returns ElementTag(Number)
        // @deprecated use util.ram_max
        // @description
        // Deprecated in favor of <@link tag util.ram_max>
        // -->

        // <--[tag]
        // @attribute <server.ram_free>
        // @returns ElementTag(Number)
        // @deprecated use util.ram_free
        // @description
        // Deprecated in favor of <@link tag util.ram_free>
        // -->

        // <--[tag]
        // @attribute <server.ram_usage>
        // @returns ElementTag(Number)
        // @deprecated use util.ram_usage
        // @description
        // Deprecated in favor of <@link tag util.ram_usage>
        // -->

        // <--[tag]
        // @attribute <server.available_processors>
        // @returns ElementTag(Number)
        // @deprecated use util.available_processors
        // @description
        // Deprecated in favor of <@link tag util.available_processors>
        // -->

        // <--[tag]
        // @attribute <server.current_tick>
        // @returns ElementTag(Number)
        // @deprecated use util.current_tick
        // @description
        // Deprecated in favor of <@link tag util.current_tick>
        // -->

        // <--[tag]
        // @attribute <server.delta_time_since_start>
        // @returns DurationTag
        // @deprecated use util.delta_time_since_start
        // @description
        // Deprecated in favor of <@link tag util.delta_time_since_start>
        // -->

        // <--[tag]
        // @attribute <server.real_time_since_start>
        // @returns DurationTag
        // @deprecated use util.real_time_since_start
        // @description
        // Deprecated in favor of <@link tag util.real_time_since_start>
        // -->

        // <--[tag]
        // @attribute <server.current_time_millis>
        // @returns ElementTag(Number)
        // @deprecated use util.current_time_millis
        // @description
        // Deprecated in favor of <@link tag util.current_time_millis>
        // -->

        // <--[tag]
        // @attribute <server.has_file[<name>]>
        // @returns ElementTag(Boolean)
        // @deprecated use util.has_file
        // @description
        // Deprecated in favor of <@link tag util.has_file>
        // -->

        // <--[tag]
        // @attribute <server.list_files[<path>]>
        // @returns ListTag
        // @deprecated use util.list_files
        // @description
        // Deprecated in favor of <@link tag util.list_files>
        // -->

        // <--[tag]
        // @attribute <server.java_version>
        // @returns ElementTag
        // @deprecated use util.java_version
        // @description
        // Deprecated in favor of <@link tag util.java_version>
        // -->

        // <--[tag]
        // @attribute <server.sql_connections>
        // @returns ListTag
        // @deprecated use util.sql_connections
        // @description
        // Deprecated in favor of <@link tag util.sql_connections>
        // -->

        // <--[tag]
        // @attribute <server.scripts>
        // @returns ListTag(ScriptTag)
        // @deprecated use util.scripts
        // @description
        // Deprecated in favor of <@link tag util.scripts>
        // -->

        // <--[tag]
        // @attribute <server.last_reload>
        // @returns TimeTag
        // @deprecated use util.last_reload
        // @description
        // Deprecated in favor of <@link tag util.last_reload>
        // -->

        // <--[tag]
        // @attribute <server.stack_trace>
        // @returns ElementTag
        // @deprecated use util.stack_trace
        // @description
        // Deprecated in favor of <@link tag util.stack_trace>
        // -->

        // <--[tag]
        // @attribute <server.debug_enabled>
        // @returns ElementTag(Boolean)
        // @deprecated use util.debug_enabled
        // @description
        // Deprecated in favor of <@link tag util.debug_enabled>
        // -->

        // <--[tag]
        // @attribute <server.color_names>
        // @returns ListTag
        // @deprecated use util.color_names
        // @description
        // Deprecated in favor of <@link tag util.color_names>
        // -->
        for (String tagName : new String[] { "current_time_millis", "real_time_since_start", "color_names",
                "delta_time_since_start", "current_tick", "available_processors", "ram_usage", "ram_free", "ram_max", "ram_allocated", "disk_usage", "debug_enabled",
                "disk_total", "disk_free", "started_time", "has_file", "list_files", "notes", "last_reload", "scripts", "sql_connections", "java_version", "stack_trace" }) {
            TagRunnable.ObjectInterface<UtilTagBase, ?> runner = UtilTagBase.instance.tagProcessor.registeredObjectTags.get(tagName).runner;
            tagProcessor.registerTag(ObjectTag.class, tagName, (attribute, object) -> {
                BukkitImplDeprecations.serverUtilTags.warn(attribute.context);
                return runner.run(attribute, UtilTagBase.instance);
            });
        }

        // <--[mechanism]
        // @object server
        // @name delete_file
        // @input ElementTag
        // @deprecated use system.delete_file
        // @description
        // Deprecated in favor of <@link mechanism system.delete_file>
        // -->

        // <--[mechanism]
        // @object server
        // @name reset_event_stats
        // @input None
        // @deprecated use system.reset_event_stats
        // @description
        // Deprecated in favor of <@link mechanism system.reset_event_stats>
        // -->

        // <--[mechanism]
        // @object server
        // @name cleanmem
        // @input None
        // @deprecated use system.cleanmem
        // @description
        // Deprecated in favor of <@link mechanism system.cleanmem>
        // -->

        for (String mechName : new String[] { "delete_file", "reset_event_stats", "cleanmem" }) {
            Mechanism.GenericMechRunnerInterface<UtilTagBase> runner = UtilTagBase.instance.tagProcessor.registeredMechanisms.get(mechName).runner;
            tagProcessor.registerMechanism(mechName, false, (object, mechanism) -> {
                BukkitImplDeprecations.serverSystemMechanisms.warn(mechanism.context);
                runner.run(UtilTagBase.instance, mechanism);
            });
        }

        if (Depends.citizens != null) {
            registerCitizensFeatures();
        }
    }

    public void registerCitizensFeatures() {

        // <--[tag]
        // @attribute <server.selected_npc>
        // @returns NPCTag
        // @description
        // Returns the server's currently selected NPC.
        // -->
        tagProcessor.registerTag(NPCTag.class, "selected_npc", (attribute, object) -> {
            NPC npc = Depends.citizens.getNPCSelector().getSelected(Bukkit.getConsoleSender());
            return npc != null ? new NPCTag(npc) : null;
        });

        // <--[tag]
        // @attribute <server.npcs_named[<name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of NPCs with a certain name.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "npcs_named", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            ListTag npcs = new ListTag();
            String name = input.asLowerString();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (name.equals(CoreUtilities.toLowerCase(npc.getName()))) {
                    npcs.addObject(new NPCTag(npc));
                }
            }
            return npcs;
        }, "list_npcs_named");

        // <--[tag]
        // @attribute <server.npcs_assigned[<assignment_script>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs assigned to a specified script.
        // -->
        tagProcessor.registerTag(ListTag.class, ScriptTag.class, "npcs_assigned", (attribute, object, script) -> {
            listDeprecateWarn(attribute);
            if (!(script.getContainer() instanceof AssignmentScriptContainer assignmentScriptContainer)) {
                attribute.echoError("Invalid script '" + script + "' specified: must be an assignment script.");
                return null;
            }
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.hasTrait(AssignmentTrait.class) && npc.getTraitNullable(AssignmentTrait.class).isAssigned(assignmentScriptContainer)) {
                    npcs.addObject(new NPCTag(npc));
                }
            }
            return npcs;
        }, "list_npcs_assigned");

        // <--[tag]
        // @attribute <server.spawned_npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all spawned NPCs with a specified flag set.
        // Can use "!<flag_name>" style to only return NPCs *without* the flag.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "spawned_npcs_flagged", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            String flag = input.asString();
            ListTag npcs = new ListTag();
            boolean want = true;
            if (flag.startsWith("!")) {
                want = false;
                flag = flag.substring(1);
            }
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag npcTag = new NPCTag(npc);
                if (npcTag.isSpawned() && npcTag.hasFlag(flag) == want) {
                    npcs.addObject(npcTag);
                }
            }
            return npcs;
        }, "list_spawned_npcs_flagged");

        // <--[tag]
        // @attribute <server.npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs with a specified flag set.
        // Can use "!<flag_name>" style to only return NPCs *without* the flag.
        // -->
        tagProcessor.registerTag(ListTag.class, ElementTag.class, "npcs_flagged", (attribute, object, input) -> {
            listDeprecateWarn(attribute);
            String flag = input.asString();
            ListTag npcs = new ListTag();
            boolean want = true;
            if (flag.startsWith("!")) {
                want = false;
                flag = flag.substring(1);
            }
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag npcTag = new NPCTag(npc);
                if (npcTag.hasFlag(flag) == want) {
                    npcs.addObject(npcTag);
                }
            }
            return npcs;
        }, "list_npcs_flagged");

        // <--[tag]
        // @attribute <server.npc_registries>
        // @returns ListTag
        // @description
        // Returns a list of all NPC registries.
        // -->
        tagProcessor.registerTag(ListTag.class, "npc_registries", (attribute, object) -> {
            ListTag registries = new ListTag();
            for (NPCRegistry registry : CitizensAPI.getNPCRegistries()) {
                registries.add(registry.getName());
            }
            return registries;
        });

        // <--[tag]
        // @attribute <server.npcs[(<registry>)]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs.
        // -->
        tagProcessor.registerTag(ListTag.class, "npcs", (attribute, object) -> {
            listDeprecateWarn(attribute);
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            if (attribute.hasParam()) {
                registry = NPCTag.getRegistryByName(attribute.getParam());
                if (registry == null) {
                    attribute.echoError("NPC Registry '" + attribute.getParam() + "' does not exist.");
                    return null;
                }
            }
            ListTag npcs = new ListTag();
            for (NPC npc : registry) {
                npcs.addObject(new NPCTag(npc));
            }
            return npcs;
        }, "list_npcs");

        // <--[tag]
        // @attribute <server.traits>
        // @Plugin Citizens
        // @returns ListTag
        // @description
        // Returns a list of all available NPC traits on the server.
        // -->
        tagProcessor.registerTag(ListTag.class, "traits", (attribute, object) -> {
            listDeprecateWarn(attribute);
            ListTag traits = new ListTag();
            for (TraitInfo trait : CitizensAPI.getTraitFactory().getRegisteredTraits()) {
                traits.add(trait.getTraitName());
            }
            return traits;
        }, "list_traits");

        // <--[mechanism]
        // @object server
        // @name save_citizens
        // @input None
        // @description
        // Immediately saves the Citizens saves files.
        // -->
        tagProcessor.registerMechanism("save_citizens", false, (object, mechanism) -> {
            Depends.citizens.storeNPCs();
        });
    }

    public void registerEnumListTag(String name, Class<? extends Enum<?>> enumType, String... deprecatedVariants) {
        tagProcessor.registerStaticTag(ListTag.class, name, (attribute, object) -> {
            listDeprecateWarn(attribute);
            Enum<?>[] enumConstants = enumType.getEnumConstants();
            ListTag result = new ListTag(enumConstants.length);
            for (Enum<?> constant : enumConstants) {
                result.addObject(new ElementTag(constant));
            }
            return result;
        }, deprecatedVariants);
    }

    private static MapTag areaNotesDebug(Int2ObjectOpenHashMap<NotedAreaTracker.AreaSet> set) {
        MapTag out = new MapTag();
        for (Int2ObjectMap.Entry<NotedAreaTracker.AreaSet> pair : set.int2ObjectEntrySet()) {
            out.putObject(String.valueOf(pair.getIntKey()), new ListTag(pair.getValue().list, trackedArea -> trackedArea.area));
        }
        return out;
    }

    public static void listDeprecateWarn(Attribute attribute) {
        if (attribute.getAttributeWithoutParam(1).startsWith("list_")) {
            BukkitImplDeprecations.listStyleTags.warn(attribute.context);
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
}

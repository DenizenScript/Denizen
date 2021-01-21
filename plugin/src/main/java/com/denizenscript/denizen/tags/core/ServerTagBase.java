package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.scripts.commands.server.BossBarCommand;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptHelper;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
import com.denizenscript.denizencore.utilities.debugging.FutureWarning;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.*;
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
        }, "server", "global");
    }

    public static final long serverStartTimeMillis = System.currentTimeMillis();

    public void serverTag(ReplaceableTagEvent event) {
        if (!event.matches("server", "global") || event.replaced()) {
            return;
        }
        if (event.matches("global")) {
            Deprecations.globalTagName.warn(event.getScriptEntry());
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
            if (attribute.startsWith("format") && attribute.hasContext(1)) {
                double amount = attribute.getDoubleContext(1);
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
            if (attribute.startsWith("currency_name") && attribute.hasContext(1)) {
                double amount = attribute.getDoubleContext(1);
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
        if (attribute.startsWith("slot_id") && attribute.hasContext(1)) {
            int slotId = SlotHelper.nameToIndex(attribute.getContext(1));
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
        if (attribute.startsWith("parse_bukkit_item") && attribute.hasContext(1)) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(attribute.getContext(1));
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
            String type = attribute.hasContext(1) ? CoreUtilities.toLowerCase(attribute.getContext(1)) : null;
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
        if (attribute.startsWith("recipe_items") && attribute.hasContext(1)) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getContext(1));
            Recipe recipe = NMSHandler.getItemHelper().getRecipeById(key);
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
        if (attribute.startsWith("recipe_shape") && attribute.hasContext(1)) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getContext(1));
            Recipe recipe = NMSHandler.getItemHelper().getRecipeById(key);
            if (!(recipe instanceof ShapedRecipe)) {
                return;
            }
            String[] shape = ((ShapedRecipe) recipe).getShape();
            event.setReplacedObject(new ElementTag(shape[0].length() + "x" + shape.length));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_type[<id>]>
        // @returns ElementTag
        // @description
        // Returns the type of recipe that the given recipe ID is.
        // Will be one of FURNACE, BLASTING, SHAPED, SHAPELESS, SMOKING, CAMPFIRE, STONECUTTING.
        // -->
        if (attribute.startsWith("recipe_type") && attribute.hasContext(1)) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getContext(1));
            Recipe recipe = NMSHandler.getItemHelper().getRecipeById(key);
            if (recipe == null) {
                return;
            }
            event.setReplacedObject(new ElementTag(Utilities.getRecipeType(recipe)));
            return;
        }

        // <--[tag]
        // @attribute <server.recipe_result[<id>]>
        // @returns ItemTag
        // @description
        // Returns the item that a recipe will create when crafted.
        // -->
        if (attribute.startsWith("recipe_result") && attribute.hasContext(1)) {
            NamespacedKey key = Utilities.parseNamespacedKey(attribute.getContext(1));
            Recipe recipe = NMSHandler.getItemHelper().getRecipeById(key);
            if (recipe == null) {
                return;
            }
            event.setReplacedObject(new ItemTag(recipe.getResult()));
            return;
        }

        if (attribute.startsWith("scoreboard")) {
            Scoreboard board;
            String name = "main";
            if (attribute.hasContext(1)) {
                name = attribute.getContext(1);
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

            if (attribute.startsWith("objective") && attribute.hasContext(1)) {
                Objective objective = board.getObjective(attribute.getContext(1));
                if (objective == null) {
                    attribute.echoError("Scoreboard objective '" + attribute.getContext(1) + "' does not exist.");
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

            if (attribute.startsWith("team") && attribute.hasContext(1)) {
                Team team = board.getTeam(attribute.getContext(1));
                if (team == null) {
                    attribute.echoError("Scoreboard team '" + attribute.getContext(1) + "' does not exist.");
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
            ObjectTag o = ObjectFetcher.pickObjectFor(attribute.getContext(1), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag(!(o == null || o instanceof ElementTag)).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_whitelist>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the server's whitelist is active, otherwise returns false.
        // -->
        if (attribute.startsWith("has_whitelist")) {
            event.setReplacedObject(new ElementTag(Bukkit.hasWhitelist()).getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // Documented in AbstractFlagTracker
        if (attribute.startsWith("has_flag")) {
            event.setReplacedObject(Denizen.getInstance().serverFlagMap.doHasFlagTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }
        if (attribute.startsWith("flag_expiration")) {
            TimeTag exp = Denizen.getInstance().serverFlagMap.doFlagExpirationTag(attribute);
            if (exp != null) {
                event.setReplacedObject(exp
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }
        if (attribute.startsWith("flag")) {
            ObjectTag flag = Denizen.getInstance().serverFlagMap.doFlagTag(attribute);
            if (flag != null) {
                event.setReplacedObject(flag
                        .getObjectAttribute(attribute.fulfill(1)));
            }
            return;
        }
        if (attribute.startsWith("list_flags")) {
            event.setReplacedObject(Denizen.getInstance().serverFlagMap.doListFlagsTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }
        if (attribute.startsWith("flag_map")) {
            event.setReplacedObject(Denizen.getInstance().serverFlagMap.doFlagMapTag(attribute)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
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
            ListTag list = new ListTag(CommandScriptHelper.knownCommands.keySet());
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
        // @attribute <server.biome_types>
        // @returns ListTag(BiomeTag)
        // @description
        // Returns a list of all biomes known to the server.
        // Generally used with <@link language BiomeTag Objects>.
        // This is based on Bukkit Biome enum, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html>.
        // -->
        if (attribute.startsWith("biome_types") || attribute.startsWith("list_biome_types")) {
            listDeprecateWarn(attribute);
            ListTag allBiomes = new ListTag();
            for (Biome biome : Biome.values()) {
                allBiomes.addObject(new BiomeTag(biome));
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
        // @attribute <server.enchantment_types>
        // @returns ListTag
        // @description
        // Returns a list of all enchantments known to the server.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html>.
        // Generally, prefer <@link tag server.enchantment_keys>.
        // -->
        if (attribute.startsWith("enchantment_types") || attribute.startsWith("list_enchantments")) {
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

        // <--[tag]
        // @attribute <server.enchantment_keys>
        // @returns ListTag
        // @description
        // Returns a list of all enchantments known to the server.
        // Generally used with <@link mechanism ItemTag.enchantments>.
        // This is specifically their minecraft key names, which generally align with the names you see in-game.
        // -->
        if (attribute.startsWith("enchantment_keys") || attribute.startsWith("list_enchantment_keys")) {
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
        // Generally used with <@link language EntityTag Objects>.
        // This is only their Bukkit enum names, as seen at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html>.
        // -->
        if (attribute.startsWith("entity_types") || attribute.startsWith("list_entity_types")) {
            listDeprecateWarn(attribute);
            ListTag allEnt = new ListTag();
            for (EntityType entity : EntityType.values()) {
                allEnt.add(entity.name());
            }
            event.setReplacedObject(allEnt.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.material_types>
        // @returns ListTag(MaterialTag)
        // @description
        // Returns a list of all materials known to the server.
        // Generally used with <@link language MaterialTag Objects>.
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
            if (attribute.hasContext(1)) {
                type = Statistic.Type.valueOf(attribute.getContext(1).toUpperCase());
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
        // @attribute <server.notables[<type>]>
        // @returns ListTag
        // @description
        // Lists all saved notable objects of a specific type currently on the server.
        // Valid types: locations, cuboids, ellipsoids, inventories
        // This is primarily intended for debugging purposes, and it's best to avoid using this in a live script if possible.
        // -->
        if (attribute.startsWith("notables") || attribute.startsWith("list_notables")) {
            listDeprecateWarn(attribute);
            ListTag allNotables = new ListTag();
            if (attribute.hasContext(1)) {
                String type = CoreUtilities.toLowerCase(attribute.getContext(1));
                for (Map.Entry<String, Class> typeClass : NotableManager.getReverseClassIdMap().entrySet()) {
                    if (type.equals(CoreUtilities.toLowerCase(typeClass.getKey()))) {
                        for (Object notable : NotableManager.getAllType(typeClass.getValue())) {
                            allNotables.addObject((ObjectTag) notable);
                        }
                        break;
                    }
                }
            }
            else {
                for (Notable notable : NotableManager.notableObjects.values()) {
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
        if (attribute.startsWith("statistic_type") && attribute.hasContext(1)) {
            Statistic statistic;
            try {
                statistic = Statistic.valueOf(attribute.getContext(1).toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                attribute.echoError("Statistic '" + attribute.getContext(1) + "' does not exist: " + ex.getMessage());
                return;
            }
            event.setReplacedObject(new ElementTag(statistic.getType().name()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.enchantment_max_level[<enchantment>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the max level (at an enchantment table) for the given enchantment.
        // Refer also to <@link tag server.enchantment_types>.
        // -->
        if (attribute.startsWith("enchantment_max_level") && attribute.hasContext(1)) {
            Enchantment ench = Utilities.getEnchantmentByName(attribute.getContext(1));
            if (ench == null) {
                attribute.echoError("Enchantment '" + attribute.getContext(1) + "' does not exist.");
                return;
            }
            event.setReplacedObject(new ElementTag(ench.getMaxLevel()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.enchantment_start_level[<enchantment>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the starting level (at an enchantment table) for the given enchantment.
        // Refer also to <@link tag server.enchantment_types>.
        // -->
        if (attribute.startsWith("enchantment_start_level") && attribute.hasContext(1)) {
            Enchantment ench = Utilities.getEnchantmentByName(attribute.getContext(1));
            if (ench == null) {
                attribute.echoError("Enchantment '" + attribute.getContext(1) + "' does not exist.");
                return;
            }
            event.setReplacedObject(new ElementTag(ench.getStartLevel()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.started_time>
        // @returns TimeTag
        // @description
        // Returns the time the server started.
        // -->
        if (attribute.startsWith("started_time")) {
            event.setReplacedObject(new TimeTag(Denizen.startTime)
                    .getObjectAttribute(attribute.fulfill(1)));
        }
        if (attribute.startsWith("start_time")) {
            Deprecations.timeTagRewrite.warn(attribute.context);
            event.setReplacedObject(new DurationTag(Denizen.startTime / 50)
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
        if ((attribute.startsWith("npcs_named") || attribute.startsWith("list_npcs_named")) && Depends.citizens != null && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            ListTag npcs = new ListTag();
            String name = attribute.getContext(1);
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
        if (attribute.startsWith("has_file") && attribute.hasContext(1)) {
            File f = new File(Denizen.getInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(f)) {
                    attribute.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
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
        // Returns a list of all files in the specified directory. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("list_files") && attribute.hasContext(1)) {
            File folder = new File(Denizen.getInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(folder)) {
                    attribute.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
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
                    Debug.echoError(attribute.getScriptEntry().getResidingQueue(), e);
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

            String group = attribute.getContext(1);

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

            String group = attribute.getContext(1);

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
        if (attribute.startsWith("match_player") && attribute.hasContext(1)) {
            Player matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getContext(1));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (CoreUtilities.equalsIgnoreCase(player.getName(), matchInput)) {
                    matchPlayer = player;
                    break;
                }
                else if (CoreUtilities.toLowerCase(player.getName()).contains(matchInput) && matchPlayer == null) {
                    matchPlayer = player;
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
        // -->
        if (attribute.startsWith("match_offline_player") && attribute.hasContext(1)) {
            UUID matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getContext(1));
            for (Map.Entry<String, UUID> entry : PlayerTag.getAllPlayers().entrySet()) {
                if (CoreUtilities.equalsIgnoreCase(entry.getKey(), matchInput)) {
                    matchPlayer = entry.getValue();
                    break;
                }
                else if (CoreUtilities.toLowerCase(entry.getKey()).contains(matchInput) && matchPlayer == null) {
                    matchPlayer = entry.getValue();
                }
            }
            if (matchPlayer != null) {
                event.setReplacedObject(new PlayerTag(matchPlayer).getObjectAttribute(attribute.fulfill(1)));
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
                && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            ScriptTag script = attribute.contextAsType(1, ScriptTag.class);
            if (script == null || !(script.getContainer() instanceof AssignmentScriptContainer)) {
                attribute.echoError("Invalid script specified.");
            }
            else {
                ListTag npcs = new ListTag();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(AssignmentTrait.class) && npc.getOrAddTrait(AssignmentTrait.class).hasAssignment()
                            && CoreUtilities.equalsIgnoreCase(npc.getOrAddTrait(AssignmentTrait.class).getAssignment().getName(), script.getName())) {
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
                && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            String flag = attribute.getContext(1);
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
                && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            String flag = attribute.getContext(1);
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
                && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            String flag = attribute.getContext(1);
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
                && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            String flag = attribute.getContext(1);
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
            if (attribute.hasContext(1)) {
                registry = NPCTag.getRegistryByName(attribute.getContext(1));
                if (registry == null) {
                    attribute.echoError("NPC Registry '" + attribute.getContext(1) + "' does not exist.");
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
        if (attribute.startsWith("is_banned") && attribute.hasContext(1)) {
            // BanList contains an isBanned method that doesn't check expiration time
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(attribute.getContext(1));

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

        if (attribute.startsWith("ban_info") && attribute.hasContext(1)) {
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(attribute.getContext(1));
            attribute.fulfill(1);
            if (ban == null || (ban.getExpiration() != null && ban.getExpiration().before(new Date()))) {
                return;
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].expiration>
            // @returns DurationTag
            // @description
            // Returns the expiration of the ip address's ban, if it is banned.
            // Potentially can be null.
            // -->
            if (attribute.startsWith("expiration") && ban.getExpiration() != null) {
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
            // @attribute <server.ban_info[<address>].created>
            // @returns DurationTag
            // @description
            // Returns when the ip address's ban was created, if it is banned.
            // -->
            else if (attribute.startsWith("created")) {
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

        // <--[tag]
        // @attribute <server.entity_is_spawned[<entity>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether an entity is spawned and valid.
        // -->
        else if (attribute.startsWith("entity_is_spawned")
                && attribute.hasContext(1)) {
            EntityTag ent = EntityTag.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag((ent != null && ent.isUnique() && ent.isSpawnedOrValidForTag()) ? "true" : "false")
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.player_is_valid[<player name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether a player exists under the specified name.
        // -->
        else if (attribute.startsWith("player_is_valid")
                && attribute.hasContext(1)) {
            event.setReplacedObject(new ElementTag(PlayerTag.playerNameIsValid(attribute.getContext(1)))
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.npc_is_valid[<npc>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether an NPC exists and is usable.
        // -->
        else if (attribute.startsWith("npc_is_valid")
                && attribute.hasContext(1)) {
            NPCTag npc = NPCTag.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, null, false, null));
            event.setReplacedObject(new ElementTag((npc != null && npc.isValid()))
                    .getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_bossbars>
        // @returns ListTag
        // @description
        // Returns a list of all currently active boss bar IDs.
        // -->
        else if (attribute.startsWith("current_bossbars")) {
            ListTag dl = new ListTag(BossBarCommand.bossBarMap.keySet());
            event.setReplacedObject(dl.getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.recent_tps>
        // @returns ListTag
        // @description
        // Returns the 3 most recent ticks per second measurements.
        // -->
        else if (attribute.startsWith("recent_tps")) {
            ListTag list = new ListTag();
            for (double tps : NMSHandler.getInstance().getRecentTps()) {
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
            event.setReplacedObject(new ElementTag(NMSHandler.getInstance().getPort()).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.debug_enabled>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether script debug is currently globally enabled on the server.
        // -->
        else if (attribute.startsWith("debug_enabled")) {
            event.setReplacedObject(new ElementTag(Debug.showDebug).getObjectAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.plugins_handling_event[<bukkit event>]>
        // @returns ListTag(PluginTag)
        // @description
        // Returns a list of all plugins that handle a given Bukkit event.
        // Can specify by ScriptEvent name ("PlayerBreaksBlock"), or by full Bukkit class name ("org.bukkit.event.block.BlockBreakEvent").
        // This is a primarily a dev tool and is not necessarily useful to most players or scripts.
        // -->
        else if ((attribute.matches("plugins_handling_event") || attribute.matches("list_plugins_handling_event")) && attribute.hasContext(1)) {
            listDeprecateWarn(attribute);
            String eventName = attribute.getContext(1);
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
    }

    public static void listDeprecateWarn(Attribute attribute) {
        if (FutureWarning.futureWarningsEnabled && attribute.getAttribute(1).startsWith("list_")) {
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

    public static void adjustServer(Mechanism mechanism) {

        // <--[mechanism]
        // @object server
        // @name delete_file
        // @input ElementTag
        // @description
        // Deletes the given file from the server.
        // Require config setting 'Commands.Delete.Allow file deletion'.
        // @tags
        // <server.has_file[<file>]>
        // -->
        if (mechanism.matches("delete_file") && mechanism.hasValue()) {
            if (!Settings.allowDelete()) {
                Debug.echoError("File deletion disabled by administrator.");
                return;
            }
            File file = new File(Denizen.getInstance().getDataFolder(), mechanism.getValue().asString());
            if (!Utilities.canWriteToFile(file)) {
                Debug.echoError("Cannot delete that file (unsafe path).");
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
            if (!Settings.allowConsoleRedirection()) {
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
        // Resets the statistics on events for the queue.stats tag.
        // @tags
        // <queue.stats>
        // -->
        if (mechanism.matches("reset_event_stats")) {
            for (ScriptEvent se : ScriptEvent.events) {
                se.stats.fires = 0;
                se.stats.scriptFires = 0;
                se.stats.nanoTimes = 0;
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
        // @tags
        // <server.recipe_ids[(<type>)]>
        // -->
        if (mechanism.matches("remove_recipes")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                NMSHandler.getItemHelper().removeRecipe(Utilities.parseNamespacedKey(str));
            }
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
        // Requires setting "Commands.Restart.Allow server restart"!
        // Note that if your server is not configured to restart, this mechanism will simply stop the server without starting it again!
        // -->
        if (mechanism.matches("restart")) {
            if (!Settings.allowServerRestart()) {
                Debug.echoError("Server restart disabled by administrator. Consider using 'shutdown'.");
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
        // Requires setting "Commands.Restart.Allow server stop"!
        // -->
        if (mechanism.matches("shutdown")) {
            if (!Settings.allowServerStop()) {
                Debug.echoError("Server stop disabled by administrator. Consider using 'restart'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server shutdown by a Denizen script, see config to prevent this!");
            Bukkit.shutdown();
        }
    }
}

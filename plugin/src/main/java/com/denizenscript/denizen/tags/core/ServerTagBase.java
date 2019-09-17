package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.scripts.commands.server.BossBarCommand;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.commands.core.SQLCommand;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.WorldScriptContainer;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.javaluator.DoubleEvaluator;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class ServerTagBase {

    public ServerTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                mathTag(event);
            }
        }, "math", "m");
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                serverTag(event);
            }
        }, "server", "svr", "global");
    }

    public void mathTag(ReplaceableTagEvent event) {
        if (!event.matches("math", "m")) {
            return;
        }
        Deprecations.mathTagBase.warn(event.getScriptEntry());
        try {
            Double evaluation = new DoubleEvaluator().evaluate(event.getValue());
            event.setReplaced(new ElementTag(String.valueOf(evaluation)).getAttribute(event.getAttributes().fulfill(1)));
        }
        catch (Exception e) {
            Debug.echoError("Invalid math tag!");
            event.setReplaced("0.0");
        }
    }


    public void serverTag(ReplaceableTagEvent event) {
        if (!event.matches("server", "svr", "global") || event.replaced()) {
            return;
        }
        if (event.matches("srv")) {
            Deprecations.serverShorthand.warn(event.getScriptEntry());
        }
        if (event.matches("global")) {
            Deprecations.globalTagName.warn(event.getScriptEntry());
        }
        Attribute attribute = event.getAttributes().fulfill(1);

        if (attribute.startsWith("economy")) {
            if (Depends.economy == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
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
                event.setReplaced(new ElementTag(slotId).getAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.parse_bukkit_item[<serial>]>
        // @returns ItemTag
        // @description
        // Returns the ItemTag resultant from parsing Bukkit item serialization data (under subkey "item").
        // -->
        if (attribute.startsWith("parse_bukkit_item")
                && attribute.hasContext(1)) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(attribute.getContext(1));
                ItemStack item = config.getItemStack("item");
                if (item != null) {
                    event.setReplaced(new ItemTag(item).getAttribute(attribute.fulfill(1)));
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            return;
        }

        // <--[tag]
        // @attribute <server.list_advancements>
        // @returns ListTag
        // @description
        // Returns a list of all registered advancement names.
        // -->
        if (attribute.startsWith("list_advancements")) {
            ListTag list = new ListTag();
            Bukkit.advancementIterator().forEachRemaining((adv) -> {
                list.add(adv.getKey().toString());
            });
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_commands>
        // @returns ListTag
        // @description
        // Returns a list of all registered command names in Bukkit.
        // -->
        if (attribute.startsWith("list_commands")) {
            ListTag list = new ListTag();
            for (String cmd : CommandScriptHelper.knownCommands.keySet()) {
                list.add(cmd);
            }
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
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
            // @returns ListTag
            // @description
            // Returns whether a given scoreboard exists on the server.
            // -->
            if (attribute.startsWith("exists")) {
                event.setReplaced(new ElementTag(board != null).getAttribute(attribute.fulfill(1)));
                return;
            }
            if (board == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Scoreboard '" + name + "' does not exist.");
                }
                return;
            }

            if (attribute.startsWith("team") && attribute.hasContext(1)) {
                Team team = board.getTeam(attribute.getContext(1));
                if (team == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Scoreboard team '" + attribute.getContext(1) + "' does not exist.");
                    }
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
        // @returns ElementTag(boolean)
        // @description
        // Returns whether the object is a valid object (non-null), as well as not an Element.
        // -->
        if (attribute.startsWith("object_is_valid")) {
            ObjectTag o = ObjectFetcher.pickObjectFor(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new ElementTag(!(o == null || o instanceof ElementTag)).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_whitelist>
        // @returns ElementTag(boolean)
        // @description
        // Returns true if the server's whitelist is active, otherwise returns false.
        // -->
        if (attribute.startsWith("has_whitelist")) {
            event.setReplaced(new ElementTag(Bukkit.hasWhitelist()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns ElementTag(boolean)
        // @description
        // Returns true if the server has the specified flag, otherwise returns false.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                event.setReplaced("null");
                return;
            }
            event.setReplaced(new ElementTag(FlagManager.serverHasFlag(flag_name)).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.flag[<name>]>
        // @returns Flag ListTag
        // @description
        // Returns the specified flag from the server.
        // -->
        if (attribute.startsWith("flag") && attribute.hasContext(1)) {
            String flag_name = attribute.getContext(1);
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <server.flag[<flag_name>].is_expired>
            // @returns ElementTag(Boolean)
            // @description
            // returns true if the flag is expired or does not exist, false if it is not yet expired or has no expiration.
            // -->
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired")) {
                event.setReplaced(new ElementTag(!FlagManager.serverHasFlag(flag_name))
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
            if (attribute.startsWith("size") && !FlagManager.serverHasFlag(flag_name)) {
                event.setReplaced(new ElementTag(0).getAttribute(attribute.fulfill(1)));
                return;
            }
            if (FlagManager.serverHasFlag(flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                        .getGlobalFlag(flag_name);

                // <--[tag]
                // @attribute <server.flag[<flag_name>].expiration>
                // @returns DurationTag
                // @description
                // Returns a DurationTag of the time remaining on the flag, if it has an expiration.
                // Works with offline players.
                // -->
                if (attribute.startsWith("expiration")) {
                    event.setReplaced(flag.expiration().getAttribute(attribute.fulfill(1)));
                }

                event.setReplaced(new ListTag(flag.toString(), true, flag.values())
                        .getAttribute(attribute));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.list_traits>
        // @Plugin Citizens
        // @returns ListTag
        // @description
        // Returns a list of all available NPC traits on the server.
        // -->
        if (attribute.startsWith("list_traits") && Depends.citizens != null) {
            ListTag allTraits = new ListTag();
            for (TraitInfo trait : CitizensAPI.getTraitFactory().getRegisteredTraits()) {
                allTraits.add(trait.getTraitName());
            }
            event.setReplaced(allTraits.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_biomes>
        // @returns ListTag
        // @description
        // Returns a list of all biomes known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_biomes")) {
            ListTag allBiomes = new ListTag();
            for (Biome biome : Biome.values()) {
                allBiomes.add(biome.name());
            }
            event.setReplaced(allBiomes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_enchantments>
        // @returns ListTag
        // @description
        // Returns a list of all enchantments known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_enchantments")) {
            ListTag enchants = new ListTag();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getName());
            }
            event.setReplaced(enchants.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_entity_types>
        // @returns ListTag
        // @description
        // Returns a list of all entity types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_entity_types")) {
            ListTag allEnt = new ListTag();
            for (EntityType entity : EntityType.values()) {
                allEnt.add(entity.name());
            }
            event.setReplaced(allEnt.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_materials>
        // @returns ListTag
        // @description
        // Returns a list of all materials known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_materials")) {
            ListTag allMats = new ListTag();
            for (Material mat : Material.values()) {
                allMats.add(mat.name());
            }
            event.setReplaced(allMats.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_sounds>
        // @returns ListTag
        // @description
        // Returns a list of all sounds known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_sounds")) {
            ListTag sounds = new ListTag();
            for (Sound s : Sound.values()) {
                sounds.add(s.toString());
            }
            event.setReplaced(sounds.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_patterns>
        // @returns ListTag
        // @description
        // Returns a list of all banner patterns known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_patterns")) {
            ListTag allPatterns = new ListTag();
            for (PatternType pat : PatternType.values()) {
                allPatterns.add(pat.toString());
            }
            event.setReplaced(allPatterns.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_potion_effects>
        // @returns ListTag
        // @description
        // Returns a list of all potion effects known to the server.
        // Can be used with <@link command cast>.
        // -->
        if (attribute.startsWith("list_potion_effects")) {
            ListTag statuses = new ListTag();
            for (PotionEffectType effect : PotionEffectType.values()) {
                if (effect != null) {
                    statuses.add(effect.getName());
                }
            }
            event.setReplaced(statuses.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_potion_types>
        // @returns ListTag
        // @description
        // Returns a list of all potion types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_potion_types")) {
            ListTag potionTypes = new ListTag();
            for (PotionType type : PotionType.values()) {
                potionTypes.add(type.toString());
            }
            event.setReplaced(potionTypes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_tree_types>
        // @returns ListTag
        // @description
        // Returns a list of all tree types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_tree_types")) {
            ListTag allTrees = new ListTag();
            for (TreeType tree : TreeType.values()) {
                allTrees.add(tree.name());
            }
            event.setReplaced(allTrees.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_map_cursor_types>
        // @returns ListTag
        // @description
        // Returns a list of all map cursor types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_map_cursor_types")) {
            ListTag mapCursors = new ListTag();
            for (MapCursor.Type cursor : MapCursor.Type.values()) {
                mapCursors.add(cursor.toString());
            }
            event.setReplaced(mapCursors.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_world_types>
        // @returns ListTag
        // @description
        // Returns a list of all world types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_world_types")) {
            ListTag worldTypes = new ListTag();
            for (WorldType world : WorldType.values()) {
                worldTypes.add(world.toString());
            }
            event.setReplaced(worldTypes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_flags[(regex:)<search>]>
        // @returns ListTag
        // @description
        // Returns a list of the server's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        if (attribute.startsWith("list_flags")) {
            ListTag allFlags = new ListTag(DenizenAPI.getCurrentInstance().flagManager().listGlobalFlags());
            ListTag searchFlags = null;
            if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                searchFlags = new ListTag();
                String search = attribute.getContext(1);
                if (search.startsWith("regex:")) {
                    try {
                        Pattern pattern = Pattern.compile(search.substring(6), Pattern.CASE_INSENSITIVE);
                        for (String flag : allFlags) {
                            if (pattern.matcher(flag).matches()) {
                                searchFlags.add(flag);
                            }
                        }
                    }
                    catch (Exception e) {
                        Debug.echoError(e);
                    }
                }
                else {
                    search = CoreUtilities.toLowerCase(search);
                    for (String flag : allFlags) {
                        if (CoreUtilities.toLowerCase(flag).contains(search)) {
                            searchFlags.add(flag);
                        }
                    }
                }
                DenizenAPI.getCurrentInstance().flagManager().shrinkGlobalFlags(searchFlags);
            }
            else {
                DenizenAPI.getCurrentInstance().flagManager().shrinkGlobalFlags(allFlags);
            }
            event.setReplaced(searchFlags == null ? allFlags.getAttribute(attribute.fulfill(1))
                    : searchFlags.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_notables[<type>]>
        // @returns ListTag
        // @description
        // Lists all saved Notables currently on the server.
        // Optionally, specify a type to search for.
        // Valid types: locations, cuboids, ellipsoids, items, inventories
        // -->
        if (attribute.startsWith("list_notables")) {
            ListTag allNotables = new ListTag();
            if (attribute.hasContext(1)) {
                String type = CoreUtilities.toLowerCase(attribute.getContext(1));
                for (Map.Entry<String, Class> typeClass : NotableManager.getReverseClassIdMap().entrySet()) {
                    if (type.equals(CoreUtilities.toLowerCase(typeClass.getKey()))) {
                        for (Object notable : NotableManager.getAllType(typeClass.getValue())) {
                            allNotables.add(((ObjectTag) notable).identify());
                        }
                        break;
                    }
                }
            }
            else {
                for (Notable notable : NotableManager.notableObjects.values()) {
                    allNotables.add(((ObjectTag) notable).identify());
                }
            }
            event.setReplaced(allNotables.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.start_time>
        // @returns DurationTag
        // @description
        // Returns the time the server started as a duration time.
        // -->
        if (attribute.startsWith("start_time")) {
            event.setReplaced(new DurationTag(Denizen.startTime / 50)
                    .getAttribute(attribute.fulfill(1)));
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
            event.setReplaced(new ElementTag(Runtime.getRuntime().totalMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_max>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is available to the server (total), in bytes (max memory).
        // -->
        if (attribute.startsWith("ram_max")) {
            event.setReplaced(new ElementTag(Runtime.getRuntime().maxMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_free>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is unused but available on the server, in bytes (free memory).
        // -->
        if (attribute.startsWith("ram_free")) {
            event.setReplaced(new ElementTag(Runtime.getRuntime().freeMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_usage>
        // @returns ElementTag(Number)
        // @description
        // How much RAM is used by the server, in bytes (free memory).
        // Equivalent to ram_max minus ram_free
        // -->
        if (attribute.startsWith("ram_usage")) {
            event.setReplaced(new ElementTag(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.available_processors>
        // @returns ElementTag(Number)
        // @description
        // How many virtual processors are available to the server.
        // (In general, Minecraft only uses one, unfortunately.)
        // -->
        if (attribute.startsWith("available_processors")) {
            event.setReplaced(new ElementTag(Runtime.getRuntime().availableProcessors())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_time_millis>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of milliseconds since Jan 1, 1970.
        // Note that this can change every time the tag is read!
        // -->
        if (attribute.startsWith("current_time_millis")) {
            event.setReplaced(new ElementTag(System.currentTimeMillis())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.has_event[<event_name>]>
        // @returns ElementTag(Number)
        // @description
        // Returns whether a world event exists on the server.
        // This tag will ignore ObjectTag identifiers (see <@link language dobject>).
        // -->
        if (attribute.startsWith("has_event")
                && attribute.hasContext(1)) {
            event.setReplaced(new ElementTag(OldEventManager.eventExists(attribute.getContext(1))
                    || OldEventManager.eventExists(OldEventManager.StripIdentifiers(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.event_handlers[<event_name>]>
        // @returns ListTag(ScriptTag)
        // @description
        // Returns a list of all world scripts that will handle a given event name.
        // This tag will ignore ObjectTag identifiers (see <@link language dobject>).
        // For use with <@link tag server.has_event[<event_name>]>
        // -->
        if (attribute.startsWith("event_handlers")
                && attribute.hasContext(1)) {
            String eventName = attribute.getContext(1).toUpperCase();
            List<WorldScriptContainer> EventsOne = OldEventManager.events.get("ON " + eventName);
            List<WorldScriptContainer> EventsTwo = OldEventManager.events.get("ON " + OldEventManager.StripIdentifiers(eventName));
            if (EventsOne == null && EventsTwo == null) {
                Debug.echoError("No world scripts will handle the event '" + eventName + "'");
            }
            else {
                ListTag list = new ListTag();
                if (EventsOne != null) {
                    for (WorldScriptContainer script : EventsOne) {
                        list.add("s@" + script.getName());
                    }
                }
                if (EventsTwo != null) {
                    for (WorldScriptContainer script : EventsTwo) {
                        if (!list.contains("s@" + script.getName())) {
                            list.add("s@" + script.getName());
                        }
                    }
                }
                event.setReplaced(list.getAttribute(attribute.fulfill(1)));
            }
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
                event.setReplaced(new NPCTag(npc).getAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs_named[<name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of NPCs with a certain name.
        // -->
        if ((attribute.startsWith("list_npcs_named") || attribute.startsWith("get_npcs_named")) && Depends.citizens != null && attribute.hasContext(1)) {
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.getName().equalsIgnoreCase(attribute.getContext(1))) {
                    npcs.add(NPCTag.mirrorCitizensNPC(npc).identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_file[<name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the specified file exists. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("has_file") && attribute.hasContext(1)) {
            File f = new File(DenizenAPI.getCurrentInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(f)) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
                    }
                    return;
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return;
            }
            event.setReplaced(new ElementTag(f.exists()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_files[<path>]>
        // @returns ListTag
        // @description
        // Returns a list of all files in the specified directory. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("list_files") && attribute.hasContext(1)) {
            File folder = new File(DenizenAPI.getCurrentInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(folder)) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
                    }
                    return;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Invalid path specified. No directory exists at that path.");
                    }
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
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
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
            event.setReplaced(new ElementTag(Depends.permissions != null && Depends.permissions.isEnabled())
                    .getAttribute(attribute.fulfill(1)));
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
            event.setReplaced(new ElementTag(Depends.economy != null && Depends.economy.isEnabled())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.denizen_version>
        // @returns ElementTag
        // @description
        // Returns the version of Denizen currently being used.
        // -->
        if (attribute.startsWith("denizen_version")) {
            event.setReplaced(new ElementTag(DenizenAPI.getCurrentInstance().getDescription().getVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.bukkit_version>
        // @returns ElementTag
        // @description
        // Returns the version of Bukkit currently being used.
        // -->
        if (attribute.startsWith("bukkit_version")) {
            event.setReplaced(new ElementTag(Bukkit.getBukkitVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.version>
        // @returns ElementTag
        // @description
        // Returns the version of the server.
        // -->
        if (attribute.startsWith("version")) {
            event.setReplaced(new ElementTag(Bukkit.getServer().getVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.java_version>
        // @returns ElementTag
        // @description
        // Returns the current Java version of the server.
        // -->
        if (attribute.startsWith("java_version")) {
            event.setReplaced(new ElementTag(System.getProperty("java.version"))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.max_players>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum number of players allowed on the server.
        // -->
        if (attribute.startsWith("max_players")) {
            event.setReplaced(new ElementTag(Bukkit.getServer().getMaxPlayers())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_sql_connections>
        // @returns ListTag
        // @description
        // Returns a list of all SQL connections opened by <@link command sql>.
        // -->
        if (attribute.startsWith("list_sql_connections")) {
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
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
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
                Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getContext(1);

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                Debug.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_prefix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat prefix for the specified WorldTag.
            // -->
            if (attribute.getAttribute(2).startsWith("world")) {
                WorldTag world = WorldTag.valueOf(attribute.getContext(2));
                if (world != null) {
                    event.setReplaced(new ElementTag(Depends.chat.getGroupPrefix(world.getWorld(), group))
                            .getAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Prefix in default world
            event.setReplaced(new ElementTag(Depends.chat.getGroupPrefix(Bukkit.getWorlds().get(0), group))
                    .getAttribute(attribute.fulfill(1)));
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
                Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getContext(1);

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                Debug.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_suffix[<group>].world[<world>]>
            // @returns ElementTag
            // @description
            // Returns an ElementTag of a group's chat suffix for the specified WorldTag.
            // -->
            if (attribute.getAttribute(2).startsWith("world")) {
                WorldTag world = WorldTag.valueOf(attribute.getContext(2));
                if (world != null) {
                    event.setReplaced(new ElementTag(Depends.chat.getGroupSuffix(world.getWorld(), group))
                            .getAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Suffix in default world
            event.setReplaced(new ElementTag(Depends.chat.getGroupSuffix(Bukkit.getWorlds().get(0), group))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_permission_groups>
        // @returns ListTag
        // @description
        // Returns a list of all permission groups on the server.
        // -->
        if (attribute.startsWith("list_permission_groups")) {
            if (Depends.permissions == null) {
                Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }
            event.setReplaced(new ListTag(Arrays.asList(Depends.permissions.getGroups())).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_plugin_names>
        // @returns ListTag
        // @description
        // Gets a list of currently enabled plugin names from the server.
        // -->
        if (attribute.startsWith("list_plugin_names")) {
            ListTag plugins = new ListTag();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.add(plugin.getName());
            }
            event.setReplaced(plugins.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_scripts>
        // @returns ListTag(ScriptTag)
        // @description
        // Gets a list of all scripts currently loaded into Denizen.
        // -->
        if (attribute.startsWith("list_scripts")) {
            ListTag scripts = new ListTag();
            for (String str : ScriptRegistry._getScriptNames()) {
                scripts.add("s@" + str);
            }
            event.setReplaced(scripts.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.match_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns the online player that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns p@bob,
        // input 'bobb' returns p@bobby, and input 'b' returns p@bo.
        // -->
        if (attribute.startsWith("match_player") && attribute.hasContext(1)) {
            Player matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getContext(1));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (CoreUtilities.toLowerCase(player.getName()).equals(matchInput)) {
                    matchPlayer = player;
                    break;
                }
                else if (CoreUtilities.toLowerCase(player.getName()).contains(matchInput) && matchPlayer == null) {
                    matchPlayer = player;
                }
            }

            if (matchPlayer != null) {
                event.setReplaced(new PlayerTag(matchPlayer).getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.match_offline_player[<name>]>
        // @returns PlayerTag
        // @description
        // Returns any player (online or offline) that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns p@bob,
        // input 'bobb' returns p@bobby, and input 'b' returns p@bo.
        // -->
        if (attribute.startsWith("match_offline_player") && attribute.hasContext(1)) {
            UUID matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getContext(1));
            for (Map.Entry<String, UUID> entry : PlayerTag.getAllPlayers().entrySet()) {
                if (CoreUtilities.toLowerCase(entry.getKey()).equals(matchInput)) {
                    matchPlayer = entry.getValue();
                    break;
                }
                else if (CoreUtilities.toLowerCase(entry.getKey()).contains(matchInput) && matchPlayer == null) {
                    matchPlayer = entry.getValue();
                }
            }

            if (matchPlayer != null) {
                event.setReplaced(new PlayerTag(matchPlayer).getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs_assigned[<assignment_script>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs assigned to a specified script.
        // -->
        if ((attribute.startsWith("list_npcs_assigned") || attribute.startsWith("get_npcs_assigned")) && Depends.citizens != null
                && attribute.hasContext(1)) {
            ScriptTag script = ScriptTag.valueOf(attribute.getContext(1));
            if (script == null || !(script.getContainer() instanceof AssignmentScriptContainer)) {
                Debug.echoError("Invalid script specified.");
            }
            else {
                ListTag npcs = new ListTag();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()
                            && npc.getTrait(AssignmentTrait.class).getAssignment().getName().equalsIgnoreCase(script.getName())) {
                        npcs.add(NPCTag.mirrorCitizensNPC(npc).identify());
                    }
                }
                event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <server.list_online_players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players with a specified flag set.
        // -->
        if ((attribute.startsWith("list_online_players_flagged") || attribute.startsWith("get_online_players_flagged"))
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            ListTag players = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag(new PlayerTag(player), flag).size() > 0) {
                    players.add(new PlayerTag(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_players_flagged[<flag_name>]>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players with a specified flag set.
        // -->
        if ((attribute.startsWith("list_players_flagged") || attribute.startsWith("get_players_flagged"))
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            ListTag players = new ListTag();
            for (Map.Entry<String, UUID> entry : PlayerTag.getAllPlayers().entrySet()) {
                if (DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag(new PlayerTag(entry.getValue()), flag).size() > 0) {
                    players.add(new PlayerTag(entry.getValue()).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_spawned_npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all spawned NPCs with a specified flag set.
        // -->
        if ((attribute.startsWith("list_spawned_npcs_flagged") || attribute.startsWith("get_spawned_npcs_flagged")) && Depends.citizens != null
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag dNpc = NPCTag.mirrorCitizensNPC(npc);
                if (dNpc.isSpawned() && FlagManager.npcHasFlag(dNpc, flag)) {
                    npcs.add(dNpc.identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs_flagged[<flag_name>]>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs with a specified flag set.
        // -->
        if ((attribute.startsWith("list_npcs_flagged") || attribute.startsWith("get_npcs_flagged")) && Depends.citizens != null
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                NPCTag dNpc = NPCTag.mirrorCitizensNPC(npc);
                if (FlagManager.npcHasFlag(dNpc, flag)) {
                    npcs.add(dNpc.identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs.
        // -->
        if (attribute.startsWith("list_npcs") && Depends.citizens != null) {
            ListTag npcs = new ListTag();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                npcs.add(NPCTag.mirrorCitizensNPC(npc).identify());
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_worlds>
        // @returns ListTag(WorldTag)
        // @description
        // Returns a list of all worlds.
        // -->
        if (attribute.startsWith("list_worlds")) {
            ListTag worlds = new ListTag();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(WorldTag.mirrorBukkitWorld(world).identify());
            }
            event.setReplaced(worlds.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_plugins>
        // @returns ListTag(PluginTag)
        // @description
        // Gets a list of currently enabled PluginTags from the server.
        // -->
        if (attribute.startsWith("list_plugins")) {
            ListTag plugins = new ListTag();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.add(new PluginTag(plugin).identify());
            }
            event.setReplaced(plugins.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("list_players")) {
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online players.
        // -->
        if (attribute.startsWith("list_online_players")) {
            ListTag players = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline players.
        // -->
        if (attribute.startsWith("list_offline_players")) {
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (!player.isOnline()) {
                    players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_banned_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all banned players.
        // -->
        if (attribute.startsWith("list_banned_players")) {
            ListTag banned = new ListTag();
            for (OfflinePlayer player : Bukkit.getBannedPlayers()) {
                banned.add(PlayerTag.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(banned.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_banned_addresses>
        // @returns ListTag
        // @description
        // Returns a list of all banned ip addresses.
        // -->
        if (attribute.startsWith("list_banned_addresses")) {
            ListTag list = new ListTag();
            list.addAll(Bukkit.getIPBans());
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
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
                event.setReplaced(new ElementTag(false).getAttribute(attribute.fulfill(1)));
            }
            else if (ban.getExpiration() == null) {
                event.setReplaced(new ElementTag(true).getAttribute(attribute.fulfill(1)));
            }
            else {
                event.setReplaced(new ElementTag(ban.getExpiration().after(new Date())).getAttribute(attribute.fulfill(1)));
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
                event.setReplaced(new DurationTag(ban.getExpiration().getTime() / 50)
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].reason>
            // @returns ElementTag
            // @description
            // Returns the reason for the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("reason")) {
                event.setReplaced(new ElementTag(ban.getReason())
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].created>
            // @returns DurationTag
            // @description
            // Returns when the ip address's ban was created, if it is banned.
            // -->
            else if (attribute.startsWith("created")) {
                event.setReplaced(new DurationTag(ban.getCreated().getTime() / 50)
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].source>
            // @returns ElementTag
            // @description
            // Returns the source of the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("source")) {
                event.setReplaced(new ElementTag(ban.getSource())
                        .getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.list_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all ops, online or not.
        // -->
        if (attribute.startsWith("list_ops")) {
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all online ops.
        // -->
        if (attribute.startsWith("list_online_ops")) {
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                if (player.isOnline()) {
                    players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_ops>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of all offline ops.
        // -->
        if (attribute.startsWith("list_offline_ops")) {
            ListTag players = new ListTag();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isOp() && !player.isOnline()) {
                    players.add(PlayerTag.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.motd>
        // @returns ElementTag
        // @description
        // Returns the server's current MOTD.
        // -->
        if (attribute.startsWith("motd")) {
            event.setReplaced(new ElementTag(Bukkit.getServer().getMotd()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.view_distance>
        // @returns ElementTag(Number)
        // @description
        // Returns the server's current view distance.
        // -->
        if (attribute.startsWith("view_distance")) {
            event.setReplaced(new ElementTag(Bukkit.getServer().getViewDistance()).getAttribute(attribute.fulfill(1)));
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
            EntityTag ent = EntityTag.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new ElementTag((ent != null && ent.isUnique() && ent.isSpawnedOrValidForTag()) ? "true" : "false")
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.player_is_valid[<player name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether a player exists under the specified name.
        // -->
        else if (attribute.startsWith("player_is_valid")
                && attribute.hasContext(1)) {
            event.setReplaced(new ElementTag(PlayerTag.playerNameIsValid(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.npc_is_valid[<npc>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether an NPC exists and is usable.
        // -->
        else if (attribute.startsWith("npc_is_valid")
                && attribute.hasContext(1)) {
            NPCTag npc = NPCTag.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new ElementTag((npc != null && npc.isValid()))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_bossbars>
        // @returns ListTag
        // @description
        // Returns a list of all currently active boss bar IDs.
        // -->
        else if (attribute.startsWith("current_bossbars")) {
            ListTag dl = new ListTag();
            for (String str : BossBarCommand.bossBarMap.keySet()) {
                dl.add(str);
            }
            event.setReplaced(dl.getAttribute(attribute.fulfill(1)));
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
                list.add(new ElementTag(tps).identify());
            }
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.port>
        // @returns ElementTag(Number)
        // @description
        // Returns the port that the server is running on.
        // -->
        else if (attribute.startsWith("port")) {
            event.setReplaced(new ElementTag(NMSHandler.getInstance().getPort()).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.debug_enabled>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether script debug is currently globally enabled on the server.
        // -->
        else if (attribute.startsWith("debug_enabled")) {
            event.setReplaced(new ElementTag(Debug.showDebug).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_plugins_handling_event[<bukkit event>]>
        // @returns ListTag(PluginTag)
        // @description
        // Returns a list of all plugins that handle a given Bukkit event.
        // Can specify by ScriptEvent name ("PlayerBreaksBlock"), or by full Bukkit class name ("org.bukkit.event.block.BlockBreakEvent").
        // This is a primarily a dev tool and is not necessarily useful to most players or scripts.
        // -->
        else if (attribute.matches("list_plugins_handling_event")
                && attribute.hasContext(1)) {
            String eventName = attribute.getContext(1);
            if (eventName.contains(".")) {
                try {
                    Class clazz = Class.forName(eventName, false, ServerTagBase.class.getClassLoader());
                    ListTag result = getHandlerPluginList(clazz);
                    if (result != null) {
                        event.setReplaced(result.getAttribute(attribute.fulfill(1)));
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
                    Plugin plugin = DenizenAPI.getCurrentInstance();
                    for (Class eventClass : plugin.getPluginLoader()
                            .createRegisteredListeners((Listener) scriptEvent, plugin).keySet()) {
                        ListTag result = getHandlerPluginList(eventClass);
                        // Return results for the first valid match.
                        if (result != null && result.size() > 0) {
                            event.setReplaced(result.getAttribute(attribute.fulfill(1)));
                            return;
                        }
                    }
                    event.setReplaced(new ListTag().getAttribute(attribute.fulfill(1)));
                }
            }
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
        // @input Element
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
            File file = new File(DenizenAPI.getCurrentInstance().getDataFolder(), mechanism.getValue().asString());
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

        // Deprecated in favor of SYSTEM.redirect_logging (Core)
        if (mechanism.matches("redirect_logging") && mechanism.hasValue()) {
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
        // @name cleanmem
        // @input None
        // @description
        // Suggests to the internal systems that it's a good time to clean the memory.
        // Does NOT force a memory cleaning.
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
        // @tags
        // None
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
        // @tags
        // None
        // -->
        if (mechanism.matches("save")) {
            DenizenAPI.getCurrentInstance().saveSaves();
        }

        // <--[mechanism]
        // @object server
        // @name save_citizens
        // @input None
        // @description
        // Immediately saves the Citizens saves files.
        // @tags
        // None
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
        // @tags
        // None
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

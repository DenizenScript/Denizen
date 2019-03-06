package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dPlugin;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.scripts.commands.core.SQLCommand;
import net.aufdemrand.denizen.scripts.commands.server.BossBarCommand;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.inventory.SlotHelper;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.core.WorldScriptContainer;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.debugging.SlowWarning;
import net.aufdemrand.denizencore.utilities.javaluator.DoubleEvaluator;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ServerTags {

    public ServerTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                mathTag(event);
            }
        }, "math", "m");
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                ternaryTag(event);
            }
        }, "ternary", "tern", "t");
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                serverTag(event);
            }
        }, "server", "svr", "global");
    }

    public SlowWarning mathShorthand = new SlowWarning("Short-named tags are hard to read. Please use 'math' instead of 'm' as a root tag.");

    // <--[tag]
    // @attribute <math:<calculation>>
    // @returns Element(Decimal)
    // @description
    // Returns a calculated result of the math placed after the :
    // Examples: '<math:1 + 1>' or '<math:sin(<npc.id>)>'.
    // Since this is a 'value' tag, to get an int value, you will need to do '<math.as_int:calc>',
    // and similar for all other element tags.
    // -->
    public void mathTag(ReplaceableTagEvent event) { // TODO: Core
        if (!event.matches("math", "m")) {
            return;
        }
        if (event.matches("m")) {
            mathShorthand.warn(event.getScriptEntry());
        }
        try {
            Double evaluation = new DoubleEvaluator().evaluate(event.getValue());
            event.setReplaced(new Element(String.valueOf(evaluation)).getAttribute(event.getAttributes().fulfill(1)));
        }
        catch (Exception e) {
            dB.echoError("Invalid math tag!");
            event.setReplaced("0.0");
        }
    }

    public SlowWarning ternShorthand = new SlowWarning("Short-named tags are hard to read. Please use 'tern' instead of 't' as a root tag.");

    // <--[tag]
    // @attribute <tern[<condition>]:<element>||<element>>
    // @returns Element
    // @description
    // Returns either the first element, or 'fallback' element depending on
    // the outcome of the condition. First element will show in a result of 'true',
    // otherwise the fallback element will show.
    // Example: '<tern[<player.is_spawned>]:Player is spawned! || Player is not spawned!>'
    // -->
    public void ternaryTag(ReplaceableTagEvent event) { // TODO: Core
        if (!event.matches("ternary", "tern", "t")) {
            return;
        }
        if (event.matches("t")) {
            ternShorthand.warn(event.getScriptEntry());
        }

        // Fallback if nothing to evaluate
        if (!event.hasNameContext()) {
            return;
        }

        // Check evaluation. A result of 'true' will return the value. Anything else
        // will result in the fallback.
        if (event.getNameContext().equalsIgnoreCase("true")) {
            event.setReplaced(new Element(event.getValue().trim())
                    .getAttribute(event.getAttributes().fulfill(1)));
        }
    }

    public SlowWarning serverShorthand = new SlowWarning("Short-named tags are hard to read. Please use 'server' instead of 'svr' as a root tag.");


    public void serverTag(ReplaceableTagEvent event) {
        if (!event.matches("server", "svr", "global") || event.replaced()) {
            return;
        }
        if (event.matches("srv")) {
            serverShorthand.warn(event.getScriptEntry());
        }
        if (event.matches("global")) {
            dB.echoError(event.getScriptEntry() == null ? null : event.getScriptEntry().getResidingQueue(),
                    "Using 'global' as a base tag is a deprecated alternate name. Please use 'server' instead.");
        }
        Attribute attribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <server.slot_id[<slot>]>
        // @returns Element(Number)
        // @description
        // Returns the slot ID number for an input slot (see <@link language Slot Inputs>).
        // -->
        if (attribute.startsWith("slot_id")
                && attribute.hasContext(1)) {
            int slotId = SlotHelper.nameToIndex(attribute.getContext(1));
            if (slotId != -1) {
                event.setReplaced(new Element(slotId).getAttribute(attribute.fulfill(1)));
            }
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
            // <--[tag]
            // @attribute <server.scoreboard[<board>].exists>
            // @returns dList
            // @description
            // Returns whether a given scoreboard exists on the server.
            // -->
            if (attribute.startsWith("exists")) {
                event.setReplaced(new Element(board != null).getAttribute(attribute.fulfill(2)));
                return;
            }
            if (board == null) {
                if (!attribute.hasAlternative()) {
                    dB.echoError("Scoreboard '" + name + "' does not exist.");
                }
                return;
            }
            // <--[tag]
            // @attribute <server.scoreboard[(<board>)].team_members[<team>]>
            // @returns dList
            // @description
            // Returns a list of all members of a scoreboard team. Generally returns as a list of names or text entries.
            // Members are not necessarily written in any given format and are not guaranteed to validly fit any requirements.
            // Optionally, specify which scoreboard to use.
            // -->
            if (attribute.startsWith("team_members") && attribute.hasContext(2)) {
                event.setReplacedObject(new dList(board.getEntries()).getObjectAttribute(attribute.fulfill(2)));
                return;
            }
        }

        // <--[tag]
        // @attribute <server.object_is_valid[<object>]>
        // @returns Element(boolean)
        // @description
        // Returns whether the object is a valid object (non-null), as well as not an Element.
        // -->
        if (attribute.startsWith("object_is_valid")) {
            dObject o = ObjectFetcher.pickObjectFor(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new Element(!(o == null || o instanceof Element)).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_whitelist>
        // @returns Element(boolean)
        // @description
        // Returns true if the server's whitelist is active, otherwise returns false.
        // -->
        if (attribute.startsWith("has_whitelist")) {
            event.setReplaced(new Element(Bukkit.hasWhitelist()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns Element(boolean)
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
            event.setReplaced(new Element(FlagManager.serverHasFlag(flag_name)).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.flag[<name>]>
        // @returns Flag dList
        // @description
        // Returns the specified flag from the server.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                event.setReplaced("null");
                return;
            }
            attribute.fulfill(1);
            // NOTE: Meta is in dList.java
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired")) {
                event.setReplaced(new Element(!FlagManager.serverHasFlag(flag_name))
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
            // NOTE: Meta is in dList.java
            if (attribute.startsWith("size") && !FlagManager.serverHasFlag(flag_name)) {
                event.setReplaced(new Element(0).getAttribute(attribute.fulfill(1)));
                return;
            }
            if (FlagManager.serverHasFlag(flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                        .getGlobalFlag(flag_name);
                event.setReplaced(new dList(flag.toString(), true, flag.values())
                        .getAttribute(attribute));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.list_biomes>
        // @returns dList
        // @description
        // Returns a list of all biomes known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_biomes")) {
            dList allBiomes = new dList();
            for (Biome biome : Biome.values()) {
                allBiomes.add(biome.name());
            }
            event.setReplaced(allBiomes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_enchantments>
        // @returns dList
        // @description
        // Returns a list of all enchantments known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_enchantments")) {
            dList enchants = new dList();
            for (Enchantment e : Enchantment.values()) {
                enchants.add(e.getName());
            }
            event.setReplaced(enchants.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_entity_types>
        // @returns dList
        // @description
        // Returns a list of all entity types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_entity_types")) {
            dList allEnt = new dList();
            for (EntityType entity : EntityType.values()) {
                allEnt.add(entity.name());
            }
            event.setReplaced(allEnt.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_materials>
        // @returns dList
        // @description
        // Returns a list of all materials known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_materials")) {
            dList allMats = new dList();
            for (Material mat : Material.values()) {
                allMats.add(mat.name());
            }
            event.setReplaced(allMats.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_sounds>
        // @returns dList
        // @description
        // Returns a list of all sounds known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_sounds")) {
            dList sounds = new dList();
            for (Sound s : Sound.values()) {
                sounds.add(s.toString());
            }
            event.setReplaced(sounds.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_patterns>
        // @returns dList
        // @description
        // Returns a list of all banner patterns known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_patterns")) {
            dList allPatterns = new dList();
            for (PatternType pat : PatternType.values()) {
                allPatterns.add(pat.toString());
            }
            event.setReplaced(allPatterns.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_potion_effects>
        // @returns dList
        // @description
        // Returns a list of all potion effects known to the server.
        // Can be used with <@link command cast>.
        // -->
        if (attribute.startsWith("list_potion_effects")) {
            dList statuses = new dList();
            for (PotionEffectType effect : PotionEffectType.values()) {
                if (effect != null) {
                    statuses.add(effect.getName());
                }
            }
            event.setReplaced(statuses.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_potion_types>
        // @returns dList
        // @description
        // Returns a list of all potion types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_potion_types")) {
            dList potionTypes = new dList();
            for (PotionType type : PotionType.values()) {
                potionTypes.add(type.toString());
            }
            event.setReplaced(potionTypes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_tree_types>
        // @returns dList
        // @description
        // Returns a list of all tree types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_tree_types")) {
            dList allTrees = new dList();
            for (TreeType tree : TreeType.values()) {
                allTrees.add(tree.name());
            }
            event.setReplaced(allTrees.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_map_cursor_types>
        // @returns dList
        // @description
        // Returns a list of all map cursor types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_map_cursor_types")) {
            dList mapCursors = new dList();
            for (MapCursor.Type cursor : MapCursor.Type.values()) {
                mapCursors.add(cursor.toString());
            }
            event.setReplaced(mapCursors.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_world_types>
        // @returns dList
        // @description
        // Returns a list of all world types known to the server (only their Bukkit enum names).
        // -->
        if (attribute.startsWith("list_world_types")) {
            dList worldTypes = new dList();
            for (WorldType world : WorldType.values()) {
                worldTypes.add(world.toString());
            }
            event.setReplaced(worldTypes.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.list_flags[(regex:)<search>]>
        // @returns dList
        // @description
        // Returns a list of the server's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        if (attribute.startsWith("list_flags")) {
            dList allFlags = new dList(DenizenAPI.getCurrentInstance().flagManager().listGlobalFlags());
            dList searchFlags = null;
            if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                searchFlags = new dList();
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
                        dB.echoError(e);
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
        // @returns dList
        // @description
        // Lists all saved Notables currently on the server.
        // Optionally, specify a type to search for.
        // Valid types: locations, cuboids, ellipsoids, items, inventories
        // -->
        if (attribute.startsWith("list_notables")) {
            dList allNotables = new dList();
            if (attribute.hasContext(1)) {
                String type = CoreUtilities.toLowerCase(attribute.getContext(1));
                for (Map.Entry<String, Class> typeClass : NotableManager.getReverseClassIdMap().entrySet()) {
                    if (type.equals(CoreUtilities.toLowerCase(typeClass.getKey()))) {
                        for (Object notable : NotableManager.getAllType(typeClass.getValue())) {
                            allNotables.add(((dObject) notable).identify());
                        }
                        break;
                    }
                }
            }
            else {
                for (Notable notable : NotableManager.notableObjects.values()) {
                    allNotables.add(((dObject) notable).identify());
                }
            }
            event.setReplaced(allNotables.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.start_time>
        // @returns Duration
        // @description
        // Returns the time the server started as a duration time.
        // -->
        if (attribute.startsWith("start_time")) {
            event.setReplaced(new Duration(Denizen.startTime / 50)
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_allocated>
        // @returns Element(Number)
        // @description
        // How much RAM is allocated to the server, in bytes (total memory).
        // -->
        if (attribute.startsWith("ram_allocated")) {
            event.setReplaced(new Element(Runtime.getRuntime().totalMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_max>
        // @returns Element(Number)
        // @description
        // How much RAM is available to the server, in bytes (max memory).
        // -->
        if (attribute.startsWith("ram_max")) {
            event.setReplaced(new Element(Runtime.getRuntime().maxMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.ram_free>
        // @returns Element(Number)
        // @description
        // How much RAM is unused but available on the server, in bytes (free memory).
        // -->
        if (attribute.startsWith("ram_free")) {
            event.setReplaced(new Element(Runtime.getRuntime().freeMemory())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.available_processors>
        // @returns Element(Number)
        // @description
        // How many virtual processors are available to the server.
        // (In general, Minecraft only uses one, unfortunately.)
        // -->
        if (attribute.startsWith("available_processors")) {
            event.setReplaced(new Element(Runtime.getRuntime().availableProcessors())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_time_millis>
        // @returns Element(Number)
        // @description
        // Returns the number of milliseconds since Jan 1, 1970.
        // Note that this can change every time the tag is read!
        // -->
        if (attribute.startsWith("current_time_millis")) {
            event.setReplaced(new Element(System.currentTimeMillis())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.has_event[<event_name>]>
        // @returns Element(Number)
        // @description
        // Returns whether a world event exists on the server.
        // This tag will ignore dObject identifiers (see <@link language dobject>).
        // -->
        if (attribute.startsWith("has_event")
                && attribute.hasContext(1)) {
            event.setReplaced(new Element(OldEventManager.eventExists(attribute.getContext(1))
                    || OldEventManager.eventExists(OldEventManager.StripIdentifiers(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.get_event_handlers[<event_name>]>
        // @returns dList(dScript)
        // @description
        // Returns a list of all world scripts that will handle a given event name.
        // This tag will ignore dObject identifiers (see <@link language dobject>).
        // For use with <@link tag server.has_event[<event_name>]>
        // -->
        if (attribute.startsWith("get_event_handlers")
                && attribute.hasContext(1)) {
            String eventName = attribute.getContext(1).toUpperCase();
            List<WorldScriptContainer> EventsOne = OldEventManager.events.get("ON " + eventName);
            List<WorldScriptContainer> EventsTwo = OldEventManager.events.get("ON " + OldEventManager.StripIdentifiers(eventName));
            if (EventsOne == null && EventsTwo == null) {
                dB.echoError("No world scripts will handle the event '" + eventName + "'");
            }
            else {
                dList list = new dList();
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
        // @returns dNPC
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
                event.setReplaced(new dNPC(npc).getAttribute(attribute.fulfill(1)));
            }
            return;
        }

        // <--[tag]
        // @attribute <server.get_npcs_named[<name>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of NPCs with a certain name.
        // -->
        if (attribute.startsWith("get_npcs_named") && Depends.citizens != null && attribute.hasContext(1)) {
            dList npcs = new dList();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.getName().equalsIgnoreCase(attribute.getContext(1))) {
                    npcs.add(dNPC.mirrorCitizensNPC(npc).identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_file[<name>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the specified file exists. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("has_file") && attribute.hasContext(1)) {
            File f = new File(DenizenAPI.getCurrentInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(f)) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
                    }
                    return;
                }
            }
            catch (Exception e) {
                dB.echoError(e);
                return;
            }
            event.setReplaced(new Element(f.exists()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_files[<path>]>
        // @returns dList
        // @description
        // Returns a list of all files in the specified directory. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("list_files") && attribute.hasContext(1)) {
            File folder = new File(DenizenAPI.getCurrentInstance().getDataFolder(), attribute.getContext(1));
            try {
                if (!Utilities.canReadFile(folder)) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("Invalid path specified. Invalid paths have been denied by the server administrator.");
                    }
                    return;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("Invalid path specified. No directory exists at that path.");
                    }
                    return;
                }
            }
            catch (Exception e) {
                dB.echoError(e);
                return;
            }
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            dList list = new dList();
            for (File file : files) {
                list.add(file.getName());
            }
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_permissions>
        // @returns Element(Boolean)
        // @description
        // Returns whether the server has a known permission plugin loaded.
        // Note: should not be considered incredibly reliable.
        // -->
        if (attribute.startsWith("has_permissions")) {
            event.setReplaced(new Element(Depends.permissions != null && Depends.permissions.isEnabled())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_economy>
        // @returns Element(Boolean)
        // @description
        // Returns whether the server has a known economy plugin loaded.
        // Note: should not be considered incredibly reliable.
        // -->
        if (attribute.startsWith("has_economy")) {
            event.setReplaced(new Element(Depends.economy != null && Depends.economy.isEnabled())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.denizen_version>
        // @returns Element
        // @description
        // Returns the version of Denizen currently being used.
        // -->
        if (attribute.startsWith("denizen_version")) {
            event.setReplaced(new Element(DenizenAPI.getCurrentInstance().getDescription().getVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.bukkit_version>
        // @returns Element
        // @description
        // Returns the version of Bukkit currently being used.
        // -->
        if (attribute.startsWith("bukkit_version")) {
            event.setReplaced(new Element(Bukkit.getBukkitVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.version>
        // @returns Element
        // @description
        // Returns the version of the server.
        // -->
        if (attribute.startsWith("version")) {
            event.setReplaced(new Element(Bukkit.getServer().getVersion())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.java_version>
        // @returns Element
        // @description
        // Returns the current Java version of the server.
        // -->
        if (attribute.startsWith("java_version")) {
            event.setReplaced(new Element(System.getProperty("java.version"))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.max_players>
        // @returns Element(Number)
        // @description
        // Returns the maximum number of players allowed on the server.
        // -->
        if (attribute.startsWith("max_players")) {
            event.setReplaced(new Element(Bukkit.getServer().getMaxPlayers())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_sql_connections>
        // @returns dList
        // @description
        // Returns a list of all SQL connections opened by <@link command sql>.
        // -->
        if (attribute.startsWith("list_sql_connections")) {
            dList list = new dList();
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
                    dB.echoError(attribute.getScriptEntry().getResidingQueue(), e);
                }
            }
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.group_prefix[<group>]>
        // @returns Element
        // @description
        // Returns an Element of a group's chat prefix.
        // -->
        if (attribute.startsWith("group_prefix")) {

            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getContext(1);

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                dB.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_prefix[<group>].world[<world>]>
            // @returns Element
            // @description
            // Returns an Element of a group's chat prefix for the specified dWorld.
            // -->
            if (attribute.getAttribute(2).startsWith("world")) {
                dWorld world = dWorld.valueOf(attribute.getContext(2));
                if (world != null) {
                    event.setReplaced(new Element(Depends.chat.getGroupPrefix(world.getWorld(), group))
                            .getAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Prefix in default world
            event.setReplaced(new Element(Depends.chat.getGroupPrefix(Bukkit.getWorlds().get(0), group))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.group_suffix[<group>]>
        // @returns Element
        // @description
        // Returns an Element of a group's chat suffix.
        // -->
        if (attribute.startsWith("group_suffix")) {

            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }

            String group = attribute.getContext(1);

            if (!Arrays.asList(Depends.permissions.getGroups()).contains(group)) {
                dB.echoError("Invalid group! '" + (group != null ? group : "") + "' could not be found.");
                return;
            }

            // <--[tag]
            // @attribute <server.group_suffix[<group>].world[<world>]>
            // @returns Element
            // @description
            // Returns an Element of a group's chat suffix for the specified dWorld.
            // -->
            if (attribute.getAttribute(2).startsWith("world")) {
                dWorld world = dWorld.valueOf(attribute.getContext(2));
                if (world != null) {
                    event.setReplaced(new Element(Depends.chat.getGroupSuffix(world.getWorld(), group))
                            .getAttribute(attribute.fulfill(2)));
                }
                return;
            }

            // Suffix in default world
            event.setReplaced(new Element(Depends.chat.getGroupSuffix(Bukkit.getWorlds().get(0), group))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_permission_groups>
        // @returns dList
        // @description
        // Returns a list of all permission groups on the server.
        // -->
        if (attribute.startsWith("list_permission_groups")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return;
            }
            event.setReplaced(new dList(Arrays.asList(Depends.permissions.getGroups())).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_plugin_names>
        // @returns dList
        // @description
        // Gets a list of currently enabled plugin names from the server.
        // -->
        if (attribute.startsWith("list_plugin_names")) {
            dList plugins = new dList();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.add(plugin.getName());
            }
            event.setReplaced(plugins.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_scripts>
        // @returns dList(dScript)
        // @description
        // Gets a list of all scripts currently loaded into Denizen.
        // -->
        if (attribute.startsWith("list_scripts")) {
            dList scripts = new dList();
            for (String str : ScriptRegistry._getScriptNames()) {
                scripts.add("s@" + str);
            }
            event.setReplaced(scripts.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.match_player[<name>]>
        // @returns dPlayer
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
                event.setReplaced(new dPlayer(matchPlayer).getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.match_offline_player[<name>]>
        // @returns dPlayer
        // @description
        // Returns any player (online or offline) that best matches the input name.
        // EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns p@bob,
        // input 'bobb' returns p@bobby, and input 'b' returns p@bo.
        // -->
        if (attribute.startsWith("match_offline_player") && attribute.hasContext(1)) {
            UUID matchPlayer = null;
            String matchInput = CoreUtilities.toLowerCase(attribute.getContext(1));
            for (Map.Entry<String, UUID> entry : dPlayer.getAllPlayers().entrySet()) {
                if (CoreUtilities.toLowerCase(entry.getKey()).equals(matchInput)) {
                    matchPlayer = entry.getValue();
                    break;
                }
                else if (CoreUtilities.toLowerCase(entry.getKey()).contains(matchInput) && matchPlayer == null) {
                    matchPlayer = entry.getValue();
                }
            }

            if (matchPlayer != null) {
                event.setReplaced(new dPlayer(matchPlayer).getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.get_npcs_assigned[<assignment_script>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs assigned to a specified script.
        // -->
        if (attribute.startsWith("get_npcs_assigned") && Depends.citizens != null
                && attribute.hasContext(1)) {
            dScript script = dScript.valueOf(attribute.getContext(1));
            if (script == null || !(script.getContainer() instanceof AssignmentScriptContainer)) {
                dB.echoError("Invalid script specified.");
            }
            else {
                dList npcs = new dList();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()
                            && npc.getTrait(AssignmentTrait.class).getAssignment().getName().equalsIgnoreCase(script.getName())) {
                        npcs.add(dNPC.mirrorCitizensNPC(npc).identify());
                    }
                }
                event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <server.get_online_players_flagged[<flag_name>]>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all online players with a specified flag set.
        // -->
        if (attribute.startsWith("get_online_players_flagged")
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            dList players = new dList();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag(new dPlayer(player), flag).size() > 0) {
                    players.add(new dPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.get_players_flagged[<flag_name>]>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all players with a specified flag set.
        // -->
        if (attribute.startsWith("get_players_flagged")
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            dList players = new dList();
            for (Map.Entry<String, UUID> entry : dPlayer.getAllPlayers().entrySet()) {
                if (DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag(new dPlayer(entry.getValue()), flag).size() > 0) {
                    players.add(new dPlayer(entry.getValue()).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.get_spawned_npcs_flagged[<flag_name>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all spawned NPCs with a specified flag set.
        // -->
        if (attribute.startsWith("get_spawned_npcs_flagged") && Depends.citizens != null
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            dList npcs = new dList();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                dNPC dNpc = dNPC.mirrorCitizensNPC(npc);
                if (dNpc.isSpawned() && FlagManager.npcHasFlag(dNpc, flag)) {
                    npcs.add(dNpc.identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.get_npcs_flagged[<flag_name>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs with a specified flag set.
        // -->
        if (attribute.startsWith("get_npcs_flagged") && Depends.citizens != null
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            dList npcs = new dList();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                dNPC dNpc = dNPC.mirrorCitizensNPC(npc);
                if (FlagManager.npcHasFlag(dNpc, flag)) {
                    npcs.add(dNpc.identify());
                }
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs.
        // -->
        if (attribute.startsWith("list_npcs") && Depends.citizens != null) {
            dList npcs = new dList();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                npcs.add(dNPC.mirrorCitizensNPC(npc).identify());
            }
            event.setReplaced(npcs.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_worlds>
        // @returns dList(dWorld)
        // @description
        // Returns a list of all worlds.
        // -->
        if (attribute.startsWith("list_worlds")) {
            dList worlds = new dList();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(dWorld.mirrorBukkitWorld(world).identify());
            }
            event.setReplaced(worlds.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_plugins>
        // @returns dList(dPlugin)
        // @description
        // Gets a list of currently enabled dPlugins from the server.
        // -->
        if (attribute.startsWith("list_plugins")) {
            dList plugins = new dList();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                plugins.add(new dPlugin(plugin).identify());
            }
            event.setReplaced(plugins.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("list_players")) {
            dList players = new dList();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                players.add(dPlayer.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all online players.
        // -->
        if (attribute.startsWith("list_online_players")) {
            dList players = new dList();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(dPlayer.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all offline players.
        // -->
        if (attribute.startsWith("list_offline_players")) {
            dList players = new dList();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (!player.isOnline()) {
                    players.add(dPlayer.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_banned_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all banned players.
        // -->
        if (attribute.startsWith("list_banned_players")) {
            dList banned = new dList();
            for (OfflinePlayer player : Bukkit.getBannedPlayers()) {
                banned.add(dPlayer.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(banned.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_banned_addresses>
        // @returns dList
        // @description
        // Returns a list of all banned ip addresses.
        // -->
        if (attribute.startsWith("list_banned_addresses")) {
            dList list = new dList();
            list.addAll(Bukkit.getIPBans());
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.is_banned[<address>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the given ip address is banned.
        // -->
        if (attribute.startsWith("is_banned") && attribute.hasContext(1)) {
            // BanList contains an isBanned method that doesn't check expiration time
            BanEntry ban = Bukkit.getBanList(BanList.Type.IP).getBanEntry(attribute.getContext(1));

            if (ban == null) {
                event.setReplaced(new Element(false).getAttribute(attribute.fulfill(1)));
            } else if (ban.getExpiration() == null) {
                event.setReplaced(new Element(true).getAttribute(attribute.fulfill(1)));
            } else {
                event.setReplaced(new Element(ban.getExpiration().after(new Date())).getAttribute(attribute.fulfill(1)));
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
            // @returns Duration
            // @description
            // Returns the expiration of the ip address's ban, if it is banned.
            // Potentially can be null.
            // -->
            if (attribute.startsWith("expiration") && ban.getExpiration() != null) {
                event.setReplaced(new Duration(ban.getExpiration().getTime() / 50)
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].reason>
            // @returns Element
            // @description
            // Returns the reason for the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("reason")) {
                event.setReplaced(new Element(ban.getReason())
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].created>
            // @returns Duration
            // @description
            // Returns when the ip address's ban was created, if it is banned.
            // -->
            else if (attribute.startsWith("created")) {
                event.setReplaced(new Duration(ban.getCreated().getTime() / 50)
                        .getAttribute(attribute.fulfill(1)));
            }

            // <--[tag]
            // @attribute <server.ban_info[<address>].source>
            // @returns Element
            // @description
            // Returns the source of the ip address's ban, if it is banned.
            // -->
            else if (attribute.startsWith("source")) {
                event.setReplaced(new Element(ban.getSource())
                        .getAttribute(attribute.fulfill(1)));
            }

            return;
        }

        // <--[tag]
        // @attribute <server.list_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all ops, online or not.
        // -->
        if (attribute.startsWith("list_ops")) {
            dList players = new dList();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                players.add(dPlayer.mirrorBukkitPlayer(player).identify());
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all online ops.
        // -->
        if (attribute.startsWith("list_online_ops")) {
            dList players = new dList();
            for (OfflinePlayer player : Bukkit.getOperators()) {
                if (player.isOnline()) {
                    players.add(dPlayer.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all offline ops.
        // -->
        if (attribute.startsWith("list_offline_ops")) {
            dList players = new dList();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isOp() && !player.isOnline()) {
                    players.add(dPlayer.mirrorBukkitPlayer(player).identify());
                }
            }
            event.setReplaced(players.getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.motd>
        // @returns Element
        // @description
        // Returns the server's current MOTD
        // -->
        if (attribute.startsWith("motd")) {
            event.setReplaced(new Element(Bukkit.getServer().getMotd()).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.entity_is_spawned[<entity>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether an entity is spawned and valid.
        // -->
        else if (attribute.startsWith("entity_is_spawned")
                && attribute.hasContext(1)) {
            dEntity ent = dEntity.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new Element((ent != null && ent.isUnique() && ent.isSpawned()) ? "true" : "false")
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.player_is_valid[<player name>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether a player exists under the specified name.
        // -->
        else if (attribute.startsWith("player_is_valid")
                && attribute.hasContext(1)) {
            event.setReplaced(new Element(dPlayer.playerNameIsValid(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.npc_is_valid[<npc>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether an NPC exists and is usable.
        // -->
        else if (attribute.startsWith("npc_is_valid")
                && attribute.hasContext(1)) {
            dNPC npc = dNPC.valueOf(attribute.getContext(1), new BukkitTagContext(null, null, false, null, false, null));
            event.setReplaced(new Element((npc != null && npc.isValid()))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_bossbars>
        // @returns dList
        // @description
        // Returns a list of all currently active boss bar IDs.
        // -->
        else if (attribute.startsWith("current_bossbars")) {
            dList dl = new dList();
            for (String str : BossBarCommand.bossBarMap.keySet()) {
                dl.add(str);
            }
            event.setReplaced(dl.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.recent_tps>
        // @returns dList
        // @description
        // Returns the 3 most recent ticks per second measurements.
        // -->
        else if (attribute.startsWith("recent_tps")) {
            dList list = new dList();
            for (double tps : NMSHandler.getInstance().getRecentTps()) {
                list.add(new Element(tps).identify());
            }
            event.setReplaced(list.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.port>
        // @returns Element(Number)
        // @description
        // Returns the port that the server is running on.
        // -->
        else if (attribute.startsWith("port")) {
            event.setReplaced(new Element(NMSHandler.getInstance().getPort()).getAttribute(attribute.fulfill(1)));
        }

        // TODO: Add everything else from Bukkit.getServer().*

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
                dB.echoError("File deletion disabled by administrator.");
                return;
            }
            File file = new File(DenizenAPI.getCurrentInstance().getDataFolder(), mechanism.getValue().asString());
            if (!Utilities.isSafeFile(file)) {
                dB.echoError("Cannot delete that file (unsafe path).");
                return;
            }
            try {
                if (!file.delete()) {
                    dB.echoError("Failed to delete file: returned false");
                }
            }
            catch (Exception e) {
                dB.echoError("Failed to delete file: " + e.getMessage());
            }
        }

        // Deprecated in favor of SYSTEM.redirect_logging (Core)
        if (mechanism.matches("redirect_logging") && mechanism.hasValue()) {
            if (!Settings.allowConsoleRedirection()) {
                dB.echoError("Console redirection disabled by administrator.");
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
                dB.echoError("Server restart disabled by administrator. Consider using 'shutdown'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server restarted by a Denizen script, see config to prevent this!");
            Bukkit.spigot().restart();
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
                dB.echoError("Server stop disabled by administrator. Consider using 'restart'.");
                return;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "+> Server shutdown by a Denizen script, see config to prevent this!");
            Bukkit.shutdown();
        }
    }
}

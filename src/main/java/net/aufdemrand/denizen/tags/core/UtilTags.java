package net.aufdemrand.denizen.tags.core;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.sql.Connection;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.SQLCommand;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.WorldScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;


import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.javaluator.DoubleEvaluator;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // <--[tag]
    // @attribute <math:<calculation>>
    // @returns Element(Decimal)
    // @description
    // Returns a calculated result of the math placed after the :
    // Examples: '<math:1 + 1>' or '<math:sin(<npc.id>)>'.
    // Since this is a 'value' tag, to get an int value, you will need to do '<math.as_int:calc>',
    // and similar for all other element tags.
    // -->
    @EventHandler
    public void mathTag(ReplaceableTagEvent event) {
        if (!event.matches("math, m")) return;
        try {
            Double evaluation = new DoubleEvaluator().evaluate(event.getValue());
            event.setReplaced(new Element(String.valueOf(evaluation)).getAttribute(event.getAttributes().fulfill(1)));
        }
        catch (Exception e) {
            dB.echoError("Invalid math tag!");
            event.setReplaced("0.0");
        }
    }


    // <--[tag]
    // @attribute <tern[<condition>]:<element>||<element>>
    // @returns Element
    // @description
    // Returns either the first element, or 'fallback' element depending on
    // the outcome of the condition. First element will show in a result of 'true',
    // otherwise the fallback element will show.
    // Example: '<t[<player.is_spawned>]:Player is spawned! || Player is not spawned!>'
    // or '<t[<player.health.is[less].than[<player.health.max>]:You look healthy! || Got some bruises, eh?>'.
    // -->
    @EventHandler
    public void ternaryTag(ReplaceableTagEvent event) {
        if (!event.matches("ternary, tern, t")) return;

        // Fallback if nothing to evaluate
        if (!event.hasNameContext()) return;

        // Check evaluation. A result of 'true' will return the value. Anything else
        // will result in the fallback.
        if (event.getNameContext().equalsIgnoreCase("true")) {
            event.setReplaced(new Element(event.getValue().trim())
                    .getAttribute(event.getAttributes().fulfill(1)));
        }
    }


    @EventHandler
    public void serverTag(ReplaceableTagEvent event) {
        if (!event.matches("server, svr, global") || event.replaced()) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns Element(boolean)
        // @description
        // returns true if the server has the specified flag, otherwise returns false.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else {
                event.setReplaced("null");
                return;
            }
            event.setReplaced(new Element(FlagManager.serverHasFlag(flag_name)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.flag[<name>]>
        // @returns Flag dList
        // @description
        // returns the specified flag from the server.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
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
            if (FlagManager.serverHasFlag(flag_name))
                event.setReplaced(new dList(DenizenAPI.getCurrentInstance().flagManager()
                        .getGlobalFlag(flag_name))
                        .getAttribute(attribute));
            else event.setReplaced("null");
            return;
        }

        // <--[tag]
        // @attribute <server.list_flags[<search>]>
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
                for (String flag : allFlags)
                    if (flag.toLowerCase().contains(attribute.getContext(1).toLowerCase()))
                        searchFlags.add(flag);
            }
            event.setReplaced(searchFlags == null ? allFlags.getAttribute(attribute.fulfill(1))
                    : searchFlags.getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.current_time_millis>
        // @returns Element(Number)
        // @description
        // Returns the number of milliseconds since Jan 1, 1970.
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
            event.setReplaced(new Element(EventManager.EventExists(attribute.getContext(1))
                    || EventManager.EventExists(EventManager.StripIdentifiers(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <server.get_event_handlers[<event_name>]>
        // @returns dList<dScript>
        // @description
        // Returns a list of all world scripts that will handle a given event name.
        // This tag will ignore dObject identifiers (see <@link language dobject>).
        // For use with <@link tag server.has_event[<event_name>]>
        // -->
        if (attribute.startsWith("get_event_handlers")
                && attribute.hasContext(1)) {
            String eventName = attribute.getContext(1).toUpperCase();
            List<WorldScriptContainer> EventsOne = EventManager.events.get("ON " + eventName);
            List<WorldScriptContainer> EventsTwo = EventManager.events.get("ON " + EventManager.StripIdentifiers(eventName));
            if (EventsOne == null && EventsTwo == null) {
                dB.echoError("No world scripts will handle the event '" + eventName + "'");
            }
            else {
                dList list = new dList();
                if (EventsOne != null) {
                    for (WorldScriptContainer script: EventsOne) {
                        list.add("s@" + script.getName());
                    }
                }
                if (EventsTwo != null) {
                    for (WorldScriptContainer script: EventsTwo) {
                        if (!list.contains("s@" + script.getName()))
                            list.add("s@" + script.getName());
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
            if (npc == null)
                event.setReplaced(Element.NULL.getAttribute(attribute.fulfill(1)));
            else
                event.setReplaced(new dNPC(npc).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.get_npcs_named[<name>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of NPCs with a certain name.
        // -->
        if (attribute.startsWith("get_npcs_named") && attribute.hasContext(1)) {
            ArrayList<dNPC> npcs = new ArrayList<dNPC>();
            for (NPC npc : CitizensAPI.getNPCRegistry())
                if (npc.getName().equalsIgnoreCase(attribute.getContext(1)))
                    npcs.add(dNPC.mirrorCitizensNPC(npc));
            event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.has_file[<name>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the specified file exists. The starting path is /plugins/Denizen.
        // -->
        if (attribute.startsWith("has_file") && attribute.hasContext(1)) {
            event.setReplaced(new Element(new File(DenizenAPI.getCurrentInstance().getDataFolder(),
                    attribute.getContext(1)).exists()).getAttribute(attribute.fulfill(1)));
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
        // Returns the version string of the server.
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
            for (Map.Entry<String, Connection> entry: SQLCommand.connections.entrySet()) {
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
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins())
                plugins.add(plugin.getName());
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
            for (String str : ScriptRegistry._getScriptNames())
                scripts.add("s@" + str);
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
            String matchInput = attribute.getContext(1).toLowerCase();
            for (Player player: Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().equals(matchInput)) {
                    matchPlayer = player;
                    break;
                }
                else if (player.getName().toLowerCase().contains(matchInput) && matchPlayer == null) {
                    matchPlayer = player;
                }
            }

            if (matchPlayer == null) {
                event.setReplaced("null");
            } else {
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
        if (attribute.startsWith("get_npcs_assigned")
                && attribute.hasContext(1)) {
            dScript script = dScript.valueOf(attribute.getContext(1));
            if (script == null || !(script.getContainer() instanceof AssignmentScriptContainer)) {
                dB.echoError("Invalid script specified.");
            }
            else {
                ArrayList<dNPC> npcs = new ArrayList<dNPC>();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()
                            && npc.getTrait(AssignmentTrait.class).getAssignment().getName().equalsIgnoreCase(script.getName()))
                        npcs.add(dNPC.mirrorCitizensNPC(npc));
                }
                event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <server.get_npcs_flagged[<flag_name>]>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs with a specified flag set.
        // -->
        if (attribute.startsWith("get_npcs_flagged")
                && attribute.hasContext(1)) {
            String flag = attribute.getContext(1);
            ArrayList<dNPC> npcs = new ArrayList<dNPC>();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                dNPC dNpc = dNPC.mirrorCitizensNPC(npc);
                if (FlagManager.npcHasFlag(dNpc, flag))
                    npcs.add(dNpc);
            }
            event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_npcs>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs.
        // -->
        if (attribute.startsWith("list_npcs")) {
            ArrayList<dNPC> npcs = new ArrayList<dNPC>();
            for (NPC npc : CitizensAPI.getNPCRegistry())
                npcs.add(dNPC.mirrorCitizensNPC(npc));
            event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_worlds>
        // @returns dList(dWorld)
        // @description
        // Returns a list of all worlds.
        // -->
        if (attribute.startsWith("list_worlds")) {
            ArrayList<dWorld> worlds = new ArrayList<dWorld>();
            for (World world : Bukkit.getWorlds())
                worlds.add(dWorld.mirrorBukkitWorld(world));
            event.setReplaced(new dList(worlds).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_plugins>
        // @returns dList(dPlugin)
        // @description
        // Gets a list of currently enabled dPlugins from the server.
        // -->
        if (attribute.startsWith("list_plugins")) {
            ArrayList<dPlugin> plugins = new ArrayList<dPlugin>();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins())
                plugins.add(new dPlugin(plugin));
            event.setReplaced(new dList(plugins).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("list_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                    players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all online players.
        // -->
        if (attribute.startsWith("list_online_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all offline players.
        // -->
        if (attribute.startsWith("list_offline_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (!player.isOnline()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all ops, online or not.
        // -->
        if (attribute.startsWith("list_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_online_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all online ops.
        // -->
        if (attribute.startsWith("list_online_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <server.list_offline_ops>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all offline ops.
        // -->
        if (attribute.startsWith("list_offline_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (player.isOp() && !player.isOnline()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
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
        // TODO: Add everything else from Bukkit.getServer().*

    }


    @EventHandler
    public void utilTag(ReplaceableTagEvent event) {
        if (!event.matches("util, u")) return;

        String type = event.getType() != null ? event.getType() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext() : "";
        String subType = event.getSubType() != null ? event.getSubType() : "";
        String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier() : "";
        String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext().toUpperCase() : "";
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        if (type.equalsIgnoreCase("RANDOM")) {

            // <--[tag]
            // @attribute <util.random.int[<#>].to[<#>]>
            // @returns Element(Number)
            // @description
            // Returns a random number between the 2 specified numbers, inclusive.
            // EG, random.int[1].to[3] could return 1, 2, or 3.
            // -->
            if (subType.equalsIgnoreCase("INT")) {
                if (specifier.equalsIgnoreCase("TO")) {
                    if (aH.matchesInteger(subTypeContext) && aH.matchesInteger(specifierContext)) {
                        int min = aH.getIntegerFrom(subTypeContext);
                        int max = aH.getIntegerFrom(specifierContext);

                        // in case the first number is larger than the second, reverse them
                        if (min > max) {
                            int store = min;
                            min = max;
                            max = store;
                        }

                        event.setReplaced(new Element(
                                String.valueOf(Utilities.getRandom().nextInt(max - min + 1) + min))
                                .getAttribute(attribute.fulfill(3)));
                    }
                }
            }

            // <--[tag]
            // @attribute <util.random.decimal>
            // @returns Element
            // @description
            // Returns a random decimal number from 0 to 1
            // -->
            else if (subType.equalsIgnoreCase("DECIMAL"))
                event.setReplaced(new Element(Utilities.getRandom().nextDouble())
                        .getAttribute(attribute.fulfill(2)));

                // <--[tag]
                // @attribute <util.random.gauss>
                // @returns Element
                // @description
                // Returns a random decimal number with a gaussian distribution.
                // 70% of all results will be within the range of -1 to 1.
                // -->
            else if (subType.equalsIgnoreCase("GAUSS"))
                event.setReplaced(new Element(Utilities.getRandom().nextGaussian())
                        .getAttribute(attribute.fulfill(2)));

            // TODO: Delete (Deprecated in favor of li@list.random)
            else if (subType.equalsIgnoreCase("ELEMENT")) {
                dList list = dList.valueOf(subTypeContext);
                event.setReplaced(new Element(list.get(new Random().nextInt(list.size())))
                        .getAttribute(attribute.fulfill(2)));
            }

            // <--[tag]
            // @attribute <util.random.uuid>
            // @returns Element
            // @description
            // Returns a random unique ID.
            // -->
            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(new Element(UUID.randomUUID().toString())
                        .getAttribute(attribute.fulfill(2)));

            // <--[tag]
            // @attribute <util.random.duuid>
            // @returns Element
            // @description
            // Returns a random 'denizen' unique ID, which resolves to a 10-character long
            // randomly generated string using the letters 'D E N I Z E N'.
            // -->
            else if (subType.equalsIgnoreCase("DUUID"))
                event.setReplaced(new Element(ScriptQueue._getNextId())
                        .getAttribute(attribute.fulfill(2)));
        }


        else if (type.equalsIgnoreCase("SUBSTR")
                || type.equalsIgnoreCase("TRIM")
                || type.equalsIgnoreCase("SUBSTRING")) {
            String text = event.getTypeContext();
            int from = 1;
            int to = text.length() + 1;
            int tags = 2;

            // TODO: Delete (Deprecated in favor of el@element.after)
            if (subType.equalsIgnoreCase("AFTER")) {
                from = text.toUpperCase().indexOf(subTypeContext) + subTypeContext.length() + 1;
            }

            // TODO: Delete (Deprecated in favor of el@element.before)
            if (subType.equalsIgnoreCase("BEFORE")) {
                to = text.toUpperCase().indexOf(subTypeContext) + 1;
            }

            // TODO: Delete (Deprecated in favor of el@element.substring)
            try {
                if (subType.equalsIgnoreCase("FROM"))
                    from = Integer.valueOf(subTypeContext);
            } catch (NumberFormatException e) { }

            try {
                if (specifier.equalsIgnoreCase("TO")) {
                    to = Integer.valueOf(specifierContext);
                    tags = 3;
                }
            } catch (NumberFormatException e) { }

            if (to > text.length())
                to = text.length() + 1;

            event.setReplaced(new Element(text.substring(from - 1, to - 1))
                    .getAttribute(attribute.fulfill(tags)));
        }


        // TODO: Delete (Deprecated in favor of el@element.replace)
        else if (type.equalsIgnoreCase("REPLACE")) {
            String item_to_replace = event.getTypeContext();
            String replace = event.getSubTypeContext();
            String replacement = event.getSpecifierContext();
            event.setReplaced(new Element(item_to_replace.replace(replace, replacement))
                    .getAttribute(attribute.fulfill(3)));
        }

        // <--[tag]
        // @attribute <util.entity_is_spawned[<entity>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether an entity is spawned and valid.
        // -->
        else if (type.equalsIgnoreCase("ENTITY_IS_SPAWNED")
                && event.hasTypeContext()) {
            dEntity ent = dEntity.valueOf(event.getTypeContext());
            event.setReplaced(new Element((ent != null && ent.isUnique() && ent.isSpawned()) ? "true" : "false")
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <util.player_is_valid[<player name>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether a player exists under the specified name.
        // -->
        else if (type.equalsIgnoreCase("PLAYER_IS_VALID")
                && event.hasTypeContext()) {
            event.setReplaced(new Element(dPlayer.playerNameIsValid(event.getTypeContext()))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <util.npc_is_valid[<npc>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether an NPC exists and is usable.
        // -->
        else if (type.equalsIgnoreCase("NPC_IS_VALID")
                && event.hasTypeContext()) {
            dNPC npc = dNPC.valueOf(event.getTypeContext());
            event.setReplaced(new Element((npc != null && npc.isValid()))
                    .getAttribute(attribute.fulfill(1)));
        }


        // TODO: Delete (Deprecated in favor of el@element.to_uppercase)
        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(new Element(item_to_uppercase.toUpperCase())
                    .getAttribute(attribute.fulfill(1)));
        }

        // TODO: Delete (Deprecated in favor of el@element.to_lowercase)
        else if (type.equalsIgnoreCase("LOWERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(new Element(item_to_uppercase.toLowerCase())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <util.date>
        // @returns Element
        // @description
        // Returns the current system date.
        // -->
        else if (type.equalsIgnoreCase("DATE")) {
            Calendar calendar = Calendar.getInstance();
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat();

            // <--[tag]
            // @attribute <util.date.time>
            // @returns Element
            // @description
            // Returns the current system time.
            // -->
            if (subType.equalsIgnoreCase("TIME")) {

                // <--[tag]
                // @attribute <util.date.time.twentyfour_hour>
                // @returns Element
                // @description
                // Returns the current system time in 24-hour format.
                // -->
                if (specifier.equalsIgnoreCase("TWENTYFOUR_HOUR")) {
                    format.applyPattern("k:mm");
                    event.setReplaced(new Element(format.format(currentDate))
                            .getAttribute(attribute.fulfill(3)));
                }
                // <--[tag]
                // @attribute <util.date.time.year>
                // @returns Element(Number)
                // @description
                // Returns the current year of the system time.
                // -->
                else if (specifier.equalsIgnoreCase("year"))
                    event.setReplaced(new Element(calendar.get(Calendar.YEAR)).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.month>
                    // @returns Element(Number)
                    // @description
                    // Returns the current month of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("month"))
                    event.setReplaced(new Element(calendar.get(Calendar.MONTH) + 1).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.day>
                    // @returns Element(Number)
                    // @description
                    // Returns the current day of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("day"))
                    event.setReplaced(new Element(calendar.get(Calendar.DAY_OF_MONTH)).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.hour>
                    // @returns Element(Number)
                    // @description
                    // Returns the current hour of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("hour"))
                    event.setReplaced(new Element(calendar.get(Calendar.HOUR_OF_DAY)).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.minute>
                    // @returns Element(Number)
                    // @description
                    // Returns the current minute of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("minute"))
                    event.setReplaced(new Element(calendar.get(Calendar.MINUTE)).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.second>
                    // @returns Element(Number)
                    // @description
                    // Returns the current second of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("second"))
                    event.setReplaced(new Element(calendar.get(Calendar.SECOND)).getAttribute(attribute.fulfill(3)));
                    // <--[tag]
                    // @attribute <util.date.time.duration>
                    // @returns Duration
                    // @description
                    // Returns the current system time as a duration.
                    // To get the exact millisecond count, use <@link tag server.current_time_millis>.
                    // -->
                else if (specifier.equalsIgnoreCase("duration"))
                    event.setReplaced(new Duration(System.currentTimeMillis() / 50).getAttribute(attribute.fulfill(3)));
                else {
                    format.applyPattern("K:mm a");
                    event.setReplaced(format.format(currentDate));
                }

            }
            // <--[tag]
            // @attribute <util.date.format[<format>]>
            // @returns Element
            // @description
            // Returns the current system time, formatted as specified
            // Example format: [EEE, MMM d, yyyy K:mm a] will become "Mon, Jan 1, 2112 0:01 AM"
            // -->
            else if (subType.equalsIgnoreCase("FORMAT") && !subTypeContext.equalsIgnoreCase("")) {
                try {
                    format.applyPattern(event.getSubTypeContext());
                    event.setReplaced(format.format(currentDate));
                }
                catch (Exception ex) {
                    dB.echoError("Error: invalid pattern '" + event.getSubTypeContext() + "'");
                    dB.echoError(ex);
                }
            }
            else {
                format.applyPattern("EEE, MMM d, yyyy");
                event.setReplaced(format.format(currentDate));
            }

        }

        // <--[tag]
        // @attribute <util.as_element[<text>]>
        // @returns Element
        // @description
        // Returns the text as an Element.
        // -->
        else if (type.equalsIgnoreCase("AS_ELEMENT")) {
            event.setReplaced(new Element(typeContext).getAttribute(attribute.fulfill(1)));
        }

    }
}

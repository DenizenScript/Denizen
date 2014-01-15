package net.aufdemrand.denizen.tags.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;


import net.aufdemrand.denizen.utilities.debugging.dB;
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
    // @attribute <math:<calculationhere>>
    // @returns Direct text output(Decimal)
    // @description
    // Returns a calculcated result of the math placed after the :
    // EG <math:1+1> or <math:sin(<npc.id>)>.
    // To get an int value, you will need to do <el@val[<math:calc>].asint>
    // -->
    @EventHandler
    public void mathTags(ReplaceableTagEvent event) {
        if (!event.matches("math, m")) return;
        try {
            Double evaluation = new DoubleEvaluator().evaluate(event.getValue());
            event.setReplaced(String.valueOf(evaluation));
        }
        catch (Exception e) {
            dB.echoError("Invalid math tag!");
            event.setReplaced("0.0");
        }
    }

    @EventHandler
    public void queueTags(ReplaceableTagEvent event) {

        if (!event.matches("queue, q")) return;

        // TODO: <queue[<id>]. * > tags?

        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // <--[tag]
        // @attribute <queue.exists[<queue_id>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the specified queue exists.
        // -->
        if (attribute.startsWith("exists")
                && attribute.hasContext(1))
            event.setReplaced(new Element(ScriptQueue._queueExists(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1)));

        // <--[tag]
        // @attribute <queue.id>
        // @returns Element
        // @description
        // Returns the current queue ID.
        // -->
        if (attribute.startsWith("id"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().id)
                    .getAttribute(attribute.fulfill(1)));

        // <--[tag]
        // @attribute <queue.stats>
        // @returns Element
        // @description
        // Returns stats for all queues during this server session.
        // -->
        if (attribute.startsWith("stats"))
            event.setReplaced(new Element(ScriptQueue._getStats())
                    .getAttribute(attribute.fulfill(1)));

        // <--[tag]
        // @attribute <queue.size>
        // @returns Element(Number)
        // @description
        // Returns the size of the current queue.
        // -->
        if (attribute.startsWith("size"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getQueueSize())
                    .getAttribute(attribute.fulfill(1)));

        // <--[tag]
        // @attribute <queue.definitions>
        // @returns Element
        // @description
        // Returns all definitions that were passed to the current queue.
        // -->
        if (attribute.startsWith("definitions"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getAllDefinitions().toString())
                    .getAttribute(attribute.fulfill(1)));

    }

    @EventHandler
    public void serverTags(ReplaceableTagEvent event) {
        if (!event.matches("server, svr, global") || event.replaced()) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // <--[tag]
        // @attribute <server.has_flag[<flag_name>]>
        // @returns Element(boolean)
        // @description
        // returns true if the Player has the specified flag, otherwise returns false.
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
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired")) {
                event.setReplaced(new Element(!FlagManager.serverHasFlag(flag_name))
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
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
        // @attribute <server.match_player[<name>]>
        // @returns dPlayer
        // @description
        // Returns the online player that best matches the input name.
        // (EG, in a group of 'bo', 'bob', and 'bobby'... input 'bob' returns p@bob,
        // input 'bobb' returns p@bobby, and input 'b' returns p@bo.)
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
        // @attribute <server.list_players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("list_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : dPlayer.offlinePlayers)
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
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
            for (OfflinePlayer player : dPlayer.offlinePlayers)
                players.add(dPlayer.mirrorBukkitPlayer(player));
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
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : dPlayer.offlinePlayers)
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
            for (OfflinePlayer player : dPlayer.offlinePlayers)
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
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
    public void miscTags(ReplaceableTagEvent event) {
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
            // Returns a random number between the 2 specified numbers.
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
            // @attribute <util.random.element[<value>|...]>
            // @returns Element
            // @description
            // Returns a random element from a list.
            // -->
            else if (subType.equalsIgnoreCase("ELEMENT")) {
                dList list = dList.valueOf(subTypeContext);
                event.setReplaced(new Element(list.get(new Random().nextInt(list.size())))
                        .getAttribute(attribute.fulfill(2)));
            }

            // <--[tag]
            // @attribute <util.random.uuid>
            // @returns Element
            // @description
            // Returns a random unique ID. (Useful for making new queues)
            // -->
            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(new Element(UUID.randomUUID().toString())
                        .getAttribute(attribute.fulfill(2)));
        }

        else if (type.equalsIgnoreCase("SUBSTR")
                || type.equalsIgnoreCase("TRIM")
                || type.equalsIgnoreCase("SUBSTRING")) {
            String text = event.getTypeContext();
            int from = 1;
            int to = text.length() + 1;
            int tags = 2;

            // <--[tag]
            // @attribute <util.substr[<text1>].after[<text2>]>
            // @returns Element
            // @description
            // Returns all text in text1 after the first occurrence of text2.
            // (Deprecated in favor of <el@element.after[<text>]>)
            // -->
            if (subType.equalsIgnoreCase("AFTER")) {
                from = text.toUpperCase().indexOf(subTypeContext) + subTypeContext.length() + 1;
            }

            // <--[tag]
            // @attribute <util.substr[<text1>].before[<text2>]>
            // @returns Element
            // @description
            // Returns all text in text1 before the first occurrence of text2.
            // (Deprecated in favor of <element.before[<text>]>)
            // -->
            if (subType.equalsIgnoreCase("BEFORE")) {
                to = text.toUpperCase().indexOf(subTypeContext) + 1;
            }

            // <--[tag]
            // @attribute <util.substr[<text>].from[<#>].to[<#>]>
            // @returns Element
            // @description
            // Returns all text in between the 2 points in the text.
            // (Deprecated in favor of <element.substring[<#>(,<#>)]>)
            // -->
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

        // <--[tag]
        // @attribute <util.replace[<text>].from[<fromText>].to[<toText>]>
        // @returns Element
        // @description
        // Returns the text with all instances of fromText replaced as toText.
        // (Deprecated in favor of <el@element.replace[<text>].with[<text>]>)
        // -->
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
        else if (type.equalsIgnoreCase("ENTITY_IS_SPAWNED")) {
            dEntity ent = dEntity.valueOf(event.getTypeContext());
            event.setReplaced(new Element((ent != null && ent.isUnique() && ent.isSpawned()) ? "true" : "false")
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <util.uppercase[<text>]>
        // @returns Element
        // @description
        // Returns the text in uppercase letters.
        // -->
        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(new Element(item_to_uppercase.toUpperCase())
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <util.lowercase[<text>]>
        // @returns Element
        // @description
        // Returns the text in lowercase letters.
        // -->
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
                // @attribute <util.date.time.24hour>
                // @returns Element
                // @description
                // Returns the current system time in 24-hour format.
                // -->
                if (specifier.equalsIgnoreCase("24HOUR")) {
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
                else {
                    format.applyPattern("K:mm a");
                    event.setReplaced(format.format(currentDate));
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

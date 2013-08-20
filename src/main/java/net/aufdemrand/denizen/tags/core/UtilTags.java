package net.aufdemrand.denizen.tags.core;

import java.text.SimpleDateFormat;
import java.util.*;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;


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

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void mathTags(ReplaceableTagEvent event) {
        if (!event.matches("math, m")) return;
        Double evaluation = new DoubleEvaluator().evaluate(event.getValue());
        event.setReplaced(String.valueOf(evaluation));
    }

    @EventHandler
    public void queueTags(ReplaceableTagEvent event) {
        
        // <--
        // <q> -> Bloop
        // Returns "q" (or "queue" if you spell it out)... Pretty useless by itself.
        // [See <q.id>, <q.stats>, <q.size>, and <q.definitions>]
        // -->
        if (!event.matches("queue, q")) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // <--
        // <q.id> -> Element
        // Returns the current queue id.
        // -->
        if (attribute.startsWith("id"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().id)
                    .getAttribute(attribute.fulfill(1)));

        // <--
        // <q.stats> -> Element
        // Returns stats for all queues during this server session.
        // -->
        if (attribute.startsWith("stats"))
            event.setReplaced(new Element(ScriptQueue._getStats())
                    .getAttribute(attribute.fulfill(1)));

        // <--
        // <q.size> -> Element
        // Returns the size of the current queue.
        // -->
        if (attribute.startsWith("size"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getQueueSize())
                    .getAttribute(attribute.fulfill(1)));

        // <--
        // <q.definitions> -> Element
        // Returns all definitions that were passed to the current queue.
        // -->
        if (attribute.startsWith("definitions"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getAllContext().toString())
                    .getAttribute(attribute.fulfill(1)));

    }

    @EventHandler
    public void serverTags(ReplaceableTagEvent event) {
        if (!event.matches("server, svr, global") || event.replaced()) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // <--
        // <server.flag[<name>]> -> Flag dList
        // Returns a "Flag dList" of the server flag specified.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else {
                event.setReplaced("null");
                return;
            }
            attribute.fulfill(1);
            
            // <--
            // <server.flag[<name>].is_expired> -> Element(Boolean)
            // Returns true if the flag specified is expired. Else, returns false.
            // -->
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired")) {
                event.setReplaced(new Element(!FlagManager.serverHasFlag(flag_name))
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
            
            // <--
            // <server.flag[<name>].size> -> Element(Number)
            // Returns the size of the Flag dList. If the flag doesn't exist, returns 0.
            // -->
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

        // <--
        // <server.current_time_millis> -> Element(Number)
        // Returns the number of milliseconds since Jan 1, 1970.
        // -->
        if (attribute.startsWith("current_time_millis")) {
            event.setReplaced(new Element(String.valueOf(System.currentTimeMillis()))
                    .getAttribute(attribute.fulfill(1)));
        }

        // <--
        // <server.selected_npc> -> dNPC
        // Returns the server's currently selected NPC.
        // -->
        if (attribute.startsWith("selected_npc")) {
            event.setReplaced(new dNPC(((Citizens) Bukkit.getPluginManager().getPlugin("Citizens"))
                    .getNPCSelector().getSelected(Bukkit.getConsoleSender())).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_npcs> -> dList(dNPC)
        // Returns a dList of dNPCs currently in the Citizens NPC Registry.
        // -->
        if (attribute.startsWith("list_npcs")) {
            ArrayList<dNPC> npcs = new ArrayList<dNPC>();
            for (NPC npc : CitizensAPI.getNPCRegistry())
                npcs.add(dNPC.mirrorCitizensNPC(npc));
            event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_worlds> -> dList(dWorld)
        // Returns a dList of all worlds.
        // -->
        if (attribute.startsWith("list_worlds")) {
            ArrayList<dWorld> worlds = new ArrayList<dWorld>();
            for (World world : Bukkit.getWorlds())
                worlds.add(dWorld.mirrorBukkitWorld(world));
            event.setReplaced(new dList(worlds).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_players> -> dList(dPlayer)
        // Returns a list of all players that have ever played on the server, online or not.
        // -->
        if (attribute.startsWith("list_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_online_players> -> dList(dPlayer)
        // Returns a list of all online players.
        // -->
        if (attribute.startsWith("list_online_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_offline_players> -> dList(dPlayer)
        // Returns a list of all offline players.
        // -->
        if (attribute.startsWith("list_offline_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_ops> -> dList(dPlayer)
        // Returns a list of all ops, online or not.
        // -->
        if (attribute.startsWith("list_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_online_ops> -> dList(dPlayer)
        // Returns a list of all online ops.
        // -->
        if (attribute.startsWith("list_online_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--
        // <server.list_offline_ops> -> dList(dPlayer)
        // Returns a list of all offline ops.
        // -->
        // server.list_offline_ops
        if (attribute.startsWith("list_offline_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

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
            
            // <--
            // <util.random.int[<#>].to[<#>]> -> Element(Number)
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

                        event.setReplaced(String.valueOf(Utilities.getRandom().nextInt(max - min + 1) + min));
                    }
                }
            }

            // <--
            // <util.random.element[<value>|...]> -> Element
            // Returns a random element from a list.
            // -->
            else if (subType.equalsIgnoreCase("ELEMENT")) {
                dList list = dList.valueOf(subTypeContext);
                event.setReplaced(list.get(new Random().nextInt(list.size())));
            }

            // <--
            // <util.random.uuid> -> Element
            // Returns a random unique ID. (Useful for making new queues)
            // -->
            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(UUID.randomUUID().toString());
        }

        else if (type.equalsIgnoreCase("SUBSTR")
                || type.equalsIgnoreCase("TRIM")
                || type.equalsIgnoreCase("SUBSTRING")) {
            String text = event.getTypeContext();
            int from = 1;
            int to = text.length() + 1;

            // <--
            // <util.substr[<text1>].after[<text2>]> -> Element
            // Returns all text in text1 after the first occurrence of text2.
            // -->
            if (subType.equalsIgnoreCase("AFTER")) {
                from = text.toUpperCase().indexOf(subTypeContext) + subTypeContext.length() + 1;
            }
            
            // <--
            // <util.substr[<text1>].before[<text2>]> -> Element
            // Returns all text in text1 before the first occurrence of text2.
            // -->
            if (subType.equalsIgnoreCase("BEFORE")) {
                to = text.toUpperCase().indexOf(subTypeContext) + 1;
            }

            // <--
            // <util.substr[<text>].from[<#>].to[<#>]> -> Element
            // Returns all text in between the 2 points in the text.
            // -->
            try {
                if (subType.equalsIgnoreCase("FROM"))
                    from = Integer.valueOf(subTypeContext);
            } catch (NumberFormatException e) { }

            try {
                if (specifier.equalsIgnoreCase("TO"))
                    to = Integer.valueOf(specifierContext);
            } catch (NumberFormatException e) { }

            if (to > text.length())
                to = text.length() + 1;

            event.setReplaced(text.substring(from - 1, to - 1));
        }

        // <--
        // <util.replace[<text>].from[<fromText>].to[<toText>]> -> Element
        // Returns the text with all instances of fromText replaced as toText.
        // -->
        else if (type.equalsIgnoreCase("REPLACE")) {
            String item_to_replace = event.getTypeContext();
            String replace = event.getSubTypeContext();
            String replacement = event.getSpecifierContext();
            event.setReplaced(item_to_replace.replace(replace, replacement));
        }

        // <--
        // <util.entity_is_spawned[<entity>]> -> Boolean
        // Returns whether an entity is spawned and valid.
        // -->
        else if (type.equalsIgnoreCase("ENTITY_IS_SPAWNED")) {
            dEntity ent = dEntity.valueOf(event.getTypeContext());
            event.setReplaced((ent != null && ent.isUnique() && ent.isSpawned()) ? "true" : "false");
        }

        // <--
        // <util.uppercase[<text>]> -> Element
        // Returns the text in uppercase letters.
        // -->
        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toUpperCase());
        }

        // <--
        // <util.lowercase[<text>]> -> Element
        // Returns the text in lowercase letters.
        // -->
        else if (type.equalsIgnoreCase("LOWERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toLowerCase());
        }

        // <--
        // <util.date> -> Element
        // Returns the current system date.
        // -->
        else if (type.equalsIgnoreCase("DATE")) {
            Calendar calendar = Calendar.getInstance();
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat();

            // <--
            // <util.date.time> -> Element
            // Returns the current system time.
            // -->
            if (subType.equalsIgnoreCase("TIME")) {
                
                // <--
                // <util.date.time.24hour> -> Element
                // Returns the current system time in 24-hour format.
                // -->
                if (specifier.equalsIgnoreCase("24HOUR")) {
                    format.applyPattern("k:mm");
                    event.setReplaced(format.format(currentDate));
                }
                // <--
                // <util.date.time.year> -> Element(Number)
                // Returns the current year of the system time.
                // -->
                else if (specifier.equalsIgnoreCase("year"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.YEAR))).getAttribute(attribute.fulfill(3)));
                    // <--
                    // <util.date.time.month> -> Element(Number)
                    // Returns the current month of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("month"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.MONTH) + 1)).getAttribute(attribute.fulfill(3)));
                    // <--
                    // <util.date.time.day> -> Element(Number)
                    // Returns the current day of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("day"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))).getAttribute(attribute.fulfill(3)));
                    // <--
                    // <util.date.time.hour> -> Element(Number)
                    // Returns the current hour of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("hour"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))).getAttribute(attribute.fulfill(3)));
                    // <--
                    // <util.date.time.minute> -> Element(Number)
                    // Returns the current minute of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("minute"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.MINUTE))).getAttribute(attribute.fulfill(3)));
                    // <--
                    // <util.date.time.second Element(Number)
                    // Returns the current second of the system time.
                    // -->
                else if (specifier.equalsIgnoreCase("second"))
                    event.setReplaced(new Element(String.valueOf(calendar.get(Calendar.SECOND))).getAttribute(attribute.fulfill(3)));
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

        // <--
        // <util.as_element[<text>]> -> Element
        // Returns the text as an Element.
        // -->
        else if (type.equalsIgnoreCase("AS_ELEMENT")) {
            event.setReplaced(new Element(typeContext).getAttribute(attribute.fulfill(1)));
        }

    }

}
package net.aufdemrand.denizen.tags.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptQueue;
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
        if (!event.matches("queue, q")) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        if (attribute.startsWith("id"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().id)
            .getAttribute(attribute.fulfill(1)));

        if (attribute.startsWith("stats"))
            event.setReplaced(new Element(ScriptQueue._getStats())
                    .getAttribute(attribute.fulfill(1)));

        if (attribute.startsWith("size"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getQueueSize())
                    .getAttribute(attribute.fulfill(1)));

        if (attribute.startsWith("speed"))
            event.setReplaced(event.getScriptEntry().getResidingQueue().getSpeed()
                    .getAttribute(attribute.fulfill(1)));

        if (attribute.startsWith("definitions"))
            event.setReplaced(new Element(event.getScriptEntry().getResidingQueue().getAllContext().toString())
                    .getAttribute(attribute.fulfill(1)));

    }

    @EventHandler
    public void serverTags(ReplaceableTagEvent event) {
        if (!event.matches("server, svr, global")) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // server.flag[...]
        // server.flag[...].is_expired
        // server.flag[...].size
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

        // server.selected_npc
        if (attribute.startsWith("selected_npc")) {
            event.setReplaced(new dNPC(((Citizens) Bukkit.getPluginManager().getPlugin("Citizens"))
                    .getNPCSelector().getSelected(Bukkit.getConsoleSender())).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_npcs
        if (attribute.startsWith("list_npcs")) {
            ArrayList<dNPC> npcs = new ArrayList<dNPC>();
            for (NPC npc : CitizensAPI.getNPCRegistry())
                npcs.add(dNPC.mirrorCitizensNPC(npc));
            event.setReplaced(new dList(npcs).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_worlds
        if (attribute.startsWith("list_worlds")) {
            ArrayList<dWorld> worlds = new ArrayList<dWorld>();
            for (World world : Bukkit.getWorlds())
                worlds.add(dWorld.mirrorBukkitWorld(world));
            event.setReplaced(new dList(worlds).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_players
        if (attribute.startsWith("list_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_online_players
        if (attribute.startsWith("list_online_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_offline_players
        if (attribute.startsWith("list_offline_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_ops
        if (attribute.startsWith("list_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

        // server.list_online_ops
        if (attribute.startsWith("list_online_ops")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isOp()) players.add(dPlayer.mirrorBukkitPlayer(player));
            event.setReplaced(new dList(players).getAttribute(attribute.fulfill(1)));
            return;
        }

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

        if (type.equalsIgnoreCase("RANDOM")) {
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

            else if (subType.equalsIgnoreCase("ELEMENT")) {
                dList list = dList.valueOf(subTypeContext);
                event.setReplaced(list.get(new Random().nextInt(list.size())));
            }

            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(UUID.randomUUID().toString());
        }

        else if (type.equalsIgnoreCase("SUBSTR")
                || type.equalsIgnoreCase("TRIM")
                || type.equalsIgnoreCase("SUBSTRING")) {
            String text = event.getTypeContext();
            int from = 1;
            int to = text.length() + 1;

            if (subType.equalsIgnoreCase("AFTER")) {
                from = text.toUpperCase().indexOf(subTypeContext) + subTypeContext.length() + 1;
            }

            if (subType.equalsIgnoreCase("BEFORE")) {
                to = text.toUpperCase().indexOf(subTypeContext) + 1;
            }

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

        else if (type.equalsIgnoreCase("REPLACE")) {
            String item_to_replace = event.getTypeContext();
            String replace = event.getSubTypeContext();
            String replacement = event.getSpecifierContext();
            event.setReplaced(item_to_replace.replace(replace, replacement));
        }

        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toUpperCase());
        }

        else if (type.equalsIgnoreCase("LOWERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toLowerCase());
        }

        else if (type.equalsIgnoreCase("DATE")) {
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat();

            if (subType.equalsIgnoreCase("TIME")) {
                if (specifier.equalsIgnoreCase("24HOUR")) {
                    format.applyPattern("k:mm");
                } else format.applyPattern("K:mm a");

            } else format.applyPattern("EEE, MMM d, yyyy");

            event.setReplaced(format.format(currentDate));
        }

        else if (type.equalsIgnoreCase("AS_ELEMENT")) {
            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
            event.setReplaced(new Element(typeContext).getAttribute(attribute.fulfill(2)));
        }

    }

}
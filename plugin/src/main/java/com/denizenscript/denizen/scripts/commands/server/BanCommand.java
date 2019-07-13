package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.List;

public class BanCommand extends AbstractCommand {

    // <--[command]
    // @Name Ban
    // @Syntax ban ({add}/remove) [<player>|.../addresses:<address>|...] (reason:<text>) (duration:<duration>) (source:<text>)
    // @Required 1
    // @Short Ban or un-ban players or ip addresses.
    // @Group server
    //
    // @Description
    // Add or remove player or ip address bans from the server. Banning a player will also kick them from the server.
    // You may optionally specify both a list of players and list of addresses.
    // Options are:
    // reason: Sets the ban reason. Defaults to "Banned.".
    // duration: Sets the duration of the temporary ban. This will be a permanent ban if not specified.
    // source: Sets the source of the ban. Defaults to "(Unknown)".
    //
    // @Tags
    // <p@player.is_banned>
    // <p@player.ban_info.reason>
    // <p@player.ban_info.expiration>
    // <p@player.ban_info.created>
    // <p@player.ban_info.source>
    // <server.is_banned[<address>]>
    // <server.ban_info[<address>].expiration>
    // <server.ban_info[<address>].reason>
    // <server.ban_info[<address>].created>
    // <server.ban_info[<address>].source>
    // <server.list_banned_addresses>
    // <server.list_banned_players>
    //
    // @Usage
    // Use to ban a player.
    // - ban p@mcmonkey4eva
    //
    // @Usage
    // Use to ban a list of players with a reason.
    // - ban p@mcmonkey4eva|p@Morphan1 "reason:Didn't grow enough potatoes."
    //
    // @Usage
    // Use to ban a list of players for 10 minutes with a reason.
    // - ban p@mcmonkey4eva|p@Morphan1 "reason:Didn't grow enough potatoes." duration:10m
    //
    // @Usage
    // Use to ban a player with a source.
    // - ban p@Mergu "reason:Grew too many potatoes." source:<player.name>
    //
    // @Usage
    // Use to ban an ip address.
    // - ban addresses:127.0.0.1
    //
    // @Usage
    // Use to temporarily ip ban all online players.
    // - ban addresses:<server.list_online_players.parse[ip]> duration:5m
    //
    // @Usage
    // Use to unban a list of players.
    // - ban remove p@mcmonkey4eva|p@Morphan1
    //
    // @Usage
    // Use to unban an ip address.
    // - ban remove addresses:127.0.0.1
    // -->

    public enum Actions {
        ADD, REMOVE
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action") && (arg.matchesPrefix("action")
                    || arg.matchesEnum(Actions.values()))) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("addresses") && arg.matchesPrefix("addresses", "address")) {
                scriptEntry.addObject("addresses", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("targets") && (arg.matchesPrefix("targets", "target")
                    || arg.matchesArgumentList(dPlayer.class))) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("reason") && arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration") && (arg.matchesPrefix("duration", "time", "d", "expiration")
                    || arg.matchesArgumentType(DurationTag.class))) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("source") && arg.matchesPrefix("source")) {
                scriptEntry.addObject("source", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", new ElementTag("add"))
                .defaultObject("reason", new ElementTag("Banned."))
                .defaultObject("source", new ElementTag("(Unknown)"));

        if (Actions.valueOf(scriptEntry.getObject("action").toString().toUpperCase()) == null) {
            throw new IllegalArgumentException("Invalid action specified.");
        }

        if ((!scriptEntry.hasObject("targets") || ((List<dPlayer>) scriptEntry.getObject("targets")).isEmpty())
                && (!scriptEntry.hasObject("addresses") || ((List<ElementTag>) scriptEntry.getObject("addresses")).isEmpty())) {
            throw new IllegalArgumentException("Must specify a valid target or address!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        ListTag addresses = (ListTag) scriptEntry.getObject("addresses");
        ElementTag reason = scriptEntry.getElement("reason");
        DurationTag duration = scriptEntry.getdObject("duration");
        ElementTag source = scriptEntry.getElement("source");

        Date expiration = null;
        if (duration != null && duration.getTicks() != 0) {
            expiration = new Date(new DurationTag(System.currentTimeMillis() / 50 + duration.getTicks()).getTicks() * 50);
        }

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    action.debug() +
                            (targets != null ? ArgumentHelper.debugObj("targets", targets) : "") +
                            (addresses != null ? addresses.debug() : "") +
                            reason.debug() +
                            (duration != null ? duration.debug() : "") +
                            source.debug());
        }

        Actions banAction = Actions.valueOf(action.toString().toUpperCase());

        switch (banAction) {
            case ADD:
                if (targets != null) {
                    for (dPlayer player : targets) {
                        if (player.isValid()) {
                            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason.toString(), expiration, source.toString());
                            if (player.isOnline()) {
                                player.getPlayerEntity().kickPlayer(reason.toString());
                            }
                        }
                    }
                }
                if (addresses != null) {
                    for (String address : addresses) {
                        Bukkit.getBanList(BanList.Type.IP).addBan(address, reason.toString(), expiration, source.toString());
                    }
                }
                break;

            case REMOVE:
                if (targets != null) {
                    for (dPlayer player : targets) {
                        if (player.isValid()) {
                            if (player.getOfflinePlayer().isBanned()) {
                                Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
                            }
                        }
                    }
                }
                if (addresses != null) {
                    for (String address : addresses) {
                        Bukkit.getBanList(BanList.Type.IP).pardon(address);
                    }
                }
                break;

        }

    }

}

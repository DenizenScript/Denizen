package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.List;

public class BanCommand extends AbstractCommand {

    public BanCommand() {
        setName("ban");
        setSyntax("ban ({add}/remove) [<player>|.../addresses:<address>|...] (reason:<text>) (expire:<time>) (source:<text>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Ban
    // @Syntax ban ({add}/remove) [<player>|.../addresses:<address>|...] (reason:<text>) (expire:<time>) (source:<text>)
    // @Required 1
    // @Maximum 5
    // @Short Ban or un-ban players or ip addresses.
    // @Group server
    //
    // @Description
    // Add or remove player or ip address bans from the server. Banning a player will also kick them from the server.
    //
    // You may optionally specify both a list of players and list of addresses.
    //
    // Additional options are:
    // reason: Sets the ban reason. Defaults to "Banned.".
    // expire: Sets the expire time of the temporary ban, as a TimeTag or a DurationTag. This will be a permanent ban if not specified.
    // source: Sets the source of the ban. Defaults to "(Unknown)".
    //
    // @Tags
    // <PlayerTag.is_banned>
    // <PlayerTag.ban_reason>
    // <PlayerTag.ban_expiration_time>
    // <PlayerTag.ban_created_time>
    // <PlayerTag.ban_source>
    // <server.is_banned[<address>]>
    // <server.ban_info[<address>].expiration_time>
    // <server.ban_info[<address>].reason>
    // <server.ban_info[<address>].created_time>
    // <server.ban_info[<address>].source>
    // <server.banned_addresses>
    // <server.banned_players>
    //
    // @Usage
    // Use to ban a player.
    // - ban <[player]>
    //
    // @Usage
    // Use to ban a list of players with a reason.
    // - ban <[player]>|<[someplayer]> "reason:Didn't grow enough potatoes."
    //
    // @Usage
    // Use to ban a list of players for 10 minutes with a reason.
    // - ban <[player]>|<[someplayer]> "reason:Didn't grow enough potatoes." expire:10m
    //
    // @Usage
    // Use to ban a player with a source.
    // - ban <[aplayer]> "reason:Grew too many potatoes." source:<player.name>
    //
    // @Usage
    // Use to ban an ip address.
    // - ban addresses:127.0.0.1
    //
    // @Usage
    // Use to temporarily ip ban all online players.
    // - ban addresses:<server.online_players.parse[ip]> expire:5m
    //
    // @Usage
    // Use to unban a list of players.
    // - ban remove <[player]>|<[someplayer]>
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
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("addresses")
                    && arg.matchesPrefix("addresses", "address")) {
                scriptEntry.addObject("addresses", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("reason")
                    && arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("expire", "duration", "time", "d", "expiration")) {
                if (arg.matchesArgumentType(TimeTag.class)) {
                    scriptEntry.addObject("expire", arg.asType(TimeTag.class));
                }
                else {
                    long duration = arg.asType(DurationTag.class).getMillis();
                    if (duration > 0) { // Explicitly consider infinite duration as null input
                        scriptEntry.addObject("expire", new TimeTag(TimeTag.now().millis() + duration));
                    }
                }
            }
            else if (!scriptEntry.hasObject("source")
                    && arg.matchesPrefix("source")) {
                scriptEntry.addObject("source", arg.asElement());
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.limitToOnlyPrefix("action")
                    && arg.matchesEnum(Actions.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.limitToOnlyPrefix("targets")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("action", new ElementTag("add"))
                .defaultObject("reason", new ElementTag("Banned."))
                .defaultObject("source", new ElementTag("(Unknown)"));
        if ((!scriptEntry.hasObject("targets") || ((List<PlayerTag>) scriptEntry.getObject("targets")).isEmpty())
                && (!scriptEntry.hasObject("addresses") || ((List<ElementTag>) scriptEntry.getObject("addresses")).isEmpty())) {
            throw new IllegalArgumentException("Must specify a valid target or address!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        ListTag addresses = scriptEntry.getObjectTag("addresses");
        ElementTag reason = scriptEntry.getElement("reason");
        TimeTag expire = scriptEntry.getObjectTag("expire");
        ElementTag source = scriptEntry.getElement("source");
        Date expiration = expire == null ? null : new Date(expire.millis());
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action, db("targets", targets), addresses, reason, expire, source);
        }
        Actions banAction = Actions.valueOf(action.toString().toUpperCase());
        switch (banAction) {
            case ADD:
                if (targets != null) {
                    for (PlayerTag player : targets) {
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
                    for (PlayerTag player : targets) {
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

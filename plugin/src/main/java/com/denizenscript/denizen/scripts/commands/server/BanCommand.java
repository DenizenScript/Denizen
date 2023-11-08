package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;

public class BanCommand extends AbstractCommand {

    public BanCommand() {
        setName("ban");
        setSyntax("ban ({add}/remove) [<player>|.../addresses:<address>|.../names:<name>|...] (reason:<text>) (expire:<time>) (source:<text>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
        autoCompile();
        addRemappedPrefixes("addresses", "address");
        addRemappedPrefixes("expire", "duration", "time", "d", "expiration");
    }

    // <--[command]
    // @Name Ban
    // @Syntax ban ({add}/remove) [<player>|.../addresses:<address>|.../names:<name>|...] (reason:<text>) (expire:<time>) (source:<text>)
    // @Required 1
    // @Maximum 5
    // @Short Ban or un-ban players or ip addresses.
    // @Group server
    //
    // @Description
    // Add or remove player or ip address bans from the server. Banning a player will also kick them from the server.
    // You may specify a list of player names instead of <@link ObjectType PlayerTag>s, which should only ever be used for special cases (such as banning players that haven't joined the server yet).
    //
    // You may optionally specify both a list of players and list of addresses.
    //
    // Additional options are:
    // reason: Sets the ban reason.
    // expire: Sets the expire time of the temporary ban, as a TimeTag or a DurationTag. This will be a permanent ban if not specified.
    // source: Sets the source of the ban.
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

    public enum Actions { ADD, REMOVE }

    public static void autoExecute(ScriptEntry scriptEntry,
                               @ArgName("action") @ArgDefaultText("add") Actions action,
                               @ArgName("targets") @ArgLinear @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> targets,
                               @ArgName("addresses") @ArgPrefixed @ArgDefaultNull ListTag addresses,
                               @ArgName("names") @ArgPrefixed @ArgDefaultNull ListTag names,
                               @ArgName("reason") @ArgPrefixed @ArgDefaultNull String reason,
                               @ArgName("expire") @ArgPrefixed @ArgDefaultNull ObjectTag rawExpire,
                               @ArgName("source") @ArgPrefixed @ArgDefaultNull String source) {
        if ((targets == null || targets.isEmpty()) && (addresses == null || addresses.isEmpty()) && (names == null || names.isEmpty())) {
            throw new InvalidArgumentsRuntimeException("Must specify valid players, addresses or names to ban.");
        }
        Date expiration = null;
        if (rawExpire != null) {
            if (rawExpire.canBeType(DurationTag.class)) {
                DurationTag banDuration = rawExpire.asType(DurationTag.class, scriptEntry.context);
                if (banDuration.getSeconds() > 0) {
                    expiration = new Date(TimeTag.now().millis() + banDuration.getMillis());
                }
            }
            else {
                TimeTag expirationTime = rawExpire.asType(TimeTag.class, scriptEntry.context);
                if (expirationTime == null) {
                    throw new InvalidArgumentsRuntimeException("Invalid 'expire:' input, must be a DurationTag or a TimeTag.");
                }
                expiration = new Date(expirationTime.millis());
            }
        }
        switch (action) {
            case ADD -> {
                if (targets != null) {
                    for (PlayerTag player : targets) {
                        if (player.isValid()) {
                            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expiration, source);
                            if (player.isOnline()) {
                                player.getPlayerEntity().kickPlayer(reason);
                            }
                        }
                    }
                }
                if (addresses != null) {
                    for (String address : addresses) {
                        Bukkit.getBanList(BanList.Type.IP).addBan(address, reason, expiration, source);
                    }
                }
                if (names != null) {
                    for (String name : names) {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expiration, source);
                        Player player = Bukkit.getPlayerExact(name);
                        if (player != null) {
                            player.kickPlayer(reason);
                        }
                    }
                }
            }
            case REMOVE -> {
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
                    addresses.forEach(Bukkit.getBanList(BanList.Type.IP)::pardon);
                }
                if (names != null) {
                    names.forEach(Bukkit.getBanList(BanList.Type.NAME)::pardon);
                }
            }
        }
    }
}

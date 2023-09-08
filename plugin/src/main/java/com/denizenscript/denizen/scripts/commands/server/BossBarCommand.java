package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.*;

public class BossBarCommand extends AbstractCommand {

    public BossBarCommand() {
        setName("bossbar");
        setSyntax("bossbar ({auto}/create/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (options:<option>|...) (uuid:<uuid>)");
        setRequiredArguments(1, 9);
        isProcedural = false;
        autoCompile();
        addRemappedPrefixes("title", "t");
        addRemappedPrefixes("progress", "health", "p", "h");
        addRemappedPrefixes("style", "s");
        addRemappedPrefixes("options", "option", "opt", "o", "flags", "flag", "f");
    }

    // <--[command]
    // @Name BossBar
    // @Syntax bossbar ({auto}/create/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (options:<option>|...) (uuid:<uuid>)
    // @Required 1
    // @Maximum 9
    // @Short Shows players a boss bar.
    // @Group server
    //
    // @Description
    // Displays a boss bar at the top of the screen of the specified player(s).
    // You can also update the values and remove the bar.
    //
    // You can CREATE a new bossbar, UPDATE an existing one, or REMOVE an existing one.
    // The default 'auto' will either 'create' or 'update' depending on whether it already exists.
    //
    // Requires an ID.
    //
    // Progress must be between 0 and 1.
    //
    // Valid colors: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW.
    // Valid styles: SEGMENTED_10, SEGMENTED_12, SEGMENTED_20, SEGMENTED_6, SOLID.
    // Valid options: CREATE_FOG, DARKEN_SKY, PLAY_BOSS_MUSIC.
    //
    // The UUID can optionally be specified, and will be sent to the client. Be careful to not overlap multiple bars with the same UUID.
    // If not specified, it will be random.
    //
    // @Tags
    // <server.current_bossbars>
    // <server.bossbar_viewers[<bossbar_id>]>
    // <PlayerTag.bossbar_ids>
    // <entry[saveName].bar_uuid> returns the bossbar's UUID.
    //
    // @Usage
    // Shows a message to all online players.
    // - bossbar MyMessageID players:<server.online_players> "title:HI GUYS" color:red
    //
    // @Usage
    // Update the boss bar's color and progress.
    // - bossbar update MyMessageID color:blue progress:0.2
    //
    // @Usage
    // Add more players to the boss bar.
    // - bossbar update MyMessageID players:<server.flag[new_players]>
    //
    // @Usage
    // Remove a player from the boss bar.
    // - bossbar remove MyMessageID players:<[player]>
    //
    // @Usage
    // Delete the boss bar.
    // - bossbar remove MyMessageID
    // -->

    public enum Action {
        AUTO, CREATE, UPDATE, REMOVE
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("id:", bossBarMap.keySet());
        tab.addWithPrefix("style:", BarStyle.values());
        tab.addWithPrefix("color:", BarColor.values());
        tab.addWithPrefix("options:", BarFlag.values());
    }

    public final static Map<String, BossBar> bossBarMap = new HashMap<>();

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgDefaultText("auto") Action action,
                                   @ArgName("id") @ArgLinear ElementTag id,
                                   @ArgName("players") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> players,
                                   @ArgName("title") @ArgPrefixed @ArgDefaultNull String title,
                                   @ArgName("progress") @ArgPrefixed @ArgDefaultNull ElementTag progress,
                                   @ArgName("color") @ArgPrefixed @ArgDefaultText("white") BarColor color,
                                   @ArgName("style") @ArgPrefixed @ArgDefaultText("solid") BarStyle style,
                                   @ArgName("options") @ArgPrefixed @ArgDefaultNull @ArgSubType(BarFlag.class) List<BarFlag> options,
                                   @ArgName("uuid") @ArgPrefixed @ArgDefaultNull String uuid) {
        String idString = id.asLowerString();
        if (action == Action.AUTO) {
            action = bossBarMap.containsKey(idString) ? Action.UPDATE : Action.CREATE;
        }
        if (players == null && action == Action.CREATE) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("Missing player input!");
            }
            players = Collections.singletonList(Utilities.getEntryPlayer(scriptEntry));
        }
        BossBar bossBar = null;
        switch (action) {
            case CREATE: {
                if (bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' already exists!");
                    return;
                }
                List<PlayerTag> barPlayers = players;
                double barProgress = progress != null ? progress.asDouble() : 1D;
                if (title == null) {
                    title = "";
                }
                bossBar = Bukkit.createBossBar(title, color, style, options == null ? new BarFlag[0] : options.toArray(new BarFlag[0]));
                NMSHandler.playerHelper.setBossBarTitle(bossBar, title);
                bossBar.setProgress(barProgress);
                if (uuid != null) {
                    NMSHandler.instance.setBossbarUUID(bossBar, UUID.fromString(uuid));
                }
                for (PlayerTag player : barPlayers) {
                    if (!player.isOnline()) {
                        Debug.echoError("Player must be online to show a BossBar to them!");
                        continue;
                    }
                    bossBar.addPlayer(player.getPlayerEntity());
                }
                bossBar.setVisible(true);
                bossBarMap.put(idString, bossBar);
                break;
            }
            case UPDATE: {
                if (!bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                bossBar = bossBarMap.get(idString);
                if (title != null) {
                    NMSHandler.playerHelper.setBossBarTitle(bossBar, title);
                }
                if (progress != null) {
                    bossBar.setProgress(progress.asDouble());
                }
                if (color != null) {
                    bossBar.setColor(color);
                }
                if (style != null) {
                    bossBar.setStyle(style);
                }
                if (options != null) {
                    HashSet<BarFlag> oldFlags = new HashSet<>(Arrays.asList(BarFlag.values()));
                    HashSet<BarFlag> newFlags = new HashSet<>(options.size());
                    for (BarFlag flag : options) {
                        newFlags.add(flag);
                        oldFlags.remove(flag);
                    }
                    for (BarFlag flag : oldFlags) {
                        bossBar.removeFlag(flag);
                    }
                    for (BarFlag flag : newFlags) {
                        bossBar.addFlag(flag);
                    }
                }
                if (players != null) {
                    for (PlayerTag player : players) {
                        bossBar.addPlayer(player.getPlayerEntity());
                    }
                }
                break;
            }
            case REMOVE: {
                bossBar = bossBarMap.get(idString);
                if (bossBar == null) {
                    Debug.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                if (players != null) {
                    for (PlayerTag player : players) {
                        bossBar.removePlayer(player.getPlayerEntity());
                    }
                    break;
                }
                bossBar.setVisible(false);
                bossBarMap.remove(idString);
                break;
            }
        }
        if (bossBar != null) {
            UUID actualUuid = NMSHandler.instance.getBossbarUUID(bossBar);
            if (actualUuid != null) {
                scriptEntry.saveObject("bar_uuid", new ElementTag(actualUuid.toString()));
            }
        }
    }
}

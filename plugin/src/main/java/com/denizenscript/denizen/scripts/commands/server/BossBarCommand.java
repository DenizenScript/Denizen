package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
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
        setSyntax("bossbar ({auto}/create/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (options:<option>|...)");
        setRequiredArguments(1, 8);
        isProcedural = false;
    }

    // <--[command]
    // @Name BossBar
    // @Syntax bossbar ({auto}/create/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (options:<option>|...)
    // @Required 1
    // @Maximum 8
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
    // @Tags
    // <server.current_bossbars>
    // <server.bossbar_viewers[<bossbar_id>]>
    // <PlayerTag.bossbar_ids>
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

    private enum Action {
        AUTO, CREATE, UPDATE, REMOVE
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("id:", bossBarMap.keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("progress")
                    && arg.matchesPrefix("progress", "health", "p", "h")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("progress", arg.asElement());
            }
            else if (!scriptEntry.hasObject("color")
                    && arg.matchesPrefix("color", "c")
                    && arg.matchesEnum(BarColor.class)) {
                scriptEntry.addObject("color", arg.asElement());
            }
            else if (!scriptEntry.hasObject("style")
                    && arg.matchesPrefix("style", "s")
                    && arg.matchesEnum(BarStyle.class)) {
                scriptEntry.addObject("style", arg.asElement());
            }
            else if (!scriptEntry.hasObject("options")
                    && arg.matchesPrefix("options", "option", "opt", "o", "flags", "flag", "f")
                    && arg.matchesEnumList(BarFlag.class)) {
                scriptEntry.addObject("options", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an ID!");
        }
        if ((!scriptEntry.hasObject("action") || scriptEntry.getElement("action").asString().equalsIgnoreCase("CREATE"))
                && !scriptEntry.hasObject("players")) {
            if (Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline()) {
                scriptEntry.addObject("players", new ListTag(Collections.singleton(Utilities.getEntryPlayer(scriptEntry).identify())));
            }
            else {
                throw new InvalidArgumentsException("Must specify valid player(s)!");
            }
        }
        scriptEntry.defaultObject("action", new ElementTag("AUTO"));
    }

    public final static Map<String, BossBar> bossBarMap = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag action = scriptEntry.getElement("action");
        ListTag players = scriptEntry.getObjectTag("players");
        ElementTag title = scriptEntry.getElement("title");
        ElementTag progress = scriptEntry.getElement("progress");
        ElementTag color = scriptEntry.getElement("color");
        ElementTag style = scriptEntry.getElement("style");
        ListTag options = scriptEntry.getObjectTag("options");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, action, players, title, progress, color, style, options);
        }
        String idString = id.asLowerString();
        Action a = Action.valueOf(action.asString().toUpperCase());
        if (a == Action.AUTO) {
            a = bossBarMap.containsKey(idString) ? Action.UPDATE : Action.CREATE;
        }
        switch (a) {
            case CREATE: {
                if (bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' already exists!");
                    return;
                }
                String barTitle = title != null ? title.asString() : "";
                List<PlayerTag> barPlayers = players.filter(PlayerTag.class, scriptEntry);
                double barProgress = progress != null ? progress.asDouble() : 1D;
                BarColor barColor = color != null ? BarColor.valueOf(color.asString().toUpperCase()) : BarColor.WHITE;
                BarStyle barStyle = style != null ? BarStyle.valueOf(style.asString().toUpperCase()) : BarStyle.SOLID;
                BarFlag[] barFlags = new BarFlag[options != null ? options.size() : 0];
                if (options != null) {
                    for (int i = 0; i < options.size(); i++) {
                        barFlags[i] = (BarFlag.valueOf(options.get(i).toUpperCase()));
                    }
                }
                BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle, barFlags);
                NMSHandler.playerHelper.setBossBarTitle(bossBar, barTitle);
                bossBar.setProgress(barProgress);
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
                BossBar bossBar1 = bossBarMap.get(idString);
                if (title != null) {
                    NMSHandler.playerHelper.setBossBarTitle(bossBar1, title.asString());
                }
                if (progress != null) {
                    bossBar1.setProgress(progress.asDouble());
                }
                if (color != null) {
                    bossBar1.setColor(BarColor.valueOf(color.asString().toUpperCase()));
                }
                if (style != null) {
                    bossBar1.setStyle(BarStyle.valueOf(style.asString().toUpperCase()));
                }
                if (options != null) {
                    HashSet<BarFlag> oldFlags = new HashSet<>(Arrays.asList(BarFlag.values()));
                    HashSet<BarFlag> newFlags = new HashSet<>(options.size());
                    for (String flagName : options) {
                        BarFlag flag = BarFlag.valueOf(flagName.toUpperCase());
                        newFlags.add(flag);
                        oldFlags.remove(flag);
                    }
                    for (BarFlag flag : oldFlags) {
                        bossBar1.removeFlag(flag);
                    }
                    for (BarFlag flag : newFlags) {
                        bossBar1.addFlag(flag);
                    }
                }
                if (players != null) {
                    for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                        bossBar1.addPlayer(player.getPlayerEntity());
                    }
                }
                break;
            }
            case REMOVE: {
                if (!bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                if (players != null) {
                    BossBar bar = bossBarMap.get(idString);
                    for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                        bar.removePlayer(player.getPlayerEntity());
                    }
                    break;
                }
                bossBarMap.get(idString).setVisible(false);
                bossBarMap.remove(idString);
                break;
            }
        }
    }
}

package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossBarCommand extends AbstractCommand {

    private enum Action {
        CREATE, UPDATE, REMOVE
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("progress")
                    && arg.matchesPrefix("progress", "health", "p", "h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("progress", arg.asElement());
            }
            else if (!scriptEntry.hasObject("color")
                    && arg.matchesPrefix("color", "c")
                    && arg.matchesEnum(BarColor.values())) {
                scriptEntry.addObject("color", arg.asElement());
            }
            else if (!scriptEntry.hasObject("style")
                    && arg.matchesPrefix("style", "s")
                    && arg.matchesEnum(BarStyle.values())) {
                scriptEntry.addObject("style", arg.asElement());
            }
            else if (!scriptEntry.hasObject("flags")
                    && arg.matchesPrefix("flags", "flag", "f")
                    && arg.matchesEnumList(BarFlag.values())) {
                scriptEntry.addObject("flags", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class));
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
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (data.hasPlayer() && data.getPlayer().isOnline()) {
                scriptEntry.addObject("players", new dList(Collections.singleton(data.getPlayer().identify())));
            }
            else {
                throw new InvalidArgumentsException("Must specify valid player(s)!");
            }
        }

        scriptEntry.defaultObject("action", new Element("CREATE"));
    }

    public final static Map<String, BossBar> bossBarMap = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element id = scriptEntry.getElement("id");
        Element action = scriptEntry.getElement("action");
        dList players = scriptEntry.getdObject("players");
        Element title = scriptEntry.getElement("title");
        Element progress = scriptEntry.getElement("progress");
        Element color = scriptEntry.getElement("color");
        Element style = scriptEntry.getElement("style");
        dList flags = scriptEntry.getdObject("flags");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), id.debug() + action.debug()
                    + (players != null ? players.debug() : "")
                    + (title != null ? title.debug() : "")
                    + (progress != null ? progress.debug() : "")
                    + (color != null ? color.debug() : "")
                    + (style != null ? style.debug() : "")
                    + (flags != null ? flags.debug() : ""));

        }

        String idString = CoreUtilities.toLowerCase(id.asString());

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case CREATE:
                if (bossBarMap.containsKey(idString)) {
                    dB.echoError("BossBar '" + idString + "' already exists!");
                    return;
                }
                String barTitle = title != null ? title.asString() : "";
                List<dPlayer> barPlayers = players.filter(dPlayer.class, scriptEntry);
                double barProgress = progress != null ? progress.asDouble() : 1D;
                BarColor barColor = color != null ? BarColor.valueOf(color.asString().toUpperCase()) : BarColor.WHITE;
                BarStyle barStyle = style != null ? BarStyle.valueOf(style.asString().toUpperCase()) : BarStyle.SOLID;
                BarFlag[] barFlags = new BarFlag[flags != null ? flags.size() : 0];
                if (flags != null) {
                    for (int i = 0; i < flags.size(); i++) {
                        barFlags[i] = (BarFlag.valueOf(flags.get(i).toUpperCase()));
                    }
                }
                BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle, barFlags);
                bossBar.setProgress(barProgress);
                for (dPlayer player : barPlayers) {
                    if (!player.isOnline()) {
                        dB.echoError("Player must be online to show a BossBar to them!");
                        continue;
                    }
                    bossBar.addPlayer(player.getPlayerEntity());
                }
                bossBar.setVisible(true);
                bossBarMap.put(idString, bossBar);
                break;

            case UPDATE:
                if (!bossBarMap.containsKey(idString)) {
                    dB.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                BossBar bossBar1 = bossBarMap.get(idString);
                if (title != null) {
                    bossBar1.setTitle(title.asString());
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
                if (players != null) {
                    for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                        bossBar1.addPlayer(player.getPlayerEntity());
                    }
                }
                break;

            case REMOVE:
                if (!bossBarMap.containsKey(idString)) {
                    dB.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                if (players != null) {
                    BossBar bar = bossBarMap.get(idString);
                    for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
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

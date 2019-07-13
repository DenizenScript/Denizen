package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class SidebarCommand extends AbstractCommand {

    // <--[command]
    // @Name Sidebar
    // @Syntax sidebar (add/remove/{set}) (title:<title>) (scores:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)
    // @Required 1
    // @Short Controls clientside-only sidebars.
    // @Group player
    //
    // @Description
    // This command was created as a simpler replacement for using the Scoreboard command to display
    // per-player sidebars. By using packets and dummies, it enables you to have non-flickering, fully
    // functional sidebars without wasting processing speed and memory on creating new Scoreboards for
    // every single player.
    //
    // Using this command, you can add, remove, or set lines on the scoreboard.
    //
    // To set the title of the sidebar, use the 'title:' parameter in any case where the action is 'set'.
    //
    // By default, the score numbers descend from the total line count to 1.
    // To customize the automatic score values, use the 'start:' and 'increment:' arguments in any case where the action is 'set'.
    // 'Start' is the score where the first line will be shown with. The default 'start' value is determined by how many items are specified in 'values:'.
    // 'Increment' is the difference between each score and the default is -1.
    //
    // To instead set entirely custom numbers, use the 'scores:' input with a list of numbers,
    // where each number is the score to use with the value at the same place in the 'values:' list.
    //
    // You can remove by line value text, or by score number.
    //
    // The per_player argument is also available, and helps to reduce the number of loops required for
    // updating multiple players' sidebars. When it is specified, all tags in the command will fill based
    // on each individual player in the players list. So, for example, you could have <player.name> on a
    // lines and it will show each player specified their name on that line.
    //
    // @Tags
    // <PlayerTag.sidebar.lines>
    // <PlayerTag.sidebar.title>
    // <PlayerTag.sidebar.scores>
    //
    // @Usage
    // Show all online players a sidebar.
    // - sidebar set "title:Hello World!" "values:This is|My Message!|Wee!" "players:<server.list_online_players>"
    //
    // @Usage
    // Show a few players their ping.
    // - sidebar set "title:Info" "value:Ping<&co> <player.ping>" "players:p@Morphan1|p@mcmonkey4eva|p@Matterom" per_player
    //
    // @Usage
    // Set a sidebar with the score values indicating information to the user.
    // - sidebar set "line:<server.list_online_players.size>|<server.max_players>" "value:Players online|Players allowed"
    //
    // @Usage
    // Add a line to the bottom of the sidebar.
    // - sidebar add "value:This is the bottom!"
    //
    // @Usage
    // Remove multiple lines from the sidebar.
    // - sidebar remove "lines:2|4|6"
    //
    // @Usage
    // Stop showing the sidebar.
    // - sidebar remove
    // -->

    // TODO: Clean me!

    private enum Action {ADD, REMOVE, SET}

    @Override
    public void onEnable() {
        setParseArgs(false);
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(new SidebarEvents(), DenizenAPI.getCurrentInstance());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.SET;

        for (Argument arg : ArgumentHelper.interpret(scriptEntry.getOriginalArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                action = Action.valueOf(arg.getValue().toUpperCase());
            }
            else if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t", "objective", "obj", "o")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("lines")
                    && arg.matchesPrefix("scores", "score", "lines", "line", "l")) {
                scriptEntry.addObject("scores", arg.asElement());
            }
            else if (!scriptEntry.hasObject("value")
                    && arg.matchesPrefix("value", "values", "val", "v")) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("increment")
                    && arg.matchesPrefix("increment", "inc", "i")) {
                scriptEntry.addObject("increment", arg.asElement());
            }
            else if (!scriptEntry.hasObject("start")
                    && arg.matchesPrefix("start", "s")) {
                scriptEntry.addObject("start", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("players", "player", "p")) {
                scriptEntry.addObject("players", arg.asElement());
            }
            else if (!scriptEntry.hasObject("per_player")
                    && arg.matches("per_player")) {
                scriptEntry.addObject("per_player", new ElementTag(true));
            }
        }

        if (action == Action.ADD && !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify value(s) for that action!");
        }

        if (action == Action.SET && !scriptEntry.hasObject("value") && !scriptEntry.hasObject("title")
                && !scriptEntry.hasObject("increment") && !scriptEntry.hasObject("start")) {
            throw new InvalidArgumentsException("Must specify at least one of: value(s), title, increment, or start for that action!");
        }

        if (action == Action.SET && scriptEntry.hasObject("scores") && !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify value(s) when setting scores!");
        }

        scriptEntry.addObject("action", new ElementTag(action.name()));

        BukkitScriptEntryData entryData = (BukkitScriptEntryData) scriptEntry.entryData;
        scriptEntry.defaultObject("per_player", new ElementTag(false))
                .defaultObject("players", new ElementTag(entryData.hasPlayer() ? entryData.getPlayer().identify() : "li@"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag action = scriptEntry.getElement("action");
        ElementTag elTitle = scriptEntry.getElement("title");
        ElementTag elScores = scriptEntry.getElement("scores");
        ElementTag elValue = scriptEntry.getElement("value");
        ElementTag elIncrement = scriptEntry.getElement("increment");
        ElementTag elStart = scriptEntry.getElement("start");
        ElementTag elPlayers = scriptEntry.getElement("players");
        ElementTag elPerPlayer = scriptEntry.getElement("per_player");

        ListTag players = ListTag.valueOf(TagManager.tag(elPlayers.asString(), new BukkitTagContext(scriptEntry, false)));
        boolean per_player = elPerPlayer.asBoolean();

        String perTitle = null;
        String perScores = null;
        String perValue = null;
        String perIncrement = null;
        String perStart = null;

        ElementTag title = null;
        ListTag scores = null;
        ListTag value = null;
        ElementTag increment = null;
        ElementTag start = null;

        String debug;

        if (per_player) {
            if (elTitle != null) {
                perTitle = elTitle.asString();
            }
            if (elScores != null) {
                perScores = elScores.asString();
            }
            if (elValue != null) {
                perValue = elValue.asString();
            }
            if (elIncrement != null) {
                perIncrement = elIncrement.asString();
            }
            if (elStart != null) {
                perStart = elStart.asString();
            }
            debug = (elTitle != null ? elTitle.debug() : "") +
                    (elScores != null ? elScores.debug() : "") +
                    (elValue != null ? elValue.debug() : "") +
                    (elIncrement != null ? elIncrement.debug() : "") +
                    (elStart != null ? elStart.debug() : "");
        }
        else {
            BukkitTagContext context = (BukkitTagContext) DenizenCore.getImplementation().getTagContextFor(scriptEntry, false);
            if (elTitle != null) {
                title = new ElementTag(TagManager.tag(elTitle.asString(), context));
            }
            if (elScores != null) {
                scores = ListTag.valueOf(TagManager.tag(elScores.asString(), context));
            }
            if (elValue != null) {
                value = ListTag.valueOf(TagManager.tag(elValue.asString(), context));
            }
            if (elIncrement != null) {
                increment = new ElementTag(TagManager.tag(elIncrement.asString(), context));
            }
            if (elStart != null) {
                start = new ElementTag(TagManager.tag(elStart.asString(), context));
            }
            debug = (title != null ? title.debug() : "") +
                    (scores != null ? ArgumentHelper.debugObj("scores", scores) : "") +
                    (value != null ? ArgumentHelper.debugObj("value", value) : "") +
                    (increment != null ? increment.debug() : "") +
                    (start != null ? start.debug() : "");
        }

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action.debug() + debug + ArgumentHelper.debugObj("players", players.debug()));
        }

        switch (Action.valueOf(action.asString())) {

            case ADD:
                for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        Debug.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        value = ListTag.valueOf(TagManager.tag(perValue, context));
                        if (perScores != null) {
                            scores = ListTag.valueOf(TagManager.tag(perScores, context));
                        }
                    }
                    try {
                        int index = start != null ? start.asInt() : (current.size() > 0 ? current.get(current.size() - 1).score : value.size());
                        int incr = increment != null ? increment.asInt() : -1;
                        for (int i = 0; i < value.size(); i++, index += incr) {
                            int score = (scores != null && i < scores.size()) ? Integer.valueOf(scores.get(i)) : index;
                            current.add(new Sidebar.SidebarLine(value.get(i), score));
                        }
                    }
                    catch (Exception e) {
                        Debug.echoError(e);
                        continue;
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
                break;

            case REMOVE:
                for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        Debug.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = ListTag.valueOf(TagManager.tag(perValue, context));
                        }
                        if (perScores != null) {
                            scores = ListTag.valueOf(TagManager.tag(perScores, context));
                        }
                    }
                    if (scores != null) {
                        try {
                            for (String scoreString : scores) {
                                int score = Integer.valueOf(scoreString);
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).score == score) {
                                        current.remove(i--);
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            Debug.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                    }
                    if (value != null) {
                        for (String line : value) {
                            for (int i = 0; i < current.size(); i++) {
                                if (current.get(i).text.equalsIgnoreCase(line)) {
                                    current.remove(i--);
                                }
                            }
                        }
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                    }
                    else {
                        sidebar.remove();
                        sidebars.remove(player.getPlayerEntity().getUniqueId());
                    }
                }
                break;

            case SET:
                for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        Debug.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<Sidebar.SidebarLine> current = new ArrayList<>();
                    boolean currEdited = false;
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = ListTag.valueOf(TagManager.tag(perValue, context));
                        }
                        if (perScores != null) {
                            scores = ListTag.valueOf(TagManager.tag(perScores, context));
                        }
                        if (perStart != null) {
                            start = new ElementTag(TagManager.tag(perStart, context));
                        }
                        if (perIncrement != null) {
                            increment = new ElementTag(TagManager.tag(perIncrement, context));
                        }
                        if (perTitle != null) {
                            title = new ElementTag(TagManager.tag(perTitle, context));
                        }
                    }
                    if (value != null) {
                        try {
                            int index = start != null ? start.asInt() : value.size();
                            int incr = increment != null ? increment.asInt() : -1;
                            for (int i = 0; i < value.size(); i++, index += incr) {
                                int score = (scores != null && i < scores.size()) ? Integer.valueOf(scores.get(i)) : index;
                                current.add(new Sidebar.SidebarLine(value.get(i), score));
                            }
                        }
                        catch (Exception e) {
                            Debug.echoError(e);
                            continue;
                        }
                        currEdited = true;
                    }
                    if (title != null) {
                        sidebar.setTitle(title.asString());
                    }
                    if (currEdited) {
                        sidebar.setLines(current);
                    }
                    sidebar.sendUpdate();
                }
                break;
        }
    }

    private static final Map<UUID, Sidebar> sidebars = new HashMap<>();

    private static Sidebar createSidebar(PlayerTag denizenPlayer) {
        if (!denizenPlayer.isOnline()) {
            return null;
        }
        Player player = denizenPlayer.getPlayerEntity();
        UUID uuid = player.getUniqueId();
        if (!sidebars.containsKey(uuid)) {
            sidebars.put(uuid, NMSHandler.getInstance().createSidebar(player));
        }
        return sidebars.get(player.getUniqueId());
    }

    public static Sidebar getSidebar(PlayerTag denizenPlayer) {
        if (!denizenPlayer.isOnline()) {
            return null;
        }
        return sidebars.get(denizenPlayer.getPlayerEntity().getUniqueId());
    }

    public static class SidebarEvents implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (sidebars.containsKey(uuid)) {
                sidebars.remove(uuid);
            }
        }
    }
}

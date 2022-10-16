package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
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

    public SidebarCommand() {
        setName("sidebar");
        setSyntax("sidebar (add/remove/{set}/set_line) (title:<title>) (scores:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)");
        setRequiredArguments(1, 8);
        setParseArgs(false);
        Denizen.getInstance().getServer().getPluginManager().registerEvents(new SidebarEvents(), Denizen.getInstance());
        isProcedural = false;
    }

    // <--[command]
    // @Name Sidebar
    // @Syntax sidebar (add/remove/{set}/set_line) (title:<title>) (scores:<#>|...) (values:<line>|...) (start:<#>/{num_of_lines}) (increment:<#>/{-1}) (players:<player>|...) (per_player)
    // @Required 1
    // @Maximum 8
    // @Short Controls clientside-only sidebars.
    // @Group player
    //
    // @Description
    // This command was created as a simpler replacement for using the Scoreboard command to display per-player sidebars.
    // By using packets and dummies, it enables you to have non-flickering, fully functional sidebars,
    // without wasting processing speed and memory on creating new Scoreboards for  every single player.
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
    // The per_player argument is also available, and helps to reduce the number of loops required for updating multiple players' sidebars.
    // When it is specified, all tags in the command will fill based on each individual player in the players list.
    // So, for example, you could have <player.name> on a line and it will show each player specified their name on that line.
    //
    // @Tags
    // <PlayerTag.sidebar_lines>
    // <PlayerTag.sidebar_title>
    // <PlayerTag.sidebar_scores>
    //
    // @Usage
    // Use to show all online players a sidebar.
    // - sidebar set "title:Hello World!" "values:This is|My Message!|Wee!" players:<server.online_players>
    //
    // @Usage
    // Use to show a few players their ping.
    // - sidebar set title:Info "values:Ping<&co> <player.ping>" players:<[someplayer]>|<[player]>|<[aplayer]> per_player
    //
    // @Usage
    // Use to set a sidebar with the score values indicating information to the user.
    // - sidebar set scores:<server.online_players.size>|<server.max_players> "values:Players online|Players allowed"
    //
    // @Usage
    // Use to change a specific line of a sidebar.
    // - sidebar set_line scores:5 "values:Better message!"
    //
    // @Usage
    // Use to add a line to the bottom of the sidebar.
    // - sidebar add "values:This is the bottom!"
    //
    // @Usage
    // Use to remove multiple lines from the sidebar.
    // - sidebar remove scores:2|4|6
    //
    // @Usage
    // Use to stop showing the sidebar.
    // - sidebar remove
    // -->

    // TODO: Clean me!

    private enum Action {ADD, REMOVE, SET, SET_LINE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        Action action = Action.SET;
        for (Argument arg : ArgumentHelper.interpret(scriptEntry, scriptEntry.getOriginalArguments())) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
                action = Action.valueOf(arg.getValue().toUpperCase());
            }
            else if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t", "objective", "obj", "o")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("scores")
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
            else {
                arg.reportUnhandled();
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
        scriptEntry.addObject("action", new ElementTag(action));
        scriptEntry.defaultObject("per_player", new ElementTag(false));
        scriptEntry.defaultObject("players", new ElementTag(Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).identify() : "li@"));
    }

    public static boolean hasScoreAlready(List<Sidebar.SidebarLine> lines, int score) {
        for (Sidebar.SidebarLine line : lines) {
            if (line.score == score) {
                return true;
            }
        }
        return false;
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
        ListTag players = ListTag.valueOf(TagManager.tag(elPlayers.asString(), scriptEntry.getContext()), scriptEntry.getContext());
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
            if (scriptEntry.dbCallShouldDebug()) {
                Debug.report(scriptEntry, getName(), action, elTitle, elScores, elValue, elIncrement, elStart, db("players", players));
            }
        }
        else {
            BukkitTagContext context = (BukkitTagContext) scriptEntry.getContext();
            if (elTitle != null) {
                title = new ElementTag(TagManager.tag(elTitle.asString(), context));
            }
            if (elScores != null) {
                scores = ListTag.getListFor(TagManager.tagObject(elScores.asString(), context), context);
            }
            if (elValue != null) {
                value = ListTag.getListFor(TagManager.tagObject(elValue.asString(), context), context);
            }
            if (elIncrement != null) {
                increment = new ElementTag(TagManager.tag(elIncrement.asString(), context));
            }
            if (elStart != null) {
                start = new ElementTag(TagManager.tag(elStart.asString(), context));
            }
            if (scriptEntry.dbCallShouldDebug()) {
                Debug.report(scriptEntry, getName(), action, title, scores, value, increment, start, db("players", players));
            }
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
                                scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        value = ListTag.getListFor(TagManager.tagObject(perValue, context), context);
                        if (perScores != null) {
                            scores = ListTag.getListFor(TagManager.tagObject(perScores, context), context);
                        }
                    }
                    try {
                        int index = start != null ? start.asInt() : (current.size() > 0 ? current.get(current.size() - 1).score : value.size());
                        int incr = increment != null ? increment.asInt() : -1;
                        for (int i = 0; i < value.size(); i++, index += incr) {
                            int score = (scores != null && i < scores.size()) ? Integer.parseInt(scores.get(i)) : index;
                            while (hasScoreAlready(current, score)) {
                                score += (incr == 0 ? 1 : incr);
                            }
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
                                scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = ListTag.getListFor(TagManager.tagObject(perValue, context), context);
                        }
                        if (perScores != null) {
                            scores = ListTag.getListFor(TagManager.tagObject(perScores, context), context);
                        }
                    }
                    boolean removedAny = false;
                    if (scores != null) {
                        try {
                            for (String scoreString : scores) {
                                int score = Integer.parseInt(scoreString);
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
                        removedAny = true;
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
                        removedAny = true;
                    }
                    if (!removedAny) {
                        sidebar.remove();
                        sidebars.remove(player.getPlayerEntity().getUniqueId());
                    }
                }
                break;
            case SET_LINE:
                for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        Debug.echoError("Invalid player!");
                        continue;
                    }
                    if ((scores == null || scores.isEmpty()) && perScores == null) {
                        Debug.echoError("Missing or invalid 'scores' parameter.");
                        return;
                    }
                    if ((value == null || value.size() != scores.size()) && perValue == null) {
                        Debug.echoError("Missing or invalid 'values' parameter.");
                        return;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry),
                                scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = ListTag.getListFor(TagManager.tagObject(perValue, context), context);
                        }
                        if (perScores != null) {
                            scores = ListTag.getListFor(TagManager.tagObject(perScores, context), context);
                        }
                    }
                    try {
                        for (int i = 0; i < value.size(); i++) {
                            if (!ArgumentHelper.matchesInteger(scores.get(i))) {
                                Debug.echoError("Sidebar command scores input contains not-a-valid-number: " + scores.get(i));
                                return;
                            }
                            int score = Integer.parseInt(scores.get(i));
                            if (hasScoreAlready(current, score)) {
                                for (Sidebar.SidebarLine line : current) {
                                    if (line.score == score) {
                                        line.text = value.get(i);
                                        break;
                                    }
                                }
                            }
                            else {
                                current.add(new Sidebar.SidebarLine(value.get(i), score));
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
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry),
                                scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = ListTag.getListFor(TagManager.tagObject(perValue, context), context);
                        }
                        if (perScores != null) {
                            scores = ListTag.getListFor(TagManager.tagObject(perScores, context), context);
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
                                int score = (scores != null && i < scores.size()) ? Integer.parseInt(scores.get(i)) : index;
                                current.add(new Sidebar.SidebarLine(value.get(i), score));
                            }
                        }
                        catch (Exception e) {
                            Debug.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                    }
                    if (title != null) {
                        sidebar.setTitle(title.asString());
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
            sidebars.put(uuid, NMSHandler.instance.createSidebar(player));
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
            sidebars.remove(uuid);
        }
    }
}

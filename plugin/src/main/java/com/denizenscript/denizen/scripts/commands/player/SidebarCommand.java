package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.tags.ParseableTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
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
        Denizen.getInstance().getServer().getPluginManager().registerEvents(new SidebarEvents(), Denizen.getInstance());
        addRemappedPrefixes("title", "t", "objective", "obj", "o");
        addRemappedPrefixes("scores", "score", "lines", "line", "l");
        addRemappedPrefixes("values", "value", "val", "v");
        addRemappedPrefixes("increment", "inc", "i");
        addRemappedPrefixes("start", "s");
        addRemappedPrefixes("players", "player", "p");
        isProcedural = false;
        autoCompile();
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

    public enum Action { ADD, REMOVE, SET, SET_LINE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgDefaultText("set") Action action,
                                   @ArgName("title") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String stringTitle,
                                   @ArgName("scores") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String stringScores,
                                   @ArgName("values") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String stringValues,
                                   @ArgName("start") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String stringStart,
                                   @ArgName("increment") @ArgPrefixed @ArgUnparsed @ArgDefaultText("-1") String stringIncrement,
                                   @ArgName("players") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> players,
                                   @ArgName("per_player") boolean perPlayer) {
        if (action == Action.ADD && stringValues == null) {
            Debug.echoError("Missing 'values' parameter!");
            return;
        }
        if (action == Action.SET && stringValues == null && stringTitle == null) {
            Debug.echoError("Must specify 'values' or 'title' for action 'set'!");
            return;
        }
        if (action == Action.SET && stringScores != null && stringValues == null) {
            Debug.echoError("Must specify value(s) when setting scores!");
            return;
        }
        if (players == null) {
            players = Utilities.entryHasPlayer(scriptEntry) ? List.of(Utilities.getEntryPlayer(scriptEntry)) : List.of();
        }
        BukkitTagContext globalCtx = (BukkitTagContext) scriptEntry.getContext();
        BukkitTagContext perPlayerCtx = perPlayer ? new BukkitTagContext(globalCtx) : null;
        ParseableTag titleTag = stringTitle == null ? null : TagManager.parseTextToTag(stringTitle, globalCtx);
        ParseableTag scoresTag = stringScores == null ? null : TagManager.parseTextToTag(stringScores, globalCtx);
        ParseableTag valuesTag = stringValues == null ? null : TagManager.parseTextToTag(stringValues, globalCtx);
        ParseableTag startTag = stringStart == null ? null : TagManager.parseTextToTag(stringStart, globalCtx);
        ParseableTag incrementTag = TagManager.parseTextToTag(stringIncrement, globalCtx);
        List<PlayerTag> cleanedList = new ArrayList<>(players.size());
        for (PlayerTag player : players) {
            if (player == null || !player.isValid() || !player.isOnline()) {
                Debug.echoError("Invalid player!");
                continue;
            }
            Sidebar sidebar = createSidebar(player);
            if (sidebar == null) {
                continue;
            }
            cleanedList.add(player);
        }
        switch (action) {
            case ADD -> {
                for (PlayerTag player : cleanedList) {
                    BukkitTagContext context = globalCtx;
                    if (perPlayer) {
                        perPlayerCtx.player = player;
                        context = perPlayerCtx;
                    }
                    Sidebar sidebar = createSidebar(player);
                    ListTag values = valuesTag == null ? null : ListTag.getListFor(valuesTag.parse(context), context);
                    if (values == null || values.isEmpty()) {
                        continue;
                    }
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    int incr = incrementTag.parse(context).asElement().asInt();
                    int index = startTag == null ? (!current.isEmpty() ? current.get(current.size() - 1).score : values.size()) : startTag.parse(context).asElement().asInt();
                    List<Integer> scores = parseScores(scoresTag, context);
                    for (int i = 0; i < values.size(); i++, index += incr) {
                        int score = (scores != null && i < scores.size()) ? scores.get(i) : index;
                        while (hasScoreAlready(current, score)) {
                            score += (incr == 0 ? 1 : incr);
                        }
                        current.add(new Sidebar.SidebarLine(values.get(i), score));
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
            }
            case REMOVE -> {
                for (PlayerTag player : cleanedList) {
                    BukkitTagContext context = globalCtx;
                    if (perPlayer) {
                        perPlayerCtx.player = player;
                        context = perPlayerCtx;
                    }
                    Sidebar sidebar = createSidebar(player);
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    boolean removedAny = false;
                    List<Integer> scores = parseScores(scoresTag, context);
                    if (scores != null && !scores.isEmpty()) {
                        for (int score : scores) {
                            for (int i = 0; i < current.size(); i++) {
                                if (current.get(i).score == score) {
                                    current.remove(i--);
                                }
                            }
                        }
                        removedAny = true;
                    }
                    ListTag values = valuesTag == null ? null : ListTag.getListFor(valuesTag.parse(context), context);
                    if (values != null && !values.isEmpty()) {
                        for (String line : values) {
                            for (int i = 0; i < current.size(); i++) {
                                if (current.get(i).text.equalsIgnoreCase(line)) {
                                    current.remove(i--);
                                }
                            }
                        }
                        removedAny = true;
                    }
                    if (!removedAny) {
                        sidebar.remove();
                        sidebars.remove(player.getPlayerEntity().getUniqueId());
                    }
                    else {
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                    }
                }
            }
            case SET_LINE -> {
                for (PlayerTag player : cleanedList) {
                    BukkitTagContext context = globalCtx;
                    if (perPlayer) {
                        perPlayerCtx.player = player;
                        context = perPlayerCtx;
                    }
                    List<Integer> scores = parseScores(scoresTag, context);
                    if (scores == null || scores.isEmpty()) {
                        Debug.echoError("Missing or invalid 'scores' parameter!");
                        return;
                    }
                    ListTag values = valuesTag == null ? null : ListTag.getListFor(valuesTag.parse(context), context);
                    if (values == null || values.size() != scores.size()) {
                        Debug.echoError("Missing or invalid 'values' parameter!");
                        return;
                    }
                    Sidebar sidebar = getSidebar(player);
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    for (int i = 0; i < values.size(); i++) {
                        int score = scores.get(i);
                        if (hasScoreAlready(current, score)) {
                            for (Sidebar.SidebarLine line : current) {
                                if (line.score == score) {
                                    line.text = values.get(i);
                                    break;
                                }
                            }
                        }
                        else {
                            current.add(new Sidebar.SidebarLine(values.get(i), score));
                        }
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
            }
            case SET -> {
                for (PlayerTag player : cleanedList) {
                    BukkitTagContext context = globalCtx;
                    if (perPlayer) {
                        perPlayerCtx.player = player;
                        context = perPlayerCtx;
                    }
                    Sidebar sidebar = getSidebar(player);
                    ListTag values = valuesTag == null ? null : ListTag.getListFor(valuesTag.parse(context), context);
                    if (values != null && !values.isEmpty()) {
                        List<Integer> scores = parseScores(scoresTag, context);
                        List<Sidebar.SidebarLine> current = new ArrayList<>(values.size());
                        int index = startTag == null ? values.size() : startTag.parse(context).asElement().asInt();
                        int incr = incrementTag.parse(context).asElement().asInt();
                        for (int i = 0; i < values.size(); i++, index += incr) {
                            int score = (scores != null && i < scores.size()) ? scores.get(i) : index;
                            current.add(new Sidebar.SidebarLine(values.get(i), score));
                        }
                        sidebar.setLines(current);
                    }
                    ElementTag title = titleTag == null ? null : titleTag.parse(context).asElement();
                    if (title != null) {
                        sidebar.setTitle(title.asString());
                    }
                    sidebar.sendUpdate();
                }
            }
        }
    }

    private static List<Integer> parseScores(ParseableTag scoresTag, BukkitTagContext context) {
        if (scoresTag == null) {
            return null;
        }
        ListTag parsed = ListTag.getListFor(scoresTag.parse(context), context);
        if (parsed == null) {
            return null;
        }
        List<Integer> scores = new ArrayList<>(parsed.size());
        for (ObjectTag s : parsed.objectForms) {
            scores.add(s.asElement().asInt());
        }
        return scores;
    }

    public static boolean hasScoreAlready(List<Sidebar.SidebarLine> lines, int score) {
        for (Sidebar.SidebarLine line : lines) {
            if (line.score == score) {
                return true;
            }
        }
        return false;
    }

    private static final Map<UUID, Sidebar> sidebars = new HashMap<>();

    private static Sidebar createSidebar(PlayerTag denizenPlayer) {
        if (!denizenPlayer.isOnline()) {
            return null;
        }
        Player player = denizenPlayer.getPlayerEntity();
        return sidebars.computeIfAbsent(player.getUniqueId(), uuid -> NMSHandler.instance.createSidebar(player));
    }

    public static Sidebar getSidebar(PlayerTag denizenPlayer) {
        return denizenPlayer.isOnline() ? sidebars.get(denizenPlayer.getPlayerEntity().getUniqueId()) : null;
    }

    public static class SidebarEvents implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            sidebars.remove(uuid);
        }
    }
}

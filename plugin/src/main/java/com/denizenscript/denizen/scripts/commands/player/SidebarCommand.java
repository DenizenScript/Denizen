package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
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
                                   @ArgName("title") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String title,
                                   @ArgName("scores") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String scores,
                                   @ArgName("values") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String values,
                                   @ArgName("start") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String start,
                                   @ArgName("increment") @ArgPrefixed @ArgUnparsed @ArgDefaultText("-1") String increment,
                                   @ArgName("players") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> players,
                                   @ArgName("per_player") boolean perPlayer) {
        if (action == Action.ADD && values == null) {
            Debug.echoError("Missing 'values' parameter!");
            return;
        }
        if (action == Action.SET && values == null && title == null) {
            Debug.echoError("Must specify at least one of: value(s), title, increment, or start for that action!");
            return;
        }
        if (action == Action.SET && scores == null && values == null) {
            Debug.echoError("Must specify value(s) when setting scores!");
            return;
        }
        if (players == null) {
            players = Utilities.entryHasPlayer(scriptEntry) ? Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)) : Collections.emptyList();
        }
        ElementTag parsedTitle = (perPlayer || title == null) ? null : new ElementTag(TagManager.tag(title, scriptEntry.getContext()));
        ListTag parsedValues = (perPlayer || values == null) ? null : ListTag.valueOf(TagManager.tag(values, scriptEntry.getContext()), scriptEntry.getContext());
        ListTag parsedScores = (perPlayer || scores == null) ? null : ListTag.valueOf(TagManager.tag(scores, scriptEntry.getContext()), scriptEntry.getContext());
        ElementTag parsedStart = (perPlayer || start == null) ? null : new ElementTag(TagManager.tag(start, scriptEntry.getContext()));
        ElementTag parsedIncrement = perPlayer ? null : new ElementTag(TagManager.tag(increment, scriptEntry.getContext()));
        Map<PlayerTag, PlayerSidebarData> sidebarData = new HashMap<>(players.size());
        for (PlayerTag player : players) {
            if (player == null || !player.isValid()) {
                Debug.echoError("Invalid player!");
                continue;
            }
            Sidebar sidebar = createSidebar(player);
            if (sidebar == null) {
                continue;
            }
            sidebarData.put(player, perPlayer ?
                    new PlayerSidebarData(sidebar, new BukkitTagContext(player, Utilities.getEntryNPC(scriptEntry), scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript()), title, scores, values, start, increment) :
                    new PlayerSidebarData(sidebar, parsedTitle, parsedScores, parsedValues, parsedStart, parsedIncrement));
        }
        switch (action) {
            case ADD -> {
                for (Map.Entry<PlayerTag, PlayerSidebarData> entry : sidebarData.entrySet()) {
                    Sidebar sidebar = entry.getValue().sidebar;
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    PlayerSidebarData data = entry.getValue();
                    try {
                        int index = data.getStart() != null ? data.getStart().asInt() : (!current.isEmpty() ? current.get(current.size() - 1).score : data.getValues().size());
                        int incr = data.getIncrement().asInt();
                        for (int i = 0; i < data.getValues().size(); i++, index += incr) {
                            int score = (data.getScores() != null && i < data.getScores().size()) ? Integer.parseInt(data.getScores().get(i)) : index;
                            while (hasScoreAlready(current, score)) {
                                score += (incr == 0 ? 1 : incr);
                            }
                            current.add(new Sidebar.SidebarLine(data.getValues().get(i), score));
                        }
                    } catch (NumberFormatException e) {
                        Debug.echoError(e);
                        continue;
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
            }
            case REMOVE -> {
                for (Map.Entry<PlayerTag, PlayerSidebarData> entry : sidebarData.entrySet()) {
                    Sidebar sidebar = entry.getValue().sidebar;
                    boolean removedAny = false;
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    PlayerSidebarData data = entry.getValue();
                    if (data.getScores() != null) {
                        try {
                            for (String scoreString : data.getScores()) {
                                int score = Integer.parseInt(scoreString);
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).score == score) {
                                        current.remove(i--);
                                    }
                                }
                            }
                        }
                        catch (NumberFormatException e) {
                            Debug.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                        removedAny = true;
                    }
                    if (data.getValues() != null) {
                        for (String line : data.getValues()) {
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
                        sidebars.remove(entry.getKey().getPlayerEntity().getUniqueId());
                    }
                }
            }
            case SET_LINE -> {
                for (Map.Entry<PlayerTag, PlayerSidebarData> entry : sidebarData.entrySet()) {
                    PlayerSidebarData data = entry.getValue();
                    if (data.getScores() == null || data.getScores().isEmpty()) {
                        Debug.echoError("Missing or invalid 'scores' parameter!");
                        return;
                    }
                    if (data.getValues() == null || data.getValues().size() != data.getScores().size()) {
                        Debug.echoError("Missing or invalid 'values' parameter!");
                        return;
                    }
                    Sidebar sidebar = entry.getValue().sidebar;
                    List<Sidebar.SidebarLine> current = sidebar.getLines();
                    try {
                        for (int i = 0; i < data.getValues().size(); i++) {
                            if (!ArgumentHelper.matchesInteger(data.getScores().get(i))) {
                                Debug.echoError("Sidebar command scores input contains not-a-valid-number: " + data.getScores().get(i));
                                return;
                            }
                            int score = Integer.parseInt(data.getScores().get(i));
                            if (hasScoreAlready(current, score)) {
                                for (Sidebar.SidebarLine line : current) {
                                    if (line.score == score) {
                                        line.text = data.getValues().get(i);
                                        break;
                                    }
                                }
                            }
                            else {
                                current.add(new Sidebar.SidebarLine(data.getValues().get(i), score));
                            }
                        }
                    } catch (NumberFormatException e) {
                        Debug.echoError(e);
                        continue;
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
            }
            case SET -> {
                for (Map.Entry<PlayerTag, PlayerSidebarData> entry : sidebarData.entrySet()) {
                    Sidebar sidebar = entry.getValue().sidebar;
                    List<Sidebar.SidebarLine> current = new ArrayList<>();
                    PlayerSidebarData data = entry.getValue();
                    if (data.getValues() != null) {
                        try {
                            int index = data.getStart() != null ? data.getStart().asInt() : data.getValues().size();
                            int incr = data.getIncrement() != null ? data.getIncrement().asInt() : -1;
                            for (int i = 0; i < data.getValues().size(); i++, index += incr) {
                                int score = (data.getScores() != null && i < data.getScores().size()) ? Integer.parseInt(data.getScores().get(i)) : index;
                                current.add(new Sidebar.SidebarLine(data.getValues().get(i), score));
                            }
                        }
                        catch (NumberFormatException e) {
                            Debug.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                    }
                    if (data.getTitle() != null) {
                        sidebar.setTitle(data.getTitle().asString());
                    }
                    sidebar.sendUpdate();
                }
            }
        }
    }

    public static class PlayerSidebarData {
        BukkitTagContext context;
        Sidebar sidebar;

        String rawTitle = null, rawScores = null, rawValues = null, rawStart = null, rawIncrement = null;

        ElementTag parsedTitle = null;
        ListTag parsedScores = null;
        ListTag parsedValues = null;
        ElementTag parsedStart = null;
        ElementTag parsedIncrement = null;

        PlayerSidebarData(Sidebar sidebar, BukkitTagContext context, String rawTitle, String rawScores, String rawValues, String rawStart, String rawIncrement) {
            this.sidebar = sidebar;
            this.context = context;
            this.rawTitle = rawTitle;
            this.rawScores = rawScores;
            this.rawValues = rawValues;
            this.rawStart = rawStart;
            this.rawIncrement = rawIncrement;
        }

        PlayerSidebarData(Sidebar sidebar, ElementTag title, ListTag scores, ListTag values, ElementTag start, ElementTag increment) {
            this.sidebar = sidebar;
            this.parsedTitle = title;
            this.parsedScores = scores;
            this.parsedValues = values;
            this.parsedStart = start;
            this.parsedIncrement = increment;
        }

        public ElementTag getTitle() {
            if (parsedTitle == null) {
                parsedTitle = rawTitle == null ? null : new ElementTag(TagManager.tag(rawTitle, context));
            }
            return parsedTitle;
        }

        public ListTag getScores() {
            if (parsedScores == null) {
                parsedScores = rawScores == null ? null : ListTag.getListFor(TagManager.tagObject(rawScores, context), context);
            }
            return parsedScores;
        }

        public ListTag getValues() {
            if (parsedValues == null) {
                parsedValues = rawValues == null ? null : ListTag.getListFor(TagManager.tagObject(rawValues, context), context);
            }
            return parsedValues;
        }

        public ElementTag getIncrement() {
            if (parsedIncrement == null) {
                parsedIncrement = rawIncrement == null ? null : new ElementTag(TagManager.tag(rawIncrement, context));
            }
            return parsedIncrement;
        }

        public ElementTag getStart() {
            if (parsedStart == null) {
                parsedStart = rawStart == null ? null : new ElementTag(TagManager.tag(rawStart, context));
            }
            return parsedStart;
        }
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

package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class SidebarCommand extends AbstractCommand {

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

        for (aH.Argument arg : aH.interpret(scriptEntry.getOriginalArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                action = Action.valueOf(arg.getValue().toUpperCase());
            }

            else if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t", "objective", "obj", "o")) {
                scriptEntry.addObject("title", arg.asElement());
            }

            else if (!scriptEntry.hasObject("lines")
                    && arg.matchesPrefix("lines", "line", "l")) {
                scriptEntry.addObject("lines", arg.asElement());
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
                scriptEntry.addObject("per_player", new Element(true));
            }
        }

        if (action == Action.ADD && !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify value(s) for that action!");
        }

        if (action == Action.SET && !scriptEntry.hasObject("value") && !scriptEntry.hasObject("title")
                && !scriptEntry.hasObject("increment") && !scriptEntry.hasObject("start")) {
            throw new InvalidArgumentsException("Must specify at least one of: value(s), title, increment, or start for that action!");
        }

        if (action == Action.SET && scriptEntry.hasObject("lines") && !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify value(s) when setting lines!");
        }

        scriptEntry.addObject("action", new Element(action.name()));

        scriptEntry.defaultObject("per_player", new Element(false)).defaultObject("players",
                new Element(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().identify()));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element elTitle = scriptEntry.getElement("title");
        Element elLines = scriptEntry.getElement("lines");
        Element elValue = scriptEntry.getElement("value");
        Element elIncrement = scriptEntry.getElement("increment");
        Element elStart = scriptEntry.getElement("start");
        Element elPlayers = scriptEntry.getElement("players");
        Element elPerPlayer = scriptEntry.getElement("per_player");

        dList players = dList.valueOf(TagManager.tag(elPlayers.asString(), new BukkitTagContext(scriptEntry, false)));
        boolean per_player = elPerPlayer.asBoolean();

        String perTitle = null;
        String perLines = null;
        String perValue = null;
        String perIncrement = null;
        String perStart = null;

        Element title = null;
        dList lines = null;
        dList value = null;
        Element increment = null;
        Element start = null;

        String debug;

        if (per_player) {
            if (elTitle != null) {
                perTitle = elTitle.asString();
            }
            if (elLines != null) {
                perLines = elLines.asString();
            }
            if (elValue != null) {
                perValue = elValue.asString();
            }
            if (elIncrement != null) {
                perIncrement = elIncrement.asString();
            }
            if (elStart != null) {
                perStart = start.asString();
            }
            debug = action.debug() +
                    (elTitle != null ? elTitle.debug() : "") +
                    (elLines != null ? elLines.debug() : "") +
                    (elValue != null ? elValue.debug() : "") +
                    (elIncrement != null ? elIncrement.debug() : "") +
                    (elStart != null ? elStart.debug() : "") +
                    players.debug();
        }
        else {
            BukkitTagContext context = new BukkitTagContext(scriptEntry, false);
            if (elTitle != null) {
                title = new Element(TagManager.tag(elTitle.asString(), context));
            }
            if (elLines != null) {
                lines = dList.valueOf(TagManager.tag(elLines.asString(), context));
            }
            if (elValue != null) {
                value = dList.valueOf(TagManager.tag(elValue.asString(), context));
            }
            if (elIncrement != null) {
                increment = new Element(TagManager.tag(elIncrement.asString(), context));
            }
            if (elStart != null) {
                start = new Element(TagManager.tag(elStart.asString(), context));
            }
            debug = action.debug() +
                    (title != null ? title.debug() : "") +
                    (lines != null ? lines.debug() : "") +
                    (value != null ? value.debug() : "") +
                    (increment != null ? increment.debug() : "") +
                    (start != null ? start.debug() : "") +
                    players.debug();
        }

        dB.report(scriptEntry, getName(), debug);

        switch (Action.valueOf(action.asString())) {

            case ADD:
                for (dPlayer player : players.filter(dPlayer.class)) {
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, null, false, scriptEntry,
                                scriptEntry.shouldDebug(), scriptEntry.getScript());
                        value = dList.valueOf(TagManager.tag(perValue, context));
                        if (perLines != null) {
                            lines = dList.valueOf(TagManager.tag(perLines, context));
                        }
                    }
                    if (lines != null) {
                        try {
                            for (int i = 0; i < lines.size(); i++) {
                                int index = Integer.valueOf(lines.get(i)) - 1;
                                String line = value.get(i);
                                current.add(index, line);
                            }
                        } catch (Exception e) {
                            dB.echoError(e);
                            continue;
                        }
                    }
                    else {
                        current.addAll(value);
                    }
                    sidebar.setLines(current);
                    sidebar.sendUpdate();
                }
                break;

            case REMOVE:
                for (dPlayer player : players.filter(dPlayer.class)) {
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, null, false, scriptEntry,
                                scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = dList.valueOf(TagManager.tag(perValue, context));
                        }
                        if (perLines != null) {
                            lines = dList.valueOf(TagManager.tag(perLines, context));
                        }
                    }
                    if (lines != null) {
                        try {
                            int offset = 0;
                            for (String line : lines) {
                                int index = Integer.valueOf(line) - 1 - offset;
                                current.remove(index);
                                offset++;
                            }
                        } catch (Exception e) {
                            dB.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                    }
                    else if (value != null) {
                        try {
                            Iterator<String> it = current.iterator();
                            while (it.hasNext()) {
                                String next = it.next();
                                for (String line : value) {
                                    if (next.equalsIgnoreCase(line)) {
                                        it.remove();
                                    }
                                }
                            }
                            for (String line : value) {
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).equalsIgnoreCase(line)) {
                                        current.remove(i);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            dB.echoError(e);
                            continue;
                        }
                        sidebar.setLines(current);
                        sidebar.sendUpdate();
                    }
                    else {
                        sidebar.remove();
                    }
                }
                break;

            case SET:
                for (dPlayer player : players.filter(dPlayer.class)) {
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    boolean currEdited = false;
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, null, false, scriptEntry,
                                scriptEntry.shouldDebug(), scriptEntry.getScript());
                        if (perValue != null) {
                            value = dList.valueOf(TagManager.tag(perValue, context));
                        }
                        if (perLines != null) {
                            lines = dList.valueOf(TagManager.tag(perLines, context));
                        }
                        if (perStart != null) {
                            start = new Element(TagManager.tag(perStart, context));
                        }
                        if (perIncrement != null) {
                            increment = new Element(TagManager.tag(perIncrement, context));
                        }
                        if (perTitle != null) {
                            title = new Element(TagManager.tag(perTitle, context));
                        }
                    }
                    if (lines != null) {
                        try {
                            for (int i = 0; i < lines.size(); i++) {
                                int index = Integer.valueOf(lines.get(i)) - 1;
                                String line = value.get(i);
                                if (index > current.size()) {
                                    current.add(line);
                                }
                                else {
                                    current.set(index, line);
                                }
                            }
                        } catch (Exception e) {
                            dB.echoError(e);
                            continue;
                        }
                        currEdited = true;
                    }
                    else if (value != null) {
                        current = value;
                        currEdited = true;
                    }
                    if (start != null) {
                        sidebar.setStart(start.asInt());
                        currEdited = true;
                    }
                    if (increment != null) {
                        sidebar.setIncrement(increment.asInt());
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

    private static final Map<UUID, Sidebar> sidebars = new HashMap<UUID, Sidebar>();

    private static Sidebar createSidebar(dPlayer denizenPlayer) {
        if (!denizenPlayer.isOnline()) {
            return null;
        }
        Player player = denizenPlayer.getPlayerEntity();
        Sidebar sidebar = sidebars.get(player.getUniqueId());
        if (sidebar == null) {
            sidebar = new Sidebar(player);
        }
        return sidebar;
    }

    public static Sidebar getSidebar(dPlayer denizenPlayer) {
        if (!denizenPlayer.isOnline()) {
            return null;
        }
        return sidebars.get(denizenPlayer.getPlayerEntity().getUniqueId());
    }

    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final IScoreboardCriteria dummyCriteria = new ScoreboardBaseCriteria("dummy");

    public static class Sidebar {
        private final Player player;
        private String title;
        private String[] lines;
        private int[] scores;
        private int start;
        private int increment;
        private ScoreboardObjective obj1;
        private ScoreboardObjective obj2;

        public Sidebar(Player player) {
            this.player = player;
            this.obj1 = new ScoreboardObjective(dummyScoreboard, "dummy_1", dummyCriteria);
            this.obj2 = new ScoreboardObjective(dummyScoreboard, "dummy_2", dummyCriteria);
            setTitle("");
            this.lines = new String[15];
            this.scores = new int[15];
            this.start = Integer.MIN_VALUE;
            this.increment = -1;
            sidebars.put(player.getUniqueId(), this);
        }

        public String getTitle() {
            return title;
        }

        public List<String> getLines() {
            return new ArrayList<String>(Arrays.asList(lines));
        }

        public int[] getScores() {
            return scores;
        }

        public int getStart() {
            return start;
        }

        public int getIncrement() {
            return increment;
        }

        public void setTitle(String title) {
            if (title.length() > 32) {
                title = title.substring(0, 32);
            }
            if (this.title == null || !this.title.equals(title)) {
                this.title = title;
                this.obj1.setDisplayName(title);
                this.obj2.setDisplayName(title);
            }
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setIncrement(int increment) {
            this.increment = increment;
        }

        public void setLines(List<String> lines) {
            lines.removeAll(Collections.singleton((String)null));
            this.lines = new String[15];
            this.scores = new int[15];
            int score = this.start;
            if (score == Integer.MIN_VALUE) {
                score = lines.size();
            }
            for (int i = 0; i < lines.size() && i < this.lines.length; i++, score += this.increment) {
                String line = lines.get(i);
                if (line.length() > 40) {
                    line = line.substring(0, 40);
                }
                this.lines[i] = line;
                this.scores[i] = score;
            }
        }

        public void sendUpdate() {
            PacketHelper.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj1, 0));
            for (int i = 0; i < this.lines.length; i++) {
                String line = this.lines[i];
                if (line == null) {
                    break;
                }
                ScoreboardScore score = new ScoreboardScore(dummyScoreboard, this.obj1, line);
                score.setScore(this.scores[i]);
                PacketHelper.sendPacket(player, new PacketPlayOutScoreboardScore(score));
            }
            PacketHelper.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this.obj1));
            PacketHelper.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
            ScoreboardObjective temp = this.obj2;
            this.obj2 = this.obj1;
            this.obj1 = temp;
        }

        public void remove() {
            PacketHelper.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
            sidebars.remove(player.getUniqueId());
        }
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

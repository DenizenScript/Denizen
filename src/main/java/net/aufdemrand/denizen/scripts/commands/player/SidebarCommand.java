package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
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
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class SidebarCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE, SET}

    static {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(new SidebarEvents(), DenizenAPI.getCurrentInstance());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.SET;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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
                scriptEntry.addObject("lines", arg.asType(dList.class));
            }

            else if (!scriptEntry.hasObject("value")
                    && arg.matchesPrefix("value", "values", "val", "v")) {
                scriptEntry.addObject("value", arg.asType(dList.class));
            }

            else if (!scriptEntry.hasObject("increment")
                    && arg.matchesPrefix("increment", "inc", "i")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("increment", arg.asElement());
            }

            else if (!scriptEntry.hasObject("start")
                    && arg.matchesPrefix("start", "s")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("start", arg.asElement());
            }

            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("players", "player", "p")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class));
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

        scriptEntry.defaultObject("players", new dList(Arrays.asList
                (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer())));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element title = scriptEntry.getElement("title");
        dList lines = scriptEntry.getdObject("lines");
        dList value = scriptEntry.getdObject("value");
        Element increment = scriptEntry.getElement("increment");
        Element start = scriptEntry.getElement("start");
        dList players = scriptEntry.getdObject("players");

        dB.report(scriptEntry, getName(),
                action.debug() +
                        (title != null ? title.debug() : "") +
                        (lines != null ? lines.debug() : "") +
                        (value != null ? value.debug() : "") +
                        (increment != null ? increment.debug() : "") +
                        (start != null ? start.debug() : "") +
                        players.debug());

        switch (Action.valueOf(action.asString())) {

            case ADD:
                for (dPlayer player : players.filter(dPlayer.class)) {
                    Sidebar sidebar = getSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
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
                    Sidebar sidebar = getSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
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
                    Sidebar sidebar = getSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    boolean currEdited = false;
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

    public static Sidebar getSidebar(dPlayer denizenPlayer) {
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

    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final IScoreboardCriteria dummyCriteria = new ScoreboardBaseCriteria("dummy");

    public static class Sidebar {
        private final Player player;
        private String title;
        private String[] lines;
        private int[] scores;
        private int start;
        private int increment;
        private ScoreboardObjective titleObjective;
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

        public List<String> getLines() {
            return new ArrayList<String>(Arrays.asList(lines));
        }

        public void setTitle(String title) {
            if (this.title == null || !this.title.equals(title)) {
                this.title = title;
                this.titleObjective = new ScoreboardObjective(dummyScoreboard, title, dummyCriteria);
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
                this.lines[i] = lines.get(i);
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

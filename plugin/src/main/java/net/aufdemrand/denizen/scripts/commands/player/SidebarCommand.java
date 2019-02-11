package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SidebarCommand extends AbstractCommand {

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

        BukkitScriptEntryData entryData = (BukkitScriptEntryData) scriptEntry.entryData;
        scriptEntry.defaultObject("per_player", new Element(false))
                .defaultObject("players", new Element(entryData.hasPlayer() ? entryData.getPlayer().identify() : "li@"));
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
                perStart = elStart.asString();
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
            BukkitTagContext context = (BukkitTagContext) DenizenAPI.getCurrentInstance().getTagContextFor(scriptEntry, false);
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

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), debug);

        }

        switch (Action.valueOf(action.asString())) {

            case ADD:
                for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        dB.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, ((BukkitScriptEntryData) scriptEntry.entryData).getNPC(),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
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
                        }
                        catch (Exception e) {
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
                for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        dB.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, ((BukkitScriptEntryData) scriptEntry.entryData).getNPC(),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
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
                        }
                        catch (Exception e) {
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
                                        current.remove(i--);
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            dB.echoError(e);
                            continue;
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
                for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                    if (player == null || !player.isValid()) {
                        dB.echoError("Invalid player!");
                        continue;
                    }
                    Sidebar sidebar = createSidebar(player);
                    if (sidebar == null) {
                        continue;
                    }
                    List<String> current = sidebar.getLines();
                    boolean currEdited = false;
                    if (per_player) {
                        TagContext context = new BukkitTagContext(player, ((BukkitScriptEntryData) scriptEntry.entryData).getNPC(),
                                false, scriptEntry, scriptEntry.shouldDebug(), scriptEntry.getScript());
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
                        }
                        catch (Exception e) {
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
        UUID uuid = player.getUniqueId();
        if (!sidebars.containsKey(uuid)) {
            sidebars.put(uuid, NMSHandler.getInstance().createSidebar(player));
        }
        return sidebars.get(player.getUniqueId());
    }

    public static Sidebar getSidebar(dPlayer denizenPlayer) {
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

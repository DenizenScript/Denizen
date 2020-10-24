package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.ScriptUtilities;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ClickableCommand extends AbstractCommand {

    public ClickableCommand() {
        setName("clickable");
        setSyntax("clickable [<script>] (def:<element>|...) (usages:<#>) (for:<player>|...) (until:<duration>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Clickable
    // @Syntax clickable [<script>] (def:<element>|...) (usages:<#>) (for:<player>|...) (until:<duration>)
    // @Required 1
    // @Maximum 5
    // @Short Generates a clickable command for players.
    // @Group player
    //
    // @Description
    // Generates a clickable command for players.
    //
    // Specify a task script to run, and optionally any definitions to pass.
    //
    // Optionally specify a maximum number of usages (defaults to unlimited).
    //
    // Optionally specify what players are allowed to use it. Defaults to unrestricted (any player may use it).
    //
    // Players will need the permission "denizen.clickable" to be able to use this.
    //
    // @Tags
    // <entry[saveName].command> returns the command to use in "on_click"
    // <ElementTag.on_click[<command>]>
    //
    // @Usage
    // Use to generate a clickable message that will run a task script named 'test_script'.
    // - clickable test_script save:my_clickable
    // - narrate "Click <blue><element[here].on_click[<entry[my_clickable].command>]><reset>!"
    //
    // @Usage
    // Use to generate a clickable message that will run a task script named 'reward_drop', that can be used by only the first person to click it.
    // - clickable reward_drop usages:1 save:reward
    // - announce "<blue><bold><element[Reward Here].on_click[<entry[reward].command>]><reset>!"
    //
    // @Usage
    // Use to generate a clickable message exclusively for the linked player, that must be used within a minute.
    // - clickable your_secret def:quest3 for:<player> until:1m save:secretmessage
    // - narrate "Do you want to know the secret? <blue><element[Yes].on_click[<entry[secretmessage].command>]><reset> / No."
    // -->

    public static class Clickable {
        public HashSet<UUID> forPlayers;
        public ListTag definitions;
        public ScriptTag script;
        public String path;
        public int remainingUsages;
        public TagContext context;
        public long until;
    }

    public static HashMap<UUID, Clickable> clickables = new HashMap<>();

    public static void runClickable(UUID id, Player player) {
        Clickable clickable = clickables.get(id);
        if (clickable == null) {
            return;
        }
        if (clickable.until != 0 && System.currentTimeMillis() > clickable.until) {
            clickables.remove(id);
            return;
        }
        if (clickable.forPlayers != null && !clickable.forPlayers.contains(player.getUniqueId())) {
            return;
        }
        if (clickable.remainingUsages > 0) {
            clickable.remainingUsages--;
            if (clickable.remainingUsages <= 0) {
                clickables.remove(id);
            }
        }
        ScriptUtilities.createAndStartQueue(clickable.script.getContainer(), clickable.path,
                new BukkitScriptEntryData(new PlayerTag(player), null), null, null, null, null, clickable.definitions, clickable.context);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("definitions")
                    && arg.matchesPrefix("def")) {
                scriptEntry.addObject("definitions", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("usages")
                    && arg.matchesPrefix("usages")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("usages", arg.asElement());
            }
            else if (!scriptEntry.hasObject("until")
                    && arg.matchesPrefix("until")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("until", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("for_players")
                    && arg.matchesPrefix("for")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("for_players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("script")) {
                String scriptName = arg.getRawValue();
                int dotIndex = scriptName.indexOf('.');
                if (dotIndex > 0) {
                    scriptEntry.addObject("path", new ElementTag(scriptName.substring(dotIndex + 1)));
                    scriptName = scriptName.substring(0, dotIndex);
                }
                ScriptTag script = new ScriptTag(scriptName);
                if (!script.isValid()) {
                    arg.reportUnhandled();
                }
                scriptEntry.addObject("script", script);
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Missing script argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ScriptTag script = scriptEntry.getObjectTag("script");
        ElementTag path = scriptEntry.getElement("path");
        List<PlayerTag> forPlayers = (List<PlayerTag>) scriptEntry.getObject("for_players");
        ElementTag usages = scriptEntry.getElement("usages");
        ListTag definitions = scriptEntry.getObjectTag("definitions");
        DurationTag until = scriptEntry.getObjectTag("until");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), script.debug()
                    + (path == null ? "" : path.debug())
                    + (usages == null ? "" : usages.debug())
                    + (definitions == null ? "" : definitions.debug())
                    + (until == null ? "" : until.debug())
                    + (forPlayers == null ? "" : ArgumentHelper.debugList("for", forPlayers)));
        }
        UUID id = UUID.randomUUID();
        Clickable newClickable = new Clickable();
        newClickable.script = script;
        newClickable.path = path == null ? null : path.asString();
        newClickable.definitions = definitions;
        newClickable.remainingUsages = usages == null ? -1 : usages.asInt();
        newClickable.until = until == null ? 0 : (System.currentTimeMillis() + until.getMillis());
        newClickable.context = scriptEntry.context;
        if (forPlayers != null) {
            newClickable.forPlayers = new HashSet<>(forPlayers.size());
            for (PlayerTag player : forPlayers) {
                newClickable.forPlayers.add(player.getOfflinePlayer().getUniqueId());
            }
        }
        clickables.put(id, newClickable);
        scriptEntry.addObject("command", new ElementTag("/denizenclickable " + id));
    }
}


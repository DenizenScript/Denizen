package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.BracedCommand;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ContextSource;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ScriptUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class ClickableCommand extends BracedCommand {

    public ClickableCommand() {
        setName("clickable");
        setSyntax("clickable (<script>/cancel:<id>) (def:<element>|.../defmap:<map>/def.<name>:<value>) (usages:<#>) (for:<player>|...) (until:<duration>)");
        setRequiredArguments(0, -1);
        isProcedural = false;
        allowedDynamicPrefixes = true;
        setPrefixesHandled("usages", "until", "for", "cancel", "def");
    }

    // <--[command]
    // @Name Clickable
    // @Syntax clickable (<script>/cancel:<id>) (def:<element>|.../defmap:<map>/def.<name>:<value>) (usages:<#>) (for:<player>|...) (until:<duration>)
    // @Required 0
    // @Maximum -1
    // @Short Generates a clickable command for players.
    // @Group player
    //
    // @Description
    // Generates a clickable command for players.
    //
    // Generally, prefer to write a command script and simply "on_click[/yourcommandhere]" rather than using generated clickables.
    // Generated clickables are a utility intended to enable clickables that are restricted from being normally accessed without receiving a clickable message.
    //
    // Specify a task script to run, or put an executable script section as sub-commands.
    //
    // When running a task, optionally any definitions to pass.
    //
    // When using a sub-section, the running commands will be in their own queue, but copy out the original queue's definitions and context source.
    //
    // Optionally specify a maximum number of usages (defaults to unlimited).
    //
    // Optionally specify a maximum duration it can be used for with 'until'.
    //
    // If no duration is specified, the clickable will remain valid until the server stops or restarts.
    // WARNING: if you use clickables very often without a duration limit, this can lead to a memory leak.
    // Clickables that have a specified max duration will occasionally be cleaned from memory.
    //
    // Optionally specify what players are allowed to use it. Defaults to unrestricted (any player that sees the click message may use it).
    // Note that it is possible for a player to find the generated command ID in their logs and send it to another player to "/" execute, so if you don't restrict player access it may be abused in that way.
    //
    // This internally generates a command of the form "/denizenclickable <generated_id>".
    //
    // Players will need the permission "denizen.clickable" to be able to use this.
    //
    // You can cancel a clickable at any time via "cancel:<id>", where ID is the generated ID from saving the initial generated command.
    //
    // @Tags
    // <entry[saveName].command> returns the command to use in "on_click".
    // <entry[saveName].id> returns the generate command's ID.
    // <ElementTag.on_click[<command>]>
    //
    // @Usage
    // Use to generate a clickable that just narrates "hello there!" when clicked.
    // - clickable save:my_clickable:
    //     - narrate "Hello there!"
    // - narrate "Click <blue><element[here].on_click[<entry[my_clickable].command>]><reset>!"
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
    // @Usage
    // Use to generate a clickable message and cancel it manually later.
    // - clickable test_script save:my_clickable save:myclickable
    // - narrate "Click <blue><element[here].on_click[<entry[my_clickable].command>]><reset> before you land!"
    // - waituntil rate:1s max:30s <player.is_on_ground>
    // - clickable cancel:<entry[myclickable].id>
    //
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addScriptsOfType(TaskScriptContainer.class);
    }

    public static class Clickable {
        public HashSet<UUID> forPlayers;
        public ListTag definitions;
        public MapTag defMap;
        public ScriptTag script;
        public String path;
        public NPCTag npc;
        public int remainingUsages;
        public TagContext context;
        public long until;
        public List<ScriptEntry> directEntries;
        public ContextSource contextSource;
        public String queueId;
    }

    public static HashMap<UUID, Clickable> clickables = new HashMap<>();

    public static void runClickable(UUID id, Player player) {
        Clickable clickable = clickables.get(id);
        if (clickable == null) {
            return;
        }
        if (clickable.until != 0 && CoreUtilities.monotonicMillis() > clickable.until) {
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
        Consumer<ScriptQueue> configure = (queue) -> {
            if (clickable.defMap != null) {
                for (Map.Entry<StringHolder, ObjectTag> val : clickable.defMap.entrySet()) {
                    queue.addDefinition(val.getKey().str, val.getValue());
                }
            }
        };
        BukkitScriptEntryData data = new BukkitScriptEntryData(new PlayerTag(player), clickable.npc);
        if (clickable.directEntries != null) {
            ScriptUtilities.createAndStartQueueArbitrary(clickable.queueId, clickable.directEntries, data, clickable.contextSource, configure);
        }
        else if (clickable.script.validate() != null) {
            ScriptUtilities.createAndStartQueue(clickable.script.getContainer(), clickable.path, data, null, configure, null, clickable.queueId, clickable.definitions, clickable.context);
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        MapTag defMap = new MapTag();
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("defmap")
                    && arg.matchesArgumentType(MapTag.class)) {
                defMap.putAll(arg.asType(MapTag.class));
            }
            else if (arg.hasPrefix()
                    && arg.getPrefix().getRawValue().startsWith("def.")) {
                defMap.putObject(arg.getPrefix().getRawValue().substring("def.".length()), arg.object);
            }
            else if (!scriptEntry.hasObject("script")) {
                String scriptName = arg.getRawValue();
                int dotIndex = scriptName.indexOf('.');
                if (dotIndex > 0) {
                    scriptEntry.addObject("path", new ElementTag(scriptName.substring(dotIndex + 1)));
                    scriptName = scriptName.substring(0, dotIndex);
                }
                ScriptTag script = ScriptTag.valueOf(scriptName, CoreUtilities.noDebugContext);
                if (script == null) {
                    arg.reportUnhandled();
                }
                else {
                    scriptEntry.addObject("script", script);
                }
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!defMap.isEmpty()) {
            scriptEntry.addObject("def_map", defMap);
        }
    }

    public static long lastTimeCleaned = CoreUtilities.monotonicMillis();

    private final static List<UUID> toCleanNow = new ArrayList<>();

    public static void cleanClickables() {
        long curTime = CoreUtilities.monotonicMillis();
        if (curTime < lastTimeCleaned + (30 * 60 * 1000)) {
            return;
        }
        lastTimeCleaned = curTime;
        toCleanNow.clear();
        for (Map.Entry<UUID, Clickable> clickable : clickables.entrySet()) {
            if (clickable.getValue().until != 0 && curTime > clickable.getValue().until) {
                toCleanNow.add(clickable.getKey());
            }
        }
        for (UUID id : toCleanNow) {
            clickables.remove(id);
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ScriptTag script = scriptEntry.getObjectTag("script");
        ElementTag path = scriptEntry.getElement("path");
        ElementTag cancel = scriptEntry.argForPrefixAsElement("cancel", null);
        List<PlayerTag> forPlayers = scriptEntry.argForPrefixList("for", PlayerTag.class, true);
        ElementTag usages = scriptEntry.argForPrefixAsElement("usages", null);
        ListTag definitions = scriptEntry.argForPrefix("def", ListTag.class, true);
        DurationTag until = scriptEntry.argForPrefix("until", DurationTag.class, true);
        MapTag defMap = scriptEntry.getObjectTag("def_map");
        if (script == null && cancel == null && scriptEntry.getInsideList() == null) {
            throw new InvalidArgumentsRuntimeException("Missing script argument!");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), script, cancel, path, usages, definitions, defMap, until, db("for", forPlayers));
        }
        if (cancel != null) {
            UUID id;
            try {
                id = UUID.fromString(cancel.asString());
            }
            catch (IllegalArgumentException ex) {
                Debug.echoError("Invalid cancel ID: " + ex.getMessage());
                return;
            }
            Clickable clicky = clickables.remove(id);
            if (clicky == null) {
                Debug.echoDebug(scriptEntry, "Cancelled ID didn't exist, nothing to cancel.");
            }
            else {
                Debug.echoDebug(scriptEntry, "Cancelled.");
            }
            return;
        }
        cleanClickables();
        UUID id = UUID.randomUUID();
        Clickable newClickable = new Clickable();
        if (script == null) {
            newClickable.contextSource = scriptEntry.queue.contextSource;
            newClickable.directEntries = getBracedCommandsDirect(scriptEntry, scriptEntry);
            newClickable.defMap = scriptEntry.queue.definitions.duplicate();
            if (defMap != null) {
                newClickable.defMap.putAll(defMap);
            }
            newClickable.queueId = "CLICKABLE_" + (scriptEntry.getScript() == null ? "UNKNOWN" : scriptEntry.getScript().getName());
        }
        else {
            newClickable.script = script;
            newClickable.defMap = defMap;
            newClickable.queueId = "CLICKABLE_" + script.getName();
        }
        newClickable.path = path == null ? null : path.asString();
        newClickable.definitions = definitions;
        newClickable.remainingUsages = usages == null ? -1 : usages.asInt();
        newClickable.until = until == null ? 0 : (CoreUtilities.monotonicMillis() + until.getMillis());
        newClickable.context = scriptEntry.context;
        newClickable.npc = Utilities.getEntryNPC(scriptEntry);
        if (forPlayers != null) {
            newClickable.forPlayers = new HashSet<>(forPlayers.size());
            for (PlayerTag player : forPlayers) {
                newClickable.forPlayers.add(player.getUUID());
            }
        }
        clickables.put(id, newClickable);
        scriptEntry.saveObject("command", new ElementTag("/denizenclickable " + id));
        scriptEntry.saveObject("id", new ElementTag(id.toString()));
    }
}

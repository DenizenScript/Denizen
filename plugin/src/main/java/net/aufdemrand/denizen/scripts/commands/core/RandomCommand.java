package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.BracedCommand;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.List;


public class RandomCommand extends BracedCommand {

    // <--[command]
    // @Name Random
    // @Syntax random [<#>/<commands>]
    // @Required 0
    // @Short Selects a random choice from the following script commands.
    // @Group core
    //
    // @Description
    // The random command picks one of the following script command
    // and skips all the other script commands that are in range.
    //
    // Specifying a number as argument will get the next following
    // scripts commands in the queue to be picked at random.
    // Example "- random 3" will get the next 3 commands
    // in the current queue and pick one of those 3 to run,
    // the other 2 commands, that was in range, will be skipped.
    //
    // If braces are used for argument, then it is for every
    // denizen command, in the brace, one of them will be picked
    // and the rest, in the brace, will be skipped.
    //
    // @Tags
    // <entry[saveName].possibilities> returns an Element of the possibility count.
    // <entry[saveName].selected> returns an Element of the selected number.
    //
    // @Usage
    // Use to choose randomly from the following commands
    // - random 3
    // - narrate "hi"
    // - narrate "hello"
    // - narrate "hey"
    //
    // @Usage
    // Use to choose randomly from a braced set of commands
    // - random:
    //   - narrate "hi"
    //   - narrate "hello"
    //   - narrate "hey"
    //
    // @Usage
    // Use to perform multiple commands randomly
    // - random:
    //   - repeat 1:
    //     - narrate "Hello"
    //     - narrate "How are you?"
    //   - repeat 1:
    //     - narrate "Hey"
    //     - narrate "It is a nice day."
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<BracedData> bdat = getBracedCommands(scriptEntry);

        if (bdat != null && bdat.size() > 0) {
            scriptEntry.addObject("braces", bdat);
        }

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matches("{")) {
                break;
            }
            else if (!scriptEntry.hasObject("possibilities")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("possibilities", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("braces")) {
            if (!scriptEntry.hasObject("possibilities")) {
                throw new InvalidArgumentsException("Missing possibilities!");
            }

            if (scriptEntry.getElement("possibilities").asInt() <= 1) {
                throw new InvalidArgumentsException("Must randomly select more than one item.");
            }

            if (scriptEntry.getResidingQueue().getQueueSize() < scriptEntry.getElement("possibilities").asInt()) {
                throw new InvalidArgumentsException("Invalid Size! Random # must not be larger than the script!");
            }
        }

    }

    private int previous = 0;
    private int previous2 = 0;
    private int previous3 = 0;

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        int possibilities = 0;
        ScriptQueue queue = scriptEntry.getResidingQueue();
        List<ScriptEntry> bracedCommands = null;

        if (!scriptEntry.hasObject("braces")) {
            possibilities = scriptEntry.getElement("possibilities").asInt();
        }
        else {
            bracedCommands = ((List<BracedData>) scriptEntry.getObject("braces")).get(0).value;
            possibilities = bracedCommands.size();
        }

        int selected = CoreUtilities.getRandom().nextInt(possibilities);
        // Try to not duplicate
        if (selected == previous || selected == previous2 || selected == previous3) {
            selected = CoreUtilities.getRandom().nextInt(possibilities);
        }
        if (selected == previous || selected == previous2 || selected == previous3) {
            selected = CoreUtilities.getRandom().nextInt(possibilities);
        }
        previous3 = previous2;
        previous2 = previous;
        previous = selected;
        scriptEntry.addObject("possibilities", new Element(possibilities));
        scriptEntry.addObject("selected", new Element(selected));

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugObj("possibilities", possibilities) + aH.debugObj("choice", selected + 1));

        }

        scriptEntry.setInstant(true);

        if (bracedCommands == null) {

            ScriptEntry keeping = null;

            for (int x = 0; x < possibilities; x++) {

                if (x != selected) {
                    queue.removeEntry(0);
                }
                else {
                    dB.echoDebug(scriptEntry, "...selected '" + queue.getEntry(0).getCommandName() + ": "
                            + queue.getEntry(0).getArguments() + "'.");
                    keeping = queue.getEntry(0);
                    queue.removeEntry(0);
                }

            }

            queue.injectEntry(keeping, 0);

        }
        else {
            queue.injectEntry(bracedCommands.get(selected), 0);
        }
    }
}

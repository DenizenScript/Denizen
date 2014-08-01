package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;


/**
 * Randomly selects a random script entry from the proceeding entries, discards
 * the rest.
 *
 *     <ol><tt>Usage:  RANDOM [#]</tt></ol>
 *
 * [#] of entries to randomly select from. Will select 1 of # to execute and
 * discard the rest.<br/><br/>
 *
 * Example Usage:<br/>
 * <ul style="list-style-type: none;">
 * <li><tt>Script:</tt></li>
 * <li><tt>- RANDOM 3</tt></li>
 * <li><tt>- CHAT Random Message 1</tt></li>
 * <li><tt>- CHAT Random Message 2</tt></li>
 * <li><tt>- CHAT Random Message 3 </tt></li>
 * </ul>
 *
 * @author Jeremy Schroeder
 */

public class RandomCommand extends BracedCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("{")) {
                scriptEntry.addObject("braces", getBracedCommands(scriptEntry));
                break;
            }

            else if (!scriptEntry.hasObject("possibilities")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("possibilities", arg.asElement());

            else arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("braces")) {
            if (!scriptEntry.hasObject("possibilities"))
                throw new InvalidArgumentsException("Missing possibilities!");

            if (scriptEntry.getElement("possibilities").asInt() <= 1)
                throw new InvalidArgumentsException("Must randomly select more than one item.");

            if (scriptEntry.getResidingQueue().getQueueSize() < scriptEntry.getElement("possibilities").asInt())
                throw new InvalidArgumentsException("Invalid Size! Random # must not be larger than the script!");
        }

    }

    private int previous = 0;
    private int previous2 = 0;
    private int previous3 = 0;

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        int possibilities = 0;
        ScriptQueue queue = scriptEntry.getResidingQueue();
        ArrayList<ScriptEntry> bracedCommands = null;

        if (!scriptEntry.hasObject("braces")) {
            possibilities = scriptEntry.getElement("possibilities").asInt();
        }
        else {
            bracedCommands = ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("RANDOM");
            possibilities = bracedCommands.size();
        }

        int selected = Utilities.getRandom().nextInt(possibilities);
        // Try to not duplicate
        if (selected == previous || selected == previous2 || selected == previous3)
            selected = Utilities.getRandom().nextInt(possibilities);
        if (selected == previous || selected == previous2 || selected == previous3)
            selected = Utilities.getRandom().nextInt(possibilities);
        previous3 = previous2;
        previous2 = previous;
        previous = selected;

        dB.report(scriptEntry, getName(), aH.debugObj("possibilities", possibilities) + aH.debugObj("choice", selected + 1));

        if (bracedCommands == null) {

            ScriptEntry keeping = null;

            for (int x = 0; x < possibilities; x++) {

                if (x != selected)
                    queue.removeEntry(0);

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
            queue.injectEntry(bracedCommands.get(selected).addObject("reqID", scriptEntry.getObject("reqID")), 0);
        }
    }
}

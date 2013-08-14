package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


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

public class RandomCommand extends AbstractCommand {

    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("possibilities")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("possibilities", arg.asElement());
            
            else
                throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        }    

        if (!scriptEntry.hasObject("possibilities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "POSSIBILITIES");
        
        if (scriptEntry.getElement("possibilities").asInt() <= 1)
            throw new InvalidArgumentsException("Must randomly select more than one item.");

        if (scriptEntry.getResidingQueue().getQueueSize() < scriptEntry.getElement("possibilities").asInt())
            throw new InvalidArgumentsException("Invalid Size! Random # must not be larger than the script!");

        scriptEntry.addObject("queue", scriptEntry.getResidingQueue());
        
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        int possibilities = scriptEntry.getElement("possibilities").asInt();
        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");

        int selected = Utilities.getRandom().nextInt(possibilities);
        List<ScriptEntry> keeping = new ArrayList<ScriptEntry>();
        int bracketsEntered = 0;
        boolean selectedBrackets = false;
        
        dB.echoDebug("...random number generator selected '%s'", String.valueOf(selected + 1));
        
        for (int x = 0; x < possibilities; x++) {
            
            if (bracketsEntered > 0) {
                if (queue.getEntry(0).getArguments().contains("}")) {
                    dB.echoDebug("Leaving brackets...");
                    bracketsEntered--;
                }
                
                if (selectedBrackets) {
                    keeping.add(queue.getEntry(0));
                    queue.removeEntry(0);
                    if (bracketsEntered == 0)
                        selectedBrackets = false;
                    continue;
                }
                
                if (x == selected) {
                    selected++;
                    queue.removeEntry(0);
                    continue;
                }
            }
            
            if (queue.getEntry(0).getArguments().contains("{")) {
                dB.echoDebug("Found brackets...");
                bracketsEntered++;
                if (x == selected)
                    selectedBrackets = true;
            }
            
            if (x != selected) {
                dB.echoDebug("...removing '%s'", queue.getEntry(0).getCommandName());
                queue.removeEntry(0);
            } 
            
            else {
                dB.echoDebug("...selected '%s'", queue.getEntry(0).getCommandName() + ": "
                        + queue.getEntry(0).getArguments());
                keeping.add(queue.getEntry(0));
                queue.removeEntry(0);
            }
            
        }
        
        queue.injectEntries(keeping, 0);
    }
}
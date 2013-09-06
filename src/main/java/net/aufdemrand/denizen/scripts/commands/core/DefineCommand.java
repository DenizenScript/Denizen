package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

/**
 * Creates a queue/script-level variable.
 *
 * @author Jeremy Schroeder
 *
 */
public class DefineCommand extends AbstractCommand implements Listener {

    public static String getHelp() {
        return  "Defines a script/queue-level variable. Once the queue is" +
                "completed, this definition is destroyed. Definitions are meant" +
                "to be used as temporary variables, if any kind of persistence " +
                "is required, use a flag instead. Definitions can be recalled" +
                "by any script entry in the same queue by using '%' characters" +
                "enclosing the variable name.\n" +
                " \n" +
                "- define doomed_player p@mastaba \n" +
                "- strike %doomed_player% \n";
    }

    public static String getUsage() {
        return "- define [<id>] [<value>]";
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("definition")
                    && !arg.matchesPrefix("value, v"))
                scriptEntry.addObject("definition", arg.getValue().toLowerCase());

            else if (!scriptEntry.hasObject("value")
                    && !arg.matchesPrefix("definition, def, d"))
                // Use the raw_value as to not exclude values with :'s in them.
                scriptEntry.addObject("value", arg.raw_value);

        }

        if (!scriptEntry.hasObject("definition") || !scriptEntry.hasObject("value"))
            throw new InvalidArgumentsException("Must specify a definition and value!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(getName(), aH.debugObj("queue", scriptEntry.getResidingQueue().id)
                + aH.debugObj("definition", scriptEntry.getObject("definition").toString())
                + aH.debugObj("value", scriptEntry.getObject("value").toString()));

        scriptEntry.getResidingQueue().addDefinition(
                (String) scriptEntry.getObject("definition"),
                (String) scriptEntry.getObject("value"));
    }

}
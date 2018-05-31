package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;


public class GameRuleCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("gamerule")) {
                scriptEntry.addObject("gamerule", arg.asElement());
            }
            else if (!scriptEntry.hasObject("value")) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("world")) {
            throw new InvalidArgumentsException("Must specify a world!");
        }

        if (!scriptEntry.hasObject("gamerule")) {
            throw new InvalidArgumentsException("Must specify a gamerule!");
        }

        if (!scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify a value!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        dWorld world = scriptEntry.getdObject("world");
        Element gamerule = scriptEntry.getElement("gamerule");
        Element value = scriptEntry.getElement("value");

        // Report to dB
        dB.report(scriptEntry, getName(), world.debug() + gamerule.debug() + value.debug());

        // Execute
        if (!world.getWorld().setGameRuleValue(gamerule.asString(), value.asString())) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid gamerule!");
        }
    }
}

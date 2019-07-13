package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;


public class GameRuleCommand extends AbstractCommand {

    // <--[command]
    // @Name Gamerule
    // @Syntax gamerule [<world>] [<rule>] [<value>]
    // @Required 3
    // @Short Sets a gamerule on the world.
    // @Group world
    //
    // @Description
    // Sets a gamerule on the world. A list of valid gamerules can be found here: http://minecraft.gamepedia.com/Commands#gamerule
    // Note: Be careful, gamerules are CASE SENSITIVE.
    //
    // @Tags
    // TODO: Add tags and then document them!
    //
    // @Usage
    // Use to disable fire spreading in world "Adventure".
    // - gamerule w@Adventure doFireTick false
    //
    // @Usage
    // Use to avoid mobs from destroying blocks (creepers, endermen...) and picking items up (zombies, skeletons...) in world "Adventure".
    // - gamerule w@Adventure mobGriefing false
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
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
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        WorldTag world = scriptEntry.getdObject("world");
        ElementTag gamerule = scriptEntry.getElement("gamerule");
        ElementTag value = scriptEntry.getElement("value");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), world.debug() + gamerule.debug() + value.debug());
        }

        // Execute
        if (!world.getWorld().setGameRuleValue(gamerule.asString(), value.asString())) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Invalid gamerule!");
        }
    }
}

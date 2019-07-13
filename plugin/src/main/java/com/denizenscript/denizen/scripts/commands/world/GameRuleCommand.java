package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
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

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        dWorld world = scriptEntry.getdObject("world");
        Element gamerule = scriptEntry.getElement("gamerule");
        Element value = scriptEntry.getElement("value");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), world.debug() + gamerule.debug() + value.debug());
        }

        // Execute
        if (!world.getWorld().setGameRuleValue(gamerule.asString(), value.asString())) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid gamerule!");
        }
    }
}

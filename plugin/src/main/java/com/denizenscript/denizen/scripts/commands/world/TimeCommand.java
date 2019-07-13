package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class TimeCommand extends AbstractCommand {

    // <--[command]
    // @Name Time
    // @Syntax time ({global}/player) [<time duration>] (<world>)
    // @Required 1
    // @Short Changes the current time in the minecraft world.
    // @Group world
    //
    // @Description
    // Changes the current time in a world or the time that a player sees the world in.
    // If no world is specified, defaults to the NPCs world. If no NPC is available,
    // defaults to the player's world. If no player is available, an error will be thrown.
    // If a player is specified, it will change their personal time.
    // This is separate from the global time, and does not affect other players.
    // When that player logs off, their time will be reset to the global time.
    //
    // @Tags
    // <w@world.time>
    // <w@world.time.period>
    //
    // @Usage
    // Use to set the time in the NPC or Player's world.
    // - time 500t
    //
    // @Usage
    // Use to make the player see a different time than everyone else.
    // - time player 500t
    //
    // @Usage
    // Use to set the time in a specific world.
    // - time 500t w@myworld
    //
    // -->

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("value")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("value", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value"))) {
            throw new InvalidArgumentsException("Must specify a value!");
        }

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to "world" if necessary
        if (!scriptEntry.hasObject("world")) {
            scriptEntry.addObject("world",
                    Utilities.entryHasNPC(scriptEntry) ?
                            new dWorld(Utilities.getEntryNPC(scriptEntry).getWorld()) :
                            (Utilities.entryHasPlayer(scriptEntry) ?
                                    new dWorld(Utilities.getEntryPlayer(scriptEntry).getWorld()) : null));
        }

        scriptEntry.defaultObject("type", new Element("GLOBAL"));

        if (!scriptEntry.hasObject("world")) {
            throw new InvalidArgumentsException("Must specify a valid world!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        Duration value = (Duration) scriptEntry.getObject("value");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Element type_element = scriptEntry.getElement("type");
        Type type = Type.valueOf(type_element.asString().toUpperCase());

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), type_element.debug()
                    + value.debug()
                    + world.debug());
        }

        if (type.equals(Type.GLOBAL)) {
            world.getWorld().setTime(value.getTicks());
        }
        else {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                dB.echoError("Must have a valid player link!");
            }
            else {
                Utilities.getEntryPlayer(scriptEntry)
                        .getPlayerEntity().setPlayerTime(value.getTicks(), true);
            }
        }
    }
}

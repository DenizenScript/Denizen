package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class TimeCommand extends AbstractCommand {

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ?
                            new dWorld(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getWorld()) :
                            (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ?
                                    new dWorld(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getWorld()) : null));
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
            if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                dB.echoError("Must have a valid player link!");
            }
            else {
                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()
                        .getPlayerEntity().setPlayerTime(value.getTicks(), true);
            }
        }
    }
}

package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.WorldBorder;

public class WorldBorderCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("center")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("center")) {
                scriptEntry.addObject("center", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("damage")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("damage")) {
                scriptEntry.addObject("damage", arg.asElement());
            }

            else if (!scriptEntry.hasObject("damagebuffer")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("damagebuffer")) {
                scriptEntry.addObject("damagebuffer", arg.asElement());
            }

            else if (!scriptEntry.hasObject("size")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("size")) {
                scriptEntry.addObject("size", arg.asElement());
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration")) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("warningdistance")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("warningdistance")) {
                scriptEntry.addObject("warningdistance", arg.asElement());
            }

            else if (!scriptEntry.hasObject("warningtime")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("warningtime")) {
                scriptEntry.addObject("warningtime", arg.asType(Duration.class));
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

        if (!scriptEntry.hasObject("world")) {
            throw new InvalidArgumentsException("Must specify a world!");
        }

        if (!scriptEntry.hasObject("center") && !scriptEntry.hasObject("size")
                && !scriptEntry.hasObject("damage") && !scriptEntry.hasObject("damagebuffer")
                && !scriptEntry.hasObject("warningdistance") && !scriptEntry.hasObject("warningtime")) {
            throw new InvalidArgumentsException("Must specify at least one option!");
        }

        // fill in default arguments if necessary

        scriptEntry.defaultObject("duration", new Duration(0));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dWorld world = (dWorld) scriptEntry.getObject("world");
        dLocation center = (dLocation) scriptEntry.getObject("center");
        Element size = scriptEntry.getElement("size");
        Element damage = scriptEntry.getElement("damage");
        Element damagebuffer = scriptEntry.getElement("damagebuffer");
        Duration duration = scriptEntry.getdObject("duration");
        Element warningdistance = scriptEntry.getElement("warningdistance");
        Duration warningtime = scriptEntry.getdObject("warningtime");

        dB.report(scriptEntry, getName(), world.debug()
                + (center != null ? center.debug() : "")
                + (size != null ? size.debug() : "")
                + (damage != null ? damage.debug() : "")
                + (damagebuffer != null ? damagebuffer.debug() : "")
                + (warningdistance != null ? warningdistance.debug() : "")
                + (warningtime != null ? warningtime.debug() : "")
                + duration.debug());

        WorldBorder worldborder = world.getWorld().getWorldBorder();

        if (center != null) {
            worldborder.setCenter(center);
        }

        if (size != null) {
            worldborder.setSize(size.asDouble(), duration.getSecondsAsInt());
        }

        if (damage != null) {
            worldborder.setDamageAmount(damage.asDouble());
        }

        if (damagebuffer != null) {
            worldborder.setDamageBuffer(damagebuffer.asDouble());
        }

        if (warningdistance != null) {
            worldborder.setWarningDistance(warningdistance.asInt());
        }

        if (warningtime != null) {
            worldborder.setWarningTime(warningtime.getSecondsAsInt());
        }
    }
}

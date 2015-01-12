package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

/**
 * Creates a sign with a certain text at a location.
 *
 * @author David Cernat
 */

public class SignCommand extends AbstractCommand {

    private enum Type { AUTOMATIC, SIGN_POST, WALL_SIGN }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values()))
                scriptEntry.addObject("type", arg.asElement());

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));

            else if (!scriptEntry.hasObject("direction")
                    && arg.matchesPrefix("direction", "dir"))
                scriptEntry.addObject("direction", arg.asElement());

            else if (!scriptEntry.hasObject("text"))
                scriptEntry.addObject("text", arg.asType(dList.class));

            else
                arg.reportUnhandled();
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a Sign location!");

        if (!scriptEntry.hasObject("text"))
            throw new InvalidArgumentsException("Must specify sign text!");

        // Default to SIGN_POST type
        scriptEntry.defaultObject("type", new Element(Type.AUTOMATIC.name()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        String direction = scriptEntry.hasObject("direction") ? ((Element) scriptEntry.getObject("direction")).asString() : null;
        Element typeElement = scriptEntry.getElement("type");
        dList text = (dList) scriptEntry.getObject("text");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        // Report to dB
        dB.report(scriptEntry, getName(), typeElement.debug()
                                          + location.debug()
                                          + text.debug());

        Type type = Type.valueOf(typeElement.asString().toUpperCase());
        Block sign = location.getBlock();
        if (type != Type.AUTOMATIC
                || (sign.getType() != Material.WALL_SIGN
                && sign.getType() != Material.SIGN_POST))
            sign.setType(type == Type.WALL_SIGN ? Material.WALL_SIGN: Material.SIGN_POST);
        BlockState signState = sign.getState();

        Utilities.setSignLines((Sign) signState, text.toArray(4));
        if (direction != null)
            Utilities.setSignRotation(signState, direction);
        else if (type == Type.WALL_SIGN)
            Utilities.setSignRotation(signState);
    }
}

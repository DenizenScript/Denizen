package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dLocation;
import net.minecraft.server.v1_7_R1.PacketPlayOutBlockAction;
import org.bukkit.Location;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;

public class AnimateChestCommand extends AbstractCommand {

    enum ChestAction { OPEN, CLOSE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(ChestAction.values()))
                scriptEntry.addObject("action", arg.asElement());

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrefix("sound")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean))
                scriptEntry.addObject("sound", arg.asElement());

            else
                arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", new Element("OPEN"));

        if (!scriptEntry.hasObject("sound"))
            scriptEntry.addObject("sound", Element.TRUE);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element action = scriptEntry.getElement("action");
        Element sound = scriptEntry.getElement("sound");

        dB.report(scriptEntry, getName(), location.debug()
                                          + action.debug()
                                          + sound.debug());

        switch (ChestAction.valueOf(action.asString().toUpperCase())) {
            case OPEN:
                if (sound.asBoolean()) scriptEntry.getPlayer().getPlayerEntity().playSound(location, Sound.CHEST_OPEN, 1, 1);
                ((CraftPlayer)scriptEntry.getPlayer().getPlayerEntity()).getHandle().playerConnection.sendPacket(
                        new PacketPlayOutBlockAction((int) location.getX(), (int) location.getY(), (int) location.getZ(),
                                ((CraftWorld) location.getWorld()).getHandle().getType((int) location.getX(), (int) location.getY(), (int) location.getZ()), 1, 1));
                break;

            case CLOSE:
                if (sound.asBoolean()) scriptEntry.getPlayer().getPlayerEntity().getWorld().playSound(location, Sound.CHEST_CLOSE, 1, 1);
                ((CraftPlayer)scriptEntry.getPlayer().getPlayerEntity()).getHandle().playerConnection.sendPacket(
                    new PacketPlayOutBlockAction((int)location.getX(), (int)location.getY(), (int)location.getZ(),
                        ((CraftWorld)location.getWorld()).getHandle().getType((int)location.getX(), (int)location.getY(), (int)location.getZ()), 1, 0));
                break;
        }
    }

}

package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.*;
import net.minecraft.server.v1_7_R2.PacketPlayOutBlockAction;
import org.bukkit.Location;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;

import java.util.Arrays;
import java.util.List;

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

            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class))
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));

            else
                arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", new Element("OPEN"));

        if (!scriptEntry.hasObject("sound"))
            scriptEntry.addObject("sound", Element.TRUE);

        if (!scriptEntry.hasObject("players")) {
            if (scriptEntry.hasPlayer())
                scriptEntry.addObject("players", Arrays.asList(scriptEntry.getPlayer()));
            else // TODO: Perhaps instead add all players in sight range?
                throw new InvalidArgumentsException("Missing 'players' argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element action = scriptEntry.getElement("action");
        Element sound = scriptEntry.getElement("sound");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");

        dB.report(scriptEntry, getName(), location.debug()
                                          + action.debug()
                                          + sound.debug()
                                          + aH.debugObj("players", players.toString()));

        switch (ChestAction.valueOf(action.asString().toUpperCase())) {
            case OPEN:
                for (dPlayer player: players) {
                    if (sound.asBoolean()) player.getPlayerEntity().playSound(location, Sound.CHEST_OPEN, 1, 1);
                    ((CraftPlayer)player.getPlayerEntity()).getHandle().playerConnection.sendPacket(
                            new PacketPlayOutBlockAction((int) location.getX(), (int) location.getY(), (int) location.getZ(),
                            ((CraftWorld) location.getWorld()).getHandle().getType((int) location.getX(), (int) location.getY(), (int) location.getZ()), 1, 1));
                }
                break;

            case CLOSE:
                for (dPlayer player: players) {
                    if (sound.asBoolean()) player.getPlayerEntity().getWorld().playSound(location, Sound.CHEST_CLOSE, 1, 1);
                    ((CraftPlayer)player.getPlayerEntity()).getHandle().playerConnection.sendPacket(
                            new PacketPlayOutBlockAction((int)location.getX(), (int)location.getY(), (int)location.getZ(),
                            ((CraftWorld)location.getWorld()).getHandle().getType((int)location.getX(), (int)location.getY(), (int)location.getZ()), 1, 0));
                }
                break;
        }
    }

}

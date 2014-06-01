package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.*;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;

import java.util.List;

/* PLAYSOUND [LOCATION:x,y,z,world] [SOUND:NAME] (VOLUME:#) (PITCH:#)*/

/*
 * Arguments: [] - Required, () - Optional
 * [LOCATION:x,y,z,world] specifies location of the sound
 * [SOUND:NAME] name of sound to be played
 * (VOLUME:#) adjusts the volume of the sound
 * (PITCH:#) adjusts the pitch of the sound
 *
 * Example Usage:
 * PLAYSOUND LOCATION:123,65,765,world SOUND:SPLASH VOLUME:1 PITCH:2
 * PLAYSOUND LOCATION:123,65,765,world S:SPLASH V:2 P:1
 *
 */

public class PlaySoundCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dLocation.class))
                scriptEntry.addObject("locations", arg.asType(dList.class).filter(dLocation.class));

            else if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dPlayer.class))
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dPlayer.class));

            else if (!scriptEntry.hasObject("volume")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("volume, v"))
                scriptEntry.addObject("volume", arg.asElement());

            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("pitch, p"))
                scriptEntry.addObject("pitch", arg.asElement());

            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrimitive(aH.PrimitiveType.String)) {
                scriptEntry.addObject("sound", arg.asElement());
            }

            else if (!scriptEntry.hasObject("custom")
                    && arg.matches("custom")) {
                scriptEntry.addObject("custom", Element.TRUE);
            }

            else
                arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("sound"))
            throw new InvalidArgumentsException("Missing sound argument!");
        if (!scriptEntry.hasObject("locations") && !scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Missing location argument!");

        scriptEntry.defaultObject("volume", new Element(1));
        scriptEntry.defaultObject("pitch", new Element(1));
        scriptEntry.defaultObject("custom", Element.FALSE);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("locations");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("entities");
        Element sound = scriptEntry.getElement("sound");
        Element volume = scriptEntry.getElement("volume");
        Element pitch = scriptEntry.getElement("pitch");
        Element custom = scriptEntry.getElement("custom");

        dB.report(scriptEntry, getName(),
                (locations != null ? aH.debugObj("locations", locations.toString()): "") +
                (players != null ? aH.debugObj("entities", players.toString()): "") +
                sound.debug() +
                volume.debug() +
                pitch.debug() +
                custom.debug());

        try {
            if (locations != null) {
                if (custom.asBoolean()) {
                    for (dLocation location : locations)
                        for (Player player: location.getWorld().getPlayers())
                            // Note: Randomly defining 100 blocks as maximum hear distance.
                            if (player.getLocation().distanceSquared(location) < 100 * 100)
                                player.playSound(location, sound.asString(),
                                        volume.asFloat(), pitch.asFloat());
                }
                else {
                for (dLocation location : locations)
                    location.getWorld().playSound(location,
                            Sound.valueOf(sound.asString().toUpperCase()),
                            volume.asFloat(), pitch.asFloat());
                }
            }
            else {
                for (dPlayer player: players) {
                    if (custom.asBoolean())
                        player.getPlayerEntity().playSound(player.getLocation(),
                                sound.asString(), volume.asFloat(), pitch.asFloat());
                    else
                        player.getPlayerEntity().playSound(player.getLocation(),
                                Sound.valueOf(sound.asString().toUpperCase()),
                                volume.asFloat(), pitch.asFloat());
                }
            }
        } catch (Exception e) {
            dB.echoDebug(scriptEntry, "Unable to play sound.");
        }
    }

}

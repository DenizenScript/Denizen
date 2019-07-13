package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Sound;

import java.util.List;

public class PlaySoundCommand extends AbstractCommand {

    // <--[command]
    // @Name PlaySound
    // @Syntax playsound [<location>|.../<player>|...] [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom) (sound_category:<category name>)
    // @Required 2
    // @Short Plays a sound at the location or to a list of players.
    // @Group world
    //
    // @Description
    // Plays a sound to a player or nearby players at a location.
    // The sound is played through the player's client just like
    // any other sounds in Minecraft. Sounds are respecfully played
    // with their sound types.
    // For example; zombie sounds are under the type: Mobs/Animals
    //
    // Specifying a player or list of players will only play
    // the sound for them for each of their current location.
    // Sounds are played at fixed locations and will not
    // follow a player while playing.
    // If a location is specified, it will play the sound for
    // all players if they are nearby that location specified.
    //
    // Optionally, specify 'custom' to play a custom sound added by a resource pack, changing the sound name to something like 'random.click'
    //
    // For a list of all sounds, check <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>
    //
    // For a list of all valid sound categories, check <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/SoundCategory.html>
    //
    // @Tags
    // <server.list_sounds>
    //
    // @Usage
    // Use to play a sound for a player
    // - playsound <player> sound:ENTITY_EXPERIENCE_ORB_PICKUP pitch:1
    // @Usage
    // Use to play a sound at a location for all nearby
    // - playsound <player.location> sound:ENTITY_PLAYER_LEVELUP
    // @Usage
    // Use to notify all players with a sound
    // - playsound <server.list_online_players> sound:ENTITY_PLAYER_LEVELUP volume:0.5 pitch:0.8
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dLocation.class)) {
                scriptEntry.addObject("locations", arg.asType(dList.class).filter(dLocation.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("volume")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("volume, v")) {
                scriptEntry.addObject("volume", arg.asElement());
            }
            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("pitch, p")) {
                scriptEntry.addObject("pitch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrimitive(aH.PrimitiveType.String)) {
                scriptEntry.addObject("sound", arg.asElement());
            }
            else if (!scriptEntry.hasObject("custom")
                    && arg.matches("custom")) {
                scriptEntry.addObject("custom", new Element(true));
            }
            else if (!scriptEntry.hasObject("sound_category")
                    && arg.matchesOnePrefix("sound_category")) {
                scriptEntry.addObject("sound_category", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("sound")) {
            throw new InvalidArgumentsException("Missing sound argument!");
        }
        if (!scriptEntry.hasObject("locations") && !scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }

        scriptEntry.defaultObject("volume", new Element(1));
        scriptEntry.defaultObject("pitch", new Element(1));
        scriptEntry.defaultObject("custom", new Element(false));
        scriptEntry.defaultObject("sound_category", new Element("MASTER"));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("locations");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("entities");
        Element sound = scriptEntry.getElement("sound");
        Element volume = scriptEntry.getElement("volume");
        Element pitch = scriptEntry.getElement("pitch");
        Element custom = scriptEntry.getElement("custom");
        Element sound_category = scriptEntry.getElement("sound_category");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (locations != null ? aH.debugObj("locations", locations.toString()) : "") +
                            (players != null ? aH.debugObj("entities", players.toString()) : "") +
                            sound.debug() +
                            volume.debug() +
                            pitch.debug() +
                            custom.debug());

        }

        try {
            if (locations != null) {
                if (custom.asBoolean()) {
                    for (dLocation location : locations) {
                        NMSHandler.getInstance().getSoundHelper().playSound(null, location, sound.asString(), volume.asFloat(),
                                pitch.asFloat(), sound_category.asString());
                    }
                }
                else {
                    for (dLocation location : locations) {
                        NMSHandler.getInstance().getSoundHelper().playSound(null, location, Sound.valueOf(sound.asString().toUpperCase()),
                                volume.asFloat(), pitch.asFloat(), sound_category.asString());
                    }
                }
            }
            else {
                for (dPlayer player : players) {
                    if (custom.asBoolean()) {
                        NMSHandler.getInstance().getSoundHelper().playSound(player.getPlayerEntity(), player.getLocation(), sound.asString(),
                                volume.asFloat(), pitch.asFloat(), sound_category.asString());
                    }
                    else {
                        NMSHandler.getInstance().getSoundHelper().playSound(player.getPlayerEntity(), player.getLocation(),
                                Sound.valueOf(sound.asString().toUpperCase()), volume.asFloat(), pitch.asFloat(), sound_category.asString());
                    }
                }
            }
        }
        catch (Exception e) {
            dB.echoDebug(scriptEntry, "Unable to play sound.");
        }
    }
}

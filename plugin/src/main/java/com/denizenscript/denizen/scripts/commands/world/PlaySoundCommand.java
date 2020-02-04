package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
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
        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("volume")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("volume", "v")) {
                scriptEntry.addObject("volume", arg.asElement());
            }
            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("pitch", "p")) {
                scriptEntry.addObject("pitch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.String)) {
                scriptEntry.addObject("sound", arg.asElement());
            }
            else if (!scriptEntry.hasObject("custom")
                    && arg.matches("custom")) {
                scriptEntry.addObject("custom", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("sound_category")
                    && arg.matchesPrefix("sound_category")) {
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

        scriptEntry.defaultObject("volume", new ElementTag(1));
        scriptEntry.defaultObject("pitch", new ElementTag(1));
        scriptEntry.defaultObject("custom", new ElementTag(false));
        scriptEntry.defaultObject("sound_category", new ElementTag("MASTER"));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("entities");
        ElementTag soundElement = scriptEntry.getElement("sound");
        ElementTag volumeElement = scriptEntry.getElement("volume");
        ElementTag pitchElement = scriptEntry.getElement("pitch");
        ElementTag custom = scriptEntry.getElement("custom");
        ElementTag sound_category = scriptEntry.getElement("sound_category");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    (locations != null ? ArgumentHelper.debugObj("locations", locations.toString()) : "") +
                            (players != null ? ArgumentHelper.debugObj("entities", players.toString()) : "") +
                            soundElement.debug() +
                            volumeElement.debug() +
                            pitchElement.debug() +
                            custom.debug());
        }

        String sound = soundElement.asString();
        float volume = volumeElement.asFloat();
        float pitch = pitchElement.asFloat();
        String category = sound_category.asString().toUpperCase();

        try {
            if (locations != null) {
                if (custom.asBoolean()) {
                    for (LocationTag location : locations) {
                        NMSHandler.getSoundHelper().playSound(null, location, sound, volume, pitch, category);
                    }
                }
                else {
                    for (LocationTag location : locations) {
                        NMSHandler.getSoundHelper().playSound(null, location, Sound.valueOf(sound.toUpperCase()), volume, pitch, category);
                    }
                }
            }
            else {
                for (PlayerTag player : players) {
                    if (custom.asBoolean()) {
                        NMSHandler.getSoundHelper().playSound(player.getPlayerEntity(), player.getLocation(), sound, volume, pitch, category);
                    }
                    else {
                        NMSHandler.getSoundHelper().playSound(player.getPlayerEntity(), player.getLocation(), Sound.valueOf(sound.toUpperCase()), volume, pitch, category);
                    }
                }
            }
        }
        catch (Exception e) {
            Debug.echoDebug(scriptEntry, "Unable to play sound.");
            if (Debug.verbose) {
                Debug.echoError(e);
            }
        }
    }
}

package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.List;

public class PlaySoundCommand extends AbstractCommand {

    public PlaySoundCommand() {
        setName("playsound");
        setSyntax("playsound (<location>|...) (<player>|...) [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom) (sound_category:<category_name>)");
        setRequiredArguments(2, 7);
        isProcedural = false;
        setBooleansHandled("custom");
        setPrefixesHandled("sound_category", "pitch", "volume");
    }

    // <--[command]
    // @Name PlaySound
    // @Syntax playsound (<location>|...) (<player>|...) [sound:<name>] (volume:<#.#>) (pitch:<#.#>) (custom) (sound_category:<category_name>)
    // @Required 2
    // @Maximum 7
    // @Short Plays a sound at the location or to a list of players.
    // @Synonyms Noise
    // @Group world
    //
    // @Description
    // Plays a sound to a player or nearby players at a location.
    // The sound is played through the player's client just like any other sounds in Minecraft.
    //
    // For a list of all sounds, check <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>
    //
    // Sounds are by default played under their normal sound type (eg zombie sounds are under the type Mobs/Animals).
    // You can optionally instead specify an alternate sound category to use.
    // For a list of all valid sound categories, check <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/SoundCategory.html>
    //
    // Specifying a player or list of players will only play the sound for each player, from their own location (but will not follow them if they move).
    // If a location is specified, it will play the sound for any players that are near the location specified.
    // If both players and locations are specified, will play the sound for only those players at those locations.
    //
    // Optionally, specify 'custom' to play a custom sound added by a resource pack, changing the sound name to something like 'random.click'
    //
    // Optionally specify a pitch value (defaults to 1.0). A pitch from 0.0 to 1.0 will be deeper (sounds like a demon), and above 1.0 will be higher pitched (sounds like a fairy).
    //
    // Optionally specify a volume value (defaults to 1.0). A volume from 0.0 to 1.0 will be quieter than normal.
    // A volume above 1.0 however will not be louder - instead it will be audible from farther (approximately 1 extra chunk of distance per value, eg 2.0 is 2 more chunks, 5.0 is 5 more chunks, etc.).
    //
    // @Tags
    // <server.sound_types>
    //
    // @Usage
    // Use to play a sound for a player
    // - playsound <player> sound:ENTITY_EXPERIENCE_ORB_PICKUP pitch:1
    // @Usage
    // Use to play a sound at a location for all nearby
    // - playsound <player.location> sound:ENTITY_PLAYER_LEVELUP
    // @Usage
    // Use to notify all players with a sound
    // - playsound <server.online_players> sound:ENTITY_PLAYER_LEVELUP volume:0.5 pitch:0.8
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("sound:", Sound.values());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("locations")
                    && arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("sound")
                    && arg.limitToOnlyPrefix("sound")) {
                scriptEntry.addObject("sound", arg.asElement());
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
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("entities");
        ElementTag soundElement = scriptEntry.getElement("sound");
        ElementTag volumeElement = scriptEntry.argForPrefixAsElement("volume", "1");
        ElementTag pitchElement = scriptEntry.argForPrefixAsElement("pitch", "1");
        boolean custom = scriptEntry.argAsBoolean("custom");
        ElementTag sound_category = scriptEntry.argForPrefixAsElement("sound_category", "MASTER");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("locations", locations), db("entities", players), soundElement, volumeElement, pitchElement, db("custom", custom));
        }
        String sound = soundElement.asString();
        float volume = volumeElement.asFloat();
        float pitch = pitchElement.asFloat();
        String category = sound_category.asString().toUpperCase();
        SoundCategory categoryEnum = category != null ? new ElementTag(category).asEnum(SoundCategory.class) : null;
        if (categoryEnum == null) {
            categoryEnum = SoundCategory.MASTER;
        }
        try {
            if (players == null) {
                if (custom) {
                    for (LocationTag location : locations) {
                        location.getWorld().playSound(location, sound, categoryEnum, volume, pitch);
                    }
                }
                else {
                    for (LocationTag location : locations) {
                        location.getWorld().playSound(location, Sound.valueOf(sound.toUpperCase()), categoryEnum, volume, pitch);
                    }
                }
            }
            else if (locations != null) {
                for (LocationTag location : locations) {
                    for (PlayerTag player : players) {
                        if (custom) {
                            player.getPlayerEntity().playSound(location, sound, categoryEnum, volume, pitch);
                        }
                        else {
                            player.getPlayerEntity().playSound(location, Sound.valueOf(sound.toUpperCase()), categoryEnum, volume, pitch);
                        }
                    }
                }
            }
            else {
                for (PlayerTag player : players) {
                    if (custom) {
                        player.getPlayerEntity().playSound(player.getLocation(), sound, categoryEnum, volume, pitch);
                    }
                    else {
                        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                            player.getPlayerEntity().playSound(player.getPlayerEntity(), Sound.valueOf(sound.toUpperCase()), categoryEnum, volume, pitch);
                        }
                        else {
                            player.getPlayerEntity().playSound(player.getLocation(), Sound.valueOf(sound.toUpperCase()), categoryEnum, volume, pitch);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Debug.echoDebug(scriptEntry, "Unable to play sound.");
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError(e);
            }
        }
    }
}

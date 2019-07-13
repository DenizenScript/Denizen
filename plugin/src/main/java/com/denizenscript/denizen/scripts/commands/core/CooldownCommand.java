package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

/**
 * <p>Sets a 'cooldown' period on a script. Can be per-player or globally.</p>
 */
public class CooldownCommand extends AbstractCommand {

    // <--[command]
    // @Name Cooldown
    // @Syntax cooldown [<duration>] (global) (s:<script>)
    // @Required 1
    // @Short Temporarily disables a script-container from meeting requirements.
    // @Group core
    //
    // @Description
    // Cools down a script-container. If an interact-container, when on cooldown, scripts will not pass a
    // requirements check allowing the next highest priority script to trigger. If any other type of script, a
    // manual requirements check (<ScriptTag_name.requirements.check>) will also return false until the cooldown
    // period is completed. Cooldown requires a type (player or global), a script, and a duration. It also requires
    // a valid link to a PlayerTag if using player-type cooldown.
    //
    // Cooldown periods are persistent through a server restart as they are saved in the saves.yml.
    //
    // @Tags
    // <ScriptTag_name.cooled_down[player]>
    // <ScriptTag_name.cooldown>
    // <s@requirements.check>
    //
    // @Usage
    // Use to keep the current interact script from meeting requirements.
    // - cooldown 20m
    //
    // @Usage
    // Use to keep a player from activating a script for a specified duration.
    // - cooldown 11h s:s@bonus_script
    // - cooldown 5s s:s@hit_indicator
    //
    // @Usage
    // Use the 'global' argument to indicate the script to be on cooldown for all players.
    // - cooldown global 24h s:s@daily_treasure_offering
    // -->

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Defaults are Type.PLAYER and the attached Script
        scriptEntry.addObject("type", Type.PLAYER);
        scriptEntry.addObject("script", scriptEntry.getScript());

        // Parse arguments.. we need a type, duration, and script.

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            // Type may be PLAYER or GLOBAL.. must not have a prefix.
            if (!arg.hasPrefix() && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }

            // DurationTag does not need a prefix, but is required.
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }

            // Require a prefix on the script, since it's optional.
            else if (arg.matchesPrefix("script", "s")) {
                // Check matchesArgumentType afterwards so we don't default
                // to the attached script unintentionally.
                if (arg.matchesArgumentType(ScriptTag.class)) {
                    scriptEntry.addObject("script", arg.asType(ScriptTag.class));
                }
                else {
                    throw new InvalidArgumentsException("Specified an invalid script!");
                }
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("duration")) {
            throw new InvalidArgumentsException("Requires a valid duration!");
        }
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        ScriptTag script = (ScriptTag) scriptEntry.getObject("script");
        DurationTag duration = (DurationTag) scriptEntry.getObject("duration");
        Type type = (scriptEntry.hasObject("type") ?
                (Type) scriptEntry.getObject("type") : Type.PLAYER);

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("Type", type.name())
                    + script.debug()
                    + (type.name().equalsIgnoreCase("player") ? Utilities.getEntryPlayer(scriptEntry).debug() : "")
                    + duration.debug());
        }

        // Perform cooldown
        switch (type) {
            case PLAYER:
                setCooldown(Utilities.getEntryPlayer(scriptEntry),
                        duration,
                        script.getName(),
                        false);
                break;

            case GLOBAL:
                setCooldown(null,
                        duration,
                        script.getName(),
                        true);
                break;
        }
    }


    /**
     * Gets the duration of a script cool-down.
     *
     * @param player     the Player to check, null if only checking Global.
     * @param scriptName the name of the script to check
     * @return a DurationTag of the time remaining
     */
    public static DurationTag getCooldownDuration(PlayerTag player, String scriptName) {

        // Change to UPPERCASE so there's no case-sensitivity.
        scriptName = scriptName.toUpperCase();

        DurationTag duration = DurationTag.ZERO;

        // Check current entry GLOBALLY, reset it if necessary
        if (DenizenAPI.getSaves().contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
            if (System.currentTimeMillis()
                    < DenizenAPI.getSaves().getLong("Global.Scripts." + scriptName + ".Cooldown Time")) {
                duration = new DurationTag((double) (DenizenAPI.getSaves().getLong("Global.Scripts." + scriptName
                        + ".Cooldown Time") - System.currentTimeMillis()) / 1000);
            }
        }

        // No player specified? No need to check any further...
        if (player == null) {
            return duration;
        }

        // If no entry for the script, return true
        if (!DenizenAPI.getSaves().contains("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time")) {
            return duration;
        }

        // If there is an entry, check against the time
        if (System.currentTimeMillis()
                <= DenizenAPI.getSaves().getLong("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time")) {
            DurationTag player_dur = new DurationTag((double) (DenizenAPI.getSaves().getLong("Players." + player.getSaveName() + ".Scripts."
                    + scriptName + ".Cooldown Time") - System.currentTimeMillis()) / 1000);
            if (player_dur.getSeconds() > duration.getSeconds()) {
                return player_dur;
            }
        }

        return duration;
    }


    /**
     * Checks if a script is cooled-down. If a cool-down is currently in progress,
     * its requirements will fail and it will not trigger. If the script is being cooled down
     * globally, this will also return false.
     *
     * @param player     the Player to check, null if only checking Global.
     * @param scriptName the name of the script to check
     * @return true if the script is cool
     */
    public static boolean checkCooldown(PlayerTag player, String scriptName) {

        // Change to UPPERCASE so there's no case-sensitivity.
        scriptName = scriptName.toUpperCase();

        // Check current entry GLOBALLY, reset it if necessary
        if (DenizenAPI.getSaves().contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
            if (System.currentTimeMillis()
                    < DenizenAPI.getSaves().getLong("Global.Scripts." + scriptName + ".Cooldown Time")) {
                return false;
            }
            else {
                DenizenAPI.getSaves().set("Global.Scripts." + scriptName + ".Cooldown Time", null);
            }
        }

        // No player specified? No need to check any further...
        if (player == null) {
            return true;
        }

        // If no entry for the script, return true
        if (!DenizenAPI.getSaves().contains("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time")) {
            return true;
        }

        // If there is an entry, check against the time
        if (System.currentTimeMillis()
                >= DenizenAPI.getSaves().getLong("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time")) {
            DenizenAPI.getSaves().set("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time", null);
            return true;
        }

        return false;
    }


    /**
     * Sets a cooldown for a Denizen Script. Can be for a specific Player, or GLOBAL.
     *
     * @param player     if not a global cooldown, the Player to set the cooldown for
     * @param duration   the duration of the cooldown period, in seconds
     * @param scriptName the name of the script to cooldown
     * @param global     whether the script should be cooled down globally
     */
    public static void setCooldown(PlayerTag player, DurationTag duration, String scriptName, boolean global) {
        scriptName = scriptName.toUpperCase();
        // Set global cooldown
        if (global) {
            DenizenAPI.getSaves().set("Global.Scripts." + scriptName + ".Cooldown Time",
                    System.currentTimeMillis()
                            + (duration.getSecondsAsInt() * 1000));

            // or set Player cooldown
        }
        else {
            DenizenAPI.getSaves().set("Players." + player.getSaveName() + ".Scripts." + scriptName + ".Cooldown Time",
                    System.currentTimeMillis()
                            + (duration.getSecondsAsInt() * 1000));
        }
    }
}

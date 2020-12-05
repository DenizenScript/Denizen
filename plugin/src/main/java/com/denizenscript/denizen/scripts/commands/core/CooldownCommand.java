package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class CooldownCommand extends AbstractCommand {

    public CooldownCommand() {
        setName("cooldown");
        setSyntax("cooldown [<duration>] (global) (script:<script>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Cooldown
    // @Syntax cooldown [<duration>] (global) (script:<script>)
    // @Required 1
    // @Maximum 3
    // @Short Temporarily disables an interact script for the linked player.
    // @Group core
    //
    // @Description
    // Temporarily disables an interact script for the linked player.
    //
    // Cooldown requires a type (player or global), a script, and a duration.
    // It also requires a valid link to a PlayerTag if using a non-global cooldown.
    //
    // To cooldown non-interact scripts automatically, consider <@link command ratelimit>.
    //
    // Cooldown periods are persistent through a server restart as they are saved in the 'saves.yml'.
    //
    // @Tags
    // <ScriptTag.cooled_down[player]>
    // <ScriptTag.cooldown>
    //
    // @Usage
    // Use to keep the current interact script from meeting requirements.
    // - cooldown 20m
    //
    // @Usage
    // Use to keep a player from activating a script for a specified duration.
    // - cooldown 11h script:bonus_script
    // - cooldown 5s script:hit_indicator
    //
    // @Usage
    // Use the 'global' argument to indicate the script to be on cooldown for all players.
    // - cooldown global 24h script:daily_treasure_offering
    // -->

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Defaults are Type.PLAYER and the attached Script
        scriptEntry.addObject("type", Type.PLAYER);
        scriptEntry.addObject("script", scriptEntry.getScript());

        // Parse arguments.. we need a type, duration, and script.

        for (Argument arg : scriptEntry.getProcessedArgs()) {

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
        ScriptTag script = scriptEntry.getObjectTag("script");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        Type type = (scriptEntry.hasObject("type") ?
                (Type) scriptEntry.getObject("type") : Type.PLAYER);

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
        TimeTag expires = Denizen.getInstance().serverFlagMap.getFlagExpirationTime("__interact_cooldown." + scriptName);
        if (expires != null) {
            return new DurationTag((TimeTag.now().millis() - expires.millis()) / 1000.0);
        }
        if (player == null) {
            return new DurationTag(0);
        }
        expires = player.getFlagTracker().getFlagExpirationTime("__interact_cooldown." + scriptName);
        if (expires != null) {
            return new DurationTag((TimeTag.now().millis() - expires.millis()) / 1000.0);
        }
        return new DurationTag(0);
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
        DurationTag cooldown = getCooldownDuration(player, scriptName);
        if (cooldown.getSeconds() <= 0) {
            return false;
        }
        return true;
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
        TimeTag cooldownTime = new TimeTag(TimeTag.now().millis() + duration.getMillis());
        if (global) {
            Denizen.getInstance().serverFlagMap.setFlag("__interact_cooldown." + scriptName, cooldownTime, cooldownTime);
        }
        else {
            player.getFlagTracker().setFlag("__interact_cooldown." + scriptName, cooldownTime, cooldownTime);
        }
    }
}

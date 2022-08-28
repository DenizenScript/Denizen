package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
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
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addScriptsOfType(InteractScriptContainer.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("script", "s")
                    && arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (arg.matchesEnum(Type.class)) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("type", Type.PLAYER);
        scriptEntry.defaultObject("script", scriptEntry.getScript());
        if (!scriptEntry.hasObject("duration")) {
            throw new InvalidArgumentsException("Requires a valid duration!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ScriptTag script = scriptEntry.getObjectTag("script");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        Type type = (scriptEntry.hasObject("type") ? (Type) scriptEntry.getObject("type") : Type.PLAYER);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Type", type.name()), script, (type.name().equalsIgnoreCase("player") ? Utilities.getEntryPlayer(scriptEntry) : null), duration);
        }
        switch (type) {
            case PLAYER:
                setCooldown(Utilities.getEntryPlayer(scriptEntry), duration, script.getName(), false);
                break;
            case GLOBAL:
                setCooldown(null, duration, script.getName(), true);
                break;
        }
    }

    public static DurationTag getCooldownDuration(PlayerTag player, String scriptName) {
        TimeTag expires = DenizenCore.serverFlagMap.getFlagExpirationTime("__interact_cooldown." + scriptName);
        if (expires != null) {
            return new DurationTag((expires.millis() - TimeTag.now().millis()) / 1000.0);
        }
        if (player == null) {
            return new DurationTag(0);
        }
        expires = player.getFlagTracker().getFlagExpirationTime("__interact_cooldown." + scriptName);
        if (expires != null) {
            return new DurationTag((expires.millis() - TimeTag.now().millis()) / 1000.0);
        }
        return new DurationTag(0);
    }

    public static boolean checkCooldown(PlayerTag player, String scriptName) {
        DurationTag cooldown = getCooldownDuration(player, scriptName);
        if (cooldown.getSeconds() > 0) {
            return false;
        }
        return true;
    }

    public static void setCooldown(PlayerTag player, DurationTag duration, String scriptName, boolean global) {
        TimeTag cooldownTime = new TimeTag(TimeTag.now().millis() + duration.getMillis());
        if (global) {
            DenizenCore.serverFlagMap.setFlag("__interact_cooldown." + scriptName, cooldownTime, cooldownTime);
        }
        else {
            player.getFlagTracker().setFlag("__interact_cooldown." + scriptName, cooldownTime, cooldownTime);
        }
    }
}

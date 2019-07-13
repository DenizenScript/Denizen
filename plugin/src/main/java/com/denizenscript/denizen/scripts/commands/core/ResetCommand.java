package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class ResetCommand extends AbstractCommand {

    // <--[command]
    // @Name Reset
    // @Syntax reset (<player>|...) [cooldown/saves/global_cooldown] (<script>)
    // @Required 1
    // @Short Resets various parts of Denizen's saves.yml, including a script's cooldowns or general player saves.
    // @Group core
    //
    // @Description
    // TODO: Document Command Details
    //
    // @Tags
    // None
    //
    // @Usage
    // TODO: Document Command Details
    // -->

    private enum Type {PLAYER_COOLDOWN, GLOBAL_COOLDOWN, SAVES}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matches("cooldown")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.PLAYER_COOLDOWN);
            }
            else if (arg.matches("global_cooldown")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.GLOBAL_COOLDOWN);
            }
            else if (arg.matches("saves") && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.SAVES);
            }
            else if (arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class));
            }
            // TODO: Reset NPCs option too!

            else {
                arg.reportUnhandled();
            }
        }

        // Use attached player if none is specified, and we're not resetting GLOBAL_COOLDOWN
        if (!scriptEntry.getObject("type").equals(Type.GLOBAL_COOLDOWN)) {
            scriptEntry.defaultObject("players", Utilities.getEntryPlayer(scriptEntry));
        }

        // Must specify a script unless resetting SAVES
        if (!scriptEntry.hasObject("script") && !scriptEntry.getObject("type").equals(Type.SAVES)) {
            throw new InvalidArgumentsException("Must specify a script!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // We allow players to be a single player or multiple players
        ObjectTag player = scriptEntry.getdObject("players");
        ListTag players;
        if (player instanceof PlayerTag) {
            players = new ListTag(player.identify());
        }
        else {
            players = scriptEntry.getdObject("players");
        }

        Type type = (Type) scriptEntry.getObject("type");
        ScriptTag script = scriptEntry.getdObject("script");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(),
                    (players != null ? players.debug() : "")
                            + ArgumentHelper.debugObj("type", type)
                            + (script != null ? script.debug() : ""));

        }

        // Deal with GLOBAL_COOLDOWN reset first, since there's no player/players involved
        if (type == Type.GLOBAL_COOLDOWN) {
            CooldownCommand.setCooldown(null, DurationTag.ZERO, script.getName(), true);
            return;
        }

        // Now deal with the rest
        for (String object : players) {

            PlayerTag resettable = PlayerTag.valueOf(object);
            if (resettable.isValid()) {

                switch (type) {
                    case PLAYER_COOLDOWN:
                        CooldownCommand.setCooldown(resettable, DurationTag.ZERO, script.getName(), false);
                        return;

                    case SAVES:
                        DenizenAPI.getCurrentInstance().getSaves().set("Players." + resettable.getSaveName(), null);
                }
            }

        }
    }
}

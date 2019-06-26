package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class ResetCommand extends AbstractCommand {

    private enum Type {PLAYER_COOLDOWN, GLOBAL_COOLDOWN, SAVES}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
            else if (arg.matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class));
            }
            // TODO: Reset NPCs option too!

            else {
                arg.reportUnhandled();
            }
        }

        // Use attached player if none is specified, and we're not resetting GLOBAL_COOLDOWN
        if (!scriptEntry.getObject("type").equals(Type.GLOBAL_COOLDOWN)) {
            scriptEntry.defaultObject("players", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
        }

        // Must specify a script unless resetting SAVES
        if (!scriptEntry.hasObject("script") && !scriptEntry.getObject("type").equals(Type.SAVES)) {
            throw new InvalidArgumentsException("Must specify a script!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // We allow players to be a single player or multiple players
        dObject player = scriptEntry.getdObject("players");
        dList players;
        if (player instanceof dPlayer) {
            players = new dList(player.identify());
        }
        else {
            players = scriptEntry.getdObject("players");
        }

        Type type = (Type) scriptEntry.getObject("type");
        dScript script = scriptEntry.getdObject("script");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (players != null ? players.debug() : "")
                            + aH.debugObj("type", type)
                            + (script != null ? script.debug() : ""));

        }

        // Deal with GLOBAL_COOLDOWN reset first, since there's no player/players involved
        if (type == Type.GLOBAL_COOLDOWN) {
            CooldownCommand.setCooldown(null, Duration.ZERO, script.getName(), true);
            return;
        }

        // Now deal with the rest
        for (String object : players) {

            dPlayer resettable = dPlayer.valueOf(object);
            if (resettable.isValid()) {

                switch (type) {
                    case PLAYER_COOLDOWN:
                        CooldownCommand.setCooldown(resettable, Duration.ZERO, script.getName(), false);
                        return;

                    case SAVES:
                        DenizenAPI.getCurrentInstance().getSaves().set("Players." + resettable.getSaveName(), null);
                }
            }

        }
    }
}

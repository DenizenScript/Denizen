package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ResetCommand extends AbstractCommand {

    private enum Type { FINISH, FAIL, PLAYER_COOLDOWN, GLOBAL_COOLDOWN }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dScript script = scriptEntry.getScript();
        Type type = null;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesArg("finishes, finished, finish", arg))
                type = Type.FINISH;

            else if (aH.matchesArg("fail, fails, failed", arg))
                type = Type.FAIL;

            else if (aH.matchesArg("cooldown", arg))
                type = Type.PLAYER_COOLDOWN;

            else if (aH.matchesArg("global_cooldown", arg))
                type = Type.GLOBAL_COOLDOWN;

            else if (aH.matchesScript(arg))
                script = aH.getScriptFrom(arg);

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (type == null)
            throw new InvalidArgumentsException("Must specify a type! Valid: FAILS, FINISHES, COOLDOWN, GLOBAL_COOLDOWN");

        if (scriptEntry.getPlayer() == null && type != Type.GLOBAL_COOLDOWN)
            throw new InvalidArgumentsException(dB.Messages.ERROR_NO_PLAYER);

        scriptEntry.addObject("script", script)
                .addObject("type", type);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Type type = (Type) scriptEntry.getObject("type");
        dScript script = (dScript) scriptEntry.getObject("script");

        switch (type) {
            case FAIL:
                FailCommand.resetFails(scriptEntry.getPlayer().getName(), script.getName());
                return;

            case FINISH:
                FinishCommand.resetFinishes(scriptEntry.getPlayer().getName(), script.getName());
                return;

            case PLAYER_COOLDOWN:
                CooldownCommand.setCooldown(scriptEntry.getPlayer().getName(), 0, script.getName(), false);
                return;

            case GLOBAL_COOLDOWN:
                CooldownCommand.setCooldown(scriptEntry.getPlayer().getName(), 0, script.getName(), true);
                return;
        }

    }

}
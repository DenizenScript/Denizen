package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;

public class ResetCommand extends AbstractCommand {

    private enum Type { FINISH, FAIL, PLAYER_COOLDOWN, GLOBAL_COOLDOWN }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // TODO: UPDATE THIS COMMAND!

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("finishes, finished, finish")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", Type.FINISH);

            if (arg.matches("fails, failed, fail")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", Type.FAIL);

            if (arg.matches("cooldown")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", Type.PLAYER_COOLDOWN);

            if (arg.matches("global_cooldown")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", Type.GLOBAL_COOLDOWN);

            else throw new InvalidArgumentsException("Unknown argument '" + arg + "'!");
        }

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
                CooldownCommand.setCooldown(scriptEntry.getPlayer().getName(), Duration.ZERO, script.getName(), false);
                return;

            case GLOBAL_COOLDOWN:
                CooldownCommand.setCooldown(scriptEntry.getPlayer().getName(), Duration.ZERO, script.getName(), true);
                return;
        }

    }

}

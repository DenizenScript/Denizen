package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftLivingEntity;

public class HealthCommand extends AbstractCommand {

    enum Action { TRUE, FALSE, TOGGLE, SET_MAX }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = null;
        Integer qty = null;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesToggle(arg))
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            else if (aH.matchesValueArg("SET_MAX", arg, ArgumentType.Integer)) {
                if (action == null) action = Action.SET_MAX;
                qty = aH.getIntegerFrom(arg);
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (action == null || scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("Missing Action or NPC!");

        scriptEntry.addObject("action", action)
                .addObject("qty", qty);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Action action = (Action) scriptEntry.getObject("action");
        Integer qty = (Integer) scriptEntry.getObject("qty");

        dB.report(getName(),
                aH.debugObj("Action", action.name())
                        + (qty != null ? aH.debugObj("health", qty.toString()) : "" ));

        switch (action) {

            case TRUE:
                scriptEntry.getNPC().getCitizen().addTrait(HealthTrait.class);
                break;

            case FALSE:
                scriptEntry.getNPC().getCitizen().removeTrait(HealthTrait.class);
                break;

            case TOGGLE:
                if (scriptEntry.getNPC().getCitizen().hasTrait(HealthTrait.class))
                    scriptEntry.getNPC().getCitizen().removeTrait(HealthTrait.class);
                else scriptEntry.getNPC().getCitizen().addTrait(HealthTrait.class);
                break;
        }

        if (scriptEntry.getNPC().getCitizen().hasTrait(HealthTrait.class)
                && qty != null)
            scriptEntry.getNPC().getHealthTrait().setMaxhealth(qty);

    }

}

package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.InvisibleTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class InvisibleCommand extends AbstractCommand {

    enum Action { TRUE, FALSE, TOGGLE }
    enum Target { PLAYER, NPC }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesToggle(arg))
                scriptEntry.addObject("toggle", Action.valueOf(aH.getStringFrom(arg).toUpperCase()));

            else if (aH.matchesArg("NPC, PLAYER", arg))
                scriptEntry.addObject("target", Target.valueOf(aH.getStringFrom(arg).toUpperCase()));

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (scriptEntry.getObject("toggle") == null)
            throw new InvalidArgumentsException("Must specify a toggle action!");

        if (scriptEntry.getObject("target") == null)
            throw new InvalidArgumentsException("Must specify a target!");

        if ((scriptEntry.getObject("target") == Target.NPC && scriptEntry.getNPC() == null)
                || (scriptEntry.getObject("target") == Target.PLAYER && scriptEntry.getPlayer() == null))
            throw new InvalidArgumentsException("NPC not found!");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("toggle");
        Target target = (Target) scriptEntry.getObject("target");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Toggle", action.name())
                        + aH.debugObj("Target", target == Target.NPC ? scriptEntry.getNPC().toString() :
                        scriptEntry.getPlayer().getName()));

        switch (target) {

            case NPC:
                if (!scriptEntry.getNPC().getCitizen().hasTrait(InvisibleTrait.class))
                    scriptEntry.getNPC().getCitizen().addTrait(InvisibleTrait.class);
                InvisibleTrait trait = scriptEntry.getNPC().getCitizen().getTrait(InvisibleTrait.class);

                switch (action) {

                    case FALSE:
                        trait.setInvisible(false);
                        break;

                    case TRUE:
                        trait.setInvisible(true);
                        break;

                    case TOGGLE:
                        trait.toggle();
                        break;
                }

                break;

            case PLAYER:

                // TODO
        }

    }
}
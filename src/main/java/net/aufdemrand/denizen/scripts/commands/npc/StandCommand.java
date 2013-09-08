package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

public class StandCommand extends AbstractCommand{

    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {
        //stand should have no additional arguments
        for (String arg: scriptEntry.getArguments())
            throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);

    }

    @Override
    public void execute(ScriptEntry scriptEntry)
            throws CommandExecutionException {

        NPC npc = scriptEntry.getNPC().getCitizen();
        SittingTrait trait = npc.getTrait(SittingTrait.class);

        if (!npc.hasTrait(SittingTrait.class)){
            npc.addTrait(SittingTrait.class);
            dB.echoDebug("...added sitting trait");
        }

        if (!trait.isSitting()) {
            dB.echoError("...NPC is already standing, removing trait");
            npc.removeTrait(SittingTrait.class);
            return;
        }

        trait.stand();
        npc.removeTrait(SittingTrait.class);

    }

}

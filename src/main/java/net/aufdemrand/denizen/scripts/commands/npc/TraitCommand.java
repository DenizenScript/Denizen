package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public class TraitCommand extends AbstractCommand {

    private enum Toggle { TOGGLE, TRUE, FALSE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            
            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state, s")
                    && arg.matchesEnum(Toggle.values()))
                scriptEntry.addObject("state", Toggle.valueOf(arg.getValue()));
            
            else if (!scriptEntry.hasObject("trait"))
                scriptEntry.addObject("trait", new Element(arg.getValue()));
            
        }
        
        if (!scriptEntry.hasObject("trait"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "TRAIT");
        
        if (!scriptEntry.hasNPC())
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        
        scriptEntry.defaultObject("state", Toggle.TOGGLE);
        
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Toggle toggle = (Toggle) scriptEntry.getObject("state");
        Element traitName = scriptEntry.getElement("trait");
        NPC npc = scriptEntry.getNPC().getCitizen();
        
        dB.echoApproval("Executing '" + getName() + "': "
                + "Trait='" + traitName.debug() + "', "
                + "Toggle='" + toggle.toString() + "', "
                + "NPC='" + scriptEntry.getNPC().identify() + "'");
        
        Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(traitName.asString());
        
        if (trait == null)
            throw new CommandExecutionException("Trait not found: " + traitName.asString());
        
        switch (toggle) {
        
            case TRUE:
                if (npc.hasTrait(trait))
                    dB.echoDebug("NPC already has trait '" + traitName.asString() + "'");
                else
                    npc.addTrait(trait);
                break;
                
            case FALSE:
                if (!npc.hasTrait(trait))
                    dB.echoDebug("NPC does not have trait '" + traitName.asString() + "'");
                else
                    npc.removeTrait(trait);
                break;
                
            case TOGGLE:
                if (npc.hasTrait(trait))
                    npc.removeTrait(trait);
                else
                    npc.addTrait(trait);
                break;
            
        }
        
    }

}

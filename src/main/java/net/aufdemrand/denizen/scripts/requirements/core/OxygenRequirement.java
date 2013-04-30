package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;


public class OxygenRequirement extends AbstractRequirement{

    public enum Range { BELOW, EQUALS, ABOVE }
    
    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        // Make sure player isn't null and then check oxygen.
        if (context.getPlayer() == null) return false;
        
        if(args.size() < 1) throw new RequirementCheckException("Must specify amount/quantity!");
        
        Range range = Range.BELOW;
        int val = context.getPlayer().getMaximumAir();
        
        for(String arg : args) {
            if(aH.matchesValueArg("range", arg, aH.ArgumentType.String)) {
                try {
                    range = Range.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug("Set range to " + range.name() + "!");
                } catch(Exception e) {
                    dB.echoError("Invalid range: " + e.getMessage());
                }
            } else if(aH.matchesQuantity(arg) || aH.matchesArg("amt", arg)) {
                val = aH.getIntegerFrom(arg);
                dB.echoDebug("Amount set to " + val);
            }
        }
        
        int oxygen = context.getPlayer().getRemainingAir();
        
        switch(range) {
            case BELOW:
                return oxygen < val;
                
            case EQUALS:
                return oxygen == val;
                
            case ABOVE:
                return oxygen > val;
        }
        
		return true;
	}
}

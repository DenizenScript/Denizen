package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;

import java.util.List;

public class TimeRequirement extends AbstractRequirement{
	
	private enum TIME {DAWN, DAY, DUSK, NIGHT }
	
	@Override
	public void onEnable() {
		//nothing to do here
	}

	TIME time;

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		
		boolean outcome = false;
		
		for (String thisArg : args){
			
			if (aH.matchesArg("DAWN, DAY, DUSK, NIGHT", thisArg)) time = TIME.valueOf(thisArg);
			
		}
		/* IM LOST
			if (!Character.isDigit(theTime.charAt(0))) {
				if (theTime.equalsIgnoreCase("DAWN")
						&& theWorld.getTime() > 23000) outcome = true;

				else if (theTime.equalsIgnoreCase("DAY")
						&& theWorld.getTime() > 0
						&& theWorld.getTime() < 13500) outcome = true;

				else if (theTime.equalsIgnoreCase("DUSK")
						&& theWorld.getTime() > 12500
						&& theWorld.getTime() < 13500) outcome = true;

				else if (theTime.equalsIgnoreCase("NIGHT")
						&& theWorld.getTime() > 13500) outcome = true;
			}

			else if (Character.isDigit(theTime.charAt(0))) 
				if (theWorld.getTime() > Long.valueOf(theTime)
						&& theWorld.getTime() < Long.valueOf(highTime)) outcome = true;
		*/
		return outcome;
	}

}

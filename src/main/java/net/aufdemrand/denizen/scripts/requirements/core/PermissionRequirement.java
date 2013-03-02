package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.World;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

public class PermissionRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {		
		if(context.getPlayer() != null)
		{		
			if(Depends.permissions != null)
			{
				boolean outcome = false;
				boolean global = false;
				
				for(String arg : args)
				{
					if (aH.matchesArg("GLOBAL", arg))
						global = true;
					else
					{
						if (global == true)
						{
							if(Depends.permissions.has((World) null, context.getPlayer().getName(), arg))
							{
								dB.echoDebug("...player has global permission: " + arg);
								outcome = true;
							}
							else
								dB.echoDebug("...player does not have global permission: " + arg);
						}
						else
						{
							if(Depends.permissions.has(context.getPlayer(), arg))
							{
								dB.echoDebug("...player has permission: " + arg);
								outcome = true;
							}
							else
							{
								dB.echoDebug("...player does not have permission: " + arg + "!");
							}
						}
					}
				}
				
				return outcome;
			}
			
			dB.echoDebug("...no permission plugin found, assume as FALSE!");
		}
		
		return false;
	}
}

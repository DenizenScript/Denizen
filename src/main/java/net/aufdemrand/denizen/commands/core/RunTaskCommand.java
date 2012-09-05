package net.aufdemrand.denizen.commands.core;

import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.citizensnpcs.command.exception.CommandException;


public class RunTaskCommand extends AbstractCommand {

	/* RUNTASK [SCRIPT:ScriptName] (DELAY:#) (NPCID:#) 
	 /* CANCELTASK [SCRIPT:ScriptName] 
	 */

	/* Arguments: [] - Required, () - Optional 
	 * (Step #) The step to make the current step. If not specified, assumes current step + 1. 
	 * 
	 * Modifiers: 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		String theScriptName = null;
		int secs = -1;
		DenizenNPC executer = theEntry.getDenizen();

		/* Get arguments */
		if (theEntry.arguments() == null) {
			aH.echoDebug("...No arguments!");
			return false;
		}

		for (String thisArg : theEntry.arguments()){

			/* Change the script to a specified one */
			if (aH.matchesScript(thisArg)) {
				theScriptName = aH.getStringModifier(thisArg);
				aH.echoDebug("...running task script '" + theScriptName + "'.");	
			}

			/* Set a duration */
			else if (thisArg.toUpperCase().contains("DELAY:")){
				secs = aH.getIntegerModifier(thisArg);
			}

			else if (aH.matchesNPCID(thisArg)){
				executer = aH.getNPCIDModifier(thisArg);
			}

			else aH.echoError("Could not match argument '%s'!", thisArg);
		}

		if (theScriptName == null){
			aH.echoDebug("...No task specified!");
			return false;
		}

		if (theEntry.getCommand().equalsIgnoreCase("CANCELTASK")){
			if (theEntry.getPlayer().hasMetadata(theScriptName)){
				int tid = theEntry.getPlayer().getMetadata(theScriptName).get(0).asInt();
				if (tid > 0) {
					plugin.getServer().getScheduler().cancelTask(tid);
					theEntry.getPlayer().removeMetadata(theScriptName, plugin);
				}		
			}
			else
			{
				aH.echoDebug("...Pending task " + theScriptName + " not found for this player");
				return false;
			}
			return true;
		}

		if (executer ==null){
			aH.echoDebug("...Invalid NPC specified!");
			return false;
		}

		/* Make delayed task to reset step if duration is set */
		if (secs>0) {

			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
					new ZapCommandRunnable<Player, String, DenizenNPC, Integer>(theEntry.getPlayer(), theScriptName, executer, 0) {

				@Override
				public void run(Player player, String theScriptName, DenizenNPC denizen, Integer oldStep) { 
					if (player.hasMetadata(theScriptName)){
						int tid = player.getMetadata(theScriptName).get(0).asInt();
						if (tid > 0) {
							plugin.getServer().getScheduler().cancelTask(tid);
							player.removeMetadata(theScriptName, plugin);
						}	
					}

					ScriptHelper sE = plugin.getScriptEngine().helper;
					List<String> theScript = sE.getScript(theScriptName + ".Script");
					if (theScript.isEmpty()) return;
					sE.queueScriptEntries(player, sE.buildScriptEntries(player, denizen, theScript, theScriptName, 1), QueueType.TASK);		
					player.removeMetadata(theScriptName, plugin);
				}		
			}, secs * 20);

			theEntry.getPlayer().setMetadata(theScriptName,new org.bukkit.metadata.FixedMetadataValue(plugin,id));

			return true;
		}

		ScriptHelper sE = plugin.getScriptEngine().helper;
		List<String> theScript = sE.getScript(theScriptName + ".Script");
		if (theScript.isEmpty()) return false;
		sE.queueScriptEntries(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), executer, theScript, theScriptName, 1), QueueType.TASK);	


		return true;
	}

}



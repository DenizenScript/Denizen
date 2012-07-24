package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.commands.core.EngageCommand;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {

	
	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {

		/* Shortcut to the ScriptHelper */
		ScriptHelper sE = plugin.scriptEngine.helper;
		
		/* Show NPC info if sneaking and right clicking */
		if (event.getClicker().isSneaking() 
				&& event.getClicker().isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) 
			sE.showInfo(event.getClicker(), event.getNPC());

		/* Check if ... 1) isDenizen is true, 2) clickTrigger enabled, 3) is cooled down, 4) is not engaged */
		if (sE.denizenIsInteractable(triggerName, event.getNPC(), event.getClicker())) {
		
			/* Apply default cooldown to avoid click-spam, then send to parser. */
			sE.setCooldown(event.getClicker(), ClickTrigger.class, plugin.settings.DefaultClickCooldown());
			parseClickTrigger(event.getNPC(), event.getClicker());
		}
	}
	
	
	
	/* Parses the script for a click trigger */

	public boolean parseClickTrigger(NPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.scriptEngine.helper;
		
		/* Get Interact Script, if any. */
		String theScriptName = sE.getInteractScript(theDenizen, thePlayer);
		
		if (theScriptName == null) failClickTrigger();
		
		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);
		
		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);
		
		/* Build scriptEntries from theScript */
		sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep);
	
		/* New ScriptCommand list built, now let's add it into the queue */
		List<ScriptEntry> scriptCommandList = new ArrayList<ScriptEntry>();
		if (triggerQue.containsKey(thePlayer))
			scriptCommandList.addAll(triggerQue.get(thePlayer));

		/* Keeps the commandQue from removing items while
		working on them here. They will be added back in. */ 
		triggerQue.remove(thePlayer); 

		if (!scriptCommands.isEmpty())
			scriptCommandList.addAll(scriptCommands);
		else
			if (plugin.debugMode) plugin.getLogger().log(Level.SEVERE, "No items in the script to add!");

		triggerQue.put(thePlayer, scriptCommandList);

		return true;
	}
	
}

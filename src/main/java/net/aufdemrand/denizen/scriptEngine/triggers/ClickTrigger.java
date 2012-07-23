package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.aufdemrand.denizen.scriptEngine.Trigger;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends Trigger implements Listener {

	
	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {

		/* Show NPC info if sneaking and right clicking */
		if (event.getClicker().isSneaking() 
				&& event.getClicker().isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) 
			plugin.scriptEngine.helper.showInfo(event.getClicker(), event.getNPC());

		/* Check if ... 1) isDenizen is true, 2) clickTrigger enabled, 3) is cooled down, 4) is not engaged */
		if (event.getNPC().getTrait(DenizenTrait.class).isDenizen
				&& event.getNPC().getTrait(DenizenTrait.class).enableClickTriggers
				&& plugin.scriptEngine.checkCooldown(event.getClicker(), ClickTrigger.class)
				&& !plugin.scriptEngine.getEngaged(event.getNPC())) {

			/* Apply default cooldown to avoid click-spam, then send to parser. */
			plugin.scriptEngine.setCooldown(event.getClicker(), ClickTrigger.class, plugin.settings.DefaultClickCooldown());
			plugin.scriptEngine.parseClickTrigger(event.getNPC(), event.getClicker());
		}
	}
	
	
	
	/* Parses the script for a click trigger */

	public boolean parseClickScript(NPC theDenizen, Player thePlayer, String theScript) {

		int theStep = getCurrentStep(thePlayer, theScript);
		List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		/* Let's get the Script from the file and turn it into ScriptCommands */
		List<String> chatScriptItems = plugin.getScripts().getStringList(theScript + ".Steps." + theStep + ".Click Trigger.Script");
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Parsing: " + theScript + ".Steps." + theStep + ".Click Trigger.Script");
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Number of items to parse: " + chatScriptItems.size());

		for (String thisItem : chatScriptItems) {
			String[] scriptEntry = new String[2];
			if (thisItem.split(" ", 2).length == 1) {
				scriptEntry[0] = thisItem;
				scriptEntry[1] = null;
			} else {
				scriptEntry = thisItem.split(" ", 2);
			}

			try {
				/* Build new script commands */
				scriptCommands.add(new ScriptEntry(scriptEntry[0], buildArgs(scriptEntry[1]), thePlayer, theDenizen, theScript, theStep));
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Building ScriptCommand with " + thisItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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

package net.aufdemrand.denizen.triggers.core;

import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {


	@EventHandler(priority = EventPriority.HIGHEST)
	public void clickTrigger(NPCRightClickEvent event) {

		if (!plugin.getDenizenNPCRegistry().isDenizenNPC(event.getNPC()))
			return;

		/* Shortcut to the ScriptHelper */
		ScriptHelper sE = plugin.getScriptEngine().helper;

		DenizenNPC theDenizen = plugin.getDenizenNPCRegistry().getDenizen(event.getNPC());

		/* Show NPC info if sneaking and right clicking */
		if (event.getClicker().isSneaking() 
				&& event.getClicker().isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) { 
			theDenizen.showInfo(event.getClicker());
			return;
		}

		if (!theDenizen.hasTrigger(triggerName)) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...click trigger not enabled for this Denizen.");
			return;
		}
		
		// If Denizen is not interactable (ie. Denizen is toggled off, engaged or not cooled down)
		if (!theDenizen.IsInteractable(triggerName, event.getClicker())) {
			theDenizen.talk(TalkType.CHAT_PLAYERONLY, event.getClicker(), Reason.DenizenIsUnavailable);
			return;
		}
		
		// Cool! Parse the Trigger...
		// Apply default cool-down to avoid click-spam, then send to parser. */
		sE.setCooldown(theDenizen, ClickTrigger.class, plugin.settings.DefaultClickCooldown());
		
		if (!parseClickTrigger(theDenizen, event.getClicker())) {
			theDenizen.talk(TalkType.CHAT_PLAYERONLY, event.getClicker(), Reason.NoMatchingClickTrigger);
			return;
		}
		
		// Success!
	}



	/* Parses the script for a click trigger */

	public boolean parseClickTrigger(DenizenNPC theDenizen, Player thePlayer) {

		CommandSender cs = Bukkit.getConsoleSender();
		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Check for Quick Click Script */
		if (!plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Quick Scripts.Click")) {

				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing QUICK CLICK script: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
				
				/* Get the contents of the Script. */
				List<String> theScript = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Quick Scripts.Click");

				if (theScript.isEmpty()) return false;

				/* Build scriptEntries from theScript and add it into the queue */
				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, "Quick Click", 1), QueueType.TASK);
				
				return true;
				
			}
		}
		
		/* Get Interact Script, if any. */
		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());

		if (theScriptName == null) return false;

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing click trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");
		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);

		if (theScript.isEmpty()) return false;

		/* Build scriptEntries from theScript and add it into the queue */
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}



}

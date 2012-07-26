package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;

import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {


	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {

		/* Shortcut to the ScriptHelper */
		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (plugin.getDenizenNPCRegistry().isDenizenNPC(event.getNPC())) {
			DenizenNPC denizenNPC = plugin.getDenizenNPCRegistry().getDenizen(event.getNPC());

			/* Show NPC info if sneaking and right clicking */
			if (event.getClicker().isSneaking() 
					&& event.getClicker().isOp()
					&& plugin.settings.RightClickAndSneakInfoModeEnabled()) 
				denizenNPC.showInfo(event.getClicker());

			/* Check if this NPC is a Denizen and is interact-able */
			if (denizenNPC.IsInteractable(triggerName, event.getClicker())) {

				/* Apply default cool-down to avoid click-spam, then send to parser. */
				sE.setCooldown(event.getClicker(), ClickTrigger.class, plugin.settings.DefaultClickCooldown());
				if (!parseClickTrigger(denizenNPC, event.getClicker())) {
					denizenNPC.talk(TalkType.Chat, event.getClicker(), Reason.NoRequirementsMet);
				}
			}
		}
	}



	/* Parses the script for a click trigger */

	public boolean parseClickTrigger(DenizenNPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Parsing Click Trigger.");
		
		/* Get Interact Script, if any. */
		String theScriptName = theDenizen.getInteractScript(thePlayer);

		if (theScriptName == null) return false;

		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);

		/* Build scriptEntries from theScript and add it into the queue */
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}



}

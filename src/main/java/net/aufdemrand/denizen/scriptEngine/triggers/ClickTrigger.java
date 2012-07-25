package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.List;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.SpeechEngine.Reason;
import net.aufdemrand.denizen.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {


	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {

		/* Shortcut to the ScriptHelper */
		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Show NPC info if sneaking and right clicking */
		if (event.getClicker().isSneaking() 
				&& event.getClicker().isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) 
			sE.showInfo(event.getClicker(), event.getNPC());

		/* Check if this NPC is a Denizen and is interact-able */
		if (sE.denizenIsInteractable(triggerName, event.getNPC(), event.getClicker())) {

			/* Apply default cool-down to avoid click-spam, then send to parser. */
			sE.setCooldown(event.getClicker(), ClickTrigger.class, plugin.settings.DefaultClickCooldown());
			if (!parseClickTrigger(event.getNPC(), event.getClicker())) {
				event.getNPC().getTrait(DenizenTrait.class).talk(TalkType.Chat, event.getClicker(), Reason.NoRequirementsMet);
			}
		}
	}



	/* Parses the script for a click trigger */

	public boolean parseClickTrigger(NPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Get Interact Script, if any. */
		String theScriptName = sE.getInteractScript(theDenizen, thePlayer);

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

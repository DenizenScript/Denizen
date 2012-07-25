package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.List;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.SpeechEngine.Reason;
import net.aufdemrand.denizen.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DamageTrigger extends AbstractTrigger implements Listener {

	/* Damage Trigger event. Virtually identical to the Click Trigger, for comments, see ClickTrigger.java */
	
	@EventHandler
	public void damageTrigger(NPCLeftClickEvent event) {

		ScriptHelper sE = plugin.getScriptEngine().helper;

		if (sE.denizenIsInteractable(triggerName, event.getNPC(), event.getClicker())) {
			sE.setCooldown(event.getClicker(), DamageTrigger.class, plugin.settings.DefaultDamageCooldown());
			if (!parseDamageTrigger(event.getNPC(), event.getClicker())) {
				event.getNPC().getTrait(DenizenTrait.class).talk(TalkType.Chat, event.getClicker(), Reason.NoRequirementsMet);
			}
		}

		else if (plugin.settings.DisabledDamageTriggerInsteadTriggersClick()) {
			plugin.getTriggerRegistry().getTrigger(ClickTrigger.class).clickTrigger(new NPCRightClickEvent(event.getNPC(), event.getClicker()));
		}
	}

	
	/* Parses the Damage Trigger */

	public boolean parseDamageTrigger(NPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		String theScriptName = sE.getInteractScript(theDenizen, thePlayer);
		if (theScriptName == null) return false;

		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}


	
	
}

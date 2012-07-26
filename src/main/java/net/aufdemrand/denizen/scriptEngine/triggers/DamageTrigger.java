package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DamageTrigger extends AbstractTrigger implements Listener {

	/* Damage Trigger event. Virtually identical to the Click Trigger, for comments, see ClickTrigger.java */

	@EventHandler
	public void damageTrigger(NPCLeftClickEvent event) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (plugin.getDenizenNPCRegistry().isDenizenNPC(event.getNPC())) {
			DenizenNPC denizenNPC = plugin.getDenizenNPCRegistry().getDenizen(event.getNPC());

			if (denizenNPC.IsInteractable(triggerName, event.getClicker())) {
				sE.setCooldown(event.getClicker(), DamageTrigger.class, plugin.settings.DefaultDamageCooldown());
				if (!parseDamageTrigger(denizenNPC, event.getClicker())) {
					denizenNPC.talk(TalkType.Chat, event.getClicker(), Reason.NoRequirementsMet);
				}
			}

			else if (plugin.settings.DisabledDamageTriggerInsteadTriggersClick()) {
				plugin.getTriggerRegistry().getTrigger(ClickTrigger.class).clickTrigger(new NPCRightClickEvent(event.getNPC(), event.getClicker()));
			}
		}
	}


	/* Parses the Damage Trigger */

	public boolean parseDamageTrigger(DenizenNPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Parsing Damage Trigger.");
		
		String theScriptName = theDenizen.getInteractScript(thePlayer);
		if (theScriptName == null) return false;

		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}




}

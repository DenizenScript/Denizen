package net.aufdemrand.denizen.triggers.core;

import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DamageTrigger extends AbstractTrigger implements Listener {

	/* Damage Trigger event. Virtually identical to the Click Trigger, for comments, see ClickTrigger.java */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageTrigger(NPCLeftClickEvent event) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (plugin.getDenizenNPCRegistry().isDenizenNPC(event.getNPC())) {
			DenizenNPC denizenNPC = plugin.getDenizenNPCRegistry().getDenizen(event.getNPC());

			/* Check if trigger is enabled, because if not, send click trigger (if enabled in settings) */
			if (denizenNPC.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled(triggerName.toUpperCase())) {

				if (denizenNPC.IsInteractable(triggerName, event.getClicker())) {
					sE.setCooldown(denizenNPC, DamageTrigger.class, plugin.settings.DefaultDamageCooldown());
					if (!parseDamageTrigger(denizenNPC, event.getClicker())) {
						denizenNPC.talk(TalkType.CHAT, event.getClicker(), Reason.NoMatchingDamageTrigger);
					}
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
		CommandSender cs = Bukkit.getConsoleSender();
		
		// Play the HURT effect.
		theDenizen.getEntity().playEffect(EntityEffect.HURT);

		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());
		if (theScriptName == null) return false;

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing damage trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");
		
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);
		
		if (theScript.isEmpty()) return false;
		
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}




}

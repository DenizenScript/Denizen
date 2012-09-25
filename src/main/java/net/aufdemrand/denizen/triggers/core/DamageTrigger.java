package net.aufdemrand.denizen.triggers.core;

import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageTrigger extends AbstractTrigger implements Listener {

	/* Damage Trigger event. Virtually identical to the Click Trigger, for comments, see ClickTrigger.java */

	@EventHandler(priority = EventPriority.LOWEST)
	public void damageTrigger(EntityDamageByEntityEvent event) {

		Player thePlayer = null;
		
		if (event.getDamager() instanceof Player) thePlayer = (Player) event.getDamager();
		if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) thePlayer = (Player) ((Projectile)event.getDamager()).getShooter();
		
		if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity()) && thePlayer != null) {
			NPC theNPC = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());

			if (plugin.getDenizenNPCRegistry().isDenizenNPC(theNPC)) {

				ScriptHelper sE = plugin.getScriptEngine().helper;

					DenizenNPC denizenNPC = plugin.getDenizenNPCRegistry().getDenizen(theNPC);

					/* Check if trigger is enabled, because if not, send click trigger (if enabled in settings) */
					if (denizenNPC.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled(triggerName.toUpperCase())) {

						if (denizenNPC.isInteractable(triggerName, thePlayer)) {
							sE.setCooldown(denizenNPC, DamageTrigger.class, plugin.settings.DefaultDamageCooldown());
							if (!parseDamageTrigger(denizenNPC, thePlayer)) {
								denizenNPC.talk(TalkType.CHAT, thePlayer, Reason.NoMatchingDamageTrigger);
							}
						}
					}

					else if (plugin.settings.DisabledDamageTriggerInsteadTriggersClick()) {
						plugin.getTriggerRegistry().getTrigger(ClickTrigger.class).clickTrigger(new NPCRightClickEvent(theNPC, thePlayer));
					}
				}
			}
		}
	


	/* Parses the Damage Trigger */
	CommandSender cs;

	public boolean parseDamageTrigger(DenizenNPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (cs == null) cs = Bukkit.getConsoleSender();

		// Play the HURT effect.
		theDenizen.getEntity().playEffect(EntityEffect.HURT);

		/* Get Interact Script, if any. */
		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());

		if (theScriptName == null) {

			// Check for Quick Script
			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Quick Scripts.Damage Trigger.Script")) {

				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing QUICK DAMAGE script: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");

				/* Get the contents of the Script. */
				List<String> theScript = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Quick Scripts.Damage Trigger.Script");

				if (theScript.isEmpty()) return false;

				/* Build scriptEntries from theScript and add it into the queue */
				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theDenizen.getName() + " Quick Damage", 1), QueueType.TASK);

				return true;
			}
		}

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing damage trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");

		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);

		if (theScript.isEmpty()) return false;

		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}




}

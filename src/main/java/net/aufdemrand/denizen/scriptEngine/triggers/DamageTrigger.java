package net.aufdemrand.denizen.scriptEngine.triggers;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DamageTrigger extends AbstractTrigger implements Listener {

	@EventHandler
	public void damageTrigger(NPCLeftClickEvent event) {

		/* Check if ... 1) isDenizen is true, 2) damageTrigger enabled, 3) is cooled down, 4) is not engaged 
		 * Special condition, as part of the design, if damageTrigger is disabled, this may trigger the
		 * click trigger if the config setting disabled_damage_trigger_instead_triggers_click is true */
		if (event.getNPC().getTrait(DenizenTrait.class).isDenizen
				&& event.getNPC().getTrait(DenizenTrait.class).enableDamageTriggers
				&& plugin.scriptEngine.checkCooldown(event.getClicker(), DamageTrigger.class)
				&& !plugin.scriptEngine.getEngaged(event.getNPC())) {

			/* Apply default cooldown to avoid click-spam, then send to parser. */
			plugin.scriptEngine.setCooldown(event.getClicker(), DamageTrigger.class, plugin.settings.DefaultDamageCooldown());
			plugin.scriptEngine.parseDamageTrigger(event.getNPC(), event.getClicker());
		}

		else if (plugin.settings.DisabledDamageTriggerInsteadTriggersClick()) {
			((ClickTrigger) plugin.triggerRegistry.getTrigger("Click")).clickTrigger(new NPCRightClickEvent(event.getNPC(), event.getClicker()));
		}
	}


}

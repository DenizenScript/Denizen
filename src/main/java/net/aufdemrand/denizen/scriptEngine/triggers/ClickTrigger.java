package net.aufdemrand.denizen.scriptEngine.triggers;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.Trigger;
import net.citizensnpcs.api.event.NPCRightClickEvent;

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
	
	
}

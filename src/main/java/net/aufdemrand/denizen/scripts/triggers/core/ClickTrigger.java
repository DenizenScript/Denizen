package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;

import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {
    
    @EventHandler
    public void clickTrigger(NPCRightClickEvent event) {
        // Check if NPC has triggers.
        if (!event.getNPC().hasTrait(TriggerTrait.class)) return;
        // Check if trigger is enabled.
        if (!event.getNPC().getTrait(TriggerTrait.class).isEnabled(name)) return;

        // If engaged or not cool, calls On Unavailable, if cool, calls On Click
        // If available (not engaged, and cool) sets cool down and returns true. 
        if (!event.getNPC().getTrait(TriggerTrait.class).trigger(this, event.getClicker())) return;

        // Get Interact Script for Player/NPC
        String script = sH.getInteractScript(event.getNPC(), event.getClicker(), this.getClass());

        // Parse Click Trigger, if unable to parse call No Click Trigger action
        if (!parse(denizen.getNPCRegistry().getDenizen(event.getNPC()), event.getClicker(), script))
            denizen.getNPCRegistry().getDenizen(event.getNPC()).action("no click trigger", event.getClicker());
    }

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

}

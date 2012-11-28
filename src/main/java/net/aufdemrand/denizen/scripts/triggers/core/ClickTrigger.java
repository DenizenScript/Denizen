package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.debugging.Debugger.DebugElement;

import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickTrigger extends AbstractTrigger implements Listener {

    @EventHandler
    public void clickTrigger(NPCRightClickEvent event) {
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
    public boolean parse(DenizenNPC npc, Player player, String script) {
        if (script == null) return false;

        dB.echoDebug(DebugElement.Header, "Parsing click trigger: " + npc.getName() + "/" + player.getName() + " -+");

        dB.echoDebug("Getting current step:");
        Integer theStep = sH.getCurrentStep(player, script);

        // Gets entries from the script
        List<String> theScript = sH.getScriptContents(sH.getKeys(script, theStep, name) + sH.scriptKey);

        // Build scriptEntries from the script and queue them up
        sB.queueScriptEntries(player, sB.buildScriptEntries(player, npc, theScript, script, theStep), QueueType.PLAYER);

        return true;
    }

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

}

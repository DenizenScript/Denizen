package com.denizenscript.denizen.scripts.triggers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class ClickTrigger extends AbstractTrigger implements Listener {

    // <--[language]
    // @name Click Triggers
    // @group NPC Interact Scripts
    // @description
    // Click Triggers are triggered when a player right clicks the NPC.
    //
    // These are very basic with no extraneous complexity.
    //
    // <code>
    // click trigger:
    //     script:
    //     - narrate "hi <player.name>"
    // </code>
    //
    // They can optionally have an item matcher with multiple triggers, for the item in the player's hand. For example:
    // <code>
    // click trigger:
    //     1:
    //         trigger: my_item_script
    //         script:
    //         - narrate "Nice item script"
    //     2:
    //         trigger: stone
    //         script:
    //         - narrate "Nice vanilla item"
    //     3:
    //         script:
    //         - narrate "I don't recognize your held item"
    // </code>
    //
    // -->

    // <--[action]
    // @Actions
    // no click trigger
    //
    // @Triggers when the NPC is clicked but no click trigger fires.
    //
    // @Context
    // None
    //
    // -->
    // Technically defined in TriggerTrait, but placing here instead.
    // <--[action]
    // @Actions
    // click
    //
    // @Triggers when the NPC is clicked by a player.
    //
    // @Context
    // None
    //
    // @Determine
    // "cancelled" to cancel the click event completely.
    //
    // -->
    @EventHandler
    public void clickTrigger(NPCRightClickEvent event) {
        if (!event.getNPC().hasTrait(TriggerTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(event.getNPC());
        if (!npc.getTriggerTrait().isEnabled(name)) {
            return;
        }
        TriggerTrait triggerTrait = npc.getTriggerTrait();
        double radius = triggerTrait.getRadius(name);
        if (radius > 0 && event.getClicker().getLocation().distanceSquared(npc.getLocation()) > radius * radius) {
            return;
        }
        PlayerTag player = PlayerTag.mirrorBukkitPlayer(event.getClicker());
        TriggerTrait.TriggerContext trigger = npc.getTriggerTrait().trigger(this, player);
        if (!trigger.wasTriggered()) {
            return;
        }
        if (trigger.hasDetermination() && trigger.getDeterminations().containsCaseInsensitive("cancelled")) {
            event.setCancelled(true);
            return;
        }
        List<InteractScriptContainer> scripts = npc.getInteractScripts(player, ClickTrigger.class);
        boolean any = false;
        if (scripts != null) {
            for (InteractScriptContainer script : scripts) {
                String id = null;
                Map<String, String> idMap = script.getIdMapFor(ClickTrigger.class, player);
                if (!idMap.isEmpty()) {
                    ItemTag heldItem = new ItemTag(player.getPlayerEntity().getEquipment().getItemInMainHand());
                    for (Map.Entry<String, String> entry : idMap.entrySet()) {
                        String entry_value = TagManager.tag(entry.getValue(), new BukkitTagContext(player, npc, null, false, null));
                        boolean isMatch = entry_value.isEmpty() || heldItem.tryAdvancedMatcher(entry_value);
                        if (script.shouldDebug()) {
                            Debug.echoDebug(script, "Comparing click trigger '<A>" + entry_value + "<W>' with item '<A>" + heldItem.debuggable() + "<W>': " + (isMatch ? "<GR>Match!" : "<Y>Not a match"));
                        }
                        if (isMatch) {
                            id = entry.getKey();
                            break;
                        }
                    }
                }
                if (parse(npc, player, script, id)) {
                    any = true;
                }
            }
        }
        if (!any) {
            npc.action("no click trigger", player);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
    }
}

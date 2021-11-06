package com.denizenscript.denizen.scripts.triggers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class ClickTrigger extends AbstractTrigger implements Listener {

    // <--[language]
    // @name Click Triggers
    // @group NPC Interact Scripts
    // @description
    // Click Triggers are triggered when when a player right clicks the NPC.
    //
    // These are very basic with no extraneous complexity.
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

        //
        // The next 3 'if's are generally recommended for any trigger:
        //

        // Check if NPC has triggers.. no use going any further if this NPC doesn't have
        // ANY triggers enabled!
        if (!event.getNPC().hasTrait(TriggerTrait.class)) {
            return;
        }

        // The rest of the methods beyond this point require a NPCTag object, which can easily be
        // obtained if a valid NPC object is available:
        NPCTag npc = new NPCTag(event.getNPC());

        // Now, check if the 'click trigger' specifically is enabled. 'name' is inherited from the
        // super AbstractTrigger and contains the name of the trigger that was use in registration.
        if (!npc.getTriggerTrait().isEnabled(name)) {
            return;
        }

        // We'll get the player too, since it makes reading the next few methods a bit easier:
        PlayerTag player = PlayerTag.mirrorBukkitPlayer(event.getClicker());

        // Check availability based on the NPC's ENGAGED status and the trigger's COOLDOWN that is
        // provided (and adjustable) by the TriggerTrait. Just use .trigger(...)!
        // If unavailable (engaged or not cool), .trigger calls 'On Unavailable' action and returns false.
        // If available (not engaged, and cool), .trigger sets cool down and returns true.
        TriggerTrait.TriggerContext trigger = npc.getTriggerTrait().trigger(this, player);

        if (!trigger.wasTriggered()) {
            return;
        }

        if (trigger.hasDetermination()
                && trigger.getDetermination().equalsIgnoreCase("cancelled")) {
            event.setCancelled(true);
            return;
        }

        // Note: In some cases, the automatic actions that .trigger offers may not be
        // desired. In this case, it's recommended to at least use .triggerCooldownOnly which
        // only handles cooling down the trigger with the triggertrait if the 'available' criteria
        // is met. This handles the built-in cooldown that TriggerTrait implements.

        // Okay, now we need to know which interact script will be selected for the Player/NPC
        // based on requirements/npc's assignment script. To get that information, use:
        // InteractScriptHelper.getInteractScript(dNPC, Player, Trigger Class)
        // .getInteractScript will check the Assignment for possible scripts, and automatically
        // check requirements for each of them.
        InteractScriptContainer script = npc.getInteractScript(player, getClass());

        // In an Interact Script, Triggers can have multiple scripts to choose from depending on
        // some kind of 'criteria'. For the 'Click Trigger', that criteria is the item the Player
        // has in hand. Let's get the possible criteria to see which 'Click Trigger script', if any,
        // should trigger. For example:
        //
        // Script Name:
        //   type: interact
        //   steps:
        //     current step:
        //       click trigger:
        String id = null;
        if (script != null) {
            Map<String, String> idMap = script.getIdMapFor(this.getClass(), player);
            if (!idMap.isEmpty()) {
                ItemTag heldItem = new ItemTag(player.getPlayerEntity().getEquipment().getItemInMainHand());
                for (Map.Entry<String, String> entry : idMap.entrySet()) {
                    String entry_value = TagManager.tag(entry.getValue(), new BukkitTagContext(player, npc, null, false, null));
                    boolean isMatch = entry_value.isEmpty() || BukkitScriptEvent.tryItem(heldItem, entry_value);
                    if (script.shouldDebug()) {
                        Debug.echoDebug(script, "Comparing click trigger '<A>" + entry_value + "<W>' with item '<A>" + heldItem.debuggable() + "<W>': " + (isMatch ? "<GR>Match!" : "<Y>Not a match"));
                    }
                    if (isMatch) {
                        id = entry.getKey();
                        break;
                    }
                }
            }
        }

        // If id is still null after this, it's assumed that the trigger's 'base script' will be used.
        // parse() will accept a null id if this is the case.

        // Click trigger is pretty straight forward, so there's not really a whole lot left to do
        // except call the parse() method which will queue up and execute the appropriate script
        // based on the Player/NPCs interact script.
        // Parses the trigger. Requires if parse returns false there probably is no trigger
        // script specified in the interact script that was selected, in which case
        // we'll call the action 'on no click trigger'.
        if (!parse(npc, player, script, id)) {
            npc.action("no click trigger", player);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
    }
}

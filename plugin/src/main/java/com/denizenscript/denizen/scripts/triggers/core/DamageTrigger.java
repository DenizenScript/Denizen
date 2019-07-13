package com.denizenscript.denizen.scripts.triggers.core;

import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class DamageTrigger extends AbstractTrigger implements Listener {


    // <--[language]
    // @name Damage Triggers
    // @group NPC Interact Scripts
    // @description
    // Damage Triggers are triggered when when a player left clicks the NPC.
    // Despite the name, these do not actually require the NPC take any damage, only that the player left clicks the NPC.
    //
    // In scripts, use <context.damage> to measure how much damage was done to the NPC
    // (though note that invincible NPCs don't necessarily take any damage even when this is non-zero).
    //
    // These are very basic with no extraneous complexity.
    //
    // -->

    // <--[action]
    // @Actions
    // no damage trigger
    //
    // @Triggers when the NPC is damaged by a player but no damage trigger fires.
    //
    // @Context
    // None
    //
    // -->
    // Technically defined in TriggerTrait, but placing here instead.
    // <--[action]
    // @Actions
    // damage
    //
    // @Triggers when the NPC is damaged by a player.
    //
    // @Context
    // <context.damage> returns how much damage was done.
    //
    // @Determine
    // "cancelled" to cancel the damage event.
    //
    // -->
    // <--[action]
    // @Actions
    // damaged
    //
    // @Triggers when the NPC is damaged by an entity.
    //
    // @Context
    // <context.damage> returns how much damage was done.
    // <context.damager> returns the entity that did the damage.
    //
    // @Determine
    // "cancelled" to cancel the damage event.
    //
    // -->
    @EventHandler
    public void damageTrigger(EntityDamageByEntityEvent event) {

        if (event.getEntity() == null) {
            return;
        }

        dEntity damager = new dEntity(event.getDamager());

        if (damager.isProjectile() && damager.hasShooter()) {
            damager = damager.getShooter();
        }

        Map<String, dObject> context = new HashMap<>();
        context.put("damage", new Element(event.getDamage()));

        dPlayer dplayer = null;

        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            dNPC npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()));
            if (npc == null) {
                return;
            }
            if (npc.getCitizen() == null) {
                return;
            }

            context.put("damager", damager);

            String determ = npc.action("damaged", null, context);

            if (determ != null && determ.equalsIgnoreCase("CANCELLED")) {
                event.setCancelled(true);
                return;
            }

            if (damager.isPlayer()) {
                dplayer = damager.getDenizenPlayer();
            }
            else {
                return;
            }

            if (!npc.getCitizen().hasTrait(TriggerTrait.class)) {
                return;
            }
            if (!npc.getTriggerTrait().isEnabled(name)) {
                return;
            }

            // Get the TriggerContext
            TriggerTrait.TriggerContext trigger = npc.getTriggerTrait().trigger(this, dplayer);

            // Return if the trigger wasn't triggered.
            if (!trigger.wasTriggered()) {
                return;
            }

            // ..or if the determination was cancelled.
            if (trigger.hasDetermination()
                    && trigger.getDetermination().equalsIgnoreCase("cancelled")) {
                event.setCancelled(true);
                return;
            }

            // Build the interact script
            InteractScriptContainer script = InteractScriptHelper
                    .getInteractScript(npc, dplayer, getClass());

            String id = null;
            if (script != null) {
                Map<String, String> idMap = script.getIdMapFor(this.getClass(), dplayer);
                if (!idMap.isEmpty())
                // Iterate through the different id entries in the step's click trigger
                {
                    for (Map.Entry<String, String> entry : idMap.entrySet()) {
                        // Tag the entry value to account for replaceables
                        // TODO: script arg?
                        String entry_value = TagManager.tag(entry.getValue(), new BukkitTagContext
                                (dplayer, npc, false, null, false, null));
                        // Check if the item specified in the specified id's 'trigger:' key
                        // matches the item that the player is holding.
                        if (dItem.valueOf(entry_value, script).comparesTo(dplayer.getPlayerEntity().getItemInHand()) >= 0) {
                            id = entry.getKey();
                        }
                    }
                }
            }

            if (!parse(npc, dplayer, script, id, context)) {
                npc.action("no damage trigger", dplayer);
            }
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }
}

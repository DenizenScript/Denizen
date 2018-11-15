package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class DamageTrigger extends AbstractTrigger implements Listener {

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

        Map<String, dObject> context = new HashMap<String, dObject>();
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
                        if (dItem.valueOf(entry_value).comparesTo(dplayer.getPlayerEntity().getItemInHand()) >= 0) {
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

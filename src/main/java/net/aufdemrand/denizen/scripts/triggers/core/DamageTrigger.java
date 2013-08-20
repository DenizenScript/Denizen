package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.objects.dItem;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

public class DamageTrigger extends AbstractTrigger implements Listener {

    @EventHandler
    public void damageTrigger(EntityDamageByEntityEvent event) {

        Player player = null;
        if (event.getDamager() instanceof Player) player = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            player = (Player) ((Projectile)event.getDamager()).getShooter();
        if (event.getEntity() == null)
            return;

        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity()) && player != null) {
            dNPC npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()));
            if (npc == null)
                return;

            if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return;
            if (!npc.getTriggerTrait().isEnabled(name)) return;

            dPlayer dplayer = dPlayer.mirrorBukkitPlayer(player);

            if (!npc.getTriggerTrait().trigger(this, dplayer)) return;

            InteractScriptContainer script = InteractScriptHelper
                    .getInteractScript(npc, dplayer, getClass());

            String id = null;
            if (script != null) {
                Map<String, String> idMap = script.getIdMapFor(this.getClass(), dplayer);
                if (!idMap.isEmpty())
                    // Iterate through the different id entries in the step's click trigger
                    for (Map.Entry<String, String> entry : idMap.entrySet()) {
                        // Tag the entry value to account for replaceables
                        String entry_value = TagManager.tag(dplayer, npc, entry.getValue());
                        // Check if the item specified in the specified id's 'trigger:' key
                        // matches the item that the player is holding.
                        if (dItem.valueOf(entry_value).comparesTo(player.getItemInHand()) >= 0
                                && script.checkSpecificTriggerScriptRequirementsFor(this.getClass(),
                                dplayer, npc, entry.getKey()))
                            id = entry.getKey();
                    }
            }

            if (!parse(npc, dplayer, script, id))
                npc.action("no damage trigger", dplayer);        }
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

}

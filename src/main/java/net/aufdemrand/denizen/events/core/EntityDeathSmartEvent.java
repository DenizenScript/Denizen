package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityDeathSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on (e@)?\\w+ (death|dies)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Event names are simple enough to just go ahead and pass on any match.
                return true;
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Entity Death SmartEvent.");
    }


    @Override
    public void breakDown() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // entity death
    // entity dies
    // <entity> dies
    // <entity> death
    //
    // @Triggers when an entity dies.
    // @Context
    // <context.entity> returns the dEntity that died.
    // <context.damager> returns the dEntity damaging the other entity, if any.
    // <context.message> returns an Element of a player's death message.
    // <context.inventory> returns the dInventory of the entity if it was a player.
    // <context.cause> returns the cause of the death.
    //
    // @Determine
    // Element(String) to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
    // "NO_DROPS_OR_XP" to specify that any drops or XP orbs should be removed.
    // "NO_XP" to specify that any XP orbs should be removed.
    // dList(dItem) to specify new items to be dropped.
    // Element(Number) to specify the new amount of XP to be dropped.
    //
    // -->
    @EventHandler
    public void entityDeath(EntityDeathEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        context.put("entity", entity.getDenizenObject());
        if (event.getEntity().getLastDamageCause() != null)
            context.put("cause", new Element(event.getEntity().getLastDamageCause().getCause().toString()));

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = new dPlayer(entity.getPlayer());

        // If this entity has a stored killer, get it and then
        // remove it from the entityKillers map
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null && lastDamage instanceof EntityDamageByEntityEvent) {
            context.put("damager", new dEntity(((EntityDamageByEntityEvent) lastDamage).getDamager()).getDenizenObject());
        }

        PlayerDeathEvent subEvent = null;

        if (event instanceof PlayerDeathEvent) {
            subEvent = (PlayerDeathEvent) event;
            context.put("message", new Element(subEvent.getDeathMessage()));

            // Null check to prevent NPCs from causing an NPE
            if (player != null)
                context.put("inventory", player.getInventory());
        }

        List<String> determinations = EventManager.doEvents1(Arrays.asList
                        ("entity dies",
                                entity.identifyType() + " dies",
                                entity.identifySimple() + " dies",
                                entity.identifySimple() + " death",
                                "entity death",
                                entity.identifyType() + " death"),
                npc, player, context, true);

        for (String determination : determinations) {
            // Handle message
            if (determination.toUpperCase().startsWith("DROPS ")) {
                determination = determination.substring(6);
            }

            if (determination.toUpperCase().startsWith("NO_DROPS")) {
                event.getDrops().clear();
                if (determination.endsWith("_OR_XP")) {
                    event.setDroppedExp(0);
                }
            } else if (determination.toUpperCase().equals("NO_XP")) {
                event.setDroppedExp(0);
            }

            // Drops
            else if (aH.Argument.valueOf(determination).matchesArgumentList(dItem.class)) {
                dList drops = dList.valueOf(determination);
                drops.filter(dItem.class);
                event.getDrops().clear();
                for (String drop : drops) {
                    dItem item = dItem.valueOf(drop);
                    if (item != null)
                        event.getDrops().add(item.getItemStack());
                }
            }

            // XP
            else if (aH.Argument.valueOf(determination)
                    .matchesPrimitive(aH.PrimitiveType.Integer)) {
                int xp = Integer.valueOf(determination.substring(3));
                event.setDroppedExp(xp);
            } else if (!determination.toUpperCase().equals("NONE")) {
                if (event instanceof PlayerDeathEvent) {
                    subEvent.setDeathMessage(determination);
                }
            }
        }
    }
}

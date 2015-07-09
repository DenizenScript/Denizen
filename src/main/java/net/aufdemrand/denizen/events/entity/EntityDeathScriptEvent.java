package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EntityDeathScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity death
    // entity dies
    // <entity> dies
    // <entity> death
    //
    // @Triggers when an entity dies.
    //
    // @Context
    // <context.entity> returns the dEntity that died.
    // <context.damager> returns the dEntity damaging the other entity, if any.
    // <context.message> returns an Element of a player's death message.
    // <context.inventory> returns the dInventory of the entity if it was a player.
    // <context.cause> returns an Element of the cause of the death. See <@link language damage cause> for a list of possible damage causes.
    // <context.drops> returns a dList of all pending item drops.
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
    public EntityDeathScriptEvent() {
        instance = this;
    }

    public static EntityDeathScriptEvent instance;

    public dEntity entity;
    public dObject damager;
    public Element message;
    public dInventory inventory;
    public Element cause;
    public dList drops;
    public Integer xp;
    public boolean changed_drops;
    public EntityDeathEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String cmd = CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s));
        return cmd.equals("dies") || cmd.equals("death");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return entity.matchesEntity(CoreUtilities.getXthArg(0, CoreUtilities.toLowerCase(s)));
    }

    @Override
    public String getName() {
        return "EntityDies";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        // finish this
        String lower = CoreUtilities.toLowerCase(determination);

        // Deprecated
        if (lower.startsWith("drops ")) {
            lower = lower.substring(6);
        }

        //Handle no_drops and no_drops_or_xp and just no_xp
        if (lower.startsWith("no_drops")) {
            drops.clear();
            changed_drops = true;
            if (lower.endsWith("_or_xp")) {
                xp = 0;
            }
        }
        else if (lower.equals("no_xp")) {
            xp = 0;
        }
        // Change xp value only
        else if (aH.matchesInteger(determination)) {
            xp = aH.Argument.valueOf(lower).asElement().asInt();
        }

        // Change dropped items if dList detected
        else if (aH.Argument.valueOf(lower).matchesArgumentList(dItem.class)) {
            drops.clear();
            changed_drops = true;
            dList drops_list = dList.valueOf(lower);
            drops_list.filter(dItem.class);
            for (String drop : drops_list) {
                dItem item = dItem.valueOf(drop);
                if (item != null)
                    drops.add(item.identify());
            }
        }

        // String containing new Death Message
        else if (event instanceof PlayerDeathEvent) {
            message = new Element(determination);
        }
        else {
            return super.applyDetermination(container, determination);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        if (damager != null) {
            context.put("damager", damager);
        }
        if (message != null) {
            context.put("message", message);
        }
        if (inventory != null) {
            context.put("inventory", inventory);
        }
        if (cause != null) {
            context.put("cause", cause);
        }
        if (drops != null) {
            context.put("drops", drops);
        }
        return context;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        entity = new dEntity(event.getEntity());

        dPlayer player = null;

        if (entity.isPlayer()) {
            player = entity.getDenizenPlayer();
        }

        // If this entity has a stored killer, get it and then
        // remove it from the entityKillers map
        damager = null;
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null) {
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                damager = new dEntity(((EntityDamageByEntityEvent) lastDamage).getDamager()).getDenizenObject();
            }

        }

        message = null;
        inventory = null;
        PlayerDeathEvent subEvent = null;
        if (event instanceof PlayerDeathEvent) {
            subEvent = (PlayerDeathEvent) event;
            message = new Element(subEvent.getDeathMessage());

            // Null check to prevent NPCs from causing an NPE
            if (player != null) {
                inventory = player.getInventory();
            }
        }
        cause = null;
        if (event.getEntity().getLastDamageCause() != null) {
            cause = new Element(event.getEntity().getLastDamageCause().getCause().toString());
        }

        drops = new dList();
        for (ItemStack stack : event.getDrops()) {
            if (stack == null) {
                drops.add("i@air");
            }
            else {
                drops.add(new dItem(stack).identify());
            }
        }
        changed_drops = false;
        xp = event.getDroppedExp();
        this.event = event;
        fire();

        event.setDroppedExp(xp);
        if (changed_drops) {
            event.getDrops().clear();
            for (String drop : drops) {
                dItem item = dItem.valueOf(drop);
                if (item != null) {
                    event.getDrops().add(item.getItemStack());
                }
            }
        }
        if (message != null && subEvent != null) {
            subEvent.setDeathMessage(message.asString());
        }
    }
}

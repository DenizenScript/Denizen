package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EntityDeathScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity death
    // entity dies
    // <entity> dies
    // <entity> death
    //
    // @Cancellable true
    //
    // @Regex ^on [^\s]+ (death|dies)$
    // @Switch in <area>
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
    // <context.xp> returns an Element of the amount of experience to be dropped.
    //
    // @Determine
    // Element to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
    // "NO_DROPS_OR_XP" to specify that any drops or XP orbs should be removed.
    // "NO_XP" to specify that any XP orbs should be removed.
    // dList(dItem) to specify new items to be dropped.
    // Element(Number) to specify the new amount of XP to be dropped.
    // "KEEP_INV" to specify (if a player death) that the inventory should be kept.
    // "KEEP_LEVEL" to specify (if a player death) that the XP level should be kept.
    // Note that the event can be cancelled to hide a player death message.
    //
    // @Player when the entity that died is a player.
    //
    // @NPC when the entity that died is an NPC.
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
    public List<dItem> dropItems;
    public Integer xp;
    public boolean keep_inv;
    public boolean keep_level;
    public EntityDeathEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String cmd = CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s));
        return cmd.equals("dies") || cmd.equals("death");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityDies";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        // finish this
        String lower = CoreUtilities.toLowerCase(determination);

        // Deprecated
        if (lower.startsWith("drops ")) {
            lower = lower.substring(6);
            determination = determination.substring(6);
        }

        //Handle no_drops and no_drops_or_xp and just no_xp
        if (lower.startsWith("no_drops")) {
            drops.clear();
            dropItems = new ArrayList<dItem>();
            if (lower.endsWith("_or_xp")) {
                xp = 0;
            }
        }
        else if (lower.equals("no_xp")) {
            xp = 0;
        }
        else if (lower.equals("keep_inv")) {
            keep_inv = true;
        }
        else if (lower.equals("keep_level")) {
            keep_level = true;
        }
        // Change xp value only
        else if (aH.matchesInteger(determination)) {
            xp = aH.Argument.valueOf(lower).asElement().asInt();
        }

        // Change dropped items if dList detected
        else if (aH.Argument.valueOf(lower).matchesArgumentList(dItem.class)) {
            drops.clear();
            dropItems = new ArrayList<dItem>();
            dList drops_list = dList.valueOf(determination);
            drops_list.filter(dItem.class, container);
            for (String drop : drops_list) {
                dItem item = dItem.valueOf(drop, container);
                if (item != null) {
                    dropItems.add(item);
                    drops.add(item.identify());
                }
            }
        }

        // String containing new Death Message
        else if (event instanceof PlayerDeathEvent && !isDefaultDetermination(determination)) {
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
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damager") && damager != null) {
            return damager;
        }
        else if (name.equals("message") && message != null) {
            return message;
        }
        else if (name.equals("inventory") && inventory != null) {
            return inventory;
        }
        else if (name.equals("cause") && cause != null) {
            return cause;
        }
        else if (name.equals("drops") && drops != null) {
            return drops;
        }
        else if (name.equals("xp") && xp != null) {
            return new Element(xp);
        }
        return super.getContext(name);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity livingEntity = event.getEntity();
        dEntity.rememberEntity(livingEntity);
        entity = new dEntity(livingEntity);

        dPlayer player = null;

        if (entity.isPlayer()) {
            player = entity.getDenizenPlayer();
        }

        cause = null;
        damager = null;
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null) {
            cause = new Element(event.getEntity().getLastDamageCause().getCause().toString());
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                dEntity damageEntity = new dEntity(((EntityDamageByEntityEvent) lastDamage).getDamager());
                dEntity shooter = damageEntity.getShooter();
                if (shooter != null) {
                    damager = shooter.getDenizenObject();
                }
                else {
                    damager = damageEntity.getDenizenObject();
                }
            }
            else if (livingEntity.getKiller() != null) {
                damager = new dEntity(livingEntity.getKiller()).getDenizenObject();
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
            keep_inv = subEvent.getKeepInventory();
            keep_level = subEvent.getKeepLevel();
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
        cancelled = false;
        dropItems = null;
        xp = event.getDroppedExp();
        this.event = event;
        fire();

        event.setDroppedExp(xp);
        if (dropItems != null) {
            event.getDrops().clear();
            for (dItem drop : dropItems) {
                if (drop != null) {
                    event.getDrops().add(drop.getItemStack());
                }
            }
        }
        if (subEvent != null) {
            subEvent.setKeepInventory(keep_inv);
            subEvent.setKeepLevel(keep_level);
            if (message != null) {
                subEvent.setDeathMessage(message.asString());
            }
            if (cancelled) { // Hacked-in player-only cancellation tool to cancel messages
                subEvent.setDeathMessage(null);
            }
        }

        dEntity.forgetEntity(livingEntity);
    }
}

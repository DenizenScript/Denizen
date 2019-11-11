package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
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
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch by:<entity type> to only process the event if the killer is of a specified entity type.
    //
    // @Triggers when an entity dies. Note that this fires *after* the entity dies, and thus some data may be lost from the entity.
    // The death cannot be cancelled, only the death message (for players).
    //
    // @Context
    // <context.entity> returns the EntityTag that died.
    // <context.damager> returns the EntityTag damaging the other entity, if any.
    // <context.message> returns an ElementTag of a player's death message.
    // <context.inventory> returns the InventoryTag of the entity if it was a player.
    // <context.cause> returns an ElementTag of the cause of the death. See <@link language damage cause> for a list of possible damage causes.
    // <context.drops> returns a ListTag of all pending item drops.
    // <context.xp> returns an ElementTag of the amount of experience to be dropped.
    //
    // @Determine
    // ElementTag to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
    // "NO_DROPS_OR_XP" to specify that any drops or XP orbs should be removed.
    // "NO_XP" to specify that any XP orbs should be removed.
    // ListTag(ItemTag) to specify new items to be dropped.
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

    public EntityTag entity;
    public EntityTag damager;
    public ElementTag message;
    public InventoryTag inventory;
    public ElementTag cause;
    public ListTag drops;
    public List<ItemTag> dropItems;
    public Integer xp;
    public boolean keep_inv;
    public boolean keep_level;
    public EntityDeathEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
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

        if (path.switches.containsKey("by") && (damager == null || !tryEntity(damager, path.switches.get("by")))) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityDies";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
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
            dropItems = new ArrayList<>();
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
        else if (ArgumentHelper.matchesInteger(determination)) {
            xp = Argument.valueOf(lower).asElement().asInt();
        }

        // Change dropped items if ListTag detected
        else if (Argument.valueOf(lower).matchesArgumentList(ItemTag.class)) {
            drops.clear();
            dropItems = new ArrayList<>();
            ListTag drops_list = ListTag.valueOf(determination);
            drops_list.filter(ItemTag.class, path.container, true);
            for (String drop : drops_list) {
                ItemTag item = ItemTag.valueOf(drop, path.container);
                if (item != null) {
                    dropItems.add(item);
                    drops.add(item.identify());
                }
            }
        }

        // String containing new Death Message
        else if (event instanceof PlayerDeathEvent && !isDefaultDetermination(determinationObj)) {
            message = new ElementTag(determination);
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? EntityTag.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damager") && damager != null) {
            return damager.getDenizenObject();
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
            return new ElementTag(xp);
        }
        return super.getContext(name);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity livingEntity = event.getEntity();
        EntityTag.rememberEntity(livingEntity);
        entity = new EntityTag(livingEntity);

        PlayerTag player = null;

        if (entity.isPlayer()) {
            player = entity.getDenizenPlayer();
        }

        cause = null;
        damager = null;
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null) {
            cause = new ElementTag(event.getEntity().getLastDamageCause().getCause().toString());
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                EntityTag damageEntity = new EntityTag(((EntityDamageByEntityEvent) lastDamage).getDamager());
                EntityTag shooter = damageEntity.getShooter();
                if (shooter != null) {
                    damager = shooter;
                }
                else {
                    damager = damageEntity;
                }
            }
            else if (livingEntity.getKiller() != null) {
                damager = new EntityTag(livingEntity.getKiller());
            }

        }

        message = null;
        inventory = null;
        PlayerDeathEvent subEvent = null;
        if (event instanceof PlayerDeathEvent) {
            subEvent = (PlayerDeathEvent) event;
            message = new ElementTag(subEvent.getDeathMessage());

            // Null check to prevent NPCs from causing an NPE
            if (player != null) {
                inventory = player.getInventory();
            }
            keep_inv = subEvent.getKeepInventory();
            keep_level = subEvent.getKeepLevel();
        }

        drops = new ListTag();
        for (ItemStack stack : event.getDrops()) {
            drops.addObject(new ItemTag(stack == null ? new ItemStack(Material.AIR) : stack));
        }
        cancelled = false;
        dropItems = null;
        xp = event.getDroppedExp();
        this.event = event;
        fire(event);

        event.setDroppedExp(xp);
        if (dropItems != null) {
            event.getDrops().clear();
            for (ItemTag drop : dropItems) {
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

        EntityTag.forgetEntity(livingEntity);
    }
}

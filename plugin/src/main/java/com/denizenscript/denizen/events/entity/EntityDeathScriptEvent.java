package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

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
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch by:<entity type> to only process the event if the killer is of a specified entity type.
    // @Switch cause:<cause> to only process the event if it was caused by a specific damage cause.
    //
    // @Triggers when an entity dies. Note that this fires *after* the entity dies, and thus some data may be lost from the entity.
    // The death can only be cancelled on Paper.
    //
    // @Context
    // <context.entity> returns the EntityTag that died.
    // <context.damager> returns the EntityTag damaging the other entity, if any.
    // <context.message> returns an ElementTag of a player's death message.
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
    // ElementTag(Number) to specify the new amount of XP to be dropped.
    // "KEEP_INV" to specify (if a player death) that the inventory should be kept.
    // "KEEP_LEVEL" to specify (if a player death) that the XP level should be kept.
    // "NO_MESSAGE" to hide a player death message.
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
    public ElementTag cause;
    public EntityDeathEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("dies") && !cmd.equals("death")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
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
        if (!runGenericSwitchCheck(path, "cause", cause == null ? null : cause.asString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityDies";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("drops ")) { // legacy drops determination format
            lower = lower.substring(6);
            determination = determination.substring(6);
        }
        if (lower.startsWith("no_drops")) {
            event.getDrops().clear();
            if (lower.endsWith("_or_xp")) {
                event.setDroppedExp(0);
            }
        }
        else if (lower.equals("no_xp")) {
            event.setDroppedExp(0);
        }
        else if (lower.equals("keep_inv") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setKeepInventory(true);
        }
        else if (lower.equals("keep_level") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setKeepLevel(true);
        }
        else if (lower.equals("no_message") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setDeathMessage(null);
        }
        else if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setDroppedExp(((ElementTag) determinationObj).asInt());
        }
        else if (Argument.valueOf(lower).matchesArgumentList(ItemTag.class)) {
            List<ItemStack> drops = event.getDrops();
            drops.clear();
            for (ItemTag item : ListTag.getListFor(determinationObj, getTagContext(path)).filter(ItemTag.class, path.container, true)) {
                if (item != null) {
                    drops.add(item.getItemStack());
                }
            }
        }
        else if (event instanceof PlayerDeathEvent && !isDefaultDetermination(determinationObj)) {
            ((PlayerDeathEvent) event).setDeathMessage(determination);
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damager") && damager != null) {
            return damager.getDenizenObject();
        }
        else if (name.equals("message") && event instanceof PlayerDeathEvent) {
            return new ElementTag(((PlayerDeathEvent) event).getDeathMessage());
        }
        else if (name.equals("cause") && cause != null) {
            return cause;
        }
        else if (name.equals("drops")) {
            ListTag list = new ListTag();
            for (ItemStack stack : event.getDrops()) {
                list.addObject(new ItemTag(stack));
            }
            return list;
        }
        else if (name.equals("xp")) {
            return new ElementTag(event.getDroppedExp());
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setDeathMessage(null); // Historical no_message was by cancelling.
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        EntityTag.rememberEntity(livingEntity);
        entity = new EntityTag(livingEntity);
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
        cancelled = false;
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(livingEntity);
    }
}

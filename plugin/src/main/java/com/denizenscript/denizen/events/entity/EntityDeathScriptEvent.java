package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
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
    // <entity> dies|death
    //
    // @Cancellable true
    //
    // @Group Entity
    //
    // @Location true
    // @Switch by:<entity> to only process the event if the killer is known and matches the specified entity matcher.
    // @Switch cause:<cause> to only process the event if it was caused by a specific damage cause.
    //
    // @Triggers when an entity dies. Note that this fires *after* the entity dies, and thus some data may be lost from the entity.
    // The death can only be cancelled on Paper.
    //
    // @Context
    // <context.entity> returns the EntityTag that died.
    // <context.damager> returns the EntityTag damaging the other entity, if any.
    // <context.projectile> returns the EntityTag of a projectile used to kill the entity, if one was used.
    // <context.message> returns an ElementTag of a player's death message.
    // <context.cause> returns an ElementTag of the cause of the death. See <@link language damage cause> for a list of possible damage causes.
    // <context.drops> returns a ListTag of all pending item drops.
    // <context.xp> returns an ElementTag of the amount of experience to be dropped.
    //
    // @Determine
    // ElementTag to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
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
        registerCouldMatcher("<entity> dies|death");
        registerSwitches("by", "cause");
    }


    public EntityTag entity;
    public EntityTag damager;
    public EntityTag projectile;
    public ElementTag cause;
    public EntityDeathEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(target)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("by", damager)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "cause", cause == null ? null : cause.asString())) {
            return false;
        }
        return super.matches(path);
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
            return true;
        }
        else if (lower.equals("no_xp")) {
            event.setDroppedExp(0);
            return true;
        }
        else if (lower.equals("keep_inv") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setKeepInventory(true);
            return true;
        }
        else if (lower.equals("keep_level") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setKeepLevel(true);
            return true;
        }
        else if (lower.equals("no_message") && event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setDeathMessage(null);
            return true;
        }
        else if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setDroppedExp(((ElementTag) determinationObj).asInt());
            return true;
        }
        else if (Argument.valueOf(lower).matchesArgumentList(ItemTag.class)) {
            List<ItemStack> drops = event.getDrops();
            drops.clear();
            for (ItemTag item : ListTag.getListFor(determinationObj, getTagContext(path)).filter(ItemTag.class, getTagContext(path), true)) {
                if (item != null) {
                    drops.add(item.getItemStack());
                }
            }
            return true;
        }
        else if (event instanceof PlayerDeathEvent) {
            PaperAPITools.instance.setDeathMessage((PlayerDeathEvent) event, determination);
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity.getDenizenObject();
            case "projectile": return projectile == null ? null : projectile.getDenizenObject();
            case "damager": return damager == null ? null : damager.getDenizenObject();
            case "message": return event instanceof PlayerDeathEvent ? new ElementTag(PaperAPITools.instance.getDeathMessage((PlayerDeathEvent) event)) : null;
            case "cause": return cause;
            case "xp": return new ElementTag(event.getDroppedExp());
            case "drops":
                ListTag list = new ListTag();
                for (ItemStack stack : event.getDrops()) {
                    list.addObject(new ItemTag(stack));
                }
                return list;
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
        projectile = null;
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null) {
            cause = new ElementTag(event.getEntity().getLastDamageCause().getCause().toString());
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                damager = new EntityTag(((EntityDamageByEntityEvent) lastDamage).getDamager());
                EntityTag shooter = damager.getShooter();
                if (damager instanceof Projectile) {
                    projectile = damager;
                }
                if (shooter != null) {
                    projectile = damager;
                    damager = shooter;
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

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.List;

public class EntityShootsBowScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity shoots bow
    // <entity> shoots <item>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity shoots something out of a bow.
    //
    // @Context
    // <context.entity> returns the EntityTag that shot the bow.
    // <context.projectile> returns a EntityTag of the projectile.
    // <context.bow> returns the ItemTag of the bow used to shoot.
    // <context.force> returns the force of the shot.
    // <context.item> returns an ItemTag of the shot projectile, if any.
    // <context.hand> returns "HAND" or "OFF_HAND" for which hand the bow was in.
    //
    // @Determine
    // ListTag(EntityTag) to change the projectile(s) being shot. (Note that in certain cases, determining an arrow may not be valid).
    // "KEEP_ITEM" to keep the projectile item on shooting it.
    //
    // @Player when the entity that shot the bow is a player.
    //
    // @NPC when the entity that shot the bow is an NPC.
    //
    // -->

    public EntityShootsBowScriptEvent() {
        registerCouldMatcher("<entity> shoots <item>");
    }

    public EntityTag entity;
    public ItemTag bow;
    public EntityTag projectile;
    public EntityShootBowEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String attacker = path.eventArgLowerAt(0);
        String item = path.eventArgLowerAt(2);
        if (!entity.tryAdvancedMatcher(attacker)) {
            return false;
        }
        if (!item.equals("bow") && !bow.tryAdvancedMatcher(item)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);

    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("keep_item")) {
                event.setConsumeItem(false);
                if (entity.isPlayer()) {
                    final Player p = entity.getPlayer();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), p::updateInventory, 1);
                }
                return true;
            }
        }
        if (Argument.valueOf(determination).matchesArgumentList(EntityTag.class)) {
            cancelled = true;
            cancellationChanged();
            // Get the list of entities
            List<EntityTag> newProjectiles = ListTag.getListFor(determinationObj, getTagContext(path)).filter(EntityTag.class, path.container, true);
            // Go through all the entities, spawning/teleporting them
            for (EntityTag newProjectile : newProjectiles) {
                newProjectile.spawnAt(entity.getEyeLocation().add(entity.getEyeLocation().getDirection()));
                // Set the entity as the shooter of the projectile,
                // where applicable
                if (newProjectile.isProjectile()) {
                    newProjectile.setShooter(entity);
                }
            }
            // Mount the projectiles on top of each other
            Position.mount(Conversion.convertEntities(newProjectiles));
            // Get the last entity on the list, i.e. the one at the bottom
            // if there are many mounted on top of each other
            Entity lastProjectile = newProjectiles.get(newProjectiles.size() - 1).getBukkitEntity();
            // Give it the same velocity as the arrow that would
            // have been shot by the bow
            // Note: No, I can't explain why this has to be multiplied by three, it just does.
            lastProjectile.setVelocity(event.getEntity().getLocation().getDirection().multiply(event.getForce() * 3));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "force" -> new ElementTag(event.getForce() * 3);
            case "bow" -> bow;
            case "projectile" -> projectile;
            case "item" -> new ItemTag(event.getConsumable());
            case "hand" -> new ElementTag(event.getHand());
            default -> super.getContext(name);
        };
    }

    @Override
    public void cancellationChanged() {
        if (cancelled && entity.isPlayer()) {
            final Player p = entity.getPlayer();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), p::updateInventory, 1);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onEntityShootsBow(EntityShootBowEvent event) {
        entity = new EntityTag(event.getEntity());
        bow = new ItemTag(event.getBow());
        Entity projectileEntity = event.getProjectile();
        EntityTag.rememberEntity(projectileEntity);
        projectile = new EntityTag(projectileEntity);
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(projectileEntity);
    }
}

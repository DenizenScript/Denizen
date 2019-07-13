package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.entity.Position;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.List;

public class EntityShootsBowEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity shoots bow
    // <entity> shoots bow
    // entity shoots <item>
    // <entity> shoots <item>
    //
    // @Regex ^on [^\s]+ shoots [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity shoots something out of a bow.
    //
    // @Context
    // <context.entity> returns the dEntity that shot the bow.
    // <context.projectile> returns a dEntity of the projectile.
    // <context.bow> returns the dItem of the bow used to shoot.
    // <context.force> returns the force of the shot.
    //
    // @Determine
    // dList(dEntity) to change the projectile(s) being shot.
    //
    // @Player when the entity that shot the bow is a player.
    //
    // @NPC when the entity that shot the bow is an NPC.
    //
    // -->

    public EntityShootsBowEvent() {
        instance = this;
    }

    public static EntityShootsBowEvent instance;

    public dEntity entity;
    public Float force;
    public dItem bow;
    public dEntity projectile;
    public EntityShootBowEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("shoots");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String attacker = path.eventArgLowerAt(0);
        String item = path.eventArgLowerAt(2);

        if (!tryEntity(entity, attacker)) {
            return false;
        }

        if (!item.equals("bow") && !tryItem(bow, item)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;

    }

    @Override
    public String getName() {
        return "EntityShootsBow";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination).matchesArgumentList(dEntity.class)) {
            cancelled = true;

            // Get the list of entities
            List<dEntity> newProjectiles = dList.valueOf(determination).filter(dEntity.class, container);
            // Go through all the entities, spawning/teleporting them
            for (dEntity newProjectile : newProjectiles) {
                newProjectile.spawnAt(entity.getEyeLocation()
                        .add(entity.getEyeLocation().getDirection()));
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
            Entity lastProjectile = newProjectiles.get
                    (newProjectiles.size() - 1).getBukkitEntity();
            // Give it the same velocity as the arrow that would
            // have been shot by the bow
            // Note: No, I can't explain why this has to be multiplied by three, it just does.
            lastProjectile.setVelocity(event.getEntity().getLocation()
                    .getDirection().multiply(force));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("force")) {
            return new Element(force);
        }
        else if (name.equals("bow")) {
            return bow;
        }
        else if (name.equals("projectile")) {
            return projectile;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityShootsBow(EntityShootBowEvent event) {
        entity = new dEntity(event.getEntity());
        force = event.getForce() * 3;
        bow = new dItem(event.getBow());
        Entity projectileEntity = event.getProjectile();
        dEntity.rememberEntity(projectileEntity);
        projectile = new dEntity(projectileEntity);
        this.event = event;
        fire(event);
        dEntity.forgetEntity(projectileEntity);
    }
}

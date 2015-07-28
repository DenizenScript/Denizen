package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.List;

public class EntityShootsBowEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity shoots bow (in <area>)
    // <entity> shoots bow (in <area>)
    // entity shoots <item> (in <area>)
    // <entity> shoots <item> (in <area>)
    //
    // @Regex ^on [^\s]+ shoots [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String attacker = CoreUtilities.getXthArg(0, lower);
        String item = CoreUtilities.getXthArg(2, lower);

        if (!entity.matchesEntity(attacker)) {
            return false;
        }

        if (!item.equals("bow") && !tryItem(bow, item)) {
            return false;
        }

        return runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "EntityShootsBow";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityShootBowEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination).matchesArgumentList(dEntity.class)) {
            cancelled = true;

            // Get the list of entities
            Object list = dList.valueOf(determination).filter(dEntity.class);
            @SuppressWarnings("unchecked")
            List<dEntity> newProjectiles = (List<dEntity>) list;
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

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootsBow(EntityShootBowEvent event) {
        entity = new dEntity(event.getEntity());
        force = event.getForce() * 3;
        bow = new dItem(event.getBow());
        projectile = new dEntity(event.getProjectile());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

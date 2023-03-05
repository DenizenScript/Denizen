package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizen.utilities.entity.Velocity;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.ScriptUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ShootCommand extends AbstractCommand implements Listener, Holdable {

    public ShootCommand() {
        setName("shoot");
        setSyntax("shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (speed:<#.#>) (script:<name>) (def:<element>|...) (shooter:<entity>) (spread:<#.#>) (lead:<location>) (no_rotate)");
        setRequiredArguments(1, 11);
        Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
        isProcedural = false;
    }

    // <--[command]
    // @Name Shoot
    // @Syntax shoot [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (height:<#.#>) (speed:<#.#>) (script:<name>) (def:<element>|...) (shooter:<entity>) (spread:<#.#>) (lead:<location>) (no_rotate)
    // @Required 1
    // @Maximum 11
    // @Short Shoots an entity through the air, useful for things like firing arrows.
    // @Synonyms Launch
    // @Group entity
    //
    // @Description
    // Shoots an entity through the air up to a certain height, optionally triggering a script on impact with a target.
    //
    // The launch has three modes: arc, lead, and direct.
    //
    // The "arc" mode calculates a launch arc to exactly hit the target location.
    // If you want to use this, specify the "height" argument as how high the arc should go, in blocks.
    // Do not specify "speed" or "lead".
    // You can optionally specify a custom "gravity" (hidden from syntax line intentionally) if you know what you're doing and really need to.
    //
    // The "lead" mode calculates a modified arc intended to hit a target based on a lead factor (usually the entity's velocity).
    // To use, specify the "lead" argument as a vector and "speed" as a launch speed.
    // Do not specify "height".
    //
    // Generally, most users should prefer direct mode: it just launches straight in the direction of the destination, at the speed you specify.
    // To use this, just input the "speed" argument, and don't specify "height" or "lead".
    //
    // If the origin is not an entity, you can specify a "shooter" so the damage handling code knows who to assume shot the projectile.
    //
    // Normally, a list of entities will spawn mounted on top of each other. To have them instead fire separately and spread out,
    // specify the "spread" argument with a decimal number indicating how wide to spread the entities.
    //
    // Optionally, add "no_rotate" to prevent the shoot command from rotating launched entities.
    //
    // Use the "script:<name>" argument to run a task script when the projectiles land.
    // When that script runs, the following definitions will be available:
    // <[shot_entities]> for all shot entities (as in, the projectiles),
    // <[last_entity]> for the last one (The controlling entity),
    // <[location]> for the last known location of the last shot entity, and
    // <[hit_entities]> for a list of any entities that were hit by fired projectiles.
    //
    // The shoot command is ~waitable. Refer to <@link language ~waitable>.
    //
    // Note that for ~waiting or the "script" arg, tracking is only accurate for projectile entities (such as arrows). This will be inaccurately estimated for other entity types.
    //
    // @Tags
    // <entry[saveName].shot_entity> returns the single entity that was shot (as in, the projectile) (if you only shot one).
    // <entry[saveName].shot_entities> returns a ListTag of entities that were shot (as in, the projectiles).
    // <entry[saveName].hit_entities> returns a ListTag of entities that were hit (if any). (Only works when you ~wait for the command).
    // <entry[saveName].location> returns the last known location of the last shot entity. (Only works when you ~wait for the command).
    //
    // @Usage
    // Use to shoot an arrow from the NPC to perfectly hit the player.
    // - shoot arrow origin:<npc> destination:<player.location>
    //
    // @Usage
    // Use to shoot an arrow out of the player with a given speed.
    // - shoot arrow origin:<player> speed:2
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteEntityTypes(tab);
    }

    Map<UUID, EntityTag> arrows = new HashMap<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "s")) {

                if (arg.matchesArgumentType(EntityTag.class)) {
                    scriptEntry.addObject("origin_entity", arg.asType(EntityTag.class));
                }
                else if (arg.matchesArgumentType(LocationTag.class)) {
                    scriptEntry.addObject("origin_location", arg.asType(LocationTag.class));
                }
                else {
                    Debug.echoError("Ignoring unrecognized argument: " + arg.getRawValue());
                }
            }
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("destination", "d")) {
                scriptEntry.addObject("destination", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("lead")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("lead")) {
                scriptEntry.addObject("lead", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("height", "h")) {
                scriptEntry.addObject("height", arg.asElement());
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("speed")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesPrefix("script")) {
                String script = arg.asElement().asString();
                int dot = script.indexOf('.');
                String subPath = null;
                if (dot != -1) {
                    subPath = script.substring(dot + 1);
                    script = script.substring(0, dot);
                }
                ScriptTag scriptTag = ScriptTag.valueOf(script, scriptEntry.getContext());
                if (scriptTag == null || !(scriptTag.getContainer() instanceof TaskScriptContainer)) {
                    throw new InvalidArgumentsException("Invalid script specified - must name a task script container.");
                }
                scriptEntry.addObject("script", scriptTag);
                if (subPath != null) {
                    scriptEntry.addObject("path", new ElementTag(subPath));
                }
            }
            else if (!scriptEntry.hasObject("shooter")
                    && arg.matchesArgumentType(EntityTag.class)
                    && arg.matchesPrefix("shooter")) {
                scriptEntry.addObject("shooter", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            // Don't document this argument; it is for debug purposes only
            else if (!scriptEntry.hasObject("gravity")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("gravity", "g")) {
                scriptEntry.addObject("gravity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("spread")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("spread")) {
                scriptEntry.addObject("spread", arg.asElement());
            }
            else if (!scriptEntry.hasObject("no_rotate")
                    && arg.matches("no_rotate")) {
                scriptEntry.addObject("no_rotate", new ElementTag(true));
            }
            else if (arg.matchesPrefix("def", "define", "context")) {
                scriptEntry.addObject("definitions", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        // Use the NPC or player's locations as the origin if one is not specified
        if (!scriptEntry.hasObject("origin_location")) {
            scriptEntry.defaultObject("origin_entity", Utilities.entryDefaultEntity(scriptEntry, false));
        }
        scriptEntry.defaultObject("height", new ElementTag(3));
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("origin_entity") && !scriptEntry.hasObject("origin_location")) {
            throw new InvalidArgumentsException("Must specify an origin location!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        EntityTag originEntity = scriptEntry.getObjectTag("origin_entity");
        LocationTag originLocation = scriptEntry.hasObject("origin_location") ?
                (LocationTag) scriptEntry.getObject("origin_location") :
                new LocationTag(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()));
        boolean no_rotate = scriptEntry.hasObject("no_rotate") && scriptEntry.getElement("no_rotate").asBoolean();
        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination
        final LocationTag destination = scriptEntry.hasObject("destination") ?
                (LocationTag) scriptEntry.getObject("destination") :
                (originEntity != null ? new LocationTag(originEntity.getEyeLocation().clone()
                        .add(originEntity.getEyeLocation().clone().getDirection().multiply(30)))
                        : (originLocation != null ? new LocationTag(originLocation.clone().add(
                        originLocation.getDirection().multiply(30))) : null));
        // TODO: Same as PUSH -- is this the place to do this?
        if (destination == null) {
            Debug.echoError("No destination specified!");
            return;
        }
        final List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final ScriptTag script = scriptEntry.getObjectTag("script");
        final ElementTag subPath = scriptEntry.getElement("path");
        final ListTag definitions = scriptEntry.getObjectTag("definitions");
        EntityTag shooter = scriptEntry.getObjectTag("shooter");
        ElementTag height = scriptEntry.getElement("height");
        ElementTag gravity = scriptEntry.getElement("gravity");
        ElementTag speed = scriptEntry.getElement("speed");
        ElementTag spread = scriptEntry.getElement("spread");
        LocationTag lead = scriptEntry.getObjectTag("lead");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), originEntity, originLocation, db("entities", entities),
                    destination, height, gravity, speed, script, subPath, shooter, spread, lead, (no_rotate ? db("no_rotate", "true") : ""), definitions);
        }
        final ListTag entityList = new ListTag();
        if (!no_rotate) {
            originLocation = new LocationTag(NMSHandler.entityHelper.faceLocation(originLocation, destination));
        }
        for (EntityTag entity : entities) {
            if (!entity.isSpawned() || !no_rotate) {
                entity.spawnAt(originLocation);
            }
            entityList.addObject(entity);
            if (entity.isProjectile()) {
                if (shooter != null || originEntity != null) {
                    entity.setShooter(shooter != null ? shooter : originEntity);
                }
                if (script != null || scriptEntry.shouldWaitFor()) {
                    arrows.put(entity.getUUID(), null);
                }
            }
        }
        scriptEntry.saveObject("shot_entities", entityList);
        if (entityList.size() == 1) {
            scriptEntry.saveObject("shot_entity", entityList.getObject(0));
        }
        if (spread == null) {
            Position.mount(Conversion.convertEntities(entities));
        }
        final EntityTag lastEntity = entities.get(entities.size() - 1);
        if (speed == null) {
            if (gravity == null) {
                gravity = new ElementTag(lastEntity.getEntityType().getGravity());
            }
            Vector v1 = lastEntity.getLocation().toVector();
            Vector v2 = destination.toVector();
            Vector v3 = Velocity.calculate(v1, v2, gravity.asDouble(), height.asDouble());
            lastEntity.setVelocity(v3);
        }
        else if (lead == null) {
            Vector relative = destination.clone().subtract(originLocation).toVector();
            lastEntity.setVelocity(relative.normalize().multiply(speed.asDouble()));
        }
        else {
            double g = 20;
            double v = speed.asDouble();
            Vector relative = destination.clone().subtract(originLocation).toVector();
            double testAng = Velocity.launchAngle(originLocation, destination.toVector(), v, relative.getY(), g);
            double hangTime = Velocity.hangtime(testAng, v, relative.getY(), g);
            Vector to = destination.clone().add(lead.clone().multiply(hangTime)).toVector();
            relative = to.clone().subtract(originLocation.toVector());
            double dist = Math.sqrt(relative.getX() * relative.getX() + relative.getZ() * relative.getZ());
            if (dist == 0) {
                dist = 0.1d;
            }
            testAng = Velocity.launchAngle(originLocation, to, v, relative.getY(), g);
            relative.setY(Math.tan(testAng) * dist);
            relative = relative.normalize();
            v = v + (1.188 * Math.pow(hangTime, 2));
            relative = relative.multiply(v / 20.0d);
            lastEntity.setVelocity(relative);
        }
        if (spread != null) {
            Vector base = lastEntity.getVelocity().clone();
            double spreadDouble = spread.asDouble();
            for (EntityTag entity : entities) {
                entity.setVelocity(Velocity.randomSpread(base, spreadDouble));
            }
        }
        final LocationTag start = new LocationTag(lastEntity.getLocation());
        // A task used to trigger a script if the entity is no longer
        // being shot, when the script argument is used
        BukkitRunnable task = new BukkitRunnable() {
            boolean flying = true;
            LocationTag lastLocation = null;
            Vector lastVelocity = null;
            public void run() {
                // If the entity is no longer spawned, stop the task
                if (!lastEntity.isSpawned()) {
                    if (CoreConfiguration.debugVerbose) {
                        Debug.log("Shoot ended because entity not spawned");
                    }
                    flying = false;
                }
                // Otherwise, if the entity is no longer traveling through
                // the air, stop the task
                else if (lastLocation != null && lastVelocity != null && !(lastEntity.getBukkitEntity() instanceof Projectile)) {
                    if (lastLocation.getWorld() != lastEntity.getBukkitEntity().getWorld()
                            || (lastLocation.distanceSquared(lastEntity.getBukkitEntity().getLocation()) < 0.1
                            && lastVelocity.distanceSquared(lastEntity.getBukkitEntity().getVelocity()) < 0.1)) {
                        if (CoreConfiguration.debugVerbose) {
                            Debug.log("Shoot ended because distances short - locations: " + (lastLocation.distanceSquared(lastEntity.getBukkitEntity().getLocation()))
                                    + ", velocity: " + (lastVelocity.distanceSquared(lastEntity.getBukkitEntity().getVelocity()) < 0.1));
                        }
                        flying = false;
                    }
                }
                if (!arrows.containsKey(lastEntity.getUUID()) || arrows.get(lastEntity.getUUID()) != null) {
                    if (CoreConfiguration.debugVerbose) {
                        Debug.log("Shoot ended because uuid was updated (hit entity?)");
                    }
                    flying = false;
                }
                // Stop the task and run the script if conditions
                // are met
                if (!flying) {
                    this.cancel();
                    ListTag hitEntities = new ListTag();
                    for (EntityTag entity : entities) {
                        if (arrows.containsKey(entity.getUUID())) {
                            EntityTag hit = arrows.get(entity.getUUID());
                            arrows.remove(entity.getUUID());
                            if (hit != null) {
                                hitEntities.addObject(hit.getDenizenObject());
                            }
                        }
                    }
                    if (lastLocation == null) {
                        lastLocation = start;
                    }
                    scriptEntry.saveObject("location", new LocationTag(lastLocation));
                    scriptEntry.saveObject("hit_entities", hitEntities);
                    if (script != null) {
                        Consumer<ScriptQueue> configure = (queue) -> {
                            queue.addDefinition("location", new LocationTag(lastLocation));
                            queue.addDefinition("shot_entities", entityList);
                            queue.addDefinition("last_entity", lastEntity);
                            queue.addDefinition("hit_entities", hitEntities);
                        };
                        ScriptUtilities.createAndStartQueue(script.getContainer(), subPath == null ? null : subPath.asString(), scriptEntry.entryData, null, configure, null, null, definitions, scriptEntry);
                    }
                    scriptEntry.setFinished(true);
                }
                else {
                    // Record its position in case the entity dies
                    lastLocation = lastEntity.getLocation();
                    lastVelocity = lastEntity.getVelocity();
                }
            }
        };
        if (script != null || scriptEntry.shouldWaitFor()) {
            task.runTaskTimer(Denizen.getInstance(), 1, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void projectileHit(ProjectileHitEvent event) {
        if (!arrows.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Shoot ending because hit");
        }
        if (event.getHitEntity() != null) {
            arrows.put(event.getEntity().getUniqueId(), new EntityTag(event.getHitEntity()));
        }
        else {
            arrows.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void arrowDamage(EntityDamageByEntityEvent event) {
        Entity arrow = event.getDamager();
        if (!(arrow instanceof Projectile)) {
            return;
        }
        if (!arrows.containsKey(arrow.getUniqueId())) {
            return;
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Shoot ending because damage");
        }
        arrows.put(arrow.getUniqueId(), new EntityTag(event.getEntity()));
    }
}

package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Velocity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShootCommand extends AbstractCommand implements Listener, Holdable {

    Map<UUID, dEntity> arrows = new HashMap<UUID, dEntity>();

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "s")) {

                if (arg.matchesArgumentType(dEntity.class)) {
                    scriptEntry.addObject("originEntity", arg.asType(dEntity.class));
                }
                else if (arg.matchesArgumentType(dLocation.class)) {
                    scriptEntry.addObject("originLocation", arg.asType(dLocation.class));
                }
                else {
                    dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
                }
            }

            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("destination", "d")) {

                scriptEntry.addObject("destination", arg.asType(dLocation.class));
            }


            else if (!scriptEntry.hasObject("lead")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("lead")) {

                scriptEntry.addObject("lead", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("height", "h")) {

                scriptEntry.addObject("height", arg.asElement());
            }

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("speed")) {

                scriptEntry.addObject("speed", arg.asElement());
            }

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)) {

                scriptEntry.addObject("script", arg.asType(dScript.class));
            }

            else if (!scriptEntry.hasObject("shooter")
                    && arg.matchesArgumentType(dEntity.class)
                    && arg.matchesPrefix("shooter")) {
                scriptEntry.addObject("shooter", arg.asType(dEntity.class));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            // Don't document this argument; it is for debug purposes only
            else if (!scriptEntry.hasObject("gravity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("gravity", "g")) {

                scriptEntry.addObject("gravity", arg.asElement());
            }

            else if (!scriptEntry.hasObject("spread")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("spread")) {
                scriptEntry.addObject("spread", arg.asElement());
            }

            else if (!scriptEntry.hasObject("no_rotate")
                    && arg.matches("no_rotate")) {
                scriptEntry.addObject("no_rotate", new Element(true));
            }

            else arg.reportUnhandled();
        }

        // Use the NPC or player's locations as the origin if one is not specified

        if (!scriptEntry.hasObject("originLocation")) {

            scriptEntry.defaultObject("originEntity",
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity() : null,
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity() : null);
        }

        scriptEntry.defaultObject("height", new Element(3));

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Must specify entity/entities!");

        if (!scriptEntry.hasObject("originEntity") && !scriptEntry.hasObject("originLocation"))
            throw new InvalidArgumentsException("Must specify an origin location!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        dEntity originEntity = (dEntity) scriptEntry.getObject("originEntity");
        dLocation originLocation = scriptEntry.hasObject("originLocation") ?
                (dLocation) scriptEntry.getObject("originLocation") :
                new dLocation(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()));
        boolean no_rotate = scriptEntry.hasObject("no_rotate") && scriptEntry.getElement("no_rotate").asBoolean();

        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination
        final dLocation destination = scriptEntry.hasObject("destination") ?
                (dLocation) scriptEntry.getObject("destination") :
                (originEntity != null ? new dLocation(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()
                                .multiply(30)))
                        : null);

        // TODO: Same as PUSH -- is this the place to do this?
        if (destination == null) {
            dB.report(scriptEntry, getName(), "No destination specified!");
            return;
        }

        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dScript script = (dScript) scriptEntry.getObject("script");
        dEntity shooter = (dEntity) scriptEntry.getObject("shooter");

        Element height = scriptEntry.getElement("height");
        Element gravity = scriptEntry.getElement("gravity");
        Element speed = scriptEntry.getElement("speed");
        Element spread = scriptEntry.getElement("spread");

        dLocation lead = (dLocation) scriptEntry.getObject("lead");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                aH.debugObj("entities", entities.toString()) +
                destination.debug() +
                height.debug() +
                (gravity != null ? gravity.debug() : "") +
                (speed != null ? speed.debug() : "") +
                (script != null ? script.debug() : "") +
                (shooter != null ? shooter.debug() : "") +
                (spread != null ? spread.debug() : "") +
                (lead != null ? lead.debug() : "") +
                (no_rotate ? aH.debugObj("no_rotate", "true") : ""));

        // Keep a dList of entities that can be called using <entry[name].shot_entities>
        // later in the script queue

        final dList entityList = new dList();

        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {
            if (!entity.isSpawned() || !no_rotate) {
                entity.spawnAt(originLocation);
            }

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it
            entityList.add(entity.toString());

            if (!no_rotate) {
                Rotation.faceLocation(entity.getBukkitEntity(), destination);
            }

            // If the current entity is a projectile, set its shooter
            // when applicable
            if (entity.isProjectile() && (shooter != null || originEntity != null)) {
                entity.setShooter(shooter != null ? shooter : originEntity);
                // Also, watch for it hitting a target
                arrows.put(entity.getUUID(), null);
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.
        scriptEntry.addObject("shot_entities", entityList);

        if (spread == null)
            Position.mount(Conversion.convertEntities(entities));

        // Get the entity at the bottom of the entity list, because
        // only its gravity should be affected and tracked considering
        // that the other entities will be mounted on it
        final dEntity lastEntity = entities.get(entities.size() - 1);

        if (gravity == null) {
            gravity = new Element(lastEntity.getEntityType().getGravity());
        }

        if (speed == null) {
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
            Double dist = Math.sqrt(relative.getX() * relative.getX() + relative.getZ() * relative.getZ());
            if (dist == 0) dist = 0.1d;
            testAng = Velocity.launchAngle(originLocation, to, v, relative.getY(), g);
            relative.setY(Math.tan(testAng) * dist);
            relative = relative.normalize();
            v = v + (1.188 * Math.pow(hangTime, 2));
            relative = relative.multiply(v / 20.0d);
            lastEntity.setVelocity(relative);
        }

        if (spread != null) {
            Vector base = lastEntity.getVelocity().clone();
            float sf = spread.asFloat();
            for (dEntity entity : entities) {
                Vector newvel = Velocity.spread(base, (CoreUtilities.getRandom().nextDouble() > 0.5f ? 1 : -1) * Math.toRadians(CoreUtilities.getRandom().nextDouble() * sf),
                        (CoreUtilities.getRandom().nextDouble() > 0.5f ? 1 : -1) * Math.toRadians(CoreUtilities.getRandom().nextDouble() * sf));
                entity.setVelocity(newvel);
            }
        }

        // A task used to trigger a script if the entity is no longer
        // being shot, when the script argument is used
        BukkitRunnable task = new BukkitRunnable() {

            boolean flying = true;
            dLocation lastLocation = null;
            Vector lastVelocity = null;

            public void run() {

                // If the entity is no longer spawned, stop the task
                if (!lastEntity.isSpawned()) {
                    flying = false;
                }

                // Otherwise, if the entity is no longer traveling through
                // the air, stop the task
                else if (lastVelocity != null) {
                    if (lastVelocity.distance
                            (lastEntity.getBukkitEntity().getVelocity()) < 0.05) {
                        flying = false;
                    }
                }

                // Stop the task and run the script if conditions
                // are met
                if (!flying) {

                    this.cancel();

                    if (script != null) {
                        // Build a queue out of the targeted script
                        List<ScriptEntry> entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
                        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(script.getContainer().getName()))
                                .addEntries(entries);

                        // Add relevant definitions
                        queue.addDefinition("location", lastLocation.identify());
                        queue.addDefinition("shot_entities", entityList.toString());
                        queue.addDefinition("last_entity", lastEntity.identify());

                        // Handle hit_entities definition
                        dList hitEntities = new dList();
                        for (dEntity entity : entities) {
                            if (arrows.containsKey(entity.getUUID())) {
                                dEntity hit = arrows.get(entity.getUUID());
                                arrows.remove(entity.getUUID());
                                if (hit != null) {
                                    hitEntities.add(hit.identify());
                                }
                            }
                        }
                        queue.addDefinition("hit_entities", hitEntities.identify());

                        // Start it!
                        queue.start();
                    }

                    scriptEntry.setFinished(true);
                }
                else {
                    // Record it's position in case the entity dies
                    lastLocation = lastEntity.getLocation();
                    lastVelocity = lastEntity.getVelocity();
                }
            }
        };

        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 2);
    }

    @EventHandler
    public void arrowDamage(EntityDamageByEntityEvent event) {
        // Get the damager
        Entity arrow = event.getDamager();

        // First, quickly confirm it's a projectile (relevant at all)
        if (!(arrow instanceof Projectile))
            return;

        // Second, more slowly check if we shot it
        if (!arrows.containsKey(arrow.getUniqueId()))
            return;

        // Replace its entry with the hit entity.
        arrows.remove(arrow.getUniqueId());
        arrows.put(arrow.getUniqueId(), new dEntity(event.getEntity()));
    }
}

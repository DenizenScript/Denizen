package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.DenizenEntityType;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.List;

public class RemoveCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("region")
                    && arg.matchesPrefix("region", "r")) {
                scriptEntry.addObject("region", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to the specified world in the server properties if necessary

        scriptEntry.defaultObject("world",
                (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned()) ? new dWorld(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getWorld()) : null,
                (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() && ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isOnline()) ? new dWorld(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getWorld()) : null,
                new dWorld(Bukkit.getWorlds().get(0)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Element region = (Element) scriptEntry.getObject("region");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), aH.debugList("entities", entities) +
                    (region != null ? aH.debugObj("region", region) : ""));
        }

        boolean conditionsMet;

        // Go through all of our entities and remove them

        for (dEntity entity : entities) {

            conditionsMet = true;

            // If this is a specific spawned entity, and all
            // other applicable conditions are met, remove it

            if (!entity.isGeneric()) {

                if (region != null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Region support is deprecated!");
                    /*conditionsMet = WorldGuardUtilities.inRegion
                                    (entity.getBukkitEntity().getLocation(),
                                    region.asString());*/
                }

                if (conditionsMet) {

                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getCitizen().destroy();
                    }
                    else {
                        entity.remove();
                    }
                }
            }

            // If this is a generic unspawned entity, remove
            // all entities of this type from the world (as
            // long as they meet all other conditions)

            else {

                // Note: getting the entities from each loaded chunk
                // in the world (like in Essentials' /killall) has the
                // exact same effect as the below

                for (Entity worldEntity : world.getEntities()) {

                    // If this entity from the world is of the same type
                    // as our current dEntity, and all other applicable
                    // conditions are met, remove it

                    if (entity.getEntityType().equals(DenizenEntityType.getByEntity(worldEntity))) {

                        if (region != null) {
                            dB.echoError(scriptEntry.getResidingQueue(), "Region support is deprecated!");
                            /*conditionsMet = WorldGuardUtilities.inRegion
                                            (worldEntity.getLocation(),
                                            region.asString());*/
                        }

                        if (conditionsMet) {
                            worldEntity.remove();
                        }
                    }
                }
            }
        }
    }
}

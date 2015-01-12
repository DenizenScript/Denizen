package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTeleportEvent;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;


/**
 * Teleports a list of entities to a location.
 *
 * @author David Cernat, aufdemrand
 */

public class TeleportCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            // NPC arg for compatibility with old scripts
            else if (arg.matches("npc") && ((BukkitScriptEntryData)scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("entities", Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getDenizenEntity()));
            }

            else arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        // Use player or NPC as default entity
        if (!scriptEntry.hasObject("entities"))
            scriptEntry.defaultObject("entities", (((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getDenizenEntity()) : null),
                                              (((BukkitScriptEntryData)scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getDenizenEntity()) : null));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("location", location) +
                             aH.debugObj("entities", entities.toString()));

        for (dEntity entity : entities) {
            // Call a Bukkit event for compatibility with "on entity teleports"
            // world event and other plugins
            if (entity.isSpawned() && entity.getEntityType() != EntityType.PLAYER)
                Bukkit.getPluginManager().callEvent(new EntityTeleportEvent(entity.getBukkitEntity(), entity.getLocation(), location));
            entity.spawnAt(location);
        }
    }
}

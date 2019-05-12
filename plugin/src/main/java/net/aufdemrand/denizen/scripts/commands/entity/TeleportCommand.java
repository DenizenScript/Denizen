package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.List;

public class TeleportCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }

            // NPC arg for compatibility with old scripts
            else if (arg.matches("npc") && ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("entities", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        // Use player or NPC as default entity
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities", (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()) : null),
                    (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null));
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        // Get objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), aH.debugObj("location", location) +
                    aH.debugObj("entities", entities.toString()));
        }

        for (dEntity entity : entities) {
            // Call a Bukkit event for compatibility with "on entity teleports"
            // world event and other plugins
            if (entity.isSpawned()) {
                if (entity.getBukkitEntityType() != EntityType.PLAYER) {
                    Bukkit.getPluginManager().callEvent(new EntityTeleportEvent(entity.getBukkitEntity(), entity.getLocation(), location));
                }
                else {
                    Bukkit.getPluginManager().callEvent(new PlayerTeleportEvent((Player) entity.getBukkitEntity(), entity.getLocation(), location));
                }
            }
            entity.spawnAt(location);
        }
    }
}

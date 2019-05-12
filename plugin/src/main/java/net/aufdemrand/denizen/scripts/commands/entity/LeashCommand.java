package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;

import java.util.List;

public class LeashCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel", "stop")) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("holder")
                    && arg.matchesPrefix("holder", "h")) {

                if (arg.matchesArgumentType(dEntity.class)) {
                    scriptEntry.addObject("holder", arg.asType(dEntity.class));
                }
                else if (arg.matchesArgumentType(dLocation.class)) {
                    scriptEntry.addObject("holder", arg.asType(dLocation.class));
                }
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("cancel")) {

            scriptEntry.defaultObject("holder",
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity() : null,
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity() : null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dEntity holder = null;
        dLocation holderLoc = null;
        Entity Holder = null;
        Object holderObject = scriptEntry.getObject("holder");
        if (holderObject instanceof dEntity) {
            holder = (dEntity) scriptEntry.getObject("holder");
            Holder = holder.getBukkitEntity();
        }
        else if (holderObject instanceof dLocation) {
            holderLoc = ((dLocation) scriptEntry.getObject("holder"));
            Material material = holderLoc.getBlock().getType();
            if (material == MaterialCompat.OAK_FENCE || material == MaterialCompat.NETHER_FENCE
                    || material == Material.ACACIA_FENCE || material == Material.BIRCH_FENCE
                    || material == Material.JUNGLE_FENCE || material == Material.DARK_OAK_FENCE
                    || material == Material.SPRUCE_FENCE) {
                Holder = holderLoc.getWorld().spawn(holderLoc, LeashHitch.class);
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Bad holder location specified - only fences are permitted!");
                return;
            }
        }
        Boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                    aH.debugObj("entities", entities.toString()) +
                    (holder != null ? aH.debugObj("holder", holder) : aH.debugObj("holder", holderLoc)));
        }

        // Go through all the entities and leash/unleash them
        for (dEntity entity : entities) {
            if (entity.isSpawned() && entity.isLivingEntity()) {

                if (cancel) {
                    entity.getLivingEntity().setLeashHolder(null);
                }
                else {
                    entity.getLivingEntity().setLeashHolder(Holder);
                }
            }
        }
    }
}

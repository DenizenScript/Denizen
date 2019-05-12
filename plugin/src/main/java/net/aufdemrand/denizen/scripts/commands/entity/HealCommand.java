package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(dList.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
                specified_targets = true;
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", Arrays.asList(arg.asType(dEntity.class)));
                specified_targets = true;
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("amount")) {
            scriptEntry.addObject("amount", new Element(-1));
        }

        if (!specified_targets) {
            List<dEntity> entities = new ArrayList<>();
            if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null) {
                entities.add(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity());
            }
            else if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() != null) {
                entities.add(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity());
            }
            else {
                throw new InvalidArgumentsException("No valid target entities found.");
            }
            scriptEntry.addObject("entities", entities);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        if (entities == null) {
            return;
        }
        Element amountelement = scriptEntry.getElement("amount");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), amountelement.debug() + aH.debugObj("entities", entities));

        }
        if (amountelement.asDouble() == -1) {
            for (dEntity entity : entities) {
                if (entity.isLivingEntity()) {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
                }
            }
        }
        else {
            double amount = amountelement.asDouble();
            for (dEntity entity : entities) {
                if (entity.getLivingEntity().getHealth() + amount < entity.getLivingEntity().getMaxHealth()) {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getHealth() + amount);
                }
                else {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
                }
            }
        }
    }
}

package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Heals an entity.
 *
 * @author Jeremy Schroeder, Mason Adkins, Morphan1
 */

public class HealCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("amount", arg.asElement());

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(dList.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", Arrays.asList(arg.asType(dEntity.class)));
            }
        }

        if (!scriptEntry.hasObject("amount"))
            scriptEntry.addObject("amount", new Element(-1));

        if (!scriptEntry.hasObject("entities")) {
            List<dEntity> entities = new ArrayList<dEntity>();
            if (scriptEntry.getPlayer() != null)
                entities.add(scriptEntry.getPlayer().getDenizenEntity());
            else if (scriptEntry.getNPC() != null)
                entities.add(scriptEntry.getNPC().getDenizenEntity());
            else
                throw new InvalidArgumentsException("No valid target entities found.");
            scriptEntry.addObject("entities", entities);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Element amountelement = scriptEntry.getElement("amount");

        dB.report(getName(), amountelement.debug() + aH.debugObj("entities", entities));
        if (amountelement.asDouble() == -1)
            for (dEntity entity : entities)
                entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
        else {
            double amount = amountelement.asDouble();
            for (dEntity entity : entities)
                if (entity.getLivingEntity().getHealth() + amount < entity.getLivingEntity().getMaxHealth())
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getHealth() + amount);
                else
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
        }
    }
}

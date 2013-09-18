package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

import org.bukkit.entity.Zombie;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Sets the ages of a list of entities, optionally locking them in those ages.
 *
 * @author David Cernat
 */

public class AgeCommand extends AbstractCommand {

    private enum AgeType { ADULT, BABY }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("agetype")
                    && arg.matchesEnum(AgeType.values())) {
                // add Action
                scriptEntry.addObject("agetype", AgeType.valueOf(arg.getValue().toUpperCase()));
            }


            else if (!scriptEntry.hasObject("age")
                     && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {

                scriptEntry.addObject("age", arg.asElement());
            }

            else if (!scriptEntry.hasObject("lock")
                     && arg.matches("lock")) {

                scriptEntry.addObject("lock", "");
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");

        // Use default duration if one is not specified

        scriptEntry.defaultObject("age", new Element(1));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        AgeType ageType = (AgeType) scriptEntry.getObject("agetype");
        int age = scriptEntry.getElement("age").asInt();
        boolean lock = scriptEntry.hasObject("lock");

        // Report to dB
        dB.report(getName(), (lock == true ? aH.debugObj("lock", lock) : "") +
                             (ageType != null ? aH.debugObj("agetype", ageType)
                                              : aH.debugObj("age", age)) +
                             aH.debugObj("entities", entities.toString()));

        // Go through all the entities and set their ages
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                if (entity.isAgeable()) {
                    if (ageType != null) {
                        if (ageType.equals(ageType.BABY))
                            entity.getAgeable().setBaby();
                        else entity.getAgeable().setAdult();
                    }
                    else entity.getAgeable().setAge(age);

                    if (lock) entity.getAgeable().setAgeLock(true);
                }
                else if (entity.getBukkitEntity() instanceof Zombie) {
                    if (ageType.equals(ageType.BABY))
                        ((Zombie) entity.getBukkitEntity()).setBaby(true);
                    else
                        ((Zombie) entity.getBukkitEntity()).setBaby(false);
                }
                else {
                    dB.report(getName(), entity + " is not ageable!");
                }
            }
        }
    }
}

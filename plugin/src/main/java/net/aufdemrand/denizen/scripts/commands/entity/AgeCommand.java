package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.properties.entity.EntityAge;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;


public class AgeCommand extends AbstractCommand {

    // <--[command]
    // @Name Age
    // @Syntax age [<entity>|...] (adult/baby/<age>) (lock)
    // @Required 1
    // @Short Sets the ages of a list of entities, optionally locking them in those ages.
    // @Group entity
    //
    // @Description
    // Some living entity types are 'ageable' which can affect an entities ability to breed, or whether they appear
    // as a baby or an adult. Using the 'age' command allows modification of an entity's age. Specify an entity and
    // either 'baby', 'adult', or an integer age to set the age of an entity. Using the 'lock' argument will
    // keep the entity from increasing its age automatically. NPCs which use ageable entity types can also be
    // specified.
    //
    // @Tags
    // <e@entity.age>
    //
    // @Usage
    // Use to make an ageable entity a permanant baby.
    // - age e@50 baby lock
    // ...or a mature adult.
    // - age e@50 adult lock
    //
    // @Usage
    // Use to make a baby entity an adult.
    // - age n@puppy adult
    //
    // @Usage
    // Use to mature an animals so that it is old enough to breed.
    // - age <player.location.find.entities.within[20]> 10
    // -->

    private enum AgeType {ADULT, BABY}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("agetype")
                    && arg.matchesEnum(AgeType.values())) {
                scriptEntry.addObject("agetype", AgeType.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("age")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("age", arg.asElement());
            }
            else if (!scriptEntry.hasObject("lock")
                    && arg.matches("lock")) {
                scriptEntry.addObject("lock", new Element(true));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("No valid entities specified.");
        }

        // Use default age if one is not specified
        scriptEntry.defaultObject("age", new Element(1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        AgeType ageType = (AgeType) scriptEntry.getObject("agetype");
        int age = scriptEntry.getElement("age").asInt();
        boolean lock = scriptEntry.hasObject("lock");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (lock ? aH.debugObj("lock", lock) : "") +
                    (ageType != null ? aH.debugObj("agetype", ageType)
                            : aH.debugObj("age", age)) +
                    aH.debugObj("entities", entities.toString()));
        }

        // Go through all the entities and set their ages
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {

                // Check if entity specified can be described by 'EntityAge'
                if (EntityAge.describes(entity)) {

                    EntityAge property = EntityAge.getFrom(entity);

                    // Adjust 'ageType'
                    if (ageType != null) {
                        if (ageType.equals(AgeType.BABY)) {
                            property.setBaby(true);
                        }
                        else {
                            property.setBaby(false);
                        }
                    }
                    else {
                        property.setAge(age);
                    }

                    // Adjust 'locked'
                    property.setLock(lock);
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not ageable!");
                }

            }
        }

    }
}

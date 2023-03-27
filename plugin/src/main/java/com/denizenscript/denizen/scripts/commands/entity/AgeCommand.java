package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAge;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Breedable;

import java.util.List;

public class AgeCommand extends AbstractCommand {

    public AgeCommand() {
        setName("age");
        setSyntax("age [<entity>|...] (adult/baby/<age>) (lock)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Age
    // @Syntax age [<entity>|...] (adult/baby/<age>) (lock)
    // @Required 1
    // @Maximum 3
    // @Short Sets the ages of a list of entities, optionally locking them in those ages.
    // @Group entity
    //
    // @Description
    // Some living entity types are 'ageable' which can affect an entities ability to breed, or whether they appear as a baby or an adult.
    // Using the 'age' command allows modification of an entity's age.
    // Specify an entity and either 'baby', 'adult', or an integer age to set the age of an entity.
    // Using the 'lock' argument will keep the entity from increasing its age automatically.
    // NPCs which use ageable entity types can also be specified.
    //
    // @Tags
    // <EntityTag.age>
    //
    // @Usage
    // Use to make an ageable entity a permanant baby.
    // - age <[some_entity]> baby lock
    //
    // @Usage
    // ...or a mature adult.
    // - age <[some_entity]> adult lock
    //
    // @Usage
    // Use to make a baby entity an adult.
    // - age <[some_npc]> adult
    //
    // @Usage
    // Use to mature some animals so that they are old enough to breed.
    // - age <player.location.find_entities.within[20]> adult
    // -->

    private enum AgeType {ADULT, BABY}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("agetype")
                    && arg.matchesEnum(AgeType.class)) {
                scriptEntry.addObject("agetype", AgeType.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("age")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("age", arg.asElement());
            }
            else if (!scriptEntry.hasObject("lock")
                    && arg.matches("lock")) {
                scriptEntry.addObject("lock", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("No valid entities specified.");
        }
        scriptEntry.defaultObject("age", new ElementTag(1));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        AgeType ageType = (AgeType) scriptEntry.getObject("agetype");
        int age = scriptEntry.getElement("age").asInt();
        boolean lock = scriptEntry.hasObject("lock");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (lock ? db("lock", true) : ""), (ageType != null ? db("agetype", ageType) : db("age", age)), db("entities", entities));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                if (EntityAge.describes(entity)) {
                    EntityAge property = new EntityAge(entity);
                    if (ageType != null) {
                        if (ageType.equals(AgeType.BABY)) {
                            property.setAge(-24000);
                        }
                        else {
                            property.setAge(0);
                        }
                    }
                    else {
                        property.setAge(age);
                    }
                    if (entity instanceof Breedable breedable) {
                        breedable.setAgeLock(lock);
                    }
                }
                else {
                    Debug.echoError(scriptEntry, entity.identify() + " is not ageable!");
                }
            }
        }
    }
}

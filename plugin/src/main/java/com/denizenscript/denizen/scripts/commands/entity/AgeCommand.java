package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAge;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Breedable;

import java.util.List;

public class AgeCommand extends AbstractCommand {

    public AgeCommand() {
        setName("age");
        setSyntax("age [<entity>|...] (adult/baby/<age>) (lock)");
        setRequiredArguments(1, 3);
        isProcedural = false;
        autoCompile();
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

    private enum AgeType { ADULT, BABY }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("entities") @ArgLinear @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> entities,
                                   @ArgName("age") @ArgLinear @ArgDefaultText("1") ElementTag age,
                                   @ArgName("lock") boolean shouldLock) {
        int ageInt = 1;
        if (age.matchesEnum(AgeType.class)) {
            switch (age.asEnum(AgeType.class)) {
                case BABY -> ageInt = -24000;
                case ADULT -> ageInt = 0;
            }
        }
        else if (age.isInt()) {
            ageInt = age.asInt();
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                if (EntityAge.describes(entity)) {
                    EntityAge property = new EntityAge(entity);
                    property.setAge(ageInt);
                    if (entity.getBukkitEntity() instanceof Breedable breedable) {
                        breedable.setAgeLock(shouldLock);
                    }
                }
                else {
                    Debug.echoError(scriptEntry, entity.identify() + " is not ageable!");
                }
            }
        }
    }
}

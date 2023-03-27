package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;

public class EntityAge extends EntityProperty {

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Ageable;
    }

    public EntityAge(EntityTag entity) {
        super(entity);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Ageable.class).getAge());
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    public void setAge(int age) {
        if (object.isCitizensNPC()) {
            object.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setAge(age);
        }
        else {
            as(Ageable.class).setAge(age);
        }
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.is_baby>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is a baby.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "is_baby", (attribute, prop) -> {
            return new ElementTag(!prop.as(Ageable.class).isAdult());
        });

        // <--[tag]
        // @attribute <EntityTag.age>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns the entity's age.
        // Age moves 1 towards zero each tick.
        // A newly spawned baby is -24000, a standard adult is 0, an adult that just bred is 6000.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "age", (attribute, prop) -> {
            return new ElementTag(prop.as(Ageable.class).getAge());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name age
        // @input ElementTag
        // @description
        // Sets the entity's age.
        // Inputs can be 'baby', 'adult', or a valid age number.
        // A newly spawned baby is -24000, a standard adult is 0, an adult that just bred is 6000.
        // Also available: <@link mechanism EntityTag.age_locked>.
        // @tags
        // <EntityTag.age>
        // <EntityTag.is_baby>
        // -->
        PropertyParser.registerMechanism(EntityAge.class, ListTag.class, "age", (prop, mechanism, param) -> {
            if (param.isEmpty()) {
                mechanism.echoError("Missing value for 'age' mechanism!");
                return;
            }
            String input = CoreUtilities.toLowerCase(param.get(0));
            switch (input) {
                case "baby" -> prop.setAge(-24000);
                case "adult" -> prop.setAge(0);
                default -> {
                    if (!ArgumentHelper.matchesInteger(input)) {
                        mechanism.echoError("Invalid age '" + input + "': must be 'baby', 'adult', or a valid age number.");
                        return;
                    }
                    prop.setAge(new ElementTag(input).asInt());
                }
            }
            if (param.size() > 1) {
                BukkitImplDeprecations.oldAgeLockedControls.warn(mechanism.context);
                if (!(prop.getEntity() instanceof Breedable breedable)) {
                    return;
                }
                switch (CoreUtilities.toLowerCase(param.get(1))) {
                    case "locked" -> breedable.setAgeLock(true);
                    case "unlocked" -> breedable.setAgeLock(false);
                    default -> mechanism.echoError("Invalid lock state '" + param.get(1) + "': must be 'locked' or 'unlocked'.");
                }
            }
        });
    }
}

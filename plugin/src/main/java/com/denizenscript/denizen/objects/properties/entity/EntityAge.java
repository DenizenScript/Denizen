package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;

public class EntityAge extends EntityProperty {

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Ageable;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getAgeable().getAge() + (getLock() ? "|locked" : ""));
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    public EntityAge(EntityTag entity) {
        super(entity);
    }

    public void setAge(int val) {
        if (object.isCitizensNPC()) {
            object.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setAge(val);
        }
        else {
            getAgeable().setAge(val);
        }
    }

    public void setLock(boolean bool) {
        if (getEntity() instanceof Breedable breedable) {
            breedable.setAgeLock(bool);
        }
    }

    public boolean getLock() {
        return !(getEntity() instanceof Breedable breedable) || breedable.getAgeLock();
    }

    public Ageable getAgeable() {
        return (Ageable) getEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.age>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns the entity's age number.
        // Age moves 1 towards zero each tick.
        // A newly spawned baby is -24000.
        // A standard adult is 0.
        // An adult that just bred is 6000.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "age", (attribute, prop) -> {
            return new ElementTag(prop.getAgeable().getAge());
        });

        // <--[tag]
        // @attribute <EntityTag.is_age_locked>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age_lock
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is age locked.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "is_age_locked", (attribute, prop) -> {
            return new ElementTag(prop.getLock());
        });

        // <--[tag]
        // @attribute <EntityTag.is_baby>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is a baby.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "is_baby", (attribute, prop) -> {
            return new ElementTag(!prop.getAgeable().isAdult());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name age_lock
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is locked into its current age.
        // Also available: <@link mechanism EntityTag.age>
        // @tags
        // <EntityTag.age>
        // <EntityTag.is_baby>
        // <EntityTag.is_age_locked>
        // <EntityTag.ageable>
        // -->
        PropertyParser.registerMechanism(EntityAge.class, ElementTag.class, "age_lock", (prop, mechanism, param) -> {
            if (mechanism.requireBoolean()) {
                prop.setLock(param.asBoolean());
            }
        });

        // <--[mechanism]
        // @object EntityTag
        // @name age
        // @input ElementTag
        // @description
        // Sets the entity's age.
        // Inputs can be 'baby', 'adult', or a valid age number. A default baby is -24000, a default adult is 0, an adult that just bred is 6000.
        // Optionally, add '|locked' or 'unlocked' to lock/unlock the entity into/from the current age.
        // (EG, age:baby|locked or age:-24000|unlocked)
        // Also available: <@link mechanism EntityTag.age_lock>
        // @tags
        // <EntityTag.age>
        // <EntityTag.is_baby>
        // <EntityTag.is_age_locked>
        // <EntityTag.ageable>
        // -->
        PropertyParser.registerMechanism(EntityAge.class, ListTag.class, "age", (prop, mechanism, param) -> {
            if (param.isEmpty()) {
                mechanism.echoError("Missing value for 'age' mechanism!");
                return;
            }
            String input = param.get(0);
            if (input.equalsIgnoreCase("baby")) {
                prop.setAge(-24000);
            }
            else if (input.equalsIgnoreCase("adult")) {
                prop.setAge(0);
            }
            else if (ArgumentHelper.matchesInteger(input)) {
                prop.setAge(new ElementTag(input).asInt());
            }
            else {
                mechanism.echoError("Invalid age '" + input + "': must be 'baby', 'adult', or a valid age number.");
            }
            if (param.size() > 1) {
                input = param.get(1);
                if (input.equalsIgnoreCase("locked")) {
                    prop.setLock(true);
                }
                else if (input.equalsIgnoreCase("unlocked")) {
                    prop.setLock(false);
                }
                else {
                    mechanism.echoError("Invalid lock state '" + input + "': must be 'locked' or 'unlocked'.");
                }
            }
        });
    }
}

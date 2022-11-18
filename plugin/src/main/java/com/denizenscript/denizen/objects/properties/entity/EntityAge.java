package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;

public class EntityAge implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Ageable;
    }

    public static EntityAge getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAge((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "age_lock", "age"
    };

    private EntityAge(EntityTag entity) {
        ageable = entity;
    }

    EntityTag ageable;

    public void setAge(int val) {
        if (ageable.isCitizensNPC()) {
            ageable.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setAge(val);
        }
        else {
            getAgeable().setAge(val);
        }
    }

    public void setLock(boolean bool) {
        if (isBreedable()) {
            getBreedable().setAgeLock(bool);
        }
    }

    public boolean getLock() {
        return !isBreedable() || getBreedable().getAgeLock();
    }

    public boolean isBreedable() {
        return ageable.getBukkitEntity() instanceof Breedable;
    }

    public Ageable getAgeable() {
        return (Ageable) ageable.getBukkitEntity();
    }

    public Breedable getBreedable() {
        return (Breedable) ageable.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getAgeable().getAge() + (getLock() ? "|locked" : "");
    }

    @Override
    public String getPropertyId() {
        return "age";
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
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "age", (attribute, object) -> {
            return new ElementTag(object.getAgeable().getAge());
        });

        // <--[tag]
        // @attribute <EntityTag.is_age_locked>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age_lock
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is age locked.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "is_age_locked", (attribute, object) -> {
            return new ElementTag(object.getLock());
        });

        // <--[tag]
        // @attribute <EntityTag.is_baby>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is a baby.
        // -->
        PropertyParser.registerTag(EntityAge.class, ElementTag.class, "is_baby", (attribute, object) -> {
            return new ElementTag(!object.getAgeable().isAdult());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
        if (mechanism.matches("age_lock") && mechanism.requireBoolean()) {
            setLock(mechanism.getValue().asBoolean());
        }

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
        if (mechanism.matches("age") && mechanism.requireObject(ListTag.class)) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.isEmpty()) {
                mechanism.echoError("Missing value for 'age' mechanism!");
                return;
            }
            String input = list.get(0);
            if (input.equalsIgnoreCase("baby")) {
                setAge(-24000);
            }
            else if (input.equalsIgnoreCase("adult")) {
                setAge(0);
            }
            else if (ArgumentHelper.matchesInteger(input)) {
                setAge(new ElementTag(input).asInt());
            }
            else {
                mechanism.echoError("Invalid age '" + input + "': must be 'baby', 'adult', or a valid age number.");
            }
            if (list.size() > 1) {
                input = list.get(1);
                if (input.equalsIgnoreCase("locked")) {
                    setLock(true);
                }
                else if (input.equalsIgnoreCase("unlocked")) {
                    setLock(false);
                }
                else {
                    mechanism.echoError("Invalid lock state '" + input + "': must be 'locked' or 'unlocked'.");
                }
            }
        }

    }
}

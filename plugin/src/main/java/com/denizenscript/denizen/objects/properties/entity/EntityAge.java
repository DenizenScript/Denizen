package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Zombie;

public class EntityAge implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        // Check if entity is Ageable, or a Zombie
        return ((EntityTag) entity).getBukkitEntity() instanceof Ageable;
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

    public boolean isBaby() {
        return !getAgeable().isAdult();
    }

    public void setBaby(boolean bool) {
        if (ageable.isCitizensNPC()) {
            ageable.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setAge(bool ? -24000 : 0);
        }
        else {
            if (bool) {
                getAgeable().setBaby();
            }
            else {
                getAgeable().setAdult();
            }
        }
    }

    public void setAge(int val) {
        if (ageable.isCitizensNPC()) {
            ageable.getDenizenNPC().getCitizen().getOrAddTrait(Age.class).setAge(val);
        }
        else {
            getAgeable().setAge(val);
        }
    }

    public int getAge() {
        return getAgeable().getAge();
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

    public boolean isZombie() {
        return ageable.getBukkitEntity() instanceof Zombie;
    }

    public Ageable getAgeable() {
        return (Ageable) ageable.getBukkitEntity();
    }

    public Breedable getBreedable() {
        return (Breedable) ageable.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getAge() + (getLock() ? "|locked" : "");
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    public static void registerTags() {

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
        PropertyParser.<EntityAge, ElementTag>registerTag(ElementTag.class, "age", (attribute, object) -> {
            return new ElementTag(object.getAge());
        });

        // <--[tag]
        // @attribute <EntityTag.is_age_locked>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age_lock
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is age locked.
        // -->
        PropertyParser.<EntityAge, ElementTag>registerTag(ElementTag.class, "is_age_locked", (attribute, object) -> {
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
        PropertyParser.<EntityAge, ElementTag>registerTag(ElementTag.class, "is_baby", (attribute, object) -> {
            return new ElementTag(object.isBaby());
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
            else if (new ElementTag(input).isInt()) {
                setAge(new ElementTag(input).asInt());
            }
            if (list.size() > 1 && list.get(1).equalsIgnoreCase("locked")) {
                setLock(true);
            }
            else if (list.size() > 1 && list.get(1).equalsIgnoreCase("unlocked")) {
                setLock(false);
            }
        }

    }
}

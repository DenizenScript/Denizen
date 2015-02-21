package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.*;

public class EntityAge implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if entity is Ageable, or a Zombie
        return (((dEntity) entity).getBukkitEntity() instanceof Ageable)
                || ((dEntity) entity).getBukkitEntity().getType() == EntityType.ZOMBIE;
    }

    public static EntityAge getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityAge((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAge(dEntity entity) {
        ageable = entity;
    }

    dEntity ageable;

    public boolean isBaby() {
        if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
            return ((Zombie) ageable.getBukkitEntity()).isBaby();
        else
            return !((Ageable) ageable.getBukkitEntity()).isAdult();

    }

    public void setBaby(boolean bool) {
        if (ageable.isCitizensNPC()) {
            NPC ageable_npc = ageable.getDenizenNPC().getCitizen();
            if (!ageable_npc.hasTrait(Age.class))
                ageable_npc.addTrait(Age.class);
            ageable_npc.getTrait(Age.class).setAge(bool ? -24000 : 0);
        }
        else {
            if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
                ((Zombie) ageable.getBukkitEntity()).setBaby(bool);

            else if (bool)
                ((Ageable) ageable.getBukkitEntity()).setBaby();

            else
                ((Ageable) ageable.getBukkitEntity()).setAdult();
        }
    }

    public void setAge(int val) {
        if (ageable.isCitizensNPC()) {
            NPC ageable_npc = ageable.getDenizenNPC().getCitizen();
            ageable_npc.getTrait(Age.class).setAge(val);
        }
        else {
            if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
                ((Zombie) ageable.getBukkitEntity()).setBaby(val >= 0);
            else
                ((Ageable) ageable.getBukkitEntity()).setAge(val);
        }
    }

    public int getAge() {
        if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
            return ((Zombie) ageable.getBukkitEntity()).isBaby() ? -24000 : 0;
        else
            return ((Ageable) ageable.getBukkitEntity()).getAge();
    }

    public void setLock(boolean bool) {
        if (ageable.getBukkitEntity().getType() != EntityType.ZOMBIE)
            ((Ageable) ageable.getBukkitEntity()).setAgeLock(bool);
    }

    public boolean getLock() {
        return ageable.getBukkitEntity().getType() == EntityType.ZOMBIE || ((Ageable) ageable.getBukkitEntity()).getAgeLock();
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (isBaby())
            return "baby" + (getLock() ? "|locked": "");
        else if (ageable.getBukkitEntity().getType() != EntityType.ZOMBIE && getLock())
            return "adult|locked";
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "age";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.age>
        // @returns Element(Number)
        // @mechanism dEntity.age
        // @group properties
        // @description
        // If the entity is ageable, returns the entity's age number (-24000 to 0)
        // -->
        if (attribute.startsWith("age"))
            return new Element(getAge())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_age_locked>
        // @returns Element(Boolean)
        // @mechanism dEntity.age_lock
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is age locked.
        // -->
        if (attribute.startsWith("is_age_locked"))
            return new Element(getLock())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_baby>
        // @returns Element(Boolean)
        // @mechanism dEntity.age
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is a baby.
        // -->
        if (attribute.startsWith("is_baby"))
            return new Element(isBaby())
                    .getAttribute(attribute.fulfill(1));


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {


        // <--[mechanism]
        // @object dEntity
        // @name age_lock
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is locked into its current age.
        // Also available: <@link mechanism dEntity.age>
        // @tags
        // <e@entity.age>
        // <e@entity.is_baby>
        // <e@entity.is_age_locked>
        // <e@entity.is_ageable>
        // -->

        if (mechanism.matches("age_lock")
                && mechanism.requireBoolean()) {
            setLock(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dEntity
        // @name age
        // @input Element
        // @description
        // Sets the entity's age.
        // Inputs can be 'baby', 'adult', or a valid age number (-24000 to 0)
        // Optionally, add '|locked' or 'unlocked' to lock/unlock the entity into/from the current age.
        // (EG, age:baby|locked or age:-24000|unlocked)
        // Also available: <@link mechanism dEntity.age_lock>
        // @tags
        // <e@entity.age>
        // <e@entity.is_baby>
        // <e@entity.is_age_locked>
        // <e@entity.is_ageable>
        // -->

        if (mechanism.matches("age")) {
            dList list = mechanism.getValue().asType(dList.class);
            if (list.size() == 0) {
                dB.echoError("Missing value for 'age' mechanism!");
                return;
            }
            if (list.get(0).equalsIgnoreCase("baby"))
                setBaby(true);
            else if (list.get(0).equalsIgnoreCase("adult"))
                setBaby(false);
            else if (new Element(list.get(0)).isInt())
                setAge(new Element(list.get(0)).asInt());
            if (list.size() > 1 && list.get(1).equalsIgnoreCase("locked"))
                setLock(true);
            else if (list.size() > 1 && list.get(1).equalsIgnoreCase("unlocked"))
                setLock(false);
        }

    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Zombie;

public class EntityAge implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        // Check if entity is Ageable, or a Zombie
        return (((EntityTag) entity).getBukkitEntity() instanceof Ageable)
                || (((EntityTag) entity).getBukkitEntity() instanceof Zombie);
    }

    public static EntityAge getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAge((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "age", "is_age_locked", "is_baby"
    };

    public static final String[] handledMechs = new String[] {
            "age_lock", "age"
    };

    private EntityAge(EntityTag entity) {
        ageable = entity;
    }

    EntityTag ageable;

    public boolean isBaby() {
        if (ageable.getBukkitEntity() instanceof Zombie) {
            return ((Zombie) ageable.getBukkitEntity()).isBaby();
        }
        else {
            return !((Ageable) ageable.getBukkitEntity()).isAdult();
        }

    }

    public void setBaby(boolean bool) {
        if (ageable.isCitizensNPC()) {
            NPC ageable_npc = ageable.getDenizenNPC().getCitizen();
            if (!ageable_npc.hasTrait(Age.class)) {
                ageable_npc.addTrait(Age.class);
            }
            ageable_npc.getTrait(Age.class).setAge(bool ? -24000 : 0);
        }
        else {
            if (ageable.getBukkitEntity() instanceof Zombie) {
                ((Zombie) ageable.getBukkitEntity()).setBaby(bool);
            }
            else if (bool) {
                ((Ageable) ageable.getBukkitEntity()).setBaby();
            }
            else {
                ((Ageable) ageable.getBukkitEntity()).setAdult();
            }
        }
    }

    public void setAge(int val) {
        if (ageable.isCitizensNPC()) {
            NPC ageable_npc = ageable.getDenizenNPC().getCitizen();
            ageable_npc.getTrait(Age.class).setAge(val);
        }
        else {
            if (ageable.getBukkitEntity() instanceof Zombie) {
                ((Zombie) ageable.getBukkitEntity()).setBaby(val >= 0);
            }
            else {
                ((Ageable) ageable.getBukkitEntity()).setAge(val);
            }
        }
    }

    public int getAge() {
        if (ageable.getBukkitEntity() instanceof Zombie) {
            return ((Zombie) ageable.getBukkitEntity()).isBaby() ? -24000 : 0;
        }
        else {
            return ((Ageable) ageable.getBukkitEntity()).getAge();
        }
    }

    public void setLock(boolean bool) {
        if (!(ageable.getBukkitEntity() instanceof Zombie)) {
            ((Ageable) ageable.getBukkitEntity()).setAgeLock(bool);
        }
    }

    public boolean getLock() {
        return (ageable.getBukkitEntity() instanceof Zombie) || ((Ageable) ageable.getBukkitEntity()).getAgeLock();
    }

    @Override
    public String getPropertyString() {
        return getAge() + (getLock() ? "|locked" : "");
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.age>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns the entity's age number (-24000 to 0)
        // -->
        if (attribute.startsWith("age")) {
            return new ElementTag(getAge())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_age_locked>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age_lock
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is age locked.
        // -->
        if (attribute.startsWith("is_age_locked")) {
            return new ElementTag(getLock())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_baby>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.age
        // @group properties
        // @description
        // If the entity is ageable, returns whether the entity is a baby.
        // -->
        if (attribute.startsWith("is_baby")) {
            return new ElementTag(isBaby())
                    .getObjectAttribute(attribute.fulfill(1));
        }


        return null;
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
        if (mechanism.matches("age_lock")
                && mechanism.requireBoolean()) {
            setLock(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name age
        // @input Element
        // @description
        // Sets the entity's age.
        // Inputs can be 'baby', 'adult', or a valid age number (-24000 to 0)
        // Optionally, add '|locked' or 'unlocked' to lock/unlock the entity into/from the current age.
        // (EG, age:baby|locked or age:-24000|unlocked)
        // Also available: <@link mechanism EntityTag.age_lock>
        // @tags
        // <EntityTag.age>
        // <EntityTag.is_baby>
        // <EntityTag.is_age_locked>
        // <EntityTag.ageable>
        // -->
        if (mechanism.matches("age")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() == 0) {
                Debug.echoError("Missing value for 'age' mechanism!");
                return;
            }
            if (list.get(0).equalsIgnoreCase("baby")) {
                setBaby(true);
            }
            else if (list.get(0).equalsIgnoreCase("adult")) {
                setBaby(false);
            }
            else if (new ElementTag(list.get(0)).isInt()) {
                setAge(new ElementTag(list.get(0)).asInt());
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

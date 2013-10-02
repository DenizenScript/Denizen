package net.aufdemrand.denizen.objects.properties;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Age;
import org.bukkit.entity.*;

public class EntityAge implements Property {

    public static boolean describes(dEntity entity) {
        return (entity.getBukkitEntity() instanceof Ageable)
                || entity.getBukkitEntity().getType() == EntityType.ZOMBIE;
    }

    public static EntityAge getFrom(dEntity entity) {
        if (!describes(entity)) return null;

        else return new EntityAge(entity);
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
        if (ageable.isNPC()) {
            NPC ageable_npc  = ageable.getNPC();
            ageable_npc.getTrait(Age.class).setAge(bool ? 0 : 1);

        } else {
            if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
                ((Zombie) ageable.getBukkitEntity()).setBaby(bool);

            else if (bool)
                ((Ageable) ageable.getBukkitEntity()).setBaby();

            else
                ((Ageable) ageable.getBukkitEntity()).setAge(1);
        }
    }

    public void setAge(int val) {
        if (ageable.isNPC()) {
            NPC ageable_npc  = ageable.getNPC();
            ageable_npc.getTrait(Age.class).setAge(val);

        } else {
            if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
                ((Zombie) ageable.getBukkitEntity()).setBaby(val < 1 ? false : true);
            else
                ((Ageable) ageable.getBukkitEntity()).setAge(val);
        }
    }

    public int getAge() {
            if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
                return ((Zombie) ageable.getBukkitEntity()).isBaby() ? 0 : 1;
            else
                return ((Ageable) ageable.getBukkitEntity()).getAge();
    }

    public void setLock(boolean bool) {
        if (ageable.getBukkitEntity().getType() != EntityType.ZOMBIE)
            ((Ageable) ageable.getBukkitEntity()).setAgeLock(bool);
    }

    public boolean getLock() {
        if (ageable.getBukkitEntity().getType() == EntityType.ZOMBIE)
            return true;
        else return ((Ageable) ageable.getBukkitEntity()).getAgeLock();
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getPropertyId() + '=' + getAge() + ';';
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
        // @attribute <e@ageable_entity.age>
        // @returns Element(Boolean)
        // @description
        // Returns 'true' if the entity is 'infected', otherwise false.
        // Currently only 'Zombie' or 'Villager' entities can be infected.
        // -->
        if (attribute.startsWith("is_baby"))
            return new Element(isBaby())
                    .getAttribute(attribute.fulfill(1));


        return new Element(getAge()).getAttribute(attribute);
    }

}

package net.aufdemrand.denizen.objects.properties;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;

public class EntityVillager implements Property {



    public static boolean describes(dEntity entity) {
        return entity.getEntityType() == EntityType.VILLAGER;
    }

    public static EntityVillager getFrom(dEntity entity) {
        if (!describes(entity)) return null;

        else return new EntityVillager(entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityVillager(dEntity entity) {
        villager = entity;
    }

    dEntity villager;

    private Villager getVillager() {
        if (villager == null) return null;
        return (Villager) villager.getBukkitEntity();
    }

    public Villager.Profession getProfession() {
        return getVillager().getProfession();
    }

    public void setProfession(Villager.Profession profession) {
        getVillager().setProfession(profession);
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getPropertyId() + "[Profession=" + isVillager() + ";" + (!isAdult() ? "Baby=true" : "" ) + "];";
    }

    @Override
    public String getPropertyId() {
        return "color";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttributes(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@zombie_entity.is_baby>
        // @returns Element(Boolean)
        // @description
        // Returns 'true' if the EntityZombie is a baby, otherwise false.
        // -->
        if (attribute.startsWith("is_baby"))
            return new Element(!isAdult())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@zombie_entity.as_zombie.is_locked>
        // @returns Element(Boolean)
        // @description
        // Returns 'true' if the entity is 'age locked', otherwise false.
        // -->
        if (attribute.startsWith("is_infected"))
            return new Element(isVillager())
                    .getAttribute(attribute.fulfill(1));

        return new Element(zombie.identify()).getAttribute(attribute);
    }

}

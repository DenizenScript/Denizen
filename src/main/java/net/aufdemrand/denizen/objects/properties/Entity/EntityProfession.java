package net.aufdemrand.denizen.objects.properties.Entity;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class EntityProfession implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Villager, the only EntityType that can be a Professional
        return ((dEntity) entity).getEntityType() == EntityType.VILLAGER;
    }

    public static EntityProfession getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityProfession((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityProfession(dEntity entity) {
        professional = entity;
    }

    dEntity professional;

    private Villager.Profession getProfession() {
        if (professional == null) return null;
        return ((Villager) professional.getBukkitEntity()).getProfession();
    }

    public void setProfession(Villager.Profession profession) {
        if (professional != null)
            ((Villager) professional.getBukkitEntity()).setProfession(profession);

    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getProfession().name().toLowerCase();
    }

    @Override
    public String getPropertyId() {
        return "profession";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.profession>
        // @returns Element
        // @description
        // If the entity can have professions, returns the entity's profession.
        // Currently, only Villager-type entities can have professions.
        // -->
        if (attribute.startsWith("profession"))
            return new Element(getProfession().name().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}

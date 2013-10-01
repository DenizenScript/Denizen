package net.aufdemrand.denizen.objects.properties;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class EntityProfessional implements Property {


    public static boolean describes(dEntity entity) {
        return entity.getEntityType() == EntityType.VILLAGER;
    }

    public static EntityProfessional getFrom(dEntity entity) {
        if (!describes(entity)) return null;

        else return new EntityProfessional(entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityProfessional(dEntity entity) {
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
        return getPropertyId() + '=' + getProfession() + ';';
    }

    @Override
    public String getPropertyId() {
        return "professional";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttributes(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@professional_entity.profession>
        // @returns Element
        // @description
        // Returns the profession of a professional entity.
        // Currently, only 'Villager'-type entities can be professionals.
        // -->
        if (attribute.startsWith("profession"))
            return new Element(getProfession().name())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}

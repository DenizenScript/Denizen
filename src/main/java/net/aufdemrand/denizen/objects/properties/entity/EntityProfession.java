package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class EntityProfession implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) {
            return false;
        }
        // Check if the entity is a Villager, the only EntityType that can be a Professional
        return ((dEntity) entity).getBukkitEntityType() == EntityType.VILLAGER;
    }

    public static EntityProfession getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }

        else {
            return new EntityProfession((dEntity) entity);
        }
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityProfession(dEntity entity) {
        professional = entity;
    }

    dEntity professional;

    private Villager.Profession getProfession() {
        return ((Villager) professional.getBukkitEntity()).getProfession();
    }

    public void setProfession(Villager.Profession profession) {
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

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.profession>
        // @returns Element
        // @mechanism dEntity.profession
        // @group properties
        // @description
        // If the entity can have professions, returns the entity's profession.
        // Currently, only Villager-type entities can have professions.
        // Possible professions: BLACKSMITH, BUTCHER, FARMER, LIBRARIAN, PRIEST.
        // -->
        if (attribute.startsWith("profession")) {
            return new Element(getProfession().name().toLowerCase())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name profession
        // @input Element
        // @description
        // Changes the entity's profession.
        // Currently, only Villager-type entities can have professions.
        // Acceptable professions: BLACKSMITH, BUTCHER, FARMER, LIBRARIAN, PRIEST.
        // @tags
        // <e@entity.profession>
        // -->

        if (mechanism.matches("profession") && mechanism.requireEnum(false, Villager.Profession.values())) {
            setProfession(Villager.Profession.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}

package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class EntityProfession implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Horse, the only EntityType that can be a Color
        return ((dEntity) entity).getEntityType() == EntityType.HORSE;
    }

    public static Horse.Color getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new Horse.Color((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityProfession(dEntity entity) {
        professional = entity;
    }

    dEntity color;

    private Color getColor() {
        if (professional == null) return null;
        return ((Horse) professional.getBukkitEntity()).getProfession();
    }

    public void setColor(Horse.Color color) {
        if (professional != null)
            ((Villager) professional.getBukkitEntity()).setProfession(profession);

    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getColor().Horse.Color().toLowerCase();
    }

    @Override
    public String getPropertyId() {
        return "color";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.horse_color>
        // @returns Element
        // @description
        // If the entity can have professions, returns the entity's profession.
        // Specify horse_color cuz of sheep D: which Can have Colors.
        // -->
        if (attribute.startsWith("horse_color"))
            return new Element(getColor().Horse.Color().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}

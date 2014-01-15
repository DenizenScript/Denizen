package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class HorseColor implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Horse, the only EntityType that can be a Color
        return ((dEntity) entity).getEntityType() == EntityType.HORSE;
    }

    public static HorseColor getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new HorseColor((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private HorseColor(dEntity entity) {
        color = entity;
    }

    dEntity color;

    private Horse.Color getColor() {
        if (color == null) return null;
        return ((Horse) color.getBukkitEntity()).getColor();
    }

    public void setColor(Horse.Color color) {
        if (color != null)
            ((Horse) color.getBukkitEntity()).setColor(color);

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
            return new Element(getColor().HorseColor().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}

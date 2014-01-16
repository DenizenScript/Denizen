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
    	dB.log(entity.debug() + " is horse? " + ((dEntity) entity.getEntityType().name());
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Horse, The only entity that can have a Horse.Color
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
        colored = entity;
    }

    dEntity colored;

    private Horse.Color getColor() {
        if (colored == null) return null;
        return ((Horse) colored.getBukkitEntity()).getColor();
    }

    public void setColor(Horse.Color color) {
        if (color != null)
            ((Horse) colored.getBukkitEntity()).setColor(color);

    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getColor().name().toLowerCase();
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
        // If the entity can have a Horse.Color, returns the entity's color.
        // Currently, only Horse-type entities can have Horse.Color.
        // -->
        if (attribute.startsWith("horse_color"))
            return new Element(getColor().name().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}

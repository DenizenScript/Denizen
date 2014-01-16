package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Sheep;

public class EntityColor implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Horse Or Sheep.
        return (((dEntity) entity).getEntityType() == EntityType.SHEEP
        || ((dEntity) entity).getEntityType() == EntityType.HORSE);
    }

    public static EntityColor getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityColor((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityColor(dEntity entity) {
        colored = entity;
    }

    dEntity colored;

    private String getColor() {
        if (colored == null) return null;

        if (colored.getEntityType() == EntityType.HORSE)
          return ((Horse) colored.getBukkitEntity()).getColor().name();

        if (colored.getEntityType() == EntityType.SHEEP)
          return ((Sheep) colored.getBukkitEntity()).getColor().name();

        return null;
    }

    public void setColor(Horse.Color color) {
        if (color != null)
            ((Horse) colored.getBukkitEntity()).setColor(color);

    }

    public void setColor(DyeColor color) {
        if (color != null)
            ((Sheep) colored.getBukkitEntity()).setColor(color);

    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getColor().toLowerCase();
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
        // @attribute <e@entity.color>
        // @returns Element
        // @description
        // If the entity can have a Color, returns the entity's color.
        // Currently, only Horse and Sheep type entities can have a color.
        // -->
        if (attribute.startsWith("color"))
            return new Element(getColor().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}


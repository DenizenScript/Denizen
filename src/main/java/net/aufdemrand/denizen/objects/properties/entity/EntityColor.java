package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;

public class EntityColor implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                (((dEntity) entity).getEntityType() == EntityType.SHEEP
                || ((dEntity) entity).getEntityType() == EntityType.HORSE
                || ((dEntity) entity).getEntityType() == EntityType.WOLF);
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
        if (colored.getEntityType() == EntityType.HORSE)
          return ((Horse) colored.getBukkitEntity()).getColor().name() + "|" +
                  ((Horse) colored.getBukkitEntity()).getStyle().name() + "|" +
                  ((Horse) colored.getBukkitEntity()).getVariant().name();

        else if (colored.getEntityType() == EntityType.SHEEP)
          return ((Sheep) colored.getBukkitEntity()).getColor().name();

        else if (colored.getEntityType() == EntityType.WOLF)
            return ((Wolf) colored.getBukkitEntity()).getCollarColor().name();

        else // Should never happen
            return null;
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
        // @mechanism dEntity.color
        // @group properties
        // @description
        // If the entity can have a color, returns the entity's color.
        // Currently, only Horse, Wolf, and Sheep type entities can have a color.
        // For horses, the output is COLOR|STYLE|VARIANT
        // -->
        if (attribute.startsWith("color"))
            return new Element(getColor().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name color
        // @input Element
        // @description
        // Changes the entity's color.
        // Currently, only Horse, Wolf, and Sheep type entities can have a color.
        // For horses, the input is COLOR|STYLE|VARIANT
        // @tags
        // <e@entity.color>
        // <e@entity.is_colorable>
        // -->

        if (mechanism.matches("color")) {
            if (colored.getEntityType() == EntityType.HORSE) {
                    dList horse_info = mechanism.getValue().asType(dList.class);
                if (horse_info.size() > 0 && new Element(horse_info.get(0)).matchesEnum(Horse.Color.values()))
                    ((Horse) colored.getBukkitEntity())
                            .setColor(Horse.Color.valueOf(horse_info.get(0).toUpperCase()));
                if (horse_info.size() > 1 && new Element(horse_info.get(1)).matchesEnum(Horse.Style.values()))
                    ((Horse) colored.getBukkitEntity())
                            .setStyle(Horse.Style.valueOf(horse_info.get(1).toUpperCase()));
                if (horse_info.size() > 2 && new Element(horse_info.get(2)).matchesEnum(Horse.Variant.values()))
                    ((Horse) colored.getBukkitEntity())
                            .setVariant(Horse.Variant.valueOf(horse_info.get(2).toUpperCase()));
            }

            else if (colored.getEntityType() == EntityType.SHEEP
                    && mechanism.getValue().matchesEnum(DyeColor.values()))
                ((Sheep) colored.getBukkitEntity())
                        .setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));

            else if (colored.getEntityType() == EntityType.WOLF
                    && mechanism.getValue().matchesEnum(DyeColor.values()))
                ((Wolf) colored.getBukkitEntity())
                        .setCollarColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));

        }
    }

}


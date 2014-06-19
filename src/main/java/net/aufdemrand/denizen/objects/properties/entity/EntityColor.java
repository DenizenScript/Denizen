package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;

public class EntityColor implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                (((dEntity) entity).getEntityType() == EntityType.SHEEP
                || ((dEntity) entity).getEntityType() == EntityType.HORSE
                || ((dEntity) entity).getEntityType() == EntityType.WOLF
                || ((dEntity) entity).getEntityType() == EntityType.OCELOT);
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

        else if (colored.getEntityType() == EntityType.OCELOT)
            return ((Ocelot) colored.getBukkitEntity()).getCatType().name();

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

    // <--[language]
    // @name Horse Type
    // @group Properties
    // @description
    // This is a quick rundown of the styling information used to create a horse,
    // used for both <@link tag e@entity.color> and <@link mechanism e@entity.color>.
    //
    // The output/input is formatted as COLOR|STYLE|VARIANT
    // Where color is:
    // BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, or WHITE.
    // and where style is:
    // WHITE, WHITE_DOTS, WHITE_FIELD, BLACK_DOTS, or NONE.
    // and where variant is:
    // DONKEY, MULE, SKELETON_HORSE, UNDEAD_HORSE, or HORSE.
    // -->


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
        // Currently, only Horse, Wolf, Ocelot, and Sheep type entities can have a color.
        // For horses, the output is COLOR|STYLE|VARIANT, see <@link language horse types>.
        // For ocelots, the types are BLACK_CAT, RED_CAT, SIAMESE_CAT, or WILD_OCELOT.
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
        // Currently, only Horse, Wolf, Ocelot, and Sheep type entities can have a color.
        // For horses, the input is COLOR|STYLE|VARIANT, see <@link language horse types>
        // For ocelots, the types are BLACK_CAT, RED_CAT, SIAMESE_CAT, or WILD_OCELOT.
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

            else if (colored.getEntityType() == EntityType.OCELOT
                && mechanism.getValue().matchesEnum(Ocelot.Type.values()))
                ((Ocelot) colored.getBukkitEntity())
                        .setCatType(Ocelot.Type.valueOf(mechanism.getValue().asString().toUpperCase()));

        }
    }

}


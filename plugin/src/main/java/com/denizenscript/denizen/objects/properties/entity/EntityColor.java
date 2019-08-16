package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.TropicalFishHelper;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;

public class EntityColor implements Property {


    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        EntityType type = ((EntityTag) entity).getBukkitEntityType();
        return type == EntityType.SHEEP ||
                type == EntityType.HORSE ||
                type == EntityType.WOLF ||
                type == EntityType.OCELOT ||
                type == EntityType.RABBIT ||
                type == EntityType.LLAMA ||
                type == EntityType.PARROT ||
                type == EntityType.SHULKER ||
                type == EntityType.MUSHROOM_COW ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && type == EntityType.TROPICAL_FISH);
    }

    public static EntityColor getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityColor((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "color"
    };

    public static final String[] handledMechs = new String[] {
            "color"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityColor(EntityTag entity) {
        colored = entity;
    }

    EntityTag colored;

    private String getColor() {
        EntityType type = colored.getBukkitEntityType();

        if (type == EntityType.HORSE) {
            Horse horse = (Horse) colored.getBukkitEntity();
            return horse.getColor().name() + "|" + horse.getStyle().name();
        }
        else if (type == EntityType.SHEEP) {
            return ((Sheep) colored.getBukkitEntity()).getColor().name();
        }
        else if (type == EntityType.WOLF) {
            return ((Wolf) colored.getBukkitEntity()).getCollarColor().name();
        }
        else if (type == EntityType.OCELOT) {
            return ((Ocelot) colored.getBukkitEntity()).getCatType().name();
        }
        else if (type == EntityType.RABBIT) {
            return ((Rabbit) colored.getBukkitEntity()).getRabbitType().name();
        }
        else if (type == EntityType.LLAMA) {
            return ((Llama) colored.getBukkitEntity()).getColor().name();
        }
        else if (type == EntityType.PARROT) {
            return ((Parrot) colored.getBukkitEntity()).getVariant().name();
        }
        else if (type == EntityType.SHULKER) {
            DyeColor color = ((Shulker) colored.getBukkitEntity()).getColor();
            return color == null ? null : color.name();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1) && type == EntityType.MUSHROOM_COW) {
            return ((MushroomCow) colored.getBukkitEntity()).getVariant().name();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && type == EntityType.TROPICAL_FISH) {
            return TropicalFishHelper.getColor(colored);
        }
        else // Should never happen
        {
            return null;
        }
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        String color = getColor();
        return color == null ? null : CoreUtilities.toLowerCase(color);
    }

    @Override
    public String getPropertyId() {
        return "color";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    // <--[language]
    // @name Horse Types
    // @group Properties
    // @description
    // This is a quick rundown of the styling information used to create a horse,
    // used for both <@link tag EntityTag.color> and <@link mechanism EntityTag.color>.
    //
    // The output/input is formatted as COLOR|STYLE(|VARIANT)
    // Where color is:
    // BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, or WHITE.
    // and where style is:
    // WHITE, WHITE_DOTS, WHITEFIELD, BLACK_DOTS, or NONE.
    // and where variant is:
    // DONKEY, MULE, SKELETON_HORSE, UNDEAD_HORSE, or HORSE.
    //  NOTE: HORSE VARIANTS DEPRECATED SINCE 1.11, use spawn instead
    // -->

    // <--[language]
    // @name Rabbit Types
    // @group Properties
    // @description
    // Denizen includes its own user-friendly list of rabbit type names, instead
    // of relying on Bukkit names which did not exist at the time of writing.
    //
    // Types currently available:
    // BROWN, WHITE, BLACK, WHITE_SPLOTCHED, GOLD, SALT, KILLER.
    //
    // Note: The KILLER rabbit type is a hostile rabbit type. It will attempt to kill
    //       nearby players and wolves. Use at your own risk.
    // -->


    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.color>
        // @returns ElementTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the entity's color. A few entity types can have colors:
        // For horses, the output is COLOR|STYLE(|VARIANT), see <@link language horse types>.
        //  NOTE: HORSE VARIANTS DEPRECATED SINCE 1.11, use spawn instead
        // For ocelots, the types are BLACK_CAT, RED_CAT, SIAMESE_CAT, or WILD_OCELOT.
        // For rabbit types, see <@link language rabbit types>.
        // For parrots, the types are BLUE, CYAN, GRAY, GREEN, or RED.
        // For llamas, the types are CREAMY, WHITE, BROWN, and GRAY.
        // For MushroomCows, the types are RED and BROWN.
        // For Tropical_Fish, the input is Pattern|BodyColor|PatternColor, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/TropicalFish.Pattern.html>
        // For sheep, wolf, and shulker entities, see <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/DyeColor.html>
        // -->
        if (attribute.startsWith("color")) {
            return new ElementTag(CoreUtilities.toLowerCase(getColor()))
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name color
        // @input Element
        // @description
        // Changes the entity's color. Refer to <@link tag EntityTag.color>.
        // @tags
        // <EntityTag.color>
        // <EntityTag.is_colorable>
        // -->

        if (mechanism.matches("color")) {
            EntityType type = colored.getBukkitEntityType();

            if (type == EntityType.HORSE) {
                ListTag horse_info = mechanism.valueAsType(ListTag.class);
                if (horse_info.size() > 0 && new ElementTag(horse_info.get(0)).matchesEnum(Horse.Color.values())) {
                    ((Horse) colored.getBukkitEntity())
                            .setColor(Horse.Color.valueOf(horse_info.get(0).toUpperCase()));
                }
                if (horse_info.size() > 1 && new ElementTag(horse_info.get(1)).matchesEnum(Horse.Style.values())) {
                    ((Horse) colored.getBukkitEntity())
                            .setStyle(Horse.Style.valueOf(horse_info.get(1).toUpperCase()));
                }
            }
            else if (type == EntityType.SHEEP && mechanism.getValue().matchesEnum(DyeColor.values())) {
                ((Sheep) colored.getBukkitEntity())
                        .setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.WOLF && mechanism.getValue().matchesEnum(DyeColor.values())) {
                ((Wolf) colored.getBukkitEntity())
                        .setCollarColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.OCELOT && mechanism.getValue().matchesEnum(Ocelot.Type.values())) {
                ((Ocelot) colored.getBukkitEntity())
                        .setCatType(Ocelot.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.RABBIT && mechanism.getValue().matchesEnum(Rabbit.Type.values())) {
                ((Rabbit) colored.getBukkitEntity()).setRabbitType(Rabbit.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.RABBIT && mechanism.getValue().matchesEnum(Rabbit.Type.values())) {
                ((Rabbit) colored.getBukkitEntity()).setRabbitType(Rabbit.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.LLAMA) {
                ((Llama) colored.getBukkitEntity()).setColor(Llama.Color.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.PARROT) {
                ((Parrot) colored.getBukkitEntity()).setVariant(Parrot.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.SHULKER && mechanism.getValue().matchesEnum(DyeColor.values())) {
                ((Shulker) colored.getBukkitEntity()).setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1) && type == EntityType.MUSHROOM_COW) {
                ((MushroomCow) colored.getBukkitEntity()).setVariant(MushroomCow.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && type == EntityType.TROPICAL_FISH) {
                TropicalFishHelper.setColor(colored, mechanism.getValue().asString());
            }
            else {
                Debug.echoError("Could not apply color '" + mechanism.getValue().toString() + "' to entity of type " + type.name() + ".");
            }

        }
    }
}

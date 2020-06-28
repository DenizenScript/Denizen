package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.CatHelper;
import com.denizenscript.denizen.utilities.entity.FoxHelper;
import com.denizenscript.denizen.utilities.entity.PandaHelper;
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
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.CAT) ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.PANDA) ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.ARROW) ||
                type == EntityType.TROPICAL_FISH;
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.MUSHROOM_COW) {
            return ((MushroomCow) colored.getBukkitEntity()).getVariant().name();
        }
        else if (type == EntityType.TROPICAL_FISH) {
            return TropicalFishHelper.getColor(colored);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.FOX) {
            return FoxHelper.getColor(colored);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.CAT) {
            return CatHelper.getColor(colored);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.PANDA) {
            return PandaHelper.getColor(colored);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.ARROW) {
            try {
                return new ColorTag(((Arrow) colored.getBukkitEntity()).getColor()).identify();
            }
            catch (Exception e) {
                return null;
            }
        }
        else { // Should never happen
            return null;
        }
    }

    @Override
    public String getPropertyString() {
        String color = getColor();
        return color == null ? null : CoreUtilities.toLowerCase(color);
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    // <--[language]
    // @name Entity Color Types
    // @group Properties
    // @description
    // This is a quick rundown of the styling information used to handle the coloration of a mob,
    // in both <@link tag EntityTag.color> and <@link mechanism EntityTag.color>.
    //
    // For horses, the format is COLOR|STYLE,
    //          where COLOR is BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, or WHITE.
    //          and where STYLE is WHITE, WHITE_DOTS, WHITEFIELD, BLACK_DOTS, or NONE.
    // For rabbits, the types are BROWN, WHITE, BLACK, BLACK_AND_WHITE, GOLD, SALT_AND_PEPPER, or THE_KILLER_BUNNY.
    // For ocelots, the types are BLACK_CAT, RED_CAT, SIAMESE_CAT, or WILD_OCELOT. (NOTE: Deprecated since 1.14 - now 'cat' entity type is separate)
    // For cats, the format is TYPE|COLOR (see below).
    //          The types are TABBY, BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, and ALL_BLACK.
    // For parrots, the types are BLUE, CYAN, GRAY, GREEN, or RED.
    // For llamas, the types are CREAMY, WHITE, BROWN, and GRAY.
    // For mushroom_cows, the types are RED and BROWN.
    // For foxes, the types are RED and SNOW.
    // For pandas, the format is MAIN_GENE|HIDDEN_GENE.
    //          The gene types are NORMAL, LAZY, WORRIED, PLAYFUL, BROWN, WEAK, and AGGRESSIVE.
    // For tropical_fish, the input is PATTERN|BODYCOLOR|PATTERNCOLOR, where BodyColor and PatterenColor are both DyeColor (see below),
    //          and PATTERN is KOB, SUNSTREAK, SNOOPER, DASHER, BRINELY, SPOTTY, FLOPPER, STRIPEY, GLITTER, BLOCKFISH, BETTY, is CLAYFISH.
    // For sheep, wolf, and shulker entities, the input is a Dye Color.
    // For Tipped Arrow entities, the input is a ColorTag.
    //
    // For all places where a DyeColor is needed, the options are:
    // BLACK, BLUE, BROWN, CYAN, GRAY, GREEN, LIGHT_BLUE, LIGHT_GRAY, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, WHITE, or YELLOW.
    // -->
    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.color>
        // @returns ElementTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the entity's color.
        // For the available color options, refer to <@link language Entity Color Types>.
        // -->
        if (attribute.startsWith("color")) {
            return new ElementTag(CoreUtilities.toLowerCase(getColor()))
                    .getObjectAttribute(attribute.fulfill(1));
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
        // Changes the entity's color.
        // For the available color options, refer to <@link language Entity Color Types>.
        // @tags
        // <EntityTag.color>
        // <EntityTag.colorable>
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
                ((Sheep) colored.getBukkitEntity()).setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.WOLF && mechanism.getValue().matchesEnum(DyeColor.values())) {
                ((Wolf) colored.getBukkitEntity()).setCollarColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.OCELOT && mechanism.getValue().matchesEnum(Ocelot.Type.values())) {
                ((Ocelot) colored.getBukkitEntity()).setCatType(Ocelot.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
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
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.MUSHROOM_COW) {
                ((MushroomCow) colored.getBukkitEntity()).setVariant(MushroomCow.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.TROPICAL_FISH) {
                TropicalFishHelper.setColor(colored, mechanism.getValue().asString());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.FOX) {
                FoxHelper.setColor(colored, mechanism.getValue().asString());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.CAT) {
                CatHelper.setColor(colored, mechanism.getValue().asString());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.PANDA) {
                PandaHelper.setColor(colored, mechanism.getValue().asString());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && type == EntityType.ARROW) {
                ((Arrow) colored.getBukkitEntity()).setColor(mechanism.valueAsType(ColorTag.class).getColor());
            }
            else { // Should never happen
                Debug.echoError("Could not apply color '" + mechanism.getValue().toString() + "' to entity of type " + type.name() + ".");
            }

        }
    }
}

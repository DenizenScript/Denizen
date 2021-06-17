package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.entity.ColorHelper1_17;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;

import java.util.Arrays;

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
                type == EntityType.CAT ||
                type == EntityType.FOX ||
                type == EntityType.PANDA ||
                type == EntityType.ARROW ||
                type == EntityType.VILLAGER ||
                type == EntityType.TRADER_LLAMA ||
                type == EntityType.TROPICAL_FISH ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ColorHelper1_17.colorIsApplicable(type));
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
            "color", "allowed_colors"
    };

    public static final String[] handledMechs = new String[] {
            "color"
    };

    private EntityColor(EntityTag entity) {
        colored = entity;
    }

    EntityTag colored;

    public String getColor(boolean includeDeprecated) {
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
        else if (type == EntityType.OCELOT && includeDeprecated) {
            return ((Ocelot) colored.getBukkitEntity()).getCatType().name();
        }
        else if (type == EntityType.RABBIT) {
            return ((Rabbit) colored.getBukkitEntity()).getRabbitType().name();
        }
        else if (type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) {
            return ((Llama) colored.getBukkitEntity()).getColor().name();
        }
        else if (type == EntityType.PARROT) {
            return ((Parrot) colored.getBukkitEntity()).getVariant().name();
        }
        else if (type == EntityType.SHULKER) {
            DyeColor color = ((Shulker) colored.getBukkitEntity()).getColor();
            return color == null ? null : color.name();
        }
        else if (type == EntityType.MUSHROOM_COW) {
            return ((MushroomCow) colored.getBukkitEntity()).getVariant().name();
        }
        else if (type == EntityType.TROPICAL_FISH) {
            TropicalFish fish = ((TropicalFish) colored.getBukkitEntity());
            return new ListTag(Arrays.asList(fish.getPattern().name(), fish.getBodyColor().name(), fish.getPatternColor().name())).identify();
        }
        else if (type == EntityType.FOX) {
            return ((Fox) colored.getBukkitEntity()).getFoxType().name();
        }
        else if (type == EntityType.CAT) {
            Cat cat = (Cat) colored.getBukkitEntity();
            return cat.getCatType().name() + "|" + cat.getCollarColor().name();
        }
        else if (type == EntityType.PANDA) {
            Panda panda = (Panda) colored.getBukkitEntity();
            return panda.getMainGene().name() + "|" + panda.getHiddenGene().name();
        }
        else if (type == EntityType.VILLAGER) {
            return ((Villager) colored.getBukkitEntity()).getVillagerType().name();
        }
        else if (type == EntityType.ARROW) {
            try {
                return new ColorTag(((Arrow) colored.getBukkitEntity()).getColor()).identify();
            }
            catch (Exception e) {
                return null;
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ColorHelper1_17.colorIsApplicable(type)) {
            return ColorHelper1_17.getColor(colored.getBukkitEntity());
        }
        else {
            return null;
        }
    }

    public static ListTag listForEnum(Enum<?>[] values) {
        ListTag list = new ListTag(values.length);
        for (Enum<?> obj : values) {
            list.addObject(new ElementTag(obj.name()));
        }
        return list;
    }

    public ListTag getAllowedColors() {
        EntityType type = colored.getBukkitEntityType();
        if (type == EntityType.HORSE) {
            ListTag toRet = listForEnum(Horse.Color.values());
            toRet.addAll(listForEnum(Horse.Style.values()));
            return toRet;
        }
        else if (type == EntityType.SHEEP) {
            return listForEnum(DyeColor.values());
        }
        else if (type == EntityType.WOLF) {
            return listForEnum(DyeColor.values());
        }
        else if (type == EntityType.RABBIT) {
            return listForEnum(Rabbit.Type.values());
        }
        else if (type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) {
            return listForEnum(Llama.Color.values());
        }
        else if (type == EntityType.PARROT) {
            return listForEnum(Parrot.Variant.values());
        }
        else if (type == EntityType.SHULKER) {
            return listForEnum(DyeColor.values());
        }
        else if (type == EntityType.MUSHROOM_COW) {
            return listForEnum(MushroomCow.Variant.values());
        }
        else if (type == EntityType.TROPICAL_FISH) {
            ListTag toRet = listForEnum(TropicalFish.Pattern.values());
            toRet.addAll(listForEnum(DyeColor.values()));
            return toRet;
        }
        else if (type == EntityType.FOX) {
            return listForEnum(Fox.Type.values());
        }
        else if (type == EntityType.CAT) {
            return listForEnum(Cat.Type.values());
        }
        else if (type == EntityType.PANDA) {
            return listForEnum(Panda.Gene.values());
        }
        else if (type == EntityType.VILLAGER) {
            return listForEnum(Villager.Type.values());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ColorHelper1_17.colorIsApplicable(type)) {
            return ColorHelper1_17.getAllowedColors(type);
        }
        else { // includes Ocelot (deprecated) and arrow (ColorTag)
            return null;
        }
    }

    @Override
    public String getPropertyString() {
        String color = getColor(false);
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
    // The list of values can be gotten in-script via <@link tag EntityTag.allowed_colors>.
    //
    // For horses, the format is COLOR|STYLE,
    //          where COLOR is BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, or WHITE.
    //          and where STYLE is WHITE, WHITE_DOTS, WHITEFIELD, BLACK_DOTS, or NONE.
    // For rabbits, the types are BROWN, WHITE, BLACK, BLACK_AND_WHITE, GOLD, SALT_AND_PEPPER, or THE_KILLER_BUNNY.
    // For cats (not ocelots), the format is TYPE|COLOR (see below).
    //          The types are TABBY, BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, and ALL_BLACK.
    // For parrots, the types are BLUE, CYAN, GRAY, GREEN, or RED.
    // For llamas, the types are CREAMY, WHITE, BROWN, and GRAY.
    // For mushroom_cows, the types are RED and BROWN.
    // For foxes, the types are RED and SNOW.
    // For pandas, the format is MAIN_GENE|HIDDEN_GENE.
    //          The gene types are NORMAL, LAZY, WORRIED, PLAYFUL, BROWN, WEAK, and AGGRESSIVE.
    // For villagers, the types are DESERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, and TAIGA.
    // For tropical_fish, the input is PATTERN|BODYCOLOR|PATTERNCOLOR, where BodyColor and PatterenColor are both DyeColor (see below),
    //          and PATTERN is KOB, SUNSTREAK, SNOOPER, DASHER, BRINELY, SPOTTY, FLOPPER, STRIPEY, GLITTER, BLOCKFISH, BETTY, is CLAYFISH.
    // For sheep, wolf, and shulker entities, the input is a Dye Color.
    // For Tipped Arrow entities, the input is a ColorTag.
    // For goats, the input is SCREAMING or NORMAL.
    // For axolotl, the input is BLUE, CYAN, GOLD, LUCY, or WILD.
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
        // @attribute <EntityTag.allowed_colors>
        // @returns ElementTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the list of allowed colors.
        // See also <@link language Entity Color Types>.
        // -->
        if (attribute.startsWith("allowed_colors")) {
            ListTag colors = getAllowedColors();
            if (colors == null) {
                return null;
            }
            return colors
                    .getObjectAttribute(attribute.fulfill(1));
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
            String color = CoreUtilities.toLowerCase(getColor(true));
            if (color == null) {
                return null;
            }
            return new ElementTag(color)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name color
        // @input ElementTag
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
            else if (type == EntityType.OCELOT && mechanism.getValue().matchesEnum(Ocelot.Type.values())) { // TODO: Deprecate?
                ((Ocelot) colored.getBukkitEntity()).setCatType(Ocelot.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.RABBIT && mechanism.getValue().matchesEnum(Rabbit.Type.values())) {
                ((Rabbit) colored.getBukkitEntity()).setRabbitType(Rabbit.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if ((type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) && mechanism.getValue().matchesEnum(Llama.Color.values())) {
                ((Llama) colored.getBukkitEntity()).setColor(Llama.Color.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.PARROT && mechanism.getValue().matchesEnum(Parrot.Variant.values())) {
                ((Parrot) colored.getBukkitEntity()).setVariant(Parrot.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.SHULKER && mechanism.getValue().matchesEnum(DyeColor.values())) {
                ((Shulker) colored.getBukkitEntity()).setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.MUSHROOM_COW) {
                ((MushroomCow) colored.getBukkitEntity()).setVariant(MushroomCow.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.TROPICAL_FISH) {
                ListTag list = ListTag.valueOf(mechanism.getValue().asString(), CoreUtilities.basicContext);
                TropicalFish fish = ((TropicalFish) colored.getBukkitEntity());
                fish.setPattern(TropicalFish.Pattern.valueOf(list.get(0).toUpperCase()));
                if (list.size() > 1) {
                    fish.setBodyColor(DyeColor.valueOf(list.get(1).toUpperCase()));
                }
                if (list.size() > 2) {
                    fish.setPatternColor(DyeColor.valueOf(list.get(2).toUpperCase()));
                }
            }
            else if (type == EntityType.FOX) {
                ((Fox) colored.getBukkitEntity()).setFoxType(Fox.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.CAT) {
                Cat cat = (Cat) colored.getBukkitEntity();
                ListTag list = ListTag.valueOf(mechanism.getValue().asString(), CoreUtilities.basicContext);
                cat.setCatType(Cat.Type.valueOf(list.get(0).toUpperCase()));
                if (list.size() > 1) {
                    cat.setCollarColor(DyeColor.valueOf(list.get(1).toUpperCase()));
                }
            }
            else if (type == EntityType.PANDA) {
                Panda panda = (Panda) colored.getBukkitEntity();
                ListTag list = ListTag.valueOf(mechanism.getValue().asString(), CoreUtilities.basicContext);
                panda.setMainGene(Panda.Gene.valueOf(list.get(0).toUpperCase()));
                panda.setHiddenGene(Panda.Gene.valueOf(list.get(1).toUpperCase()));
            }
            else if (type == EntityType.VILLAGER) {
                ((Villager) colored.getBukkitEntity()).setVillagerType(Villager.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.ARROW) {
                ((Arrow) colored.getBukkitEntity()).setColor(mechanism.valueAsType(ColorTag.class).getColor());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ColorHelper1_17.colorIsApplicable(type)) {
                ColorHelper1_17.setColor(colored.getBukkitEntity(), mechanism);
            }
            else { // Should never happen
                mechanism.echoError("Could not apply color '" + mechanism.getValue().toString() + "' to entity of type " + type.name() + ".");
            }

        }
    }
}

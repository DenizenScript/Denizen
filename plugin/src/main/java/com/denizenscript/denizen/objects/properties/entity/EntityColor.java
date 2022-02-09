package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.MultiVersionHelper1_17;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
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
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && MultiVersionHelper1_17.colorIsApplicable(type));
    }

    public static EntityColor getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityColor((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "color"
    };

    private EntityColor(EntityTag entity) {
        colored = entity;
    }

    EntityTag colored;

    public String getColor(boolean includeDeprecated) {
        EntityType type = colored.getBukkitEntityType();
        switch (type) {
            case HORSE:
                Horse horse = (Horse) colored.getBukkitEntity();
                return horse.getColor().name() + "|" + horse.getStyle().name();
            case SHEEP:
                return ((Sheep) colored.getBukkitEntity()).getColor().name();
            case WOLF:
                return ((Wolf) colored.getBukkitEntity()).getCollarColor().name();
            case OCELOT:
                if (includeDeprecated) {
                    return ((Ocelot) colored.getBukkitEntity()).getCatType().name();
                }
                break;
            case RABBIT:
                return ((Rabbit) colored.getBukkitEntity()).getRabbitType().name();
            case LLAMA:
            case TRADER_LLAMA:
                return ((Llama) colored.getBukkitEntity()).getColor().name();
            case PARROT:
                return ((Parrot) colored.getBukkitEntity()).getVariant().name();
            case SHULKER:
                DyeColor color = ((Shulker) colored.getBukkitEntity()).getColor();
                return color == null ? null : color.name();
            case MUSHROOM_COW:
                return ((MushroomCow) colored.getBukkitEntity()).getVariant().name();
            case TROPICAL_FISH:
                TropicalFish fish = ((TropicalFish) colored.getBukkitEntity());
                return new ListTag(Arrays.asList(fish.getPattern().name(), fish.getBodyColor().name(), fish.getPatternColor().name())).identify();
            case FOX:
                return ((Fox) colored.getBukkitEntity()).getFoxType().name();
            case CAT:
                Cat cat = (Cat) colored.getBukkitEntity();
                return cat.getCatType().name() + "|" + cat.getCollarColor().name();
            case PANDA:
                Panda panda = (Panda) colored.getBukkitEntity();
                return panda.getMainGene().name() + "|" + panda.getHiddenGene().name();
            case VILLAGER:
                return ((Villager) colored.getBukkitEntity()).getVillagerType().name();
            case ARROW:
                try {
                    return new ColorTag(((Arrow) colored.getBukkitEntity()).getColor()).identify();
                }
                catch (Exception e) {
                    return null;
                }
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)
                && MultiVersionHelper1_17.colorIsApplicable(type)) {
            return MultiVersionHelper1_17.getColor(colored.getBukkitEntity());
        }
        return null;
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
        switch (type) {
            case HORSE:
                ListTag horseColors = listForEnum(Horse.Color.values());
                horseColors.addAll(listForEnum(Horse.Style.values()));
                return horseColors;
            case SHEEP:
            case WOLF:
            case SHULKER:
                return listForEnum(DyeColor.values());
            case RABBIT:
                return listForEnum(Rabbit.Type.values());
            case LLAMA:
            case TRADER_LLAMA:
                return listForEnum(Llama.Color.values());
            case PARROT:
                return listForEnum(Parrot.Variant.values());
            case MUSHROOM_COW:
                return listForEnum(MushroomCow.Variant.values());
            case TROPICAL_FISH:
                ListTag patterns = listForEnum(TropicalFish.Pattern.values());
                patterns.addAll(listForEnum(DyeColor.values()));
                return patterns;
            case FOX:
                return listForEnum(Fox.Type.values());
            case CAT:
                return listForEnum(Cat.Type.values());
            case PANDA:
                return listForEnum(Panda.Gene.values());
            case VILLAGER:
                return listForEnum(Villager.Type.values());
            }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)
                && MultiVersionHelper1_17.colorIsApplicable(type)) {
            return MultiVersionHelper1_17.getAllowedColors(type);
        }
        return null; // includes Ocelot (deprecated) and arrow (ColorTag)
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

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.allowed_colors>
        // @returns ListTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the list of allowed colors.
        // See also <@link language Entity Color Types>.
        // -->
        PropertyParser.<EntityColor, ListTag>registerTag(ListTag.class, "allowed_colors", (attribute, object) -> {
            return object.getAllowedColors();
        });

        // <--[tag]
        // @attribute <EntityTag.color>
        // @returns ElementTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the entity's color.
        // For the available color options, refer to <@link language Entity Color Types>.
        // -->
        PropertyParser.<EntityColor, ElementTag>registerTag(ElementTag.class, "color", (attribute, object) -> {
            String color = object.getColor(true);
            if (color == null) {
                return null;
            }
            return new ElementTag(CoreUtilities.toLowerCase(color));
        });
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

            if (type == EntityType.HORSE && mechanism.requireObject(ListTag.class)) {
                ListTag list = mechanism.valueAsType(ListTag.class);
                Horse horse = (Horse) colored.getBukkitEntity();
                String color = list.get(0);
                if (new ElementTag(color).matchesEnum(Horse.Color.values())) {
                    horse.setColor(Horse.Color.valueOf(color.toUpperCase()));
                }
                else {
                    mechanism.echoError("Invalid horse color specified: " + color);
                }
                if (list.size() > 1) {
                    String style = list.get(1);
                    if (new ElementTag(style).matchesEnum(Horse.Style.values())) {
                        horse.setStyle(Horse.Style.valueOf(style.toUpperCase()));
                    }
                    else {
                        mechanism.echoError("Invalid horse style specified: " + style);
                    }
                }
            }
            else if (type == EntityType.SHEEP && mechanism.requireEnum(false, DyeColor.values())) {
                ((Sheep) colored.getBukkitEntity()).setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.WOLF && mechanism.requireEnum(false, DyeColor.values())) {
                ((Wolf) colored.getBukkitEntity()).setCollarColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.OCELOT && mechanism.requireEnum(false, Ocelot.Type.values())) { // TODO: Deprecate?
                ((Ocelot) colored.getBukkitEntity()).setCatType(Ocelot.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.RABBIT && mechanism.requireEnum(false, Rabbit.Type.values())) {
                ((Rabbit) colored.getBukkitEntity()).setRabbitType(Rabbit.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if ((type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA)
                    && mechanism.requireEnum(false, Llama.Color.values())) {
                ((Llama) colored.getBukkitEntity()).setColor(Llama.Color.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.PARROT && mechanism.requireEnum(false, Parrot.Variant.values())) {
                ((Parrot) colored.getBukkitEntity()).setVariant(Parrot.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.SHULKER && mechanism.requireEnum(false, DyeColor.values())) {
                ((Shulker) colored.getBukkitEntity()).setColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.MUSHROOM_COW && mechanism.requireEnum(false, MushroomCow.Variant.values())) {
                ((MushroomCow) colored.getBukkitEntity()).setVariant(MushroomCow.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.TROPICAL_FISH && mechanism.requireObject(ListTag.class)) {
                ListTag list = mechanism.valueAsType(ListTag.class);
                TropicalFish fish = ((TropicalFish) colored.getBukkitEntity());
                String pattern = list.get(0);
                if (new ElementTag(pattern).matchesEnum(TropicalFish.Pattern.values())) {
                    fish.setPattern(TropicalFish.Pattern.valueOf(pattern.toUpperCase()));
                }
                else {
                    mechanism.echoError("Invalid tropical fish pattern specified: " + pattern);
                }
                if (list.size() > 1) {
                    String color = list.get(1);
                    if (new ElementTag(color).matchesEnum(DyeColor.values())) {
                        fish.setBodyColor(DyeColor.valueOf(color.toUpperCase()));
                    }
                    else {
                        mechanism.echoError("Invalid color specified: " + color);
                    }
                }
                if (list.size() > 2) {
                    String patternColor = list.get(2);
                    if (new ElementTag(patternColor).matchesEnum(DyeColor.values())) {
                        fish.setPatternColor(DyeColor.valueOf(patternColor.toUpperCase()));
                    }
                    else {
                        mechanism.echoError("Invalid pattern color specified: " + patternColor);
                    }
                }
            }
            else if (type == EntityType.FOX && mechanism.requireEnum(false, Fox.Type.values())) {
                ((Fox) colored.getBukkitEntity()).setFoxType(Fox.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.CAT && mechanism.requireObject(ListTag.class)) {
                Cat cat = (Cat) colored.getBukkitEntity();
                ListTag list = mechanism.valueAsType(ListTag.class);
                String catType = list.get(0);
                if (new ElementTag(catType).matchesEnum(Cat.Type.values())) {
                    cat.setCatType(Cat.Type.valueOf(catType.toUpperCase()));
                }
                else {
                    mechanism.echoError("Invalid cat type specified: " + catType);
                }
                if (list.size() > 1) {
                    String color = list.get(1);
                    if (new ElementTag(color).matchesEnum(DyeColor.values())) {
                        cat.setCollarColor(DyeColor.valueOf(list.get(1).toUpperCase()));
                    }
                    else {
                        mechanism.echoError("Invalid color specified: " + color);
                    }
                }
            }
            else if (type == EntityType.PANDA && mechanism.requireObject(ListTag.class)) {
                Panda panda = (Panda) colored.getBukkitEntity();
                ListTag list = mechanism.valueAsType(ListTag.class);
                String mainGene = list.get(0);
                if (new ElementTag(mainGene).matchesEnum(Panda.Gene.values())) {
                    panda.setMainGene(Panda.Gene.valueOf(mainGene.toUpperCase()));
                }
                else {
                    mechanism.echoError("Invalid panda gene specified: " + mainGene);
                }
                if (list.size() > 1) {
                    String hiddenGene = list.get(1);
                    if (new ElementTag(hiddenGene).matchesEnum(Panda.Gene.values())) {
                        panda.setHiddenGene(Panda.Gene.valueOf(hiddenGene.toUpperCase()));
                    }
                    else {
                        mechanism.echoError("Invalid panda hidden gene specified: " + hiddenGene);
                    }
                }
            }
            else if (type == EntityType.VILLAGER && mechanism.requireEnum(false, Villager.Type.values())) {
                ((Villager) colored.getBukkitEntity()).setVillagerType(Villager.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (type == EntityType.ARROW && mechanism.requireObject(ColorTag.class)) {
                ((Arrow) colored.getBukkitEntity()).setColor(mechanism.valueAsType(ColorTag.class).getColor());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && MultiVersionHelper1_17.colorIsApplicable(type)) {
                MultiVersionHelper1_17.setColor(colored.getBukkitEntity(), mechanism);
            }
            else { // Should never happen
                mechanism.echoError("Could not apply color '" + mechanism.getValue().toString() + "' to entity of type " + type.name() + ".");
            }
        }
    }
}

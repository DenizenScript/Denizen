package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizen.utilities.LegacyNamingHelper;
import com.denizenscript.denizen.utilities.MultiVersionHelper1_19;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.Arrays;

public class EntityColor extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name color
    // @input ElementTag
    // @description
    // If the entity can have a color, controls the entity's color.
    // For the available color options, refer to <@link language Entity Color Types>.
    // -->

    // TODO once 1.20 is the minimum supported version, can reference the enum directly
    public static final EntityType MOOSHROOM_ENTITY_TYPE = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft("mooshroom"));

    public static boolean describes(EntityTag entity) {
        EntityType type = entity.getBukkitEntityType();
        return type == EntityType.SHEEP ||
                type == EntityType.HORSE ||
                type == EntityType.WOLF ||
                type == EntityType.OCELOT ||
                type == EntityType.RABBIT ||
                type == EntityType.LLAMA ||
                type == EntityType.PARROT ||
                type == EntityType.SHULKER ||
                type == MOOSHROOM_ENTITY_TYPE ||
                type == EntityType.CAT ||
                type == EntityType.FOX ||
                type == EntityType.PANDA ||
                type == EntityType.ARROW ||
                type == EntityType.VILLAGER ||
                type == EntityType.ZOMBIE_VILLAGER ||
                type == EntityType.TRADER_LLAMA ||
                type == EntityType.TROPICAL_FISH ||
                type == EntityType.GOAT ||
                type == EntityType.AXOLOTL ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && MultiVersionHelper1_19.colorIsApplicable(type));
    }

    @Override
    public ElementTag getPropertyValue() {
        String color = getColor(true);
        return color == null ? null : new ElementTag(CoreUtilities.toLowerCase(color));
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    @Override
    public void setPropertyValue(ElementTag color, Mechanism mechanism) {
        EntityType type = getType();
        if (type == EntityType.HORSE && mechanism.requireObject(ListTag.class)) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            Horse horse = as(Horse.class);
            ElementTag horseColor = new ElementTag(list.get(0));
            if (horseColor.matchesEnum(Horse.Color.class)) {
                horse.setColor(horseColor.asEnum(Horse.Color.class));
            }
            else {
                mechanism.echoError("Invalid horse color specified: " + horseColor);
            }
            if (list.size() > 1) {
                ElementTag style = new ElementTag(list.get(1));
                if (style.matchesEnum(Horse.Style.class)) {
                    horse.setStyle(style.asEnum(Horse.Style.class));
                }
                else {
                    mechanism.echoError("Invalid horse style specified: " + style);
                }
            }
        }
        else if (type == EntityType.SHEEP && mechanism.requireEnum(DyeColor.class)) {
            as(Sheep.class).setColor(color.asEnum(DyeColor.class));
        }
        else if (type == EntityType.WOLF && mechanism.requireEnum(DyeColor.class)) {
            as(Wolf.class).setCollarColor(color.asEnum(DyeColor.class));
        }
        else if (type == EntityType.OCELOT && mechanism.requireEnum(Ocelot.Type.class)) { // TODO: Deprecate?
            as(Ocelot.class).setCatType(color.asEnum(Ocelot.Type.class));
        }
        else if (type == EntityType.RABBIT && mechanism.requireEnum(Rabbit.Type.class)) {
            as(Rabbit.class).setRabbitType(color.asEnum(Rabbit.Type.class));
        }
        else if ((type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) && mechanism.requireEnum(Llama.Color.class)) {
            as(Llama.class).setColor(color.asEnum(Llama.Color.class));
        }
        else if (type == EntityType.PARROT && mechanism.requireEnum(Parrot.Variant.class)) {
            as(Parrot.class).setVariant(color.asEnum(Parrot.Variant.class));
        }
        else if (type == EntityType.SHULKER && mechanism.requireEnum(DyeColor.class)) {
            as(Shulker.class).setColor(color.asEnum(DyeColor.class));
        }
        else if (type == MOOSHROOM_ENTITY_TYPE && mechanism.requireEnum(MushroomCow.Variant.class)) {
            as(MushroomCow.class).setVariant(color.asEnum(MushroomCow.Variant.class));
        }
        else if (type == EntityType.TROPICAL_FISH && mechanism.requireObject(ListTag.class)) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            TropicalFish fish = as(TropicalFish.class);
            ElementTag pattern = new ElementTag(list.get(0));
            if (pattern.matchesEnum(TropicalFish.Pattern.class)) {
                fish.setPattern(pattern.asEnum(TropicalFish.Pattern.class));
            }
            else {
                mechanism.echoError("Invalid tropical fish pattern specified: " + pattern);
            }
            if (list.size() > 1) {
                ElementTag fishColor = new ElementTag(list.get(1));
                if (fishColor.matchesEnum(DyeColor.class)) {
                    fish.setBodyColor(fishColor.asEnum(DyeColor.class));
                }
                else {
                    mechanism.echoError("Invalid color specified: " + fishColor);
                }
            }
            if (list.size() > 2) {
                ElementTag patternColor = new ElementTag(list.get(2));
                if (patternColor.matchesEnum(DyeColor.class)) {
                    fish.setPatternColor(patternColor.asEnum(DyeColor.class));
                }
                else {
                    mechanism.echoError("Invalid pattern color specified: " + patternColor);
                }
            }
        }
        else if (type == EntityType.FOX && mechanism.requireEnum(Fox.Type.class)) {
            as(Fox.class).setFoxType(color.asEnum(Fox.Type.class));
        }
        else if (type == EntityType.CAT && mechanism.requireObject(ListTag.class)) {
            Cat cat = as(Cat.class);
            ListTag list = mechanism.valueAsType(ListTag.class);
            String catTypeStr = list.get(0);
            Cat.Type catType = LegacyNamingHelper.convert(Cat.Type.class, catTypeStr);
            if (catType != null) {
                cat.setCatType(catType);
            }
            else {
                mechanism.echoError("Invalid cat type specified: " + catTypeStr);
            }
            if (list.size() > 1) {
                ElementTag collarColor = new ElementTag(list.get(1));
                if (collarColor.matchesEnum(DyeColor.class)) {
                    cat.setCollarColor(collarColor.asEnum(DyeColor.class));
                }
                else {
                    mechanism.echoError("Invalid color specified: " + collarColor);
                }
            }
        }
        else if (type == EntityType.PANDA && mechanism.requireObject(ListTag.class)) {
            Panda panda = as(Panda.class);
            ListTag list = mechanism.valueAsType(ListTag.class);
            ElementTag mainGene = new ElementTag(list.get(0));
            if (mainGene.matchesEnum(Panda.Gene.class)) {
                panda.setMainGene(mainGene.asEnum(Panda.Gene.class));
            }
            else {
                mechanism.echoError("Invalid panda gene specified: " + mainGene);
            }
            if (list.size() > 1) {
                ElementTag hiddenGene = new ElementTag(list.get(1));
                if (hiddenGene.matchesEnum(Panda.Gene.class)) {
                    panda.setHiddenGene(hiddenGene.asEnum(Panda.Gene.class));
                }
                else {
                    mechanism.echoError("Invalid panda hidden gene specified: " + hiddenGene);
                }
            }
        }
        // TODO This technically has registries on all supported versions
        else if (type == EntityType.VILLAGER) {
            LegacyNamingHelper.requireType(mechanism, Villager.Type.class).ifPresent(as(Villager.class)::setVillagerType);
        }
        else if (type == EntityType.ZOMBIE_VILLAGER) {
            LegacyNamingHelper.requireType(mechanism, Villager.Type.class).ifPresent(as(ZombieVillager.class)::setVillagerType);
        }
        else if (type == EntityType.ARROW && mechanism.requireObject(ColorTag.class)) {
            as(Arrow.class).setColor(BukkitColorExtensions.getColor(mechanism.valueAsType(ColorTag.class)));
        }
        else if (type == EntityType.GOAT) {
            as(Goat.class).setScreaming(color.asLowerString().equals("screaming"));
        }
        else if (type == EntityType.AXOLOTL && mechanism.requireEnum(Axolotl.Variant.class)) {
            as(Axolotl.class).setVariant(color.asEnum(Axolotl.Variant.class));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && MultiVersionHelper1_19.colorIsApplicable(type)) {
            MultiVersionHelper1_19.setColor(getEntity(), mechanism);
        }
    }

    public String getColor(boolean includeDeprecated) {
        EntityType type = getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && MultiVersionHelper1_19.colorIsApplicable(type)) {
            return MultiVersionHelper1_19.getColor(getEntity());
        }
        if (getEntity() instanceof MushroomCow mushroomCow) {
            return mushroomCow.getVariant().name();
        }
        return switch (type) {
            case HORSE -> {
                Horse horse = as(Horse.class);
                yield horse.getColor().name() + "|" + horse.getStyle().name();
            }
            case SHEEP -> as(Sheep.class).getColor().name();
            case WOLF -> as(Wolf.class).getCollarColor().name();
            case OCELOT -> {
                if (includeDeprecated) {
                    yield as(Ocelot.class).getCatType().name();
                }
                yield null;
            }
            case RABBIT -> as(Rabbit.class).getRabbitType().name();
            case LLAMA, TRADER_LLAMA -> as(Llama.class).getColor().name();
            case PARROT -> as(Parrot.class).getVariant().name();
            case SHULKER -> {
                DyeColor color = as(Shulker.class).getColor();
                yield  color == null ? null : color.name();
            }
            case TROPICAL_FISH -> {
                TropicalFish fish = as(TropicalFish.class);
                yield new ListTag(Arrays.asList(fish.getPattern().name(), fish.getBodyColor().name(), fish.getPatternColor().name())).identify();
            }
            case FOX -> as(Fox.class).getFoxType().name();
            case CAT -> {
                Cat cat = as(Cat.class);
                // TODO once 1.21 is the minimum supported version, replace with direct registry-based handling
                yield cat.getCatType() + "|" + cat.getCollarColor().name();
            }
            case PANDA -> {
                Panda panda = as(Panda.class);
                yield panda.getMainGene().name() + "|" + panda.getHiddenGene().name();
            }
            // TODO This technically has registries on all supported versions
            case VILLAGER -> String.valueOf(as(Villager.class).getVillagerType());
            case ZOMBIE_VILLAGER -> String.valueOf(as(ZombieVillager.class).getVillagerType());
            case ARROW -> {
                try {
                    yield BukkitColorExtensions.fromColor(as(Arrow.class).getColor()).identify();
                }
                catch (Exception e) {
                    yield null;
                }
            }
            case GOAT -> as(Goat.class).isScreaming() ? "screaming" : "normal";
            case AXOLOTL -> as(Axolotl.class).getVariant().name();
            default -> null;
        };
    }

    public ListTag getAllowedColors() {
        EntityType type = getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && MultiVersionHelper1_19.colorIsApplicable(type)) {
            return MultiVersionHelper1_19.getAllowedColors(type);
        }
        if (type == MOOSHROOM_ENTITY_TYPE) {
            return Utilities.listTypes(MushroomCow.Variant.class);
        }
        return switch (type) {
            case HORSE -> {
                ListTag horseColors = Utilities.listTypes(Horse.Color.class);
                horseColors.addAll(Utilities.listTypes(Horse.Style.class));
                yield horseColors;
            }
            case SHEEP, WOLF, SHULKER -> Utilities.listTypes(DyeColor.class);
            case RABBIT -> Utilities.listTypes(Rabbit.Type.class);
            case LLAMA, TRADER_LLAMA -> Utilities.listTypes(Llama.Color.class);
            case PARROT -> Utilities.listTypes(Parrot.Variant.class);
            case TROPICAL_FISH -> {
                ListTag patterns = Utilities.listTypes(TropicalFish.Pattern.class);
                patterns.addAll(Utilities.listTypes(DyeColor.class));
                yield patterns;
            }
            case FOX -> Utilities.listTypes(Fox.Type.class);
            case CAT -> Utilities.listTypes(Cat.Type.class);
            case PANDA -> Utilities.listTypes(Panda.Gene.class);
            // TODO This technically has registries on all supported versions
            case VILLAGER, ZOMBIE_VILLAGER -> Utilities.listTypes(Villager.Type.class);
            case GOAT -> {
                ListTag result = new ListTag();
                result.add("screaming");
                result.add("normal");
                yield result;
            }
            case AXOLOTL -> Utilities.listTypes(Axolotl.Variant.class);
            default -> null; // includes Ocelot (deprecated) and arrow (ColorTag)
        };
    }

    // <--[language]
    // @name Entity Color Types
    // @group Properties
    // @description
    // This is a quick rundown of the styling information used to handle the coloration of a mob in <@link property EntityTag.color>.
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
    // For villagers and zombie_villagers, the types are DESERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, and TAIGA.
    // For tropical_fish, the input is PATTERN|BODYCOLOR|PATTERNCOLOR, where BodyColor and PatterenColor are both DyeColor (see below),
    //          and PATTERN is KOB, SUNSTREAK, SNOOPER, DASHER, BRINELY, SPOTTY, FLOPPER, STRIPEY, GLITTER, BLOCKFISH, BETTY, is CLAYFISH.
    // For sheep, wolf, and shulker entities, the input is a Dye Color.
    // For Tipped Arrow entities, the input is a ColorTag.
    // For goats, the input is SCREAMING or NORMAL.
    // For axolotl, the types are BLUE, CYAN, GOLD, LUCY, or WILD.
    // For frogs, the types are TEMPERATE, WARM, or COLD.
    // For boats, type types are ACACIA, BAMBOO, BIRCH, CHERRY, DARK_OAK, JUNGLE, MANGROVE, OAK, or SPRUCE.
    //
    // For all places where a DyeColor is needed, the options are:
    // BLACK, BLUE, BROWN, CYAN, GRAY, GREEN, LIGHT_BLUE, LIGHT_GRAY, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, WHITE, or YELLOW.
    // -->

    public static void register() {
        autoRegister("color", EntityColor.class, ElementTag.class, false);

        // <--[tag]
        // @attribute <EntityTag.allowed_colors>
        // @returns ListTag
        // @mechanism EntityTag.color
        // @group properties
        // @description
        // If the entity can have a color, returns the list of allowed colors.
        // See also <@link language Entity Color Types>.
        // -->
        PropertyParser.registerTag(EntityColor.class, ListTag.class, "allowed_colors", (attribute, object) -> {
            return object.getAllowedColors();
        });
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.*;

import java.lang.invoke.MethodHandle;

public class EntityVariant extends EntityProperty<ElementTag> {

    // TODO: once the plugin.yml API version is 1.21, replace with direct method calls (see https://github.com/DenizenScript/Denizen/pull/2727)
    public static final MethodHandle COW_GET_VARIANT, COW_SET_VARIANT;

    static {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            Class<?> cowClass = ReflectionHelper.getClassOrThrow("org.bukkit.entity.Cow");
            COW_GET_VARIANT = ReflectionHelper.getMethodHandle(cowClass, "getVariant");
            COW_SET_VARIANT = ReflectionHelper.getMethodHandle(cowClass, "setVariant", Cow.Variant.class);
        }
        else {
            COW_GET_VARIANT = null;
            COW_SET_VARIANT = null;
        }
    }

    // <--[property]
    // @object EntityTag
    // @name variant
    // @input ElementTag
    // @description
    // Controls which variant a chicken, copper golem, cow, fox, frog, llama, parrot, pig, trader llama, rabbit, villager, wolf, zombie nautilus, or zombie villager is.
    // A list of valid chicken variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Chicken.Variant.html>.
    // A list of valid copper golem variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/CopperGolem.CopperWeatherState.html>.
    // A list of valid cow variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Cow.Variant.html>.
    // A list of valid fox variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Fox.Type.html>.
    // A list of valid frog variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Frog.Variant.html>.
    // A list of valid llama and trader llama variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Llama.Color.html>.
    // A list of valid parrot variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Parrot.Variant.html>.
    // A list of valid pig variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Pig.Variant.html>.
    // A list of valid rabbit variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Rabbit.Type.html>.
    // A list of valid villager and zombie villager variants can be found at <@link urlhttps://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Type.html>.
    // A list of valid wolf variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Wolf.Variant.html>.
    // A list of valid zombie nautilus variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/ZombieNautilus.Variant.html>.
    // -->

    public static boolean describes(EntityTag entityTag) {
        Entity entity = entityTag.getBukkitEntity();
        return entity instanceof Fox || entity instanceof Llama || entity instanceof Parrot
                || entity instanceof Rabbit || entity instanceof Villager || entity instanceof ZombieVillager
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof Frog)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && entity instanceof Wolf)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && (entity instanceof Chicken
                                                                        || entity instanceof CopperGolem
                                                                        || entity instanceof Cow
                                                                        || entity instanceof Pig
                                                                        || entity instanceof ZombieNautilus));
    }

    @Override
    public ElementTag getPropertyValue() {
        Entity entity = getEntity();
        if (entity instanceof Fox fox) {
            return new ElementTag(fox.getFoxType());
        }
        else if (entity instanceof Llama llama) {
            return new ElementTag(llama.getColor());
        }
        else if (entity instanceof Parrot parrot) {
            return new ElementTag(parrot.getVariant());
        }
        else if (entity instanceof Rabbit rabbit) {
            return new ElementTag(rabbit.getRabbitType());
        }
        else if (entity instanceof Villager villager) {
            return new ElementTag(Utilities.namespacedKeyToString(villager.getVillagerType().getKey()), true);
        }
        else if (entity instanceof ZombieVillager zombieVillager) {
            return new ElementTag(Utilities.namespacedKeyToString(zombieVillager.getVillagerType().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof Frog frog) {
            return new ElementTag(Utilities.namespacedKeyToString(frog.getVariant().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && entity instanceof Wolf wolf) {
            return new ElementTag(Utilities.namespacedKeyToString(wolf.getVariant().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Chicken chicken) {
            return new ElementTag(Utilities.namespacedKeyToString(chicken.getVariant().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof CopperGolem copperGolem) {
            return new ElementTag(PaperAPITools.instance.getCopperGolemVariant(copperGolem), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && COW_GET_VARIANT != null && entity instanceof Cow cow) {
            try {
                return new ElementTag(Utilities.namespacedKeyToString(((Cow.Variant) COW_GET_VARIANT.invoke(cow)).getKey()), true);
            }
            catch (Throwable e) {
                Debug.echoError(e);
                return null;
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Pig pig) {
            return new ElementTag(Utilities.namespacedKeyToString(pig.getVariant().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof ZombieNautilus zombieNautilus) {
            return new ElementTag(Utilities.namespacedKeyToString(zombieNautilus.getVariant().getKey()), true);
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag variant, Mechanism mechanism) {
        Entity entity = getEntity();
        if (entity instanceof Fox fox) {
            Fox.Type foxVariant = Utilities.elementToRequiredEnumLike(variant, Fox.Type.class, mechanism);
            if (foxVariant != null) {
                fox.setFoxType(foxVariant);
            }
        }
        else if (entity instanceof Llama llama) {
            Llama.Color llamaVariant = Utilities.elementToRequiredEnumLike(variant, Llama.Color.class, mechanism);
            if (llamaVariant != null) {
                llama.setColor(llamaVariant);
            }
        }
        else if (entity instanceof Parrot parrot) {
            Parrot.Variant parrotVariant = Utilities.elementToRequiredEnumLike(variant, Parrot.Variant.class, mechanism);
            if (parrotVariant != null) {
                parrot.setVariant(parrotVariant);
            }
        }
        else if (entity instanceof Rabbit rabbit) {
            Rabbit.Type rabbitVariant = Utilities.elementToRequiredEnumLike(variant, Rabbit.Type.class, mechanism);
            if (rabbitVariant != null) {
                rabbit.setRabbitType(rabbitVariant);
            }
        }
        else if (entity instanceof Villager || entity instanceof ZombieVillager) {
            Villager.Type villagerVariant = Utilities.elementToRequiredEnumLike(variant, Villager.Type.class, mechanism);
            if (villagerVariant != null) {
                if (entity instanceof Villager villager) {
                    villager.setVillagerType(villagerVariant);
                }
                else {
                    as(ZombieVillager.class).setVillagerType(villagerVariant);
                }
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof Frog frog) {
            Frog.Variant frogVariant = Utilities.elementToRequiredEnumLike(variant, Frog.Variant.class, mechanism);
            if (frogVariant != null) {
                frog.setVariant(frogVariant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && entity instanceof Wolf wolf) {
            Wolf.Variant wolfVariant = Utilities.elementToRequiredEnumLike(variant, Wolf.Variant.class, mechanism);
            if (wolfVariant != null) {
                wolf.setVariant(wolfVariant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Chicken chicken) {
            Chicken.Variant chickenVariant = Utilities.elementToRequiredEnumLike(variant, Chicken.Variant.class, mechanism);
            if (chickenVariant != null) {
                chicken.setVariant(chickenVariant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof CopperGolem copperGolem) {
            PaperAPITools.instance.setCopperGolemVariant(variant, copperGolem, mechanism);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && COW_SET_VARIANT != null && entity instanceof Cow cow) {
            Cow.Variant cowVariant = Utilities.elementToRequiredEnumLike(variant, Cow.Variant.class, mechanism);
            if (cowVariant != null) {
                try {
                    COW_SET_VARIANT.invoke(cow, cowVariant);
                }
                catch (Throwable e) {
                    Debug.echoError(e);
                }
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Pig pig) {
            Pig.Variant pigVariant = Utilities.elementToRequiredEnumLike(variant, Pig.Variant.class, mechanism);
            if (pigVariant != null) {
                pig.setVariant(pigVariant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof ZombieNautilus zombieNautilus) {
            ZombieNautilus.Variant zombieNautilusVariant = Utilities.elementToRequiredEnumLike(variant, ZombieNautilus.Variant.class, mechanism);
            if (zombieNautilusVariant != null) {
                zombieNautilus.setVariant(zombieNautilusVariant);
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "variant";
    }

    public static void register() {
        autoRegister("variant", EntityVariant.class, ElementTag.class, false);

        // <--[tag]
        // @attribute <EntityTag.allowed_variants>
        // @returns ListTag
        // @mechanism EntityTag.variant
        // @group properties
        // @description
        // If the entity can have a variant, returns the list of allowed variants.
        // See also <@link tag EntityTag.variant>.
        // -->
        PropertyParser.registerTag(EntityVariant.class, ListTag.class, "allowed_variants", (attribute, object) -> {
            Entity entity = object.getEntity();
            if (entity instanceof Fox) {
                return new ListTag(Utilities.listTypes(Fox.Type.class));
            }
            else if (entity instanceof Llama) {
                return new ListTag(Utilities.listTypes(Llama.Color.class));
            }
            else if (entity instanceof Parrot) {
                return new ListTag(Utilities.listTypes(Parrot.Variant.class));
            }
            else if (entity instanceof Rabbit) {
                return new ListTag(Utilities.listTypes(Rabbit.Type.class));
            }
            else if (entity instanceof Villager || entity instanceof ZombieVillager) {
                return new ListTag(Utilities.listTypes(Villager.Type.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof Frog) {
                return new ListTag(Utilities.listTypes(Frog.Variant.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && entity instanceof Wolf) {
                return new ListTag(Utilities.listTypes(Wolf.Variant.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Chicken) {
                return new ListTag(Utilities.listTypes(Chicken.Variant.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof CopperGolem) {
                return PaperAPITools.instance.getCopperGolemVariants();
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Cow) {
                return new ListTag(Utilities.listTypes(Cow.Variant.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof Pig) {
                return new ListTag(Utilities.listTypes(Pig.Variant.class));
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof ZombieNautilus) {
                return new ListTag(Utilities.listTypes(ZombieNautilus.Variant.class));
            }
            return null;
        });
    }
}

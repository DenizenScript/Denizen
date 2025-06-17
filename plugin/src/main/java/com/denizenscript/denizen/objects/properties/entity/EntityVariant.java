package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Wolf;

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
    // Controls which variant a chicken, cow, pig, or wolf is.
    // A list of valid chicken variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Chicken.Variant.html>.
    // A list of valid cow variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Cow.Variant.html>.
    // A list of valid pig variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Pig.Variant.html>.
    // A list of valid wolf variants can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Wolf.Variant.html>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Wolf
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity.getBukkitEntity() instanceof Chicken)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity.getBukkitEntity() instanceof Cow)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity.getBukkitEntity() instanceof Pig);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Wolf wolf) {
            return new ElementTag(Utilities.namespacedKeyToString(wolf.getVariant().getKey()), true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Chicken chicken) {
            return new ElementTag(Utilities.namespacedKeyToString(chicken.getVariant().getKey()), true);
        }
        else if (COW_GET_VARIANT != null && getEntity() instanceof Cow cow) {
            try {
                return new ElementTag(Utilities.namespacedKeyToString(((Cow.Variant) COW_GET_VARIANT.invoke(cow)).getKey()), true);
            }
            catch (Throwable e) {
                Debug.echoError(e);
                return null;
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Pig pig) {
            return new ElementTag(Utilities.namespacedKeyToString(pig.getVariant().getKey()), true);
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag variant, Mechanism mechanism) {
        if (getEntity() instanceof Wolf wolf) {
            Wolf.Variant wolfVariant = Utilities.elementToRequiredEnumLike(variant, Wolf.Variant.class, mechanism);
            if (wolfVariant != null) {
                wolf.setVariant(wolfVariant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Chicken chicken) {
            Chicken.Variant chickenVariant = Utilities.elementToRequiredEnumLike(variant, Chicken.Variant.class, mechanism);
            if (chickenVariant != null) {
                chicken.setVariant(chickenVariant);
            }
        }
        else if (COW_SET_VARIANT != null && getEntity() instanceof Cow cow) {
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Pig pig) {
            Pig.Variant pigVariant = Utilities.elementToRequiredEnumLike(variant, Pig.Variant.class, mechanism);
            if (pigVariant != null) {
                pig.setVariant(pigVariant);
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "variant";
    }

    public static void register() {
        autoRegister("variant", EntityVariant.class, ElementTag.class, false);
    }
}

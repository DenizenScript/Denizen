package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.*;

public class EntityVariant extends EntityProperty<ElementTag> {

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
            return new ElementTag(Utilities.namespacedKeyToString(wolf.getVariant().getKey()));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Chicken chicken) {
            return new ElementTag(Utilities.namespacedKeyToString(chicken.getVariant().getKey()));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Cow cow) {
            return new ElementTag(Utilities.namespacedKeyToString(cow.getVariant().getKey()));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Pig pig) {
            return new ElementTag(Utilities.namespacedKeyToString(pig.getVariant().getKey()));
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag variant, Mechanism mechanism) {
        if (getEntity() instanceof Wolf wolf) {
            Wolf.Variant entityVariety = Utilities.elementToEnumlike(variant, Wolf.Variant.class);
            if (entityVariety != null) {
                wolf.setVariant(entityVariety);
            }
            else {
                mechanism.echoError("Invalid wolf variant specified: " + variant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Chicken chicken) {
            Chicken.Variant entityVariety = Utilities.elementToEnumlike(variant, Chicken.Variant.class);
            if (entityVariety != null) {
                chicken.setVariant(entityVariety);
            }
            else {
                mechanism.echoError("Invalid chicken variant specified: " + variant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Cow cow) {
            Cow.Variant entityVariety = Utilities.elementToEnumlike(variant, Cow.Variant.class);
            if (entityVariety != null) {
                cow.setVariant(entityVariety);
            }
            else {
                mechanism.echoError("Invalid cow variant specified: " + variant);
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Pig pig) {
            Pig.Variant entityVariety = Utilities.elementToEnumlike(variant, Pig.Variant.class);
            if (entityVariety != null) {
                pig.setVariant(entityVariety);
            }
            else {
                mechanism.echoError("Invalid pig variant specified: " + variant);
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

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionType;

public class EntityPotionType extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name potion_type
    // @input ElementTag
    // @description
    // Controls an Arrow's base potion type, if any.
    // See <@link url https://minecraft.wiki/w/Potion#Item_data> for a list of potion types.
    // See <@link property EntityTag.potion_effects> to control the potion effects an arrow applies.
    // @mechanism
    // Specify no input to remove the base potion type.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Arrow;
    }

    @Override
    public ElementTag getPropertyValue() {
        PotionType type = as(Arrow.class).getBasePotionType();
        return type != null ? new ElementTag(Utilities.namespacedKeyToString(type.getKey()), true) : null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (value == null) {
            as(Arrow.class).setBasePotionType(null);
            return;
        }
        if (Utilities.requireEnumlike(mechanism, PotionType.class)) {
            as(Arrow.class).setBasePotionType(Utilities.elementToEnumlike(value, PotionType.class));
        }
    }

    @Override
    public String getPropertyId() {
        return "potion_type";
    }

    public static void register() {
        autoRegisterNullable("potion_type", EntityPotionType.class, ElementTag.class, false);
    }
}

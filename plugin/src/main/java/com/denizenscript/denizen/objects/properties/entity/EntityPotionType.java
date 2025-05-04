package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionType;

public class EntityPotionType extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name potion_type
    // @input ElementTag
    // @description
    // Controls an Arrow or Area Effect Cloud's base potion type, if any.
    // See <@link url https://minecraft.wiki/w/Potion#Item_data> for a list of potion types.
    // See <@link property EntityTag.potion_effects> to control the potion effects applied.
    // @mechanism
    // Specify no input to remove the base potion type.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Arrow || entity.getBukkitEntity() instanceof AreaEffectCloud;
    }

    @Override
    public ElementTag getPropertyValue() {
        PotionType type = getEntity() instanceof Arrow arrow ? arrow.getBasePotionType() : as(AreaEffectCloud.class).getBasePotionType();
        return type != null ? new ElementTag(Utilities.namespacedKeyToString(type.getKey()), true) : null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        PotionType type = null;
        if (value != null) {
            if (!Utilities.requireEnumlike(mechanism, PotionType.class)) {
                return;
            }
            type = Utilities.elementToEnumlike(value, PotionType.class);
        }
        if (getEntity() instanceof Arrow arrow) {
            arrow.setBasePotionType(type);
        }
        else {
            as(AreaEffectCloud.class).setBasePotionType(type);
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

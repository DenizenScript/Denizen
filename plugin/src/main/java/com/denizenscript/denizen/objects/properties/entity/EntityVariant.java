package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Wolf;

public class EntityVariant extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name variant
    // @input ElementTag
    // @description
    // Controls which variant a wolf is.
    // Valid variants: ASHEN, BLACK, CHESTNUT, PALE, RUSTY, SNOWY, SPOTTED, STRIPED, WOODS

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Wolf;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Wolf.class).getVariant().toString());
    }

    @Override
    public void setPropertyValue(ElementTag variant, Mechanism mechanism) {
        Wolf.Variant wolfVariety = Utilities.elementToEnumlike(variant, Wolf.Variant.class);
        if (wolfVariety != null) {
            as(Wolf.class).setVariant(wolfVariety);
        }
        else {
            mechanism.echoError("Invalid wolf variant specified: " + variant);
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

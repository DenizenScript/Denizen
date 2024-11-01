package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.attribute.AttributeInstance;

public class EntityArmorBonus implements Property {
    
    // <--[property]
    // @object EntityTag
    // @name armor_bonus
    // @input ElementTag(Decimal)
    // @description
    // Controls an entity's base armor bonus.
    // -->

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).isLivingEntity();
    }

    public static EntityArmorBonus getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArmorBonus((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "armor_bonus"
    };

    public EntityArmorBonus(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;
    
    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        getAttribute().setBaseValue(mechanism.getValue().asDouble());
    }

    @Override
    public String getPropertyString() {
        double value = getAttribute().getValue();
        return value > 0 ? String.valueOf(value) : null;
    }

    @Override
    public String getPropertyId() {
        return "armor_bonus";
    }

    public AttributeInstance getAttribute() {
        return entity.getLivingEntity().getAttribute(EntityHelper.ATTRIBUTE_ARMOR);
    }

    public static void register() {
        autoRegister("armor_bonus", EntityArmorBonus.class, ElementTag.class, false)
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

public class EntityArmorBonus implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).isLivingEntity();
    }

    public static EntityArmorBonus getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityArmorBonus((EntityTag) entity);
    }

    public static final String[] handledMechs = new String[] {
            "armor_bonus"
    };

    private EntityArmorBonus(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (getAttribute().getValue() > 0.0) {
            return getArmorBonus().asString();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "armor_bonus";
    }

    public ElementTag getArmorBonus() {
        return new ElementTag(getAttribute().getValue());
    }

    public AttributeInstance getAttribute() {
        return entity.getLivingEntity().getAttribute(Attribute.GENERIC_ARMOR);
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.armor_bonus>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.armor_bonus
        // @group attributes
        // @description
        // Returns the entity's base armor bonus.
        // -->
        PropertyParser.<EntityArmorBonus, ElementTag>registerTag(ElementTag.class, "armor_bonus", (attribute, object) -> {
            return object.getArmorBonus();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name armor_bonus
        // @input ElementTag(Decimal)
        // @description
        // Sets the entity's base armor bonus.
        // @tags
        // <EntityTag.armor_bonus>
        // -->
        if (mechanism.matches("armor_bonus") && mechanism.requireDouble()) {
            getAttribute().setBaseValue(mechanism.getValue().asDouble());
        }

    }
}

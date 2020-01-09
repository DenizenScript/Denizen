package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityArmorBonus implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).isLivingEntity();
    }

    public static EntityArmorBonus getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityArmorBonus((EntityTag) entity);
    }

    public static final String[] handledTags = new String[] {
            "armor_bonus"
    };

    public static final String[] handledMechs = new String[] {
            "armor_bonus"
    };

    private EntityArmorBonus(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (entity.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).getValue() > 0.0) {
            return getArmorBonus().asString();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "armor_bonus";
    }

    public ElementTag getArmorBonus() {
        return new ElementTag(entity.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).getValue());
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.armor_bonus>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.armor_bonus
        // @group attributes
        // @description
        // Returns the entity's base armor bonus.
        // -->
        if (attribute.startsWith("armor_bonus")) {
            return getArmorBonus().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
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
            entity.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR)
                    .setBaseValue(mechanism.getValue().asDouble());
        }

    }
}

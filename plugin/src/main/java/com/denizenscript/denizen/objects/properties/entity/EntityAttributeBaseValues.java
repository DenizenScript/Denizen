package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.AttributeInstance;

import java.util.Map;

public class EntityAttributeBaseValues implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Attributable;
    }

    public static EntityAttributeBaseValues getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAttributeBaseValues((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_attribute", "attribute_value", "attribute_base_value", "attribute_default_value"
    };

    public static final String[] handledMechs = new String[] {
            "attribute_base_values"
    };

    private EntityAttributeBaseValues(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public MapTag attributeBaseValues() {
        MapTag result = new MapTag();
        Attributable ent = (Attributable) entity.getBukkitEntity();
        for (org.bukkit.attribute.Attribute attr : org.bukkit.attribute.Attribute.values()) {
            AttributeInstance instance = ent.getAttribute(attr);
            if (instance != null) {
                result.putObject(attr.name(), new ElementTag(instance.getBaseValue()));
            }
        }
        return result;
    }

    @Override
    public String getPropertyString() {
        MapTag map = attributeBaseValues();
        if (map.map.isEmpty()) {
            return null;
        }
        return map.savable();
    }

    @Override
    public String getPropertyId() {
        return "attribute_base_values";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.has_attribute[<attribute>]>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity has the named attribute.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // -->
        if (attribute.startsWith("has_attribute") && attribute.hasParam()) {
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(
                    org.bukkit.attribute.Attribute.valueOf(attribute.getParam().toUpperCase()));
            return new ElementTag(instance != null).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.attribute_base_values
        // @group properties
        // @description
        // Returns the final calculated value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // -->
        if (attribute.startsWith("attribute_value") && attribute.hasParam()) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getParam().toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                return null;
            }
            return new ElementTag(instance.getValue()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_base_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.attribute_base_values
        // @group properties
        // @description
        // Returns the base value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // -->
        if (attribute.startsWith("attribute_base_value") && attribute.hasParam()) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getParam().toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                return null;
            }
            return new ElementTag(instance.getBaseValue()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_default_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.attribute_base_values
        // @group properties
        // @description
        // Returns the default value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // -->
        if (attribute.startsWith("attribute_default_value") && attribute.hasParam()) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getParam().toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                return null;
            }
            return new ElementTag(instance.getDefaultValue()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name attribute_base_values
        // @input MapTag
        // @description
        // Sets the base value of an entity's attributes.
        // Specify a MapTag where the keys are attribute names, and values are the new base values.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("attribute_base_values") && mechanism.requireObject(MapTag.class)) {
            MapTag input = mechanism.valueAsType(MapTag.class);
            Attributable ent = (Attributable) entity.getBukkitEntity();
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                    continue;
                }
                instance.setBaseValue(Double.parseDouble(subValue.getValue().toString()));
            }
        }
    }
}

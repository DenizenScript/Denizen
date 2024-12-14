package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.Map;

public class EntityAttributeBaseValues implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Attributable;
    }

    public static EntityAttributeBaseValues getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAttributeBaseValues((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "attribute_base_values"
    };

    public EntityAttributeBaseValues(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public MapTag attributeBaseValues() {
        MapTag result = new MapTag();
        Attributable ent = getAttributable();
        for (Attribute attr : Utilities.listTypesRaw(Attribute.class)) {
            AttributeInstance instance = ent.getAttribute(attr);
            if (instance != null) {
                result.putObject(Utilities.enumlikeToElement(attr).asString(), new ElementTag(instance.getBaseValue()));
            }
        }
        return result;
    }

    public Attributable getAttributable() {
        return (Attributable) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        MapTag map = attributeBaseValues();
        return map.isEmpty() ? null : map.savable();
    }

    @Override
    public String getPropertyId() {
        return "attribute_base_values";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.has_attribute[<attribute>]>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity has the named attribute.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // See also <@link language attribute modifiers>.
        // -->
        PropertyParser.registerTag(EntityAttributeBaseValues.class, ElementTag.class, "has_attribute", (attribute, object) -> {
            if (!(attribute.hasParam() && Utilities.matchesEnumlike(attribute.getParamElement(), Attribute.class))) {
                attribute.echoError("Invalid entity.has_attribute[...] input: must be a valid attribute name.");
                return null;
            }
            Attribute attr = Utilities.elementToEnumlike(attribute.getParamElement(), Attribute.class);
            return new ElementTag(object.getAttributable().getAttribute(attr) != null);
        });

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
        PropertyParser.registerTag(EntityAttributeBaseValues.class, ElementTag.class, "attribute_value", (attribute, object) -> {
            if (!(attribute.hasParam() && Utilities.matchesEnumlike(attribute.getParamElement(), Attribute.class))) {
                attribute.echoError("Invalid entity.attribute_value[...] input: must be a valid attribute name.");
                return null;
            }
            Attribute attr = Utilities.elementToEnumlike(attribute.getParamElement(), Attribute.class);
            AttributeInstance instance = object.getAttributable().getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + Utilities.enumlikeToElement(attr) + " is not applicable to entity of type " + object.entity.getBukkitEntityType().name());
                return null;
            }
            return new ElementTag(instance.getValue());
        });

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
        PropertyParser.registerTag(EntityAttributeBaseValues.class, ElementTag.class, "attribute_base_value", (attribute, object) -> {
            if (!(attribute.hasParam() && Utilities.matchesEnumlike(attribute.getParamElement(), Attribute.class))) {
                attribute.echoError("Invalid entity.attribute_base_value[...] input: must be a valid attribute name.");
                return null;
            }
            Attribute attr = Utilities.elementToEnumlike(attribute.getParamElement(), Attribute.class);
            AttributeInstance instance = object.getAttributable().getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + Utilities.enumlikeToElement(attr) + " is not applicable to entity of type " + object.entity.getBukkitEntityType().name());
                return null;
            }
            return new ElementTag(instance.getBaseValue());
        });

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
        PropertyParser.registerTag(EntityAttributeBaseValues.class, ElementTag.class, "attribute_default_value", (attribute, object) -> {
            if (!(attribute.hasParam() && Utilities.matchesEnumlike(attribute.getParamElement(), Attribute.class))) {
                attribute.echoError("Invalid entity.attribute_default_value[...] input: must be a valid attribute name.");
                return null;
            }
            Attribute attr = Utilities.elementToEnumlike(attribute.getParamElement(), Attribute.class);
            AttributeInstance instance = object.getAttributable().getAttribute(attr);
            if (instance == null) {
                attribute.echoError("Attribute " + Utilities.enumlikeToElement(attr) + " is not applicable to entity of type " + object.entity.getBukkitEntityType().name());
                return null;
            }
            return new ElementTag(instance.getDefaultValue());
        });
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
            Attributable ent = getAttributable();
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.entrySet()) {
                Attribute attr = Utilities.elementToEnumlike(new ElementTag(subValue.getKey().str), Attribute.class);
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + Utilities.enumlikeToElement(attr) + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                    continue;
                }
                ElementTag value = subValue.getValue().asElement();
                if (!value.isDouble()) {
                    mechanism.echoError("Invalid input '" + value + "': must be a decimal number.");
                    continue;
                }
                instance.setBaseValue(value.asDouble());
            }
        }
    }
}

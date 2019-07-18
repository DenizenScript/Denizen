package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.UUID;

public class EntityAttributes implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Attributable;
    }

    public static EntityAttributes getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAttributes((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_attribute", "attribute_value", "attribute_base_value", "attribute_default_value", "attributes"
    };

    public static final String[] handledMechs = new String[] {
            "attributes"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAttributes(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    public static String stringify(AttributeModifier modifier) {
        return EscapeTagBase.escape(modifier.getName()) + "/" + modifier.getAmount() + "/" + modifier.getOperation().name()
                + "/" + (modifier.getSlot() == null ? "any" : modifier.getSlot().name());
    }

    public ListTag getAttributes() {
        ListTag list = new ListTag();
        for (org.bukkit.attribute.Attribute attribute : org.bukkit.attribute.Attribute.values()) {
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            StringBuilder modifiers = new StringBuilder();
            for (AttributeModifier modifier : instance.getModifiers()) {
                modifiers.append("/").append(stringify(modifier));
            }
            list.add(EscapeTagBase.escape(attribute.name()) + "/" + instance.getBaseValue() + modifiers.toString());
        }
        return list;
    }

    @Override
    public String getPropertyString() {
        ListTag list = getAttributes();
        if (list.size() > 0) {
            return list.identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "attributes";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.attributes>
        // @returns ListTag
        // @mechanism attributes
        // @group properties
        // @description
        // Returns a list of all attributes on the entity, formatted in a way that can be sent back into the 'attributes' mechanism.
        // -->
        if (attribute.startsWith("attributes")) {
            return getAttributes().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_attribute[<attribute>]>
        // @returns ElementTag(Boolean)
        // @mechanism attributes
        // @group properties
        // @description
        // Returns whether the entity has the named attribute.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // -->
        if (attribute.startsWith("has_attribute") && attribute.hasContext(1)) {
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(
                    org.bukkit.attribute.Attribute.valueOf(attribute.getContext(1).toUpperCase()));
            return new ElementTag(instance != null).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism attributes
        // @group properties
        // @description
        // Returns the final calculated value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // -->
        if (attribute.startsWith("attribute_value") && attribute.hasContext(1)) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getContext(1).toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                }
                return null;
            }
            return new ElementTag(instance.getValue()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_base_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism attributes
        // @group properties
        // @description
        // Returns the base value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // -->
        if (attribute.startsWith("attribute_base_value") && attribute.hasContext(1)) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getContext(1).toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                }
                return null;
            }
            return new ElementTag(instance.getBaseValue()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_default_value[<attribute>]>
        // @returns ElementTag(Decimal)
        // @mechanism attributes
        // @group properties
        // @description
        // Returns the default value of the named attribute for the entity.
        // See also <@link tag EntityTag.has_attribute>.
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // -->
        if (attribute.startsWith("attribute_default_value") && attribute.hasContext(1)) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(attribute.getContext(1).toUpperCase());
            AttributeInstance instance = ((Attributable) entity.getBukkitEntity()).getAttribute(attr);
            if (instance == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                }
                return null;
            }
            return new ElementTag(instance.getDefaultValue()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name attributes
        // @input ListTag
        // @description
        // Changes the attributes of an entity.
        // Specify a list of attributes in the format: name/base/value/(modifier_name/mod_amount/mod_operation/mod_slot|...)
        // For example: GENERIC_ARMOR/5/boost/3/add_number/any/reduction/-5/add_number/chest
        // Note that the 'slot' value is probably just ignored.
        //
        // Valid operations are ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attributes>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("attributes") && mechanism.hasValue()) {
            Attributable ent = (Attributable) entity.getBukkitEntity();
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                List<String> subList = CoreUtilities.split(str, '/');
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(EscapeTagBase.unEscape(subList.get(0)).toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    Debug.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                    continue;
                }
                instance.setBaseValue(ArgumentHelper.getDoubleFrom(subList.get(1)));
                for (AttributeModifier modifier : instance.getModifiers()) {
                    instance.removeModifier(modifier);
                }
                for (int x = 2; x < subList.size(); x += 4) {
                    String slot = subList.get(x + 3).toUpperCase();
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), EscapeTagBase.unEscape(subList.get(x)),
                            ArgumentHelper.getDoubleFrom(subList.get(x + 1)), AttributeModifier.Operation.valueOf(subList.get(x + 2).toUpperCase()),
                                    slot.equals("ANY") ? null : EquipmentSlot.valueOf(slot));
                    instance.addModifier(modifier);
                }
            }
        }
    }
}

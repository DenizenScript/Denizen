package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityAttributeModifiers implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Attributable;
    }

    public static EntityAttributeModifiers getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAttributeModifiers((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "attributes", "attribute_modifiers"
    };

    public static final String[] handledMechs = new String[] {
            "attributes", "attribute_modifiers", "add_attribute_modifiers", "remove_attribute_modifiers"
    };

    private EntityAttributeModifiers(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Deprecated
    public static String stringify(AttributeModifier modifier) {
        return EscapeTagBase.escape(modifier.getName()) + "/" + modifier.getAmount() + "/" + modifier.getOperation().name()
                + "/" + (modifier.getSlot() == null ? "any" : modifier.getSlot().name());
    }

    @Deprecated
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

    public static MapTag mapify(AttributeModifier modifier) {
        MapTag result = new MapTag();
        result.putObject("name", new ElementTag(modifier.getName()));
        result.putObject("amount", new ElementTag(modifier.getAmount()));
        result.putObject("operation", new ElementTag(modifier.getOperation().name()));
        result.putObject("slot", new ElementTag(modifier.getSlot() == null ? "any" : modifier.getSlot().name()));
        result.putObject("id", new ElementTag(modifier.getUniqueId().toString()));
        return result;
    }

    public static AttributeModifier modiferForMap(org.bukkit.attribute.Attribute attr, MapTag map) {
        ObjectTag name = map.getObject("name");
        ObjectTag amount = map.getObject("amount");
        ObjectTag operation = map.getObject("operation");
        ObjectTag slot = map.getObject("slot");
        ObjectTag id = map.getObject("id");
        AttributeModifier.Operation operationValue;
        EquipmentSlot slotValue;
        UUID idValue;
        double amountValue;
        try {
            operationValue = AttributeModifier.Operation.valueOf(operation.toString().toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Attribute modifier operation '" + operation.toString() + "' does not exist.");
            return null;
        }
        try {
            idValue = id == null ? UUID.randomUUID() : UUID.fromString(id.toString());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Attribute modifier ID '" + id.toString() + "' is not a valid UUID.");
            return null;
        }
        try {
            slotValue = slot == null || CoreUtilities.equalsIgnoreCase(slot.toString(), "any") ? null : EquipmentSlot.valueOf(slot.toString().toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Attribute modifier slot '" + slot.toString() + "' is not a valid slot.");
            return null;
        }
        try {
            amountValue = Double.parseDouble(amount.toString());
        }
        catch (NumberFormatException ex) {
            Debug.echoError("Attribute modifier amount '" + amount.toString() + "' is not a valid decimal number.");
            return null;
        }
        return new AttributeModifier(idValue, name == null ? attr.name() : name.toString(), amountValue, operationValue, slotValue);
    }

    public ListTag getAttributeModifierList(AttributeInstance instance) {
        if (instance == null) {
            return null;
        }
        ListTag result = new ListTag();
        for (AttributeModifier modifier : instance.getModifiers()) {
            result.addObject(mapify(modifier));
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    public MapTag getAttributeModifiers() {
        MapTag map = new MapTag();
        for (org.bukkit.attribute.Attribute attribute : org.bukkit.attribute.Attribute.values()) {
            ListTag list = getAttributeModifierList(((Attributable) entity.getBukkitEntity()).getAttribute(attribute));
            if (list != null) {
                map.putObject(attribute.name(), list);
            }
        }
        return map;
    }

    @Override
    public String getPropertyString() {
        MapTag map = getAttributeModifiers();
        if (map.map.isEmpty()) {
            return null;
        }
        return map.savable();
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.attribute_modifiers>
        // @returns MapTag
        // @mechanism EntityTag.attribute_modifiers
        // @group properties
        // @description
        // Returns a map of all attribute modifiers on the entity, with key as the attribute name and value as a list of modifiers,
        // where each modifier is a MapTag containing keys 'name', 'amount', 'slot', 'operation', and 'id'.
        // This is formatted in a way that can be sent back into the 'attribute_modifiers' mechanism.
        // -->
        if (attribute.startsWith("attribute_modifiers")) {
            return getAttributeModifiers().getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("attributes")) {
            Deprecations.legacyAttributeProperties.warn(attribute.context);
            return getAttributes().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name attribute_modifiers
        // @input MapTag
        // @description
        // Sets the attribute modifiers of an entity.
        // This is a SET operation, meaning pre-existing modifiers are removed.
        // Specify a MapTag where the keys are attribute names, and values are a ListTag of modifiers,
        // where each modifier is itself a MapTag with required keys 'operation' and 'amount', and optional keys 'name', 'slot', and 'id'.
        // Valid operations: ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
        // Valid slots: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, ANY
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // The default ID will be randomly generated, the default name will be the attribute name, the default slot is any.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            MapTag input = mechanism.valueAsType(MapTag.class);
            Attributable ent = (Attributable) entity.getBukkitEntity();
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                    continue;
                }
                for (AttributeModifier modifier : instance.getModifiers()) {
                    instance.removeModifier(modifier);
                }
                for (ObjectTag listValue : (((ListTag) subValue.getValue()).objectForms)) {
                    instance.addModifier(modiferForMap(attr, (MapTag) listValue));
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name add_attribute_modifiers
        // @input MapTag
        // @description
        // Adds attribute modifiers to an entity without altering existing modifiers.
        // All input is the same as <@link mechanism EntityTag.attribute_modifiers>.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("add_attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            MapTag input = mechanism.valueAsType(MapTag.class);
            Attributable ent = (Attributable) entity.getBukkitEntity();
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                    continue;
                }
                for (ObjectTag listValue : (((ListTag) subValue.getValue()).objectForms)) {
                    instance.addModifier(modiferForMap(attr, (MapTag) listValue));
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_attribute_modifiers
        // @input ListTag
        // @description
        // Removes attribute modifiers from an entity. Specify a list of attribute names or modifier UUIDs as input.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("remove_attribute_modifiers") && mechanism.requireObject(ListTag.class)) {
            ArrayList<String> inputList = new ArrayList<>(mechanism.valueAsType(ListTag.class));
            Attributable ent = (Attributable) entity.getBukkitEntity();
            for (String toRemove : new ArrayList<>(inputList)) {
                if (new ElementTag(toRemove).matchesEnum(org.bukkit.attribute.Attribute.values())) {
                    inputList.remove(toRemove);
                    org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(toRemove.toUpperCase());
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                        continue;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                }
            }
            for (String toRemove : inputList) {
                UUID id = UUID.fromString(toRemove);
                for (org.bukkit.attribute.Attribute attr : org.bukkit.attribute.Attribute.values()) {
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        continue;
                    }
                    for (AttributeModifier modifer : instance.getModifiers()) {
                        if (modifer.getUniqueId().equals(id)) {
                            instance.removeModifier(modifer);
                            break;
                        }
                    }
                }
            }
        }

        if (mechanism.matches("attributes") && mechanism.hasValue()) {
            Deprecations.legacyAttributeProperties.warn(mechanism.context);
            Attributable ent = (Attributable) entity.getBukkitEntity();
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                List<String> subList = CoreUtilities.split(str, '/');
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(EscapeTagBase.unEscape(subList.get(0)).toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntity().getType().name());
                    continue;
                }
                instance.setBaseValue(Double.parseDouble(subList.get(1)));
                for (AttributeModifier modifier : instance.getModifiers()) {
                    instance.removeModifier(modifier);
                }
                for (int x = 2; x < subList.size(); x += 4) {
                    String slot = subList.get(x + 3).toUpperCase();
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), EscapeTagBase.unEscape(subList.get(x)),
                            Double.parseDouble(subList.get(x + 1)), AttributeModifier.Operation.valueOf(subList.get(x + 2).toUpperCase()),
                                    slot.equals("ANY") ? null : EquipmentSlot.valueOf(slot));
                    instance.addModifier(modifier);
                }
            }
        }
    }
}

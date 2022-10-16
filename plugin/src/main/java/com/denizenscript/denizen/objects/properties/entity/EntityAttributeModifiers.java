package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityAttributeModifiers implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Attributable;
    }

    public static EntityAttributeModifiers getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAttributeModifiers((EntityTag) entity);
        }
    }

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
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = getAttributable().getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            StringBuilder modifiers = new StringBuilder();
            for (AttributeModifier modifier : instance.getModifiers()) {
                modifiers.append("/").append(stringify(modifier));
            }
            list.add(EscapeTagBase.escape(attribute.name()) + "/" + instance.getBaseValue() + modifiers);
        }
        return list;
    }

    public static MapTag mapify(AttributeModifier modifier) {
        MapTag result = new MapTag();
        result.putObject("name", new ElementTag(modifier.getName()));
        result.putObject("amount", new ElementTag(modifier.getAmount()));
        result.putObject("operation", new ElementTag(modifier.getOperation()));
        result.putObject("slot", new ElementTag(modifier.getSlot() == null ? "any" : modifier.getSlot().name()));
        result.putObject("id", new ElementTag(modifier.getUniqueId().toString()));
        return result;
    }

    public static AttributeModifier modiferForMap(Attribute attr, MapTag map) {
        ElementTag name = map.getElement("name");
        ElementTag amount = map.getElement("amount");
        ElementTag operation = map.getElement("operation");
        ElementTag slot = map.getElement("slot", "any");
        ElementTag id = map.getElement("id");
        UUID idValue;
        double amountValue;
        AttributeModifier.Operation operationValue = operation.asEnum(AttributeModifier.Operation.class);
        if (operationValue == null) {
            Debug.echoError("Attribute modifier operation '" + operation + "' does not exist.");
            return null;
        }
        try {
            idValue = id == null ? UUID.randomUUID() : UUID.fromString(id.toString());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Attribute modifier ID '" + id + "' is not a valid UUID.");
            return null;
        }
        EquipmentSlot slotValue = CoreUtilities.equalsIgnoreCase(slot.toString(), "any") ? null : slot.asEnum(EquipmentSlot.class);
        try {
            amountValue = Double.parseDouble(amount.toString());
        }
        catch (NumberFormatException ex) {
            Debug.echoError("Attribute modifier amount '" + amount + "' is not a valid decimal number.");
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
        for (Attribute attribute : Attribute.values()) {
            ListTag list = getAttributeModifierList(getAttributable().getAttribute(attribute));
            if (list != null) {
                map.putObject(attribute.name(), list);
            }
        }
        return map;
    }

    public Attributable getAttributable() {
        return (Attributable) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        MapTag map = getAttributeModifiers();
        return map.map.isEmpty() ? null : map.savable();
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }


    // <--[language]
    // @name Attribute Modifiers
    // @group Properties
    // @description
    // In minecraft, the "attributes" system defined certain core numerical values on entities, such as max health or attack damage.
    // The value of an "attribute" is determined by its "base value" modified mathematically by each of its "attribute modififers".
    // "Attribute modifiers" can be added either directly to the entity, or onto items - when on an item, an entity can equip it into the correct slot to automatically apply the modifier.
    //
    // These can be read via such tags as <@link tag EntityTag.attribute_modifiers>, <@link tag ItemTag.attribute_modifiers>,
    // <@link tag EntityTag.has_attribute>, <@link tag EntityTag.attribute_value>, <@link tag EntityTag.attribute_base_value>, <@link tag EntityTag.attribute_default_value>, ...
    //
    // These can be modified by such mechanisms as <@link mechanism EntityTag.attribute_base_values>, <@link mechanism EntityTag.attribute_modifiers>, <@link mechanism EntityTag.add_attribute_modifiers>,
    // <@link mechanism EntityTag.remove_attribute_modifiers>, <@link mechanism ItemTag.attribute_modifiers>, <@link mechanism ItemTag.add_attribute_modifiers>, <@link mechanism ItemTag.remove_attribute_modifiers>, ...
    //
    // The input format of each of the 'add' and set mechanisms is slightly complicated:  a MapTag where the keys are attribute names, and values are a ListTag of modifiers,
    // where each modifier is itself a MapTag with required keys 'operation' and 'amount', and optional keys 'name', 'slot', and 'id'.
    //
    // Valid operations: ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
    // Valid slots: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, ANY
    // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
    // The default ID will be randomly generated, the default name will be the attribute name, the default slot is any.
    //
    // Operation names are based on the Bukkit enum.
    // ADD_NUMBER corresponds to Mojang "ADDITION" - adds on top of the base value.
    // ADD_SCALAR corresponds to Mojang "MULTIPLY_BASE" - adds to the total, multiplied by the base value.
    // MULTIPLY_SCALAR_1 corresponds to Mojang "MULTIPLY_TOTAL", multiplies the final value (after both "add_number" and "add_scaler") by the amount given plus one.
    //
    // They are combined like (pseudo-code):
    // <code>
    // - define x <[base_value]>
    // - foreach <all_modifiers[ADD_NUMBER]>:
    //     - define x:+:<[value]>
    // - define y <[x]>
    // - foreach <all_modifiers[ADD_SCALAR]>:
    //     - define y:+:<[x].mul[<[value]>]>
    // - foreach <all_modifiers[MULTIPLY_SCALAR_1]>:
    //     - define y:*:<[value].add[1]>
    // - determine <[y]>
    // </code>
    //
    // See also <@link url https://minecraft.fandom.com/wiki/Attribute#Modifiers>
    //
    // For a quick and dirty in-line input, you can do for example: [generic_max_health=<list[<map[operation=ADD_NUMBER;amount=20;slot=HEAD]>]>]
    //
    // For more clean/proper input, instead do something like:
    // <code>
    // - definemap attributes:
    //     generic_max_health:
    //         1:
    //             operation: ADD_NUMBER
    //             amount: 20
    //             slot: head
    // - inventory adjust slot:head add_attribute_modifiers:<[attributes]>
    // </code>
    //
    // When pre-defining a custom item, instead of any of this, simply use an item script: <@link language item script containers>. That page shows an example of valid attribute modifiers on an item script.
    //
    // -->

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.attribute_modifiers>
        // @returns MapTag
        // @mechanism EntityTag.attribute_modifiers
        // @group properties
        // @description
        // Returns a map of all attribute modifiers on the entity, with key as the attribute name and value as a list of modifiers,
        // where each modifier is a MapTag containing keys 'name', 'amount', 'slot', 'operation', and 'id'.
        // This is formatted in a way that can be sent back into the 'attribute_modifiers' mechanism.
        // See also <@link language attribute modifiers>.
        // -->
        PropertyParser.registerTag(EntityAttributeModifiers.class, MapTag.class, "attribute_modifiers", (attribute, object) -> {
            return object.getAttributeModifiers();
        });

        PropertyParser.registerTag(EntityAttributeModifiers.class, ListTag.class, "attributes", (attribute, object) -> {
            BukkitImplDeprecations.legacyAttributeProperties.warn(attribute.context);
            return object.getAttributes();
        });
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
        // For input format details, refer to <@link language attribute modifiers>.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            try {
                MapTag input = mechanism.valueAsType(MapTag.class);
                Attributable ent = getAttributable();
                for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                    Attribute attr = Attribute.valueOf(subValue.getKey().str.toUpperCase());
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        continue;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                    for (ObjectTag listValue : CoreUtilities.objectToList(subValue.getValue(), mechanism.context)) {
                        instance.addModifier(modiferForMap(attr, listValue.asType(MapTag.class, mechanism.context)));
                    }
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name add_attribute_modifiers
        // @input MapTag
        // @description
        // Adds attribute modifiers to an entity without altering existing modifiers.
        // For input format details, refer to <@link language attribute modifiers>.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("add_attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            try {
                MapTag input = mechanism.valueAsType(MapTag.class);
                Attributable ent = getAttributable();
                for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                    Attribute attr = Attribute.valueOf(subValue.getKey().str.toUpperCase());
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        continue;
                    }
                    for (ObjectTag listValue : CoreUtilities.objectToList(subValue.getValue(), mechanism.context)) {
                        instance.addModifier(modiferForMap(attr, listValue.asType(MapTag.class, mechanism.context)));
                    }
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_attribute_modifiers
        // @input ListTag
        // @description
        // Removes attribute modifiers from an entity. Specify a list of attribute names or modifier UUIDs as input.
        // See also <@link language attribute modifiers>.
        // @tags
        // <EntityTag.has_attribute>
        // <EntityTag.attribute_modifiers>
        // <EntityTag.attribute_default_value>
        // <EntityTag.attribute_base_value>
        // <EntityTag.attribute_value>
        // -->
        if (mechanism.matches("remove_attribute_modifiers") && mechanism.requireObject(ListTag.class)) {
            ArrayList<String> inputList = new ArrayList<>(mechanism.valueAsType(ListTag.class));
            Attributable ent = getAttributable();
            for (String toRemove : new ArrayList<>(inputList)) {
                if (new ElementTag(toRemove).matchesEnum(Attribute.class)) {
                    inputList.remove(toRemove);
                    Attribute attr = Attribute.valueOf(toRemove.toUpperCase());
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        continue;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                }
            }
            for (String toRemove : inputList) {
                UUID id = UUID.fromString(toRemove);
                for (Attribute attr : Attribute.values()) {
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
            BukkitImplDeprecations.legacyAttributeProperties.warn(mechanism.context);
            Attributable ent = getAttributable();
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                List<String> subList = CoreUtilities.split(str, '/');
                Attribute attr = Attribute.valueOf(EscapeTagBase.unEscape(subList.get(0)).toUpperCase());
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr.name() + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
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

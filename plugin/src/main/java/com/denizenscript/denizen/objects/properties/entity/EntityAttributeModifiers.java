package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.AttributeUtil;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
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

    public EntityAttributeModifiers(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

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
                modifiers.append("/").append(AttributeUtil.modifierToLegacyString(modifier));
            }
            list.add(EscapeTagUtil.escape(AttributeUtil.legacyName(attribute)) + "/" + instance.getBaseValue() + modifiers);
        }
        return list;
    }

    public MapTag getAttributeModifiers(boolean includeDeprecated) {
        MapTag map = new MapTag();
        for (Attribute attribute : Utilities.listTypesRaw(Attribute.class)) {
            AttributeInstance attributeInstance = getAttributable().getAttribute(attribute);
            if (attributeInstance != null) {
                AttributeUtil.addToMap(map, attribute, attributeInstance.getModifiers(), includeDeprecated);
            }
        }
        return map;
    }

    public Attributable getAttributable() {
        return (Attributable) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        MapTag map = getAttributeModifiers(false);
        return map.isEmpty() ? null : map.savable();
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.attribute_modifiers>
        // @returns MapTag
        // @mechanism EntityTag.attribute_modifiers
        // @group properties
        // @description
        // Returns a map of all attribute modifiers on the entity, with keys as attribute names and values as a list of modifiers,
        // see <@link language attribute modifiers> for how modifiers are formatted.
        // This is formatted in a way that can be sent back into the 'attribute_modifiers' mechanism.
        // -->
        PropertyParser.registerTag(EntityAttributeModifiers.class, MapTag.class, "attribute_modifiers", (attribute, object) -> {
            return object.getAttributeModifiers(true);
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
                AttributeUtil.parseModifiers(input, mechanism, attribute -> {
                    AttributeInstance instance = ent.getAttribute(attribute);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attribute + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        return null;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                    return instance;
                }, AttributeInstance::addModifier);
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
                AttributeUtil.parseModifiers(input, mechanism, attribute -> {
                    AttributeInstance instance = ent.getAttribute(attribute);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attribute + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        return null;
                    }
                    return instance;
                }, (attributeInstance, modifier) -> {
                    try {
                        attributeInstance.addModifier(modifier);
                    }
                    catch (IllegalArgumentException ex) {
                        if (!ex.getMessage().equals("Modifier is already applied on this attribute!")) {
                            throw ex;
                        }
                        if (AttributeUtil.MODERN_ATTRIBUTE_FORMAT) {
                            mechanism.echoError("Cannot add attribute with key '" + modifier.getKey() + "' as the entity already has a modifier with the same key.");
                        }
                        else {
                            mechanism.echoError("Cannot add attribute with ID '" + modifier.getUniqueId() + "' as the entity already has a modifier with the same ID.");
                        }
                    }
                });
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
        // Removes attribute modifiers from an entity. Specify a list of attribute names or modifier keys (UUIDs on versions below MC 1.21) as input.
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
                Attribute attr = Utilities.elementToEnumlike(new ElementTag(toRemove, true), Attribute.class);
                if (attr != null) {
                    inputList.remove(toRemove);
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        mechanism.echoError("Attribute " + attr + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                        continue;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                }
            }
            for (String toRemove : inputList) {
                UUID id = null;
                NamespacedKey key = null;
                if (AttributeUtil.MODERN_ATTRIBUTE_FORMAT) {
                    key = Utilities.parseNamespacedKey(toRemove);
                }
                else {
                    id = UUID.fromString(toRemove);
                }
                for (Attribute attr : Utilities.listTypesRaw(Attribute.class)) {
                    AttributeInstance instance = ent.getAttribute(attr);
                    if (instance == null) {
                        continue;
                    }
                    for (AttributeModifier modifier : instance.getModifiers()) {
                        if (AttributeUtil.MODERN_ATTRIBUTE_FORMAT ? modifier.getKey().equals(key) : modifier.getUniqueId().equals(id)) {
                            instance.removeModifier(modifier);
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
                Attribute attr = Utilities.elementToEnumlike(new ElementTag(EscapeTagUtil.unEscape(subList.get(0)), true), Attribute.class);
                AttributeInstance instance = ent.getAttribute(attr);
                if (instance == null) {
                    mechanism.echoError("Attribute " + attr + " is not applicable to entity of type " + entity.getBukkitEntityType().name());
                    continue;
                }
                instance.setBaseValue(Double.parseDouble(subList.get(1)));
                for (AttributeModifier modifier : instance.getModifiers()) {
                    instance.removeModifier(modifier);
                }
                for (int x = 2; x < subList.size(); x += 4) {
                    String slot = subList.get(x + 3).toUpperCase();
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), EscapeTagUtil.unEscape(subList.get(x)),
                            Double.parseDouble(subList.get(x + 1)), AttributeModifier.Operation.valueOf(subList.get(x + 2).toUpperCase()),
                                    slot.equals("ANY") ? null : EquipmentSlot.valueOf(slot));
                    instance.addModifier(modifier);
                }
            }
        }
    }
}

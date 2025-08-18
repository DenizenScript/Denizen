package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AttributeUtil {


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
    // The input format of each of the 'add' and 'set' mechanisms is slightly complicated: a MapTag where the keys are attribute names, and values are a ListTag of modifiers.
    // Modifiers are MapTags with required keys 'operation' and 'amount', and additionally:
    // Before MC 1.21: optional 'name', 'slot', and 'id' keys.
    // The default ID will be randomly generated, the default name will be the attribute name.
    // After MC 1.21: required 'key' key, and optional 'slot'.
    // The 'key' is the attribute's name/identifier in a "namespace:key" format (defaulting to the "minecraft" namespace), which has to be distinct to other modifiers of the same type on the object.
    //
    // Valid operations: ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
    // Valid slots (used up to MC 1.20.6): HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, ANY
    // Valid slot groups (used on MC 1.20.6+): <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlotGroup.html>
    // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
    // The default slot/slot group is "any".
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
    // See also <@link url https://minecraft.wiki/w/Attribute#Modifiers>.
    //
    // For a quick and dirty in-line input, you can do for example: [max_health=<list[<map[key=my_project:add_health;operation=ADD_NUMBER;amount=20;slot=HEAD]>]>]
    //
    // For more clean/proper input, instead do something like:
    // <code>
    // - definemap attributes:
    //     max_health:
    //         1:
    //             key: my_project:add_health
    //             operation: ADD_NUMBER
    //             amount: 20
    //             slot: head
    // - inventory adjust slot:head add_attribute_modifiers:<[attributes]>
    // </code>
    //
    // When pre-defining a custom item, instead of this, simply use an item script: <@link language item script containers>. That page shows an example of valid attribute modifiers on an item script.
    //
    // -->

    public static final boolean MODERN_ATTRIBUTE_FORMAT = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21);

    public static String legacyName(Attribute attribute) {
        if (!MODERN_ATTRIBUTE_FORMAT) {
            return String.valueOf(attribute); // Enum on older versions, #toString == #name
        }
        String nameLower = attribute.getKey().getKey();
        return switch (nameLower) {
            case "block_interaction_range", "entity_interaction_range", "block_break_speed" -> "player_" + nameLower;
            case "spawn_reinforcements" -> "zombie_" + nameLower;
            default -> "generic_" + nameLower;
        };
    }

    public static void addToMap(MapTag map, Attribute attribute, ListTag value, boolean includeDeprecated) {
        if (!MODERN_ATTRIBUTE_FORMAT) {
            map.putObject(String.valueOf(attribute), value);
            return;
        }
        if (includeDeprecated && Settings.cache_legacySpigotNamesSupport) {
            map.putObject(legacyName(attribute), value);
        }
        map.putObject(Utilities.namespacedKeyToString(attribute.getKey()), value);
    }

    public static void parseModifiers(MapTag input, Mechanism mechanism, BiConsumer<Attribute, AttributeModifier> handler) {
        parseModifiers(input, mechanism, Function.identity(), handler);
    }

    public static <T> void parseModifiers(MapTag input, Mechanism mechanism, Function<Attribute, T> attrToHolder, BiConsumer<@NotNull T, @NotNull AttributeModifier> handler) {
        final Map<Attribute, ObjectTag> unappliedLegacyValues = MODERN_ATTRIBUTE_FORMAT && Settings.cache_legacySpigotNamesSupport ? new HashMap<>() : null;
        for (Map.Entry<StringHolder, ObjectTag> mapEntry : input.entrySet()) {
            Attribute attr;
            String keyLow = mapEntry.getKey().low;
            if (!MODERN_ATTRIBUTE_FORMAT) {
                attr = ElementTag.asEnum(Attribute.class, keyLow);
            }
            else {
                attr = Registry.ATTRIBUTE.get(Utilities.parseNamespacedKey(keyLow));
                if (unappliedLegacyValues != null) {
                    if (attr != null) {
                        unappliedLegacyValues.remove(attr);
                    }
                    else if (keyLow.startsWith("generic_") || keyLow.startsWith("player_") || keyLow.startsWith("zombie_")) {
                        Attribute attribute = Utilities.elementToEnumlike(new ElementTag(keyLow, true), Attribute.class, false);
                        if (attribute != null) {
                            unappliedLegacyValues.put(attribute, mapEntry.getValue());
                            continue;
                        }
                    }
                }
            }
            if (attr == null) {
                mechanism.echoError("Invalid attribute specified: " + mapEntry.getKey() + '.');
                continue;
            }
            T holder = attrToHolder.apply(attr);
            if (holder == null) {
                continue;
            }
            for (ObjectTag listValue : CoreUtilities.objectToList(mapEntry.getValue(), mechanism.context)) {
                handler.accept(holder, parseModifier(attr, (MapTag) listValue, mechanism.context));
            }
        }
        if (unappliedLegacyValues != null && !unappliedLegacyValues.isEmpty()) {
            BukkitImplDeprecations.oldSpigotNames.warn(mechanism.context);
            for (Map.Entry<Attribute, ObjectTag> unappliedEntry : unappliedLegacyValues.entrySet()) {
                Attribute attribute = unappliedEntry.getKey();
                T holder = attrToHolder.apply(attribute);
                if (holder == null) {
                    continue;
                }
                for (ObjectTag listValue : CoreUtilities.objectToList(unappliedEntry.getValue(), mechanism.context)) {
                    handler.accept(holder, parseModifier(attribute, (MapTag) listValue, mechanism.context));
                }
            }
        }
    }

    public static MapTag modifierToMap(AttributeModifier modifier, boolean includeDeprecated) {
        MapTag result = new MapTag();
        result.putObject("amount", new ElementTag(modifier.getAmount()));
        result.putObject("operation", new ElementTag(modifier.getOperation()));
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            result.putObject("slot", new ElementTag(modifier.getSlotGroup().toString(), true));
        }
        else {
            result.putObject("slot", new ElementTag(modifier.getSlot() == null ? "any" : modifier.getSlot().name(), true));
        }
        if (MODERN_ATTRIBUTE_FORMAT) {
            result.putObject("key", new ElementTag(Utilities.namespacedKeyToString(modifier.getKey()), true));
        }
        // TODO: remove/deprecate these
        if (!MODERN_ATTRIBUTE_FORMAT || includeDeprecated) {
            result.putObject("id", new ElementTag(modifier.getUniqueId().toString(), true));
            result.putObject("name", new ElementTag(modifier.getName(), true));
        }
        return result;
    }

    public static AttributeModifier parseModifier(Attribute attr, MapTag map, TagContext context) {
        ElementTag amount = map.getElement("amount");
        ElementTag operation = map.getElement("operation");
        double amountValue;
        AttributeModifier.Operation operationValue = operation.asEnum(AttributeModifier.Operation.class);
        if (operationValue == null) {
            Debug.echoError("Attribute modifier operation '" + operation + "' does not exist.");
            return null;
        }
        try {
            amountValue = Double.parseDouble(amount.toString());
        }
        catch (NumberFormatException ex) {
            Debug.echoError("Attribute modifier amount '" + amount + "' is not a valid decimal number.");
            return null;
        }
        if (!MODERN_ATTRIBUTE_FORMAT) {
            return parseLegacyModifier(attr, map, amountValue, operationValue);
        }
        ElementTag key = map.getElement("key");
        if (key == null && map.size() >= 2) {
            BukkitImplDeprecations.pre1_21AttributeFormat.warn(context);
            return parseLegacyModifier(attr, map, amountValue, operationValue);
        }
        if (key == null) {
            Debug.echoError("Must specify a key.");
            return null;
        }
        String slotGroupName = map.getElement("slot", "any").asString();
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(slotGroupName);
        if (group == null) {
            EquipmentSlot slot = ElementTag.asEnum(EquipmentSlot.class, slotGroupName);
            if (slot == null) {
                Debug.echoError("Invalid equipment slot group specified: " + slotGroupName);
                return null;
            }
            group = slot.getGroup();
        }
        return new AttributeModifier(Utilities.parseNamespacedKey(key.asString()), amountValue, operationValue, group);
    }

    @Deprecated(forRemoval = true)
    public static String modifierToLegacyString(AttributeModifier modifier) {
        return EscapeTagUtil.escape(modifier.getName()) + "/" + modifier.getAmount() + "/" + modifier.getOperation().name()
                + "/" + (modifier.getSlot() == null ? "any" : modifier.getSlot().name());
    }

    @Deprecated(forRemoval = true)
    public static AttributeModifier parseLegacyModifier(Attribute attr, MapTag map, double amount, AttributeModifier.Operation operation) {
        ElementTag name = map.getElement("name");
        ElementTag slot = map.getElement("slot", "any");
        ElementTag id = map.getElement("id");
        UUID idValue;
        try {
            idValue = id == null ? UUID.randomUUID() : UUID.fromString(id.toString());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Attribute modifier ID '" + id + "' is not a valid UUID.");
            return null;
        }
        EquipmentSlot slotValue = CoreUtilities.equalsIgnoreCase(slot.toString(), "any") ? null : slot.asEnum(EquipmentSlot.class);
        if (slotValue == null && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            EquipmentSlotGroup group = EquipmentSlotGroup.getByName(slot.asString());
            if (group == null) {
                Debug.echoError("Invalid equipment slot group specified: " + slot);
                return null;
            }
            return new AttributeModifier(idValue, name == null ? AttributeUtil.legacyName(attr) : name.asString(), amount, operation, group);
        }
        return new AttributeModifier(idValue, name == null ? AttributeUtil.legacyName(attr) : name.toString(), amount, operation, slotValue);
    }
}

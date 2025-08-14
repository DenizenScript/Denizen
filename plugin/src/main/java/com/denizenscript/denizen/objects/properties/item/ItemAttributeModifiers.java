package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAttributeModifiers;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ItemAttributeModifiers extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name attribute_modifiers
    // @input MapTag
    // @description
    // Controls the attribute modifiers of an item, with key as the attribute name and value as a list of modifiers,
    // where each modifier is a MapTag containing keys 'name', 'amount', 'slot', 'operation', and 'id'.
    // For use as a mechanism, this is a SET operation, meaning pre-existing modifiers are removed.
    // For format details, refer to <@link language attribute modifiers>.
    // -->

    public static boolean describes(ItemTag item) {
        return true;
    }
    
    public static final boolean MODERN_ATTRIBUTE_FORMAT = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21);

    public static void parseAttributeModifiers(MapTag input, Mechanism mechanism, BiConsumer<org.bukkit.attribute.Attribute, AttributeModifier> handler) {
        parseAttributeModifiers(input, mechanism, Function.identity(), handler);
    }

    public static <T> void parseAttributeModifiers(MapTag input, Mechanism mechanism, Function<org.bukkit.attribute.Attribute, T> attrToHolder, BiConsumer<@NotNull T, @NotNull AttributeModifier> handler) {
        final Map<org.bukkit.attribute.Attribute, ObjectTag> unappliedLegacyValues = MODERN_ATTRIBUTE_FORMAT && Settings.cache_legacySpigotNamesSupport ? new HashMap<>() : null;
        for (Map.Entry<StringHolder, ObjectTag> mapEntry : input.entrySet()) {
            org.bukkit.attribute.Attribute attr;
            String keyLow = mapEntry.getKey().low;
            if (!MODERN_ATTRIBUTE_FORMAT) {
                attr = ElementTag.asEnum(org.bukkit.attribute.Attribute.class, keyLow);
                Debug.log("Got old attribute " + attr);
            }
            else {
                attr = Registry.ATTRIBUTE.get(Utilities.parseNamespacedKey(keyLow));
                if (unappliedLegacyValues != null) {
                    if (attr != null) {
                        Debug.log("Got modern attribute " + attr);
                        if (unappliedLegacyValues.remove(attr) != null) {
                            Debug.log("Cleared legacy attribute " + attr);
                        }
                    }
                    else if (keyLow.startsWith("generic_") || keyLow.startsWith("player_") || keyLow.startsWith("zombie_")) {
                        org.bukkit.attribute.Attribute attribute = Utilities.elementToEnumlike(new ElementTag(keyLow, true), org.bukkit.attribute.Attribute.class, false);
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
                handler.accept(holder, EntityAttributeModifiers.modiferForMap(attr, (MapTag) listValue, mechanism.context));
            }
        }
        if (unappliedLegacyValues != null && !unappliedLegacyValues.isEmpty()) {
            BukkitImplDeprecations.oldSpigotNames.warn(mechanism.context);
            for (Map.Entry<org.bukkit.attribute.Attribute, ObjectTag> unappliedEntry : unappliedLegacyValues.entrySet()) {
                org.bukkit.attribute.Attribute attribute = unappliedEntry.getKey();
                T holder = attrToHolder.apply(attribute);
                if (holder == null) {
                    continue;
                }
                Debug.log("Applying legacy attribute " + attribute);
                for (ObjectTag listValue : CoreUtilities.objectToList(unappliedEntry.getValue(), mechanism.context)) {
                    handler.accept(holder, EntityAttributeModifiers.modiferForMap(attribute, (MapTag) listValue, mechanism.context));
                }
            }
        }
    }

    @Override
    public boolean isDefaultValue(MapTag map) {
        return map.isEmpty();
    }

    @Override
    public MapTag getPropertyValue() {
        return getItemMeta() != null ? getAttributeModifiersFor(getItemMeta().getAttributeModifiers(), false) : null;
    }

    @Override
    public MapTag getTagValue(com.denizenscript.denizencore.tags.Attribute attribute) {
        return getItemMeta() != null ? getAttributeModifiersFor(getItemMeta().getAttributeModifiers(), true) : null;
    }

    @Override
    public void setPropertyValue(MapTag param, Mechanism mechanism) {
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = LinkedHashMultimap.create();
        parseAttributeModifiers(param, mechanism, metaMap::put);
        ItemMeta meta = getItemMeta();
        meta.setAttributeModifiers(metaMap);
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }

    public static String legacyAttributeName(org.bukkit.attribute.Attribute attribute) {
        if (!MODERN_ATTRIBUTE_FORMAT) {
            return attribute.toString(); // Enum on older versions, #toString == #name
        }
        String nameLower = attribute.getKey().getKey();
        return switch (nameLower) {
            case "block_interaction_range", "entity_interaction_range", "block_break_speed" -> "player_" + nameLower;
            case "spawn_reinforcements" -> "zombie_" + nameLower;
            default -> "generic_" + nameLower;
        };
    }

    public static void addAttributeToMap(MapTag map, org.bukkit.attribute.Attribute attribute, ListTag value, boolean includeDeprecated) {
        if (!MODERN_ATTRIBUTE_FORMAT) {
            map.putObject(attribute.toString(), value);
            return;
        }
        if (includeDeprecated && Settings.cache_legacySpigotNamesSupport) {
            map.putObject(legacyAttributeName(attribute), value);
        }
        map.putObject(Utilities.namespacedKeyToString(attribute.getKey()), value);
    }

    public static MapTag getAttributeModifiersFor(Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap, boolean includeDeprecated) {
        MapTag map = new MapTag();
        if (metaMap == null) {
            return map;
        }
        for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
            Collection<AttributeModifier> modifiers = metaMap.get(attribute);
            if (modifiers.isEmpty()) {
                continue;
            }
            addAttributeToMap(map, attribute, new ListTag(modifiers, EntityAttributeModifiers::mapify), includeDeprecated);
        }
        return map;
    }

    public static void register() {
        autoRegister("attribute_modifiers", ItemAttributeModifiers.class, MapTag.class, false);

        // <--[tag]
        // @attribute <ItemTag.default_attribute_modifiers[<slot>]>
        // @returns MapTag
        // @group properties
        // @description
        // Returns a map of all default attribute modifiers on the item based purely on its material type, for the given slot,
        // in the same format as <@link tag ItemTag.attribute_modifiers>
        // Slot must be one of: HAND, OFF_HAND, FEET, LEGS, CHEST, or HEAD
        // -->
        PropertyParser.registerTag(ItemAttributeModifiers.class, MapTag.class, "default_attribute_modifiers", (attribute, prop) -> {
            if (!attribute.hasParam() || !NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                return null;
            }
            EquipmentSlot slot = attribute.getParamElement().asEnum(EquipmentSlot.class);
            if (slot == null) {
                attribute.echoError("Invalid slot specified: " + attribute.getParam());
                return null;
            }
            return getAttributeModifiersFor(prop.getMaterial().getDefaultAttributeModifiers(slot), true);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name add_attribute_modifiers
        // @input MapTag
        // @description
        // Adds attribute modifiers to an item without altering existing modifiers.
        // For input format details, refer to <@link language attribute modifiers>.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        PropertyParser.registerMechanism(ItemAttributeModifiers.class, MapTag.class, "add_attribute_modifiers", (prop, mechanism, param) -> {
            ItemMeta meta = prop.getItemMeta();
            parseAttributeModifiers(param, mechanism, meta::addAttributeModifier);
            prop.setItemMeta(meta);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name remove_attribute_modifiers
        // @input ListTag
        // @description
        // Removes attribute modifiers from an item. Specify a list of attribute names or modifier keys (UUIDs on versions below MC 1.21) as input.
        // See also <@link language attribute modifiers>.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        PropertyParser.registerMechanism(ItemAttributeModifiers.class, ListTag.class, "remove_attribute_modifiers", (prop, mechanism, param) -> {
            ItemMeta meta = prop.getItemMeta();
            ArrayList<String> inputList = new ArrayList<>(param);
            for (String toRemove : new ArrayList<>(inputList)) {
                org.bukkit.attribute.Attribute attr = Utilities.elementToEnumlike(new ElementTag(toRemove, true), org.bukkit.attribute.Attribute.class);
                if (attr != null) {
                    inputList.remove(toRemove);
                    meta.removeAttributeModifier(attr);
                }
            }
            for (String toRemove : inputList) {
                UUID id = null;
                NamespacedKey key = null;
                boolean is1_21 = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21);
                if (is1_21) {
                    key = Utilities.parseNamespacedKey(toRemove);
                }
                else {
                    id = UUID.fromString(toRemove);
                }
                Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
                for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
                    for (AttributeModifier modifer : metaMap.get(attribute)) {
                        if (is1_21 ? modifer.getKey().equals(key) : modifer.getUniqueId().equals(id)) {
                            meta.removeAttributeModifier(attribute, modifer);
                            break;
                        }
                    }
                }
            }
            prop.setItemMeta(meta);
        });
    }
}

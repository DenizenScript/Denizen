package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAttributeModifiers;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public boolean isDefaultValue(MapTag map) {
        return map.isEmpty();
    }

    @Override
    public MapTag getPropertyValue() {
        ItemMeta meta = getItemMeta();
        if (meta == null) {
            return null;
        }
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
        return getAttributeModifiersFor(metaMap);
    }

    @Override
    public void setPropertyValue(MapTag param, Mechanism mechanism) {
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = LinkedHashMultimap.create();
        for (Map.Entry<StringHolder, ObjectTag> mapEntry : param.entrySet()) {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(mapEntry.getKey().str.toUpperCase());
            for (ObjectTag listValue : CoreUtilities.objectToList(mapEntry.getValue(), mechanism.context)) {
                metaMap.put(attr, EntityAttributeModifiers.modiferForMap(attr, (MapTag) listValue));
            }
        }
        ItemMeta meta = getItemMeta();
        meta.setAttributeModifiers(metaMap);
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }

    public static MapTag getAttributeModifiersFor(Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap) {
        MapTag map = new MapTag();
        if (metaMap == null) {
            return map;
        }
        for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
            Collection<AttributeModifier> modifiers = metaMap.get(attribute);
            if (modifiers.isEmpty()) {
                continue;
            }
            ListTag subList = new ListTag();
            for (AttributeModifier modifier : modifiers) {
                subList.addObject(EntityAttributeModifiers.mapify(modifier));
            }
            map.putObject(attribute.name(), subList);
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
            return getAttributeModifiersFor(prop.getMaterial().getDefaultAttributeModifiers(slot));
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
            for (Map.Entry<StringHolder, ObjectTag> subValue : param.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                for (ObjectTag listValue : CoreUtilities.objectToList(subValue.getValue(), mechanism.context)) {
                    meta.addAttributeModifier(attr, EntityAttributeModifiers.modiferForMap(attr, (MapTag) listValue));
                }
            }
            prop.setItemMeta(meta);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name remove_attribute_modifiers
        // @input ListTag
        // @description
        // Removes attribute modifiers from an item. Specify a list of attribute names or modifier UUIDs as input.
        // See also <@link language attribute modifiers>.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        PropertyParser.registerMechanism(ItemAttributeModifiers.class, ListTag.class, "remove_attribute_modifiers", (prop, mechanism, param) -> {
            ItemMeta meta = prop.getItemMeta();
            ArrayList<String> inputList = new ArrayList<>(param);
            for (String toRemove : new ArrayList<>(inputList)) {
                if (new ElementTag(toRemove).matchesEnum(org.bukkit.attribute.Attribute.class)) {
                    inputList.remove(toRemove);
                    org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(toRemove.toUpperCase());
                    meta.removeAttributeModifier(attr);
                }
            }
            for (String toRemove : inputList) {
                UUID id = UUID.fromString(toRemove);
                Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
                for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
                    for (AttributeModifier modifer : metaMap.get(attribute)) {
                        if (modifer.getUniqueId().equals(id)) {
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

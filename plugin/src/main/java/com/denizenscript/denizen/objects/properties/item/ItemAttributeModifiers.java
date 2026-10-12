package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.AttributeUtil;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

public class ItemAttributeModifiers extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name attribute_modifiers
    // @input MapTag
    // @description
    // Controls the attribute modifiers of an item, with keys as the attribute names and values as a list of modifiers,
    // see <@link language attribute modifiers> for how modifiers are formatted.
    // @mechanism
    // This is a SET operation, meaning pre-existing modifiers are removed.
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
        return getItemMeta() != null ? getAttributeModifiersFor(getItemMeta().getAttributeModifiers(), false) : null;
    }

    @Override
    public MapTag getTagValue(com.denizenscript.denizencore.tags.Attribute attribute) {
        return getItemMeta() != null ? getAttributeModifiersFor(getItemMeta().getAttributeModifiers(), true) : null;
    }

    @Override
    public void setPropertyValue(MapTag param, Mechanism mechanism) {
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = LinkedHashMultimap.create();
        AttributeUtil.parseModifiers(param, mechanism, metaMap::put);
        ItemMeta meta = getItemMeta();
        meta.setAttributeModifiers(metaMap);
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "attribute_modifiers";
    }

    public static MapTag getAttributeModifiersFor(Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap, boolean includeDeprecated) {
        MapTag map = new MapTag();
        if (metaMap == null) {
            return map;
        }
        for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
            AttributeUtil.addToMap(map, attribute, metaMap.get(attribute), includeDeprecated);
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
            AttributeUtil.parseModifiers(param, mechanism, meta::addAttributeModifier);
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
                if (AttributeUtil.MODERN_ATTRIBUTE_FORMAT) {
                    key = Utilities.parseNamespacedKey(toRemove);
                }
                else {
                    id = UUID.fromString(toRemove);
                }
                Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
                for (org.bukkit.attribute.Attribute attribute : metaMap.keys()) {
                    for (AttributeModifier modifer : metaMap.get(attribute)) {
                        if (AttributeUtil.MODERN_ATTRIBUTE_FORMAT ? modifer.getKey().equals(key) : modifer.getUniqueId().equals(id)) {
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

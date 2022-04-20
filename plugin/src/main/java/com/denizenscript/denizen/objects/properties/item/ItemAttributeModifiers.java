package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAttributeModifiers;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
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

public class ItemAttributeModifiers implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemAttributeModifiers getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttributeModifiers((ItemTag) item);
        }
    }

    public static final String[] handledMechs = new String[] {
            "attribute_modifiers", "add_attribute_modifiers", "remove_attribute_modifiers"
    };

    private ItemAttributeModifiers(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public MapTag getAttributeModifiers() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
        return getAttributeModifiersFor(metaMap);
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

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.attribute_modifiers>
        // @returns MapTag
        // @group properties
        // @mechanism ItemTag.attribute_modifiers
        // @description
        // Returns a map of all attribute modifiers on the item, with key as the attribute name and value as a list of modifiers,
        // where each modifier is a MapTag containing keys 'name', 'amount', 'slot', 'operation', and 'id'.
        // This is formatted in a way that can be sent back into the 'attribute_modifiers' mechanism.
        // See also <@link language attribute modifiers>.
        // -->
        PropertyParser.<ItemAttributeModifiers, MapTag>registerTag(MapTag.class, "attribute_modifiers", (attribute, object) -> {
            return object.getAttributeModifiers();
        });

        // <--[tag]
        // @attribute <ItemTag.default_attribute_modifiers[<slot>]>
        // @returns MapTag
        // @group properties
        // @description
        // Returns a map of all default attribute modifiers on the item based purely on its material type, for the given slot,
        // in the same format as <@link tag ItemTag.attribute_modifiers>
        // Slot must be one of: HAND, OFF_HAND, FEET, LEGS, CHEST, or HEAD
        // -->
        PropertyParser.<ItemAttributeModifiers, MapTag>registerTag(MapTag.class, "default_attribute_modifiers", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            EquipmentSlot slot = attribute.getParamElement().asEnum(EquipmentSlot.class);
            if (slot == null) {
                attribute.echoError("Invalid slot.");
                return null;
            }
            return object.getAttributeModifiersFor(NMSHandler.itemHelper.getDefaultAttributes(object.item.getItemStack(), slot));
        });
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
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name attribute_modifiers
        // @input MapTag
        // @description
        // Sets the attribute modifiers of an item.
        // This is a SET operation, meaning pre-existing modifiers are removed.
        // For input format details, refer to <@link language attribute modifiers>.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        if (mechanism.matches("attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = LinkedHashMultimap.create();
            MapTag map = mechanism.valueAsType(MapTag.class);
            for (Map.Entry<StringHolder, ObjectTag> mapEntry : map.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(mapEntry.getKey().str.toUpperCase());
                for (ObjectTag listValue : CoreUtilities.objectToList(mapEntry.getValue(), mechanism.context)) {
                    metaMap.put(attr, EntityAttributeModifiers.modiferForMap(attr, (MapTag) listValue));
                }
            }
            ItemMeta meta = item.getItemMeta();
            meta.setAttributeModifiers(metaMap);
            item.setItemMeta(meta);
        }

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
        if (mechanism.matches("add_attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            ItemMeta meta = item.getItemMeta();
            MapTag input = mechanism.valueAsType(MapTag.class);
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                for (ObjectTag listValue : CoreUtilities.objectToList(subValue.getValue(), mechanism.context)) {
                    meta.addAttributeModifier(attr, EntityAttributeModifiers.modiferForMap(attr, (MapTag) listValue));
                }
            }
            item.setItemMeta(meta);
        }

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
        if (mechanism.matches("remove_attribute_modifiers") && mechanism.requireObject(ListTag.class)) {
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> inputList = new ArrayList<>(mechanism.valueAsType(ListTag.class));
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
            item.setItemMeta(meta);
        }
    }
}

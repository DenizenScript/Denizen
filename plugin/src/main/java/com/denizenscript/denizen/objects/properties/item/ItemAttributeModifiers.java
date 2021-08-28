package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.entity.EntityAttributeModifiers;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.AttributeModifier;
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

    public static final String[] handledTags = new String[] {
            "attribute_modifiers"
    };

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
        MapTag map = new MapTag();
        Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = meta.getAttributeModifiers();
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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.attribute_modifiers>
        // @returns MapTag
        // @group properties
        // @mechanism ItemTag.attribute_modifiers
        // @description
        // Returns a map of all attribute modifiers on the item, with key as the attribute name and value as a list of modifiers,
        // where each modifier is a MapTag containing keys 'name', 'amount', 'slot', 'operation', and 'id'.
        // This is formatted in a way that can be sent back into the 'attribute_modifiers' mechanism.
        // -->
        if (attribute.startsWith("attribute_modifiers")) {
            return getAttributeModifiers().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
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
        // Specify a MapTag where the keys are attribute names, and values are a ListTag of modifiers,
        // where each modifier is itself a MapTag with required keys 'operation' and 'amount', and optional keys 'name', 'slot', and 'id'.
        // Valid operations: ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
        // Valid slots: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, ANY
        // Valid attribute names are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html>
        // The default ID will be randomly generated, the default name will be the attribute name, the default slot is any.
        // Example of valid input: [generic_max_health=<list[<map[operation=ADD_NUMBER;amount=20;slot=HEAD]>]>]
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        if (mechanism.matches("attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            Multimap<org.bukkit.attribute.Attribute, AttributeModifier> metaMap = LinkedHashMultimap.create();
            MapTag map = mechanism.valueAsType(MapTag.class);
            for (Map.Entry<StringHolder, ObjectTag> mapEntry : map.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(mapEntry.getKey().str.toUpperCase());
                EntityAttributeModifiers.addAttributeModifiers((mod) -> metaMap.put(attr, mod), attr, mapEntry.getValue());
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
        // All input is the same as <@link mechanism ItemTag.attribute_modifiers>.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        if (mechanism.matches("add_attribute_modifiers") && mechanism.requireObject(MapTag.class)) {
            ItemMeta meta = item.getItemMeta();
            MapTag input = mechanism.valueAsType(MapTag.class);
            for (Map.Entry<StringHolder, ObjectTag> subValue : input.map.entrySet()) {
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(subValue.getKey().str.toUpperCase());
                EntityAttributeModifiers.addAttributeModifiers((mod) -> meta.addAttributeModifier(attr, mod), attr, subValue.getValue());
            }
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name remove_attribute_modifiers
        // @input ListTag
        // @description
        // Removes attribute modifiers from an item. Specify a list of attribute names or modifier UUIDs as input.
        // @tags
        // <ItemTag.attribute_modifiers>
        // -->
        if (mechanism.matches("remove_attribute_modifiers") && mechanism.requireObject(ListTag.class)) {
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> inputList = new ArrayList<>(mechanism.valueAsType(ListTag.class));
            for (String toRemove : new ArrayList<>(inputList)) {
                if (new ElementTag(toRemove).matchesEnum(org.bukkit.attribute.Attribute.values())) {
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

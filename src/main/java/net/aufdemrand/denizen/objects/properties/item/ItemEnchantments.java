package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemEnchantments implements Property {

    public static boolean describes(dObject item) {
        // Technically, all items can hold enchants.
        return item instanceof dItem;
    }

    public static ItemEnchantments getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemEnchantments((dItem)_item);
    }


    private ItemEnchantments(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.enchantments.with_levels>
        // @returns dList
        // @description
        // Returns a list of enchantments on the item, with their levels listed too.
        // In the format of ENCHANTMENT,LEVEL - EG: DAMAGE_ALL,3
        // -->
        if (attribute.startsWith("enchantments.with_levels")) {
            if (item.getItemStack().getEnchantments().size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : item.getItemStack().getEnchantments().entrySet())
                    enchants.add(enchantment.getKey().getName() + "," + enchantment.getValue());
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.enchantments.levels>
        // @returns dList
        // @description
        // Returns a list of enchantments on the item, showing only the level.
        // -->
        if (attribute.startsWith("enchantments.levels")) {
            if (item.getItemStack().getEnchantments().size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : item.getItemStack().getEnchantments().entrySet())
                    enchants.add(String.valueOf(enchantment.getValue()));
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.enchantments.level[<name>]>
        // @returns Element(Number)
        // @description
        // Returns the level of a specified enchantment.
        // -->
        if (attribute.startsWith("enchantments.level")
                && attribute.hasContext(2)) {
            if (item.getItemStack().getEnchantments().size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : item.getItemStack().getEnchantments().entrySet()) {
                    if (enchantment.getKey().getName().equalsIgnoreCase(attribute.getContext(2)))
                        return new Element(enchantment.getValue())
                                .getAttribute(attribute.fulfill(2));
                }
            }
            return new Element(0)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <i@item.enchantments>
        // @returns dList
        // @description
        // Returns a list of enchantments on the item.
        // -->
        if (attribute.startsWith("enchantments")) {
            if (item.getItemStack().getEnchantments().size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : item.getItemStack().getEnchantments().entrySet())
                    enchants.add(enchantment.getKey().getName());
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getEnchantments().size() > 0) {
            StringBuilder returnable = new StringBuilder();
            for (Map.Entry<Enchantment, Integer> enchantment : item.getItemStack().getEnchantments().entrySet()) {
                returnable.append(enchantment.getKey().getName()).append(",").append(enchantment.getValue()).append("|");
            }
            return returnable.substring(0, returnable.length() - 1);
        }
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "enchantments";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name enchantments
        // @input dList
        // @description
        // Sets the item's enchantments.
        // @tags
        // <i@item.enchantments>
        // <i@item.enchantments.levels>
        // <i@item.enchantments.with_levels>
        // -->

        if (mechanism.matches("enchantments")) {
            for (String enchant: mechanism.getValue().asType(dList.class)) {
                if (!enchant.contains(","))
                    dB.echoError("Invalid enchantment format, use name,level|...");
                else {
                    String[] data = enchant.split(",", 2);
                    if (Integer.valueOf(data[1]) == null)
                        dB.echoError("Cannot apply enchantment '" + data[0] +"': '" + data[1] + "' is not a valid integer!");
                    else {
                        try {
                            item.getItemStack().addUnsafeEnchantment(Enchantment.getByName(data[0].toUpperCase()), Integer.valueOf(data[1]));
                        }
                        catch (NullPointerException e) {
                            dB.echoError("Unknown enchantment '" + data[0] + "'");
                        }
                    }
                }
            }
        }
    }
}

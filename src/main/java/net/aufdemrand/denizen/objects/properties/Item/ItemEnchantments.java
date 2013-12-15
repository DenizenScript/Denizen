package net.aufdemrand.denizen.objects.properties.Item;


import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
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
}

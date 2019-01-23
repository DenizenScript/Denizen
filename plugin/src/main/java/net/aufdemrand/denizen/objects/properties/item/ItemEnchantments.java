package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class ItemEnchantments implements Property {

    public static boolean describes(dObject item) {
        // Technically, all items can hold enchants.
        return item instanceof dItem;
    }

    public static ItemEnchantments getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemEnchantments((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
            "is_enchanted", "enchantments"
    };

    public static final String[] handledMechs = new String[] {
            "remove_enchantments", "enchantments"
    };


    private ItemEnchantments(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        Set<Map.Entry<Enchantment, Integer>> enchantments = GetEnchantments();

        // <--[tag]
        // @attribute <i@item.is_enchanted>
        // @returns Element(Boolean)
        // @mechanism dItem.enchantments
        // @group properties
        // @description
        // Returns whether the item has any enchantments.
        // -->
        if (attribute.startsWith("is_enchanted")) {
            return new Element(enchantments.size() > 0)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.enchantments.with_levels>
        // @returns dList
        // @mechanism dItem.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item, with their levels listed too.
        // In the format of ENCHANTMENT,LEVEL - For example: DAMAGE_ALL,3
        // -->
        if (attribute.startsWith("enchantments.with_levels")) {
            if (enchantments.size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    enchants.add(enchantment.getKey().getName() + "," + enchantment.getValue());
                }
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.enchantments.levels>
        // @returns dList
        // @mechanism dItem.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item, showing only the level.
        // -->
        if (attribute.startsWith("enchantments.levels")) {
            if (enchantments.size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    enchants.add(String.valueOf(enchantment.getValue()));
                }
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.enchantments.level[<name>]>
        // @returns Element(Number)
        // @mechanism dItem.enchantments
        // @group properties
        // @description
        // Returns the level of a specified enchantment.
        // -->
        if (attribute.startsWith("enchantments.level")
                && attribute.hasContext(2)) {
            if (enchantments.size() > 0) {
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    if (enchantment.getKey().getName().equalsIgnoreCase(attribute.getContext(2))) {
                        return new Element(enchantment.getValue())
                                .getAttribute(attribute.fulfill(2));
                    }
                }
            }
            return new Element(0)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <i@item.enchantments>
        // @returns dList
        // @mechanism dItem.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item.
        // -->
        if (attribute.startsWith("enchantments")) {
            if (enchantments.size() > 0) {
                List<String> enchants = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    enchants.add(enchantment.getKey().getName());
                }
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    public Set<Map.Entry<Enchantment, Integer>> GetEnchantments() {
        if (item.getItemStack().getEnchantments().size() > 0) {
            return item.getItemStack().getEnchantments().entrySet();
        }
        else if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof EnchantmentStorageMeta) {
            return ((EnchantmentStorageMeta) item.getItemStack().getItemMeta()).getStoredEnchants().entrySet();
        }
        return new HashSet<Map.Entry<Enchantment, Integer>>();
    }


    @Override
    public String getPropertyString() {
        Set<Map.Entry<Enchantment, Integer>> enchants = GetEnchantments();
        if (enchants.size() > 0) {
            StringBuilder returnable = new StringBuilder();
            for (Map.Entry<Enchantment, Integer> enchantment : enchants) {
                returnable.append(enchantment.getKey().getName()).append(",").append(enchantment.getValue()).append("|");
            }
            return returnable.substring(0, returnable.length() - 1);
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "enchantments";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name remove_enchantments
        // @input None
        // @description
        // Removes the specified enchantments from the item (as a list of enchantment names).
        // Give no value input to remove all enchantments.
        // @tags
        // <i@item.enchantments>
        // <i@item.enchantments.levels>
        // <i@item.enchantments.with_levels>
        // -->
        if (mechanism.matches("remove_enchantments")) {
            HashSet<String> names = null;
            if (mechanism.hasValue()) {
                names = new HashSet<>();
                for (String ench : mechanism.getValue().asType(dList.class)) {
                    names.add(CoreUtilities.toLowerCase(ench));
                }
            }
            if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemStack().getItemMeta();
                for (Enchantment ench : meta.getStoredEnchants().keySet()) {
                    if (names == null || names.contains(CoreUtilities.toLowerCase(ench.getName()))) {
                        meta.removeStoredEnchant(ench);
                    }
                }
                item.getItemStack().setItemMeta(meta);
            }
            else {
                for (Enchantment ench : item.getItemStack().getEnchantments().keySet()) {
                    item.getItemStack().removeEnchantment(ench);
                }
            }
        }

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
            for (String enchant : mechanism.getValue().asType(dList.class)) {
                if (!enchant.contains(",")) {
                    dB.echoError("Invalid enchantment format, use name,level|...");
                }
                else {
                    String[] data = enchant.split(",", 2);
                    if (Integer.valueOf(data[1]) == null) {
                        dB.echoError("Cannot apply enchantment '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                    }
                    else {
                        try {
                            Enchantment ench = Enchantment.getByName(data[0].toUpperCase());
                            if (ench != null) {
                                if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemStack().getItemMeta();
                                    meta.addStoredEnchant(ench, Integer.valueOf(data[1]), true);
                                    item.getItemStack().setItemMeta(meta);
                                }
                                else {
                                    item.getItemStack().addUnsafeEnchantment(ench, Integer.valueOf(data[1]));
                                }
                            }
                            else {
                                dB.echoError("Unknown enchantment '" + data[0] + "'");
                            }
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

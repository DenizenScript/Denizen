package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemLore implements Property {

    public static boolean describes(ObjectTag item) {
        // Technically, all items can hold lore
        return item instanceof ItemTag;
    }

    public static ItemLore getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLore((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "lore", "has_lore"
    };

    public static final String[] handledMechs = new String[] {
            "lore"
    };

    public boolean hasLore() {
        return item.getItemStack().hasItemMeta()
                && (item.getItemStack().getItemMeta().hasLore());
    }

    private ItemLore(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.lore>
        // @returns ListTag
        // @mechanism ItemTag.lore
        // @group properties
        // @description
        // Returns lore as a ListTag. Excludes the custom-script-id lore.
        // To get that information, use <ItemTag.scriptname>.
        // -->
        if (attribute.startsWith("lore")) {
            if (hasLore()) {
                List<String> loreList = new ArrayList<>();
                for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                    if (!itemLore.startsWith(ItemTag.itemscriptIdentifier)
                            && !itemLore.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                        loreList.add(itemLore);
                    }
                }
                return new ListTag(loreList).getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ItemTag.has_lore>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.lore
        // @group properties
        // @description
        // Returns whether the item has lore set on it.
        // -->
        if (attribute.startsWith("has_lore")) {
            return new ElementTag(hasLore())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        if (hasLore()) {
            StringBuilder output = new StringBuilder();
            for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                if (!itemLore.startsWith(ItemTag.itemscriptIdentifier)
                        && !itemLore.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                    output.append(EscapeTagBase.escape(itemLore)).append("|");
                }
            }
            return (output.length() == 0) ? null : output.substring(0, output.length() - 1);
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "lore";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name lore
        // @input ListTag
        // @description
        // Sets the item's lore.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.lore>
        // -->
        if (mechanism.matches("lore")) {
            ItemMeta meta = item.getItemStack().getItemMeta();
            ListTag lore = mechanism.valueAsType(ListTag.class);
            if (item.isItemscript()) {
                if (!Settings.packetInterception()) {
                    lore.add(0, ItemScriptHelper.createItemScriptID(item.getScriptName()));
                }
            }
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, EscapeTagBase.unEscape(lore.get(i)));
            }
            meta.setLore(lore);
            item.getItemStack().setItemMeta(meta);
        }

    }
}

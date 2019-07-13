package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTags;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemLore implements Property {

    public static boolean describes(ObjectTag item) {
        // Technically, all items can hold lore
        return item instanceof dItem;
    }

    public static ItemLore getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLore((dItem) _item);
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


    private ItemLore(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // deprecated in favor of .escape_contents
        if (attribute.startsWith("lore.escaped")) {
            if (hasLore()) {
                return new ElementTag(getPropertyString()).getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.lore>
        // @returns ListTag
        // @mechanism dItem.lore
        // @group properties
        // @description
        // Returns lore as a ListTag. Excludes the custom-script-id lore.
        // To get that information, use <i@item.scriptname>.
        // -->
        if (attribute.startsWith("lore")) {
            if (hasLore()) {
                List<String> loreList = new ArrayList<>();
                for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                    if (!itemLore.startsWith(dItem.itemscriptIdentifier)
                            && !itemLore.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                        loreList.add(itemLore);
                    }
                }
                return new ListTag(loreList).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <i@item.has_lore>
        // @returns ElementTag(Boolean)
        // @mechanism dItem.lore
        // @group properties
        // @description
        // Returns whether the item has lore set on it.
        // -->
        if (attribute.startsWith("has_lore")) {
            return new ElementTag(hasLore())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (hasLore()) {
            StringBuilder output = new StringBuilder();
            for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                if (!itemLore.startsWith(dItem.itemscriptIdentifier)
                        && !itemLore.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                    output.append(EscapeTags.escape(itemLore)).append("|");
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
        // @object dItem
        // @name lore
        // @input ListTag
        // @description
        // Sets the item's lore.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.lore>
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
                lore.set(i, EscapeTags.unEscape(lore.get(i)));
            }
            meta.setLore(lore);
            item.getItemStack().setItemMeta(meta);
        }

    }
}

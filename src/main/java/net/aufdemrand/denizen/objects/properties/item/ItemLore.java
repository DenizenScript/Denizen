package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.EscapeTags;

import java.util.ArrayList;
import java.util.List;

public class ItemLore implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().hasItemMeta()
                && ((dItem) item).getItemStack().getItemMeta().hasLore();
    }

    public static ItemLore getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemLore((dItem)_item);
    }


    private ItemLore(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.lore>
        // @returns dList
        // @description
        // Returns lore as a dList. Excludes the custom-script-id lore.
        // To get that information, use <i@item.scriptname>.
        // -->
        if (attribute.startsWith("lore")) {
            List<String> loreList = new ArrayList<String>();
            for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                if (!itemLore.startsWith(dItem.itemscriptIdentifier)) {
                    loreList.add(itemLore);
                }
            }
            return new dList(loreList).getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        StringBuilder output = new StringBuilder();
        for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
            if (!itemLore.startsWith(dItem.itemscriptIdentifier)) {
                output.append(EscapeTags.Escape(itemLore)).append("|");
            }
        }
        return (output.length() == 0) ? "": output.substring(0, output.length() - 1);
    }

    @Override
    public String getPropertyId() {
        return "lore";
    }
}

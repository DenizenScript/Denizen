package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemLore implements Property {

    public static boolean describes(dObject item) {
        // Technically, all items can hold lore
        return item instanceof dItem;
    }

    public static ItemLore getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLore((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
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
                return new Element(getPropertyString()).getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <i@item.lore>
        // @returns dList
        // @mechanism dItem.lore
        // @group properties
        // @description
        // Returns lore as a dList. Excludes the custom-script-id lore.
        // To get that information, use <i@item.scriptname>.
        // -->
        if (attribute.startsWith("lore")) {
            if (hasLore()) {
                List<String> loreList = new ArrayList<String>();
                for (String itemLore : item.getItemStack().getItemMeta().getLore()) {
                    if (!itemLore.startsWith(dItem.itemscriptIdentifier)
                            && !itemLore.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                        loreList.add(itemLore);
                    }
                }
                return new dList(loreList).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <i@item.has_lore>
        // @returns Element(Boolean)
        // @mechanism dItem.lore
        // @group properties
        // @description
        // Returns whether the item has lore set on it.
        // -->
        if (attribute.startsWith("has_lore")) {
            return new Element(hasLore())
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
                    output.append(EscapeTags.Escape(itemLore)).append("|");
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
        // @input dList
        // @description
        // Sets the item's lore.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.lore>
        // -->

        if (mechanism.matches("lore")) {
            ItemMeta meta = item.getItemStack().getItemMeta();
            dList lore = mechanism.getValue().asType(dList.class);
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

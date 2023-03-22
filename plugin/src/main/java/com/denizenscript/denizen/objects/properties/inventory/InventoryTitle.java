package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryTitle extends ObjectProperty<InventoryTag> {

    public static boolean describes(InventoryTag inventory) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        // Only show a property string for titles that can actually change
        if (object.isGeneric() || object.isSaving) {
            return new ElementTag(getTitle());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "title";
    }

    public String getTitle() {
        if (object.getInventory() != null) {
            String title = PaperAPITools.instance.getTitle(object.getInventory());
            if (title != null) {
                if (!title.startsWith("container.")) {
                    return title;
                }
            }
        }
        return null;
    }

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.title>
        // @returns ElementTag
        // @group properties
        // @mechanism InventoryTag.title
        // @description
        // Returns the title of the inventory.
        // -->
        PropertyParser.registerTag(InventoryTitle.class, ElementTag.class, "title", (attribute, inventory) -> {
            return new ElementTag(inventory.getTitle(), true);
        });
        // <--[mechanism]
        // @object InventoryTag
        // @name title
        // @input ElementTag
        // @description
        // Sets the title of the inventory. (Only works for "generic" inventories.)
        // @tags
        // <InventoryTag.title>
        // -->
        PropertyParser.registerMechanism(InventoryTitle.class, ElementTag.class, "title", (prop, mechanism, param) -> {
            InventoryTag inventory = prop.object;
            if (!inventory.isGeneric() && !inventory.isUnique()) {
                mechanism.echoError("Cannot set a title on a non-generic inventory.");
                return;
            }
            String title = param.asString();
            if (InventoryScriptHelper.isPersonalSpecialInv(inventory.getInventory())) {
                inventory.customTitle = title;
                return;
            }
            if (inventory.getInventory() != null && PaperAPITools.instance.getTitle(inventory.getInventory()).equals(title)) {
                return;
            }
            inventory.uniquifier = null;
            if (inventory.getInventory() == null) {
                inventory.setInventory(PaperAPITools.instance.createInventory(null, InventoryTag.maxSlots, title));
                InventoryTag.trackTemporaryInventory(inventory);
                return;
            }
            ItemStack[] contents = inventory.getContents();
            if (inventory.getInventory().getType() == InventoryType.CHEST) {
                inventory.setInventory(PaperAPITools.instance.createInventory(null, inventory.getSize(), title));
            }
            else {
                inventory.setInventory(PaperAPITools.instance.createInventory(null, inventory.getInventory().getType(), title));
            }
            inventory.setContents(contents);
            InventoryTag.trackTemporaryInventory(inventory);
        });
    }
}

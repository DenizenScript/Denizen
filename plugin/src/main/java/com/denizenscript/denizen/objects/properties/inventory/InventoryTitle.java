package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryTitle implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories could possibly have a title
        return inventory instanceof InventoryTag;
    }

    public static InventoryTitle getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryTitle((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
            "title"
    };

    InventoryTag inventory;

    public InventoryTitle(InventoryTag inventory) {
        this.inventory = inventory;
    }

    public String getTitle() {
        if (inventory.getInventory() != null) {
            String title = PaperAPITools.instance.getTitle(inventory.getInventory());
            if (title != null) {
                if (!title.startsWith("container.")) {
                    return title;
                }
            }
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        // Only show a property string for titles that can actually change
        if (inventory.isGeneric() || inventory.isSaving) {
            return getTitle();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "title";
    }

    public static void registerTags() {

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
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object InventoryTag
        // @name title
        // @input ElementTag
        // @description
        // Sets the title of the inventory. (Only works for "generic" inventories.)
        // @tags
        // <InventoryTag.title>
        // -->
        if (mechanism.matches("title")) {
            if (!inventory.isGeneric() && !inventory.isUnique()) {
                mechanism.echoError("Cannot set a title on a non-generic inventory.");
                return;
            }
            String title = mechanism.getValue().asString();
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
        }

    }
}

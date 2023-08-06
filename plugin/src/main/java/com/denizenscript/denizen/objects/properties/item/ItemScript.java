package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

public class ItemScript implements Property {

    public static boolean describes(ObjectTag item) {
        // All items can have a script
        return item instanceof ItemTag;
    }

    public static ItemScript getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemScript((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_script", "scriptname", "script"
    };

    public static final String[] handledMechs = new String[] {
            "script"
    };

    public ItemScript(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("has_script")) {
            BukkitImplDeprecations.hasScriptTags.warn(attribute.context);
            return new ElementTag(item.isItemscript())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.scriptname>
        // @returns ElementTag
        // @deprecated use ".script.name" instead.
        // @group data
        // @description
        // Use ".script.name" instead.
        // This deprecated tag may be useful for debugging items when item scripts may have been deleted.
        // -->
        if (attribute.startsWith("scriptname")) {
            BukkitImplDeprecations.hasScriptTags.warn(attribute.context);
            if (item.isItemscript()) {
                return new ElementTag(item.getScriptName())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ItemTag.script>
        // @returns ScriptTag
        // @group scripts
        // @description
        // Returns the script of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("script")) {
            ItemScriptContainer container = ItemScriptHelper.getItemScriptContainer(item.getItemStack());
            if (container != null) {
                return new ScriptTag(container)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        return item.getScriptName();
    }

    @Override
    public String getPropertyId() {
        return "script";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Undocumented as meant for internal usage.

        if (mechanism.matches("script") && mechanism.hasValue()) {
            ScriptTag script = mechanism.valueAsType(ScriptTag.class);
            if (script == null) {
                mechanism.echoError("Invalid item script '" + mechanism.getValue().asString() + "' - doesn't exist. Applying anyway. Resultant item may be corrupt.");
                item.setItemScriptName(mechanism.getValue().asString());
            }
            else if (script.getContainer() instanceof ItemScriptContainer) {
                item.setItemScript((ItemScriptContainer) script.getContainer());
            }
            else {
                mechanism.echoError("Script '" + script.getName() + "' is not an item script (but was specified as one).");
            }
        }
    }
}

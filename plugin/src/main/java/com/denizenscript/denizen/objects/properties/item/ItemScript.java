package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.ScriptTag;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class ItemScript implements Property {

    public static boolean describes(ObjectTag item) {
        // All items can have a script
        return item instanceof dItem;
    }

    public static ItemScript getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemScript((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_script", "scriptname", "script"
    };

    public static final String[] handledMechs = new String[] {
            "script"
    };


    private ItemScript(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.has_script>
        // @returns ElementTag(Boolean)
        // @group scripts
        // @description
        // Returns whether the item was created by an item script.
        // -->
        if (attribute.startsWith("has_script")) {
            return new ElementTag(item.isItemscript())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.scriptname>
        // @returns ElementTag
        // @group scripts
        // @description
        // Returns the script name of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("scriptname")) {
            if (item.isItemscript()) {
                return new ElementTag(item.getScriptName())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <i@item.script>
        // @returns ScriptTag
        // @group scripts
        // @description
        // Returns the script of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("script")) {
            if (item.isItemscript()) {
                return new ScriptTag(item.getScriptName())
                        .getAttribute(attribute.fulfill(1));
            }
        }
        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.isItemscript()) {
            return item.getScriptName();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "script";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Undocumented as meant for internal usage.

        if (mechanism.matches("script") && mechanism.requireObject(ScriptTag.class)) {
            ScriptTag script = mechanism.valueAsType(ScriptTag.class);
            if (script.getContainer() instanceof ItemScriptContainer) {
                item.setItemScript((ItemScriptContainer) script.getContainer());
            }
            else {
                Debug.echoError("Script '" + script.getName() + "' is not an item script (but was specified as one).");
            }
        }
    }
}

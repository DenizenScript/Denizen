package net.aufdemrand.denizen.objects.properties.item;

import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.dScript;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class ItemScript implements Property {

    public static boolean describes(dObject item) {
        // All items can have a script
        return item instanceof dItem;
    }

    public static ItemScript getFrom(dObject _item) {
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
        // @returns Element(Boolean)
        // @group scripts
        // @description
        // Returns whether the item was created by an item script.
        // -->
        if (attribute.startsWith("has_script")) {
            return new Element(item.isItemscript())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.scriptname>
        // @returns Element
        // @group scripts
        // @description
        // Returns the script name of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("scriptname")) {
            if (item.isItemscript()) {
                return new Element(item.getScriptName())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <i@item.script>
        // @returns dScript
        // @group scripts
        // @description
        // Returns the script of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("script")) {
            if (item.isItemscript()) {
                return new dScript(item.getScriptName())
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

        if (mechanism.matches("script") && mechanism.requireObject(dScript.class)) {
            dScript script = mechanism.valueAsType(dScript.class);
            if (script.getContainer() instanceof ItemScriptContainer) {
                item.setItemScript((ItemScriptContainer) script.getContainer());
            }
            else {
                dB.echoError("Script '" + script.getName() + "' is not an item script (but was specified as one).");
            }
        }
    }
}

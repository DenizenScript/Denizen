package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.scripts.commands.core.FlagCommand;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.data.DataAction;
import com.denizenscript.denizencore.utilities.data.DataActionHelper;

public class ItemFlags implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemFlags getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemFlags((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
    }; // None: use the standard FlaggableObject flag tags

    public static final String[] handledMechs = new String[] {
            "flag", "flag_map"
    };

    private ItemFlags(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        // Handled elsewhere
        return null;
    }

    @Override
    public String getPropertyString() {
        AbstractFlagTracker tracker = item.getFlagTracker();
        if (tracker instanceof MapTagFlagTracker && ((MapTagFlagTracker) tracker).map.map.isEmpty()) {
            return null;
        }
        return tracker.toString();
    }

    @Override
    public String getPropertyId() {
        return "flag_map";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name flag_map
        // @input MapTag
        // @description
        // Internal-usage direct re-setter for the item's full raw flag data.
        // -->
        if (mechanism.matches("flag_map") && mechanism.requireObject(MapTag.class)) {
            item.reapplyTracker(new MapTagFlagTracker(mechanism.valueAsType(MapTag.class)));
        }

        // <--[mechanism]
        // @object ItemTag
        // @name flag
        // @input ObjectTag
        // @description
        // Modifies a flag on this item, using syntax similar to <@link command flag>.
        // For example, 'flag:myflagname:!' will remove flag 'myflagname', or 'flag:myflagname:3' sets flag 'myflagname' to value '3'.
        // @tags
        // <FlaggableObject.flag[<flag_name>]>
        // <FlaggableObject.has_flag[<flag_name>]>
        // <FlaggableObject.flag_expiration[<flag_name>]>
        // <FlaggableObject.list_flags>
        // -->
        if (mechanism.matches("flag")) {
            FlagCommand.FlagActionProvider provider = new FlagCommand.FlagActionProvider();
            provider.tracker = item.getFlagTracker();
            DataAction action = DataActionHelper.parse(provider, mechanism.getValue().asString());
            action.execute(mechanism.context);
            item.reapplyTracker(provider.tracker);
        }
    }
}

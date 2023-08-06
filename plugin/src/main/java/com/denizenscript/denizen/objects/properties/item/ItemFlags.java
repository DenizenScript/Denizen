package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
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
            "with_flag"
    };

    public static final String[] handledMechs = new String[] {
            "flag", "flag_map"
    };

    public ItemFlags(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.with_flag[<flag_set_action>]>
        // @returns ItemTag
        // @mechanism ItemTag.flag
        // @group properties
        // @description
        // Returns a copy of the item with the specified flag data action applied to it.
        // For example: <[item].with_flag[myflagname]>, or <[item].with_flag[myflag:myvalue]>, or <[item].with_flag[mycounter:+:<[amount]>]>
        // -->
        if (attribute.startsWith("with_flag")) {
            ItemTag item = new ItemTag(this.item.getItemStack().clone());
            FlagCommand.FlagActionProvider provider = new FlagCommand.FlagActionProvider();
            provider.tracker = item.getFlagTracker();
            DataAction action = DataActionHelper.parse(provider, attribute.getParam(), attribute.context);

            // <--[tag]
            // @attribute <ItemTag.with_flag[<flag_set_action>].duration[<expire_duration>]>
            // @returns ItemTag
            // @mechanism ItemTag.flag
            // @group properties
            // @description
            // Returns a copy of the item with the specified flag data action (and the specified expiration duration) applied to it.
            // For example: <[item].with_flag[myflagname].duration[5m]>
            // -->
            if (attribute.startsWith("duration", 2)) {
                provider.expiration = new TimeTag(TimeTag.now().millis() + attribute.getContextObject(2).asType(DurationTag.class, attribute.context).getMillis());
                attribute.fulfill(1);
            }
            action.execute(attribute.context);
            item.reapplyTracker(provider.tracker);
            return item
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        AbstractFlagTracker tracker = item.getFlagTracker();
        if (tracker instanceof MapTagFlagTracker && ((MapTagFlagTracker) tracker).map.isEmpty()) {
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
        // @deprecated Internal-usage only.
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
            DataAction action = DataActionHelper.parse(provider, mechanism.getValue().asString(), mechanism.context);
            action.execute(mechanism.context);
            item.reapplyTracker(provider.tracker);
        }
    }
}

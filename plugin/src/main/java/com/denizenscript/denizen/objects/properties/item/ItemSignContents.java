package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;

public class ItemSignContents implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof Sign;
    }

    public static ItemSignContents getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSignContents((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "sign_contents"
    };

    public static final String[] handledMechs = new String[] {
            "sign_contents"
    };

    private ListTag getSignContents() {
        return new ListTag(Arrays.asList(PaperAPITools.instance.getSignLines((Sign) ((BlockStateMeta) item.getItemMeta()).getBlockState())), true);
    }

    private ItemSignContents(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.sign_contents>
        // @returns ListTag
        // @mechanism ItemTag.sign_contents
        // @group properties
        // @description
        // Returns a list of lines on a sign item.
        // -->
        if (attribute.startsWith("sign_contents")) {
            return getSignContents().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        for (String line : getSignContents()) {
            if (line.length() > 0) {
                return getSignContents().identify();
            }
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "sign_contents";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name sign_contents
        // @input ListTag
        // @description
        // Sets the contents of a sign item.
        // @tags
        // <ItemTag.sign_contents>
        // -->
        if (mechanism.matches("sign_contents")) {
            BlockStateMeta bsm = ((BlockStateMeta) item.getItemMeta());
            Sign sign = (Sign) bsm.getBlockState();
            for (int i = 0; i < 4; i++) {
                PaperAPITools.instance.setSignLine(sign, i, "");
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            CoreUtilities.fixNewLinesToListSeparation(list);
            if (list.size() > 4) {
                Debug.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    PaperAPITools.instance.setSignLine(sign, i, list.get(i));
                }
            }
            bsm.setBlockState(sign);
            item.setItemMeta(bsm);
        }
    }
}

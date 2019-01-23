package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;

public class ItemSignContents implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((dItem) item).getItemStack().getItemMeta()).getBlockState() instanceof Sign;
    }

    public static ItemSignContents getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSignContents((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
            "sign_contents"
    };

    public static final String[] handledMechs = new String[] {
            "sign_contents"
    };


    private dList getSignContents() {
        return new dList(Arrays.asList(((Sign) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).getLines()));
    }

    private ItemSignContents(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.sign_contents>
        // @returns dList
        // @mechanism dItem.sign_contents
        // @group properties
        // @description
        // Returns a list of lines on a sign item.
        // -->
        if (attribute.startsWith("sign_contents")) {
            return getSignContents().getAttribute(attribute.fulfill(1));
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
        // @object dItem
        // @name sign_contents
        // @input dList
        // @description
        // Sets the contents of a sign item.
        // @tags
        // <i@item.sign_contents>
        // -->
        if (mechanism.matches("sign_contents")) {
            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            Sign sign = (Sign) bsm.getBlockState();

            for (int i = 0; i < 4; i++) {
                sign.setLine(i, "");
            }
            dList list = mechanism.getValue().asType(dList.class);
            if (list.size() > 4) {
                dB.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    sign.setLine(i, EscapeTags.unEscape(list.get(i)));
                }
            }

            bsm.setBlockState(sign);
            itemStack.setItemMeta(bsm);
        }
    }
}

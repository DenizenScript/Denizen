package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSignContentsBack extends ItemProperty<ListTag> {

    // <--[property]
    // @object ItemTag
    // @name sign_back_contents
    // @input ListTag
    // @description
    // Controls the contents on the back of a sign item.
    // For the front of the sign, see <@link property ItemTag.sign_contents>.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.getBlockState() instanceof Sign;
    }

    @Override
    public ListTag getPropertyValue() {
        Sign sign = (((Sign) ((BlockStateMeta) getItemMeta()).getBlockState()));
        return new ListTag(PaperAPITools.instance.getBackSignLines(sign), true);
    }

    @Override
    public void setPropertyValue(ListTag value, Mechanism mechanism) {
        BlockStateMeta meta = ((BlockStateMeta) getItemMeta());
        Sign sign = (Sign) meta.getBlockState();
        for (int i = 0; i < 4; i++) {
            PaperAPITools.instance.setBackSignLine(sign, i, "");
        }
        CoreUtilities.fixNewLinesToListSeparation(value);
        if (value.size() > 4) {
            mechanism.echoError("Sign can only hold four lines!");
        }
        else {
            for (int i = 0; i < value.size(); i++) {
                PaperAPITools.instance.setBackSignLine(sign, i, value.get(i));
            }
        }
        meta.setBlockState(sign);
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "sign_back_contents";
    }

    public static void register() {
        autoRegister("sign_back_contents", ItemSignContentsBack.class, ListTag.class, false);
    }
}

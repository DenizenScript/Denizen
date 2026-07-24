package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSignContents extends ItemProperty<ListTag> {

    // <--[property]
    // @object ItemTag
    // @name sign_contents
    // @input ListTag
    // @description
    // Controls the contents of a sign item.
    // For MC 1.20+, this is the contents on the front of the sign.
    // For the back of the sign, see <@link property ItemTag.sign_back_contents>.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.getBlockState() instanceof Sign;
    }

    @Override
    public ListTag getPropertyValue() {
        return new ListTag(PaperAPITools.instance.getSignLines((Sign) as(BlockStateMeta.class).getBlockState()), true);
    }

    @Override
    public boolean isDefaultValue(ListTag value) {
        for (String line : value) {
            if (!line.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setPropertyValue(ListTag value, Mechanism mechanism) {
        BlockStateMeta blockStateMeta = as(BlockStateMeta.class);
        Sign sign = (Sign) blockStateMeta.getBlockState();
        for (int i = 0; i < 4; i++) {
            PaperAPITools.instance.setSignLine(sign, i, "");
        }
        CoreUtilities.fixNewLinesToListSeparation(value);
        if (value.size() > 4) {
            mechanism.echoError("Sign can only hold four lines!");
        }
        else {
            for (int i = 0; i < value.size(); i++) {
                PaperAPITools.instance.setSignLine(sign, i, value.get(i));
            }
        }
        blockStateMeta.setBlockState(sign);
        setItemMeta(blockStateMeta);
    }

    @Override
    public String getPropertyId() {
        return "sign_contents";
    }

    public static void register() {
        autoRegister("sign_contents", ItemSignContents.class, ListTag.class, false);
    }
}

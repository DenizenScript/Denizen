package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSignIsWaxed extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name is_waxed
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a sign item is waxed (cannot be edited once placed).
    // -->

    public static boolean describes(ItemTag item) {
        return Tag.ALL_SIGNS.isTagged(item.getBukkitMaterial());
    }

    @Override
    public ElementTag getPropertyValue() {
        BlockStateMeta stateMeta = as(BlockStateMeta.class);
        if (!stateMeta.hasBlockState()) {
            return null;
        }
        return new ElementTag(((Sign) stateMeta.getBlockState()).isWaxed());
    }

    @Override
    public ElementTag getTagValue(Attribute attribute) {
        ElementTag value = getPropertyValue();
        return value == null ? new ElementTag(false) : value;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        editMeta(BlockStateMeta.class, stateMeta -> {
            Sign sign = (Sign) stateMeta.getBlockState();
            sign.setWaxed(value.asBoolean());
            stateMeta.setBlockState(sign);
        });
    }

    @Override
    public String getPropertyId() {
        return "is_waxed";
    }

    public static void register() {
        autoRegister("is_waxed", ItemSignIsWaxed.class, ElementTag.class, false);
    }
}
